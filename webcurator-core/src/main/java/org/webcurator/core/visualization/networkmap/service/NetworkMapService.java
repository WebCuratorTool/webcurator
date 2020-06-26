package org.webcurator.core.visualization.networkmap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;
import org.webcurator.core.visualization.VisualizationServiceInterface;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;

import java.io.IOException;
import java.util.List;

public interface NetworkMapService extends VisualizationServiceInterface {
    NetworkMapResult get(long job, int harvestResultNumber, String key);

    NetworkMapResult getNode(long job, int harvestResultNumber, long id);

    NetworkMapResult getOutlinks(long job, int harvestResultNumber, long id);

    NetworkMapResult getChildren(long job, int harvestResultNumber, long id);

    NetworkMapResult getAllDomains(long job, int harvestResultNumber);

    NetworkMapResult getSeedUrls(long job, int harvestResultNumber);

    NetworkMapResult getMalformedUrls(long job, int harvestResultNumber);

    NetworkMapResult searchUrl(long job, int harvestResultNumber, NetworkMapServiceSearchCommand searchCommand);

    //List<String>
    NetworkMapResult searchUrlNames(long job, int harvestResultNumber, String substring);

    NetworkMapResult getHopPath(long job, int harvestResultNumber, long id);

    NetworkMapResult getHierarchy(long job, int harvestResultNumber, List<Long> ids);

    NetworkMapResult getUrlByName(long job, int harvestResultNumber, String urlName);

    VisualizationProgressBar getProgress(long targetInstanceId, int harvestResultNumber);

    default NetworkMapNode getNodeEntity(String json) {
        if (json == null) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, NetworkMapNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
