package org.webcurator.core.networkmap.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.webcurator.core.networkmap.metadata.NetworkMapNode;

import java.io.IOException;
import java.util.List;

public interface NetworkMapService extends DefaultServiceInterface {
    public static final Logger log = LoggerFactory.getLogger(NetworkMapService.class);

    public String get(long job, int harvestResultNumber, String key);

    public String getNode(long job, int harvestResultNumber, long id);

    public String getOutlinks(long job, int harvestResultNumber, long id);

    public String getChildren(long job, int harvestResultNumber, long id);

    public String getAllDomains(long job, int harvestResultNumber);

    public String getSeedUrls(long job, int harvestResultNumber);

    public String getMalformedUrls(long job, int harvestResultNumber);

    public String searchUrl(long job, int harvestResultNumber, NetworkMapServiceSearchCommand searchCommand);

    public String getHopPath(long job, int harvestResultNumber, long id);

    public String getHierarchy(long job, int harvestResultNumber, List<Long> ids);

    default public NetworkMapNode getNodeEntity(String json) {
        if (json == null) {
            return null;
        }

//        log.debug(json);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, NetworkMapNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
