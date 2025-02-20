package org.webcurator.core.store;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.coordinator.WctCoordinatorPaths;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.store.RunnableIndex.Mode;
import org.webcurator.domain.model.core.HarvestResultDTO;

public class Indexer extends AbstractRestClient {
    private static final Logger log = LoggerFactory.getLogger(Indexer.class);
    private static final Map<Long, IndexerExecutor> runningIndexes = new HashMap<>();
    public static final Object lock = new Object();

    private boolean doCreate = false;
    private List<RunnableIndex> indexers;

    public Indexer() {
        this(false);
    }

    public Indexer(boolean doCreate) {
        super();
        this.doCreate = doCreate;
    }

    public Indexer(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }

    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30_000L))
    protected void finaliseIndex(HarvestResultDTO dto, boolean indexResult) {
        try {
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(WctCoordinatorPaths.FINALISE_INDEX))
                    .queryParam("targetInstanceId", dto.getTargetInstanceOid())
                    .queryParam("harvestNumber", dto.getHarvestNumber())
                    .queryParam("indexResult", indexResult);
            RestTemplate restTemplate = restTemplateBuilder.build();
            URI uri = uriComponentsBuilder.build().toUri();
            restTemplate.postForObject(uri, null, Void.class);
            log.info("Finalised index: {}-{} {}", dto.getTargetInstanceOid(), dto.getHarvestNumber(), indexResult);
        } catch (ResourceAccessException ex) {
            log.error("Failed to finalize index: {} {}, {}", dto.getTargetInstanceOid(), dto.getHarvestNumber(), ex.getMessage());
        } catch (Exception ex) {
            log.error("Failed to finalize index: {} {}", dto.getTargetInstanceOid(), dto.getHarvestNumber(), ex);
        }
    }

    public void abortIndex(HarvestResultDTO dto) {
        synchronized (lock) {
            if (runningIndexes.containsKey(dto.getOid())) {
                IndexerExecutor oldProcessor = runningIndexes.get(dto.getOid());
                oldProcessor.close();
                runningIndexes.remove(dto.getOid());
                log.warn("Indexer is canceled: {} {}", dto.getTargetInstanceOid(), dto.getHarvestNumber());
            }
        }
    }

    public void runIndex(HarvestResultDTO dto, File directory) {
        if (indexers == null || indexers.isEmpty()) {
            log.error("No indexing indexers are defined");
            return;
        }

        IndexerExecutor processor = new IndexerExecutor(indexers, Mode.INDEX, dto, directory);
        synchronized (lock) {
            if (runningIndexes.containsKey(dto.getOid())) {
                log.warn("Indexer is running. The request will be skipped: {} {}", dto.getTargetInstanceOid(), dto.getHarvestNumber());
                return;
            }
            runningIndexes.put(dto.getOid(), processor);
        }

        CompletableFuture<Boolean> future = processor.process();
        future.thenAccept(ret -> {
            finaliseIndex(dto, ret);
        }).whenComplete((ret, ex) -> {
            synchronized (lock) {
                runningIndexes.remove(dto.getOid());
            }
        });
    }

    public void removeIndex(HarvestResultDTO dto, File directory) {
        if (indexers == null || indexers.isEmpty()) {
            log.error("No removing indexers are defined");
            return;
        }

        IndexerExecutor processor = new IndexerExecutor(indexers, Mode.REMOVE, dto, directory);
        synchronized (lock) {
            if (runningIndexes.containsKey(dto.getOid())) {
                log.warn("Removing indexer is running. The request will be skipped: {} {}", dto.getTargetInstanceOid(), dto.getHarvestNumber());
                return;
            }
            runningIndexes.put(dto.getOid(), processor);
        }

        CompletableFuture<Boolean> future = processor.process();
        future.whenComplete((ret, ex) -> {
            synchronized (lock) {
                runningIndexes.remove(dto.getOid());
            }
        });
    }

    public Boolean checkIndexing(Long hrOid) {
        return runningIndexes.containsKey(hrOid);
    }

    public boolean isDoCreate() {
        return doCreate;
    }

    public void setIndexers(List<RunnableIndex> indexers) {
        this.indexers = indexers;
    }

    public List<RunnableIndex> getIndexers() {
        return indexers;
    }

    public static class IndexerExecutor {
        private final List<RunnableIndex> indexers;
        private final Mode mode;
        private final HarvestResultDTO dto;
        private final File directory;
        private final List<RunnableIndex> processors = new ArrayList<>();
        private boolean isRunning = true;
        private CompletableFuture<Boolean> mainFuture;

        public IndexerExecutor(List<RunnableIndex> indexers, Mode mode, HarvestResultDTO dto, File directory) {
            super();
            this.indexers = indexers;
            this.mode = mode;
            this.dto = dto;
            this.directory = directory;
        }

        public void close() {
            this.isRunning = false;
            int count = 0;
            //wait until the thread_pool is ended
            while (count < 15 && !this.processors.isEmpty()) {
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException ignored) {
                }
                count++;
                log.debug("Waiting for all the processors ended");
            }
            log.info("Closed all the indexers in {} seconds", count);

            if (!this.mainFuture.isDone()) {
                this.mainFuture.cancel(true);
            }
        }

        public CompletableFuture<Boolean> process() {
            this.mainFuture = CompletableFuture.supplyAsync(this::_process);
            return this.mainFuture;
        }

        public boolean _process() {
            boolean indexResult = false;
            try {
                for (RunnableIndex indexer : this.indexers) {
                    //Use a new indexer each time to make it thread safe
                    RunnableIndex theCopy = indexer.getCopy();
                    theCopy.initialise(this.dto, this.directory);
                    theCopy.setMode(this.mode);
                    processors.add(theCopy);
                    theCopy.submitAsync();
                }

                boolean totalResult = true;
                while (this.isRunning && !processors.isEmpty()) {
                    List<RunnableIndex> toBeRemovedProcessors = new ArrayList<>();
                    for (RunnableIndex holder : processors) {
                        if (holder.isDone()) {
                            toBeRemovedProcessors.add(holder);
                        }
                    }
                    for (RunnableIndex holder : toBeRemovedProcessors) {
                        boolean ret = holder.getValue();
                        log.info("{}, result={}, {} {}", holder.getName(), ret, dto.getTargetInstanceOid(), dto.getHarvestNumber());
                        processors.remove(holder);
                        totalResult = totalResult && ret;
                    }
                    toBeRemovedProcessors.clear();
                    try {
                        TimeUnit.SECONDS.sleep(1L);
                    } catch (InterruptedException ignored) {
                    }
                }

                //If the process is closed before all tasks are ended, the left tasks should be canceled
                for (RunnableIndex holder : this.processors) {
                    holder.cancel();
                }
                this.processors.clear();

                indexResult = totalResult && this.isRunning;
                log.info("All the indexers are done: {}-{} {}", dto.getTargetInstanceOid(), dto.getHarvestNumber(), indexResult);
            } catch (Exception ex) {
                log.error("Failed to index: {}-{}", dto.getTargetInstanceOid(), dto.getHarvestNumber());
                indexResult = false;
            }
            return indexResult;
        }
    }
}
