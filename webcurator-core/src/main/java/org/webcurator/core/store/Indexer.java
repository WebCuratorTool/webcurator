package org.webcurator.core.store;

import java.io.File;
import java.net.ConnectException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.lang.StringUtils;
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
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.domain.model.core.HarvestResultDTO;

public class Indexer extends AbstractRestClient {
    private static final Logger log = LoggerFactory.getLogger(Indexer.class);
    private static final Map<Long, Thread> runningIndexes = new HashMap<>();
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

    public void runIndex(HarvestResultDTO dto, File directory) {
        if (indexers == null || indexers.isEmpty()) {
            log.error("No indexing indexers are defined");
            return;
        }

        Runnable executor = new Runnable() {
            @Override
            public void run() {
                boolean indexResult = false;

                try {
                    List<ProcessorHolder> processors = new ArrayList<>();
                    ExecutorService thread_pool = Executors.newFixedThreadPool(indexers.size());
                    for (RunnableIndex indexer : indexers) {
                        //Use a new indexer each time to make it thread safe
                        RunnableIndex theCopy = indexer.getCopy();
                        theCopy.initialise(dto, directory);
                        theCopy.setMode(Mode.INDEX);
                        Future<Boolean> future = thread_pool.submit(theCopy);
                        ProcessorHolder holder = new ProcessorHolder(dto, theCopy, future);
                        processors.add(holder);
                    }

                    boolean totalResult = true;
                    while (!processors.isEmpty()) {
                        List<ProcessorHolder> toBeRemovedProcessors = new ArrayList<>();
                        for (ProcessorHolder holder : processors) {
                            if (holder.isDone()) {
                                toBeRemovedProcessors.add(holder);
                            }
                        }
                        for (ProcessorHolder holder : toBeRemovedProcessors) {
                            totalResult = totalResult && holder.getValue();
                            processors.remove(holder);
                        }
                        toBeRemovedProcessors.clear();
                        try {
                            TimeUnit.SECONDS.sleep(10L);
                        } catch (InterruptedException e) {
                            log.error("Indexing timer is interrupted");
                        }
                    }
                    indexResult = totalResult;
                    log.info("All the indexing indexers are done: {} {}", dto.getTargetInstanceOid(), dto.getHarvestNumber());
                } finally {
                    runningIndexes.remove(dto.getOid());
                    finaliseIndex(dto, indexResult);
                }
            }
        };
        Thread t = new Thread(executor);
        runningIndexes.put(dto.getOid(), t);
        t.start();
        log.info("Indexing started: {} {}", dto.getTargetInstanceOid(), dto.getHarvestNumber());
    }

    public void removeIndex(HarvestResultDTO dto, File directory) {
        if (indexers == null || indexers.isEmpty()) {
            log.error("No removing indexers are defined");
            return;
        }

        Runnable executor = new Runnable() {
            @Override
            public void run() {
                try {
                    List<ProcessorHolder> processors = new ArrayList<>();
                    ExecutorService thread_pool = Executors.newFixedThreadPool(indexers.size());
                    for (RunnableIndex indexer : indexers) {
                        //Use a new indexer each time to make it thread safe
                        RunnableIndex theCopy = indexer.getCopy();
                        theCopy.initialise(dto, directory);
                        theCopy.setMode(Mode.REMOVE);
                        Future<Boolean> future = thread_pool.submit(theCopy);
                        ProcessorHolder holder = new ProcessorHolder(dto, theCopy, future);
                        processors.add(holder);
                    }

                    while (!processors.isEmpty()) {
                        List<ProcessorHolder> toBeRemovedProcessors = new ArrayList<>();
                        for (ProcessorHolder holder : processors) {
                            if (holder.isDone()) {
                                toBeRemovedProcessors.add(holder);
                            }
                        }
                        for (ProcessorHolder holder : toBeRemovedProcessors) {
                            boolean ret = holder.getValue();
                            log.info("{}, result={}, {} {}", holder.processor.getName(), ret, dto.getTargetInstanceOid(), dto.getHarvestNumber());
                            processors.remove(holder);
                        }
                        toBeRemovedProcessors.clear();
                        try {
                            TimeUnit.SECONDS.sleep(10L);
                        } catch (InterruptedException e) {
                            log.error("Removing timer is interrupted");
                        }
                    }
                    log.info("All the removing indexers are done: {} {}", dto.getTargetInstanceOid(), dto.getHarvestNumber());
                } finally {
                    runningIndexes.remove(dto.getOid());
                }
            }
        };
        Thread t = new Thread(executor);
        runningIndexes.put(dto.getOid(), t);
        t.start();
        log.info("Remove indexing started: {} {}", dto.getTargetInstanceOid(), dto.getHarvestNumber());
    }

