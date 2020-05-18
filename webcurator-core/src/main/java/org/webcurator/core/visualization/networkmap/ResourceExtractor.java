package org.webcurator.core.visualization.networkmap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;
import org.webcurator.domain.model.core.SeedHistory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

abstract public class ResourceExtractor {
    private static final Logger log = LoggerFactory.getLogger(ResourceExtractor.class);

    protected static final int MAX_URL_LENGTH = 1020;
    protected AtomicLong atomicIdGeneratorDomain = new AtomicLong();
    protected AtomicLong atomicIdGeneratorUrl = new AtomicLong();

    protected Map<String, NetworkMapNode> results;
    protected Map<String, Boolean> seeds = new HashMap<>();

    protected ResourceExtractor(Map<String, NetworkMapNode> results, Set<SeedHistory> seeds) {
        this.results = results;
        seeds.forEach(seed -> {
            this.seeds.put(seed.getSeed(), seed.isPrimary());
        });
    }

    public void extract(ArchiveReader reader) throws IOException {
        preProcess();
        for (ArchiveRecord record : reader) {
            extractRecord(record);
            record.close();
            log.info("Extracting, results.size:{}", results.size());
        }
        postProcess();
    }

    abstract protected void preProcess();

    abstract protected void postProcess();

    abstract protected void extractRecord(ArchiveRecord rec) throws IOException;

    public void clear() {
    }

    /**
     * borrowed(copied) from org.archive.io.arc.ARCRecord...
     *
     * @param bytes Array of bytes to examine for an EOL.
     * @return Count of end-of-line characters or zero if none.
     */
    public int getEolCharsCount(byte[] bytes) {
        int count = 0;
        if (bytes != null && bytes.length >= 1 &&
                bytes[bytes.length - 1] == '\n') {
            count++;
            if (bytes.length >= 2 && bytes[bytes.length - 2] == '\r') {
                count++;
            }
        }
        return count;
    }

    public String getJson(Object obj) {
        String json = "{}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            json = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

    public long getDomainCount() {
        return atomicIdGeneratorDomain.get();
    }

    public long getUrlCount() {
        return atomicIdGeneratorUrl.get();
    }
}
