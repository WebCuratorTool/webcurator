package org.webcurator.core.store;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.harvester.coordinator.HarvestCoordinatorPaths;
import org.webcurator.core.rest.RestClientResponseHandler;
import org.webcurator.core.util.WebServiceEndPoint;
import org.webcurator.domain.model.core.HarvestResultDTO;

// TODO Note that the spring boot application needs @EnableRetry for the @Retryable to work.
public abstract class IndexerBase implements RunnableIndex {
    private static final Log log = LogFactory.getLog(IndexerBase.class);

    private WebServiceEndPoint wsEndPoint;
    private boolean defaultIndexer = false;
    private Mode mode = Mode.INDEX;
    protected final RestTemplateBuilder restTemplateBuilder;

    public class ARCFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.toLowerCase().endsWith(".arc") ||
                    name.toLowerCase().endsWith(".arc.gz") ||
                    name.toLowerCase().endsWith(".warc") ||
                    name.toLowerCase().endsWith(".warc.gz"));
        }
    }

    public IndexerBase() {
        this(new RestTemplateBuilder());
    }

    public IndexerBase(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
        restTemplateBuilder.errorHandler(new RestClientResponseHandler());
    }


    protected IndexerBase(IndexerBase original) {
        this.defaultIndexer = original.defaultIndexer;
        this.wsEndPoint = original.wsEndPoint;
        this.restTemplateBuilder = original.restTemplateBuilder;
    }

    public String baseUrl() {
        return "http://" + wsEndPoint.getHost() + ":" + wsEndPoint.getPort();
    }

    public String getUrl(String appendUrl) {
        return baseUrl() + appendUrl;
    }

    protected abstract HarvestResultDTO getResult();

    @Override
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void run() {
        Long harvestResultOid = null;
        try {
            harvestResultOid = begin();
            if (mode == Mode.REMOVE) {
                removeIndex(harvestResultOid);
            } else {
                indexFiles(harvestResultOid);
                markComplete(harvestResultOid);

            }
        } finally {
            synchronized (Indexer.lock) {
                Indexer.removeRunningIndex(getName(), harvestResultOid);
            }
        }
    }

    @Override
    public final void markComplete(Long harvestResultOid) {

        synchronized (Indexer.lock) {
            if (Indexer.lastRunningIndex(this.getName(), harvestResultOid)) {
                log.info("Marking harvest result for job " + getResult().getTargetInstanceOid() + " as ready");
                finaliseIndex(harvestResultOid);

                log.info("Index for job " + getResult().getTargetInstanceOid() + " is now ready");
            }

            Indexer.removeRunningIndex(getName(), harvestResultOid);
        }
    }

    @Retryable(maxAttempts = Integer.MAX_VALUE, backoff = @Backoff(delay = 30_000L))
    protected void finaliseIndex(Long harvestResultOid) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.FINALISE_INDEX));

        Map<String, Long> pathVariables = ImmutableMap.of("harvest-result-oid", harvestResultOid);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(), null, String.class);
    }

    public File getDownloadFileURL(long job, int harvestResultNumber, String fileName, File downloadedFile) throws IOException {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.MODIFICATION_DOWNLOAD_IMPORTED_FILE))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber)
                .queryParam("fileName", fileName);
        URI uri = uriComponentsBuilder.build().toUri();

        URL url = uri.toURL();
        URLConnection conn = url.openConnection();

        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadedFile));
        InputStream inputStream = conn.getInputStream();
        while (true) {
            byte[] buf = new byte[1024 * 32];
            int len = inputStream.read(buf);
            if (len < 0) {
                break;
            }
            outputStream.write(buf, 0, len);
        }
        outputStream.close();

        return downloadedFile;
    }

    @Override
    public void removeIndex(Long harvestResultOid) {
        //Default implementation is to do nothing
    }

    public void setWsEndPoint(WebServiceEndPoint wsEndPoint) {
        this.wsEndPoint = wsEndPoint;
    }

    public WebServiceEndPoint getWsEndPoint() {
        return wsEndPoint;
    }
}