    public Boolean checkIndexing(Long hrOid) {
        return runningIndexes.containsKey(hrOid);
    }

    public static class ProcessorHolder {
        public long startTime = System.currentTimeMillis();
        public HarvestResultDTO dto;
        public RunnableIndex processor;
        public Future<Boolean> future;

        public ProcessorHolder(HarvestResultDTO dto, RunnableIndex processor, Future<Boolean> future) {
            this.dto = dto;
            this.processor = processor;
            this.future = future;
        }

        public long getRunningDuration() {
            return System.currentTimeMillis() - startTime;
        }

        public boolean isDone() {
            boolean done = future.isDone();
            if (done) {
                log.info("{}, is done, {} {}, time used: {}", processor.getName(), dto.getTargetInstanceOid(), dto.getHarvestNumber(), getRunningDuration());
            } else {
                log.debug("{}, is running, {} {}, time used: {}", processor.getName(), dto.getTargetInstanceOid(), dto.getHarvestNumber(), getRunningDuration());
            }
            return done;
        }

        public boolean getValue() {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException ex) {
                log.error("{}, failed to get value: {} {}", processor.getName(), dto.getTargetInstanceOid(), dto.getHarvestNumber(), ex);
                return false;
            }
        }
    }

    public static class CommandLine {
        Properties props = new Properties();

        public CommandLine(String[] args) {
            for (int i = 0; i < args.length; i += 2) {
                props.put(args[i].substring(1), args[i + 1]);
            }
        }

        public String getArg(String key) {
            return props.getProperty(key, "");
        }
    }

    public static void main(String[] args) {
        try {
//            BDBNetworkMapPool pool = Objects.requireNonNull(ApplicationContextFactory.getApplicationContext()).getBean(BDBNetworkMapPool.class);
//            pool.close(result.getTargetInstanceOid(), result.getHarvestNumber()); //Close the BDB instance when it is available

            CommandLine cl = new CommandLine(args);

            String host = cl.getArg("host");
            if (StringUtils.isEmpty(host)) {
                System.out.println("Host is not specified, set the value to: localhost");
                host = "localhost";
            }

            String port = cl.getArg("port");
            if (StringUtils.isEmpty(port)) {
                System.out.println("Port is not specified, set the value to: 8080");
                port = "8080";
            }
            String baseUrl = String.format("http://%s:%s/wct", host, port);


            String targetInstanceOid = cl.getArg("ti");
            if (StringUtils.isEmpty(targetInstanceOid)) {
                System.out.println("Target Instance ID must be specified");
                syntax();
            }

            String hrnum = cl.getArg("hrnum");
            if (StringUtils.isEmpty(hrnum)) {
                System.out.println("Harvest Result Number must be specified");
                syntax();
            }

            String baseDir = cl.getArg("baseDir");
            if (StringUtils.isEmpty(baseDir)) {
                System.out.println("Directory must be specified");
                syntax();
            }

            File dir = new File(baseDir);
            if (!dir.exists()) {
                System.out.println("Directory does not exist");
                syntax();
            }

            HarvestResultDTO dto = new HarvestResultDTO();
            dto.setTargetInstanceOid(Long.parseLong(targetInstanceOid));
            dto.setHarvestNumber(Integer.parseInt(hrnum));
            dto.setProvenanceNote("Manual Intervention");
            dto.setCreationDate(new Date());

            BDBNetworkMapPool pool = new BDBNetworkMapPool(dir.getAbsolutePath(), "4.0.1");

            Indexer indexer = new Indexer(true);
            WCTIndexer wctIndexer = new WCTIndexer();
            wctIndexer.setDoCreate(true);
            wctIndexer.setEnabled(true);
            wctIndexer.setPool(pool);
            List<RunnableIndex> indexers = new ArrayList<RunnableIndex>();
            indexers.add(wctIndexer);
            indexer.setIndexers(indexers);
            indexer.runIndex(dto, dir);
        } catch (Exception ex) {
            log.error("Failed to execute indexing", ex);
            syntax();
        }
    }

    private static void syntax() {
        System.out.println("Syntax: ");
        System.out.println(" -host hostname -port portnumber -ti tiOid -hrnum 1 -baseDir basedir");
        System.exit(1);
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
}
