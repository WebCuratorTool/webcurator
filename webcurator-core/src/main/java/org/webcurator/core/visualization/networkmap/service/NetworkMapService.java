package org.webcurator.core.visualization.networkmap.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.webcurator.core.visualization.VisualizationServiceInterface;
import org.webcurator.core.visualization.networkmap.metadata.NetworkDbVersionDTO;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeUrlEntity;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapUrlCommand;

import java.io.IOException;
import java.util.List;

public interface NetworkMapService extends VisualizationServiceInterface {
    NetworkMapResult initialIndex(long job, int harvestResultNumber);

    NetworkMapResult getDbVersion(long job, int harvestResultNumber);

//    NetworkMapResult get(long job, int harvestResultNumber, String key);

    NetworkMapResult getNode(long job, int harvestResultNumber, long id);

    NetworkMapResult getOutlinks(long job, int harvestResultNumber, long id);

    NetworkMapResult getChildren(long job, int harvestResultNumber, long id);

    NetworkMapResult getAllDomains(long job, int harvestResultNumber);

    NetworkMapResult getSeedUrls(long job, int harvestResultNumber);

    NetworkMapResult getMalformedUrls(long job, int harvestResultNumber);

    NetworkMapResult searchUrl(long job, int harvestResultNumber, NetworkMapServiceSearchCommand searchCommand);

    NetworkMapResult searchUrl2CascadePaths(long job, int harvestResultNumber, long folderId, NetworkMapServiceSearchCommand searchCommand);

    //List<String>
    NetworkMapResult searchUrlNames(long job, int harvestResultNumber, String substring);

    NetworkMapResult getHopPath(long job, int harvestResultNumber, long id);

    NetworkMapResult getHierarchy(long job, int harvestResultNumber, List<Long> ids);

    NetworkMapResult getUrlByName(long job, int harvestResultNumber, NetworkMapUrlCommand url);

    NetworkMapResult getUrlsByNames(long job, int harvestResultNumber, List<String> urlNameList);

    NetworkMapResult getProgress(long job, int harvestResultNumber);

    NetworkMapResult getProcessingHarvestResultDTO(long job, int harvestResultNumber);

    default NetworkMapNodeUrlEntity getNodeEntity(String json) {
        if (json == null) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, NetworkMapNodeUrlEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    default NetworkDbVersionDTO getDbVersionDTO(String json) {
        if (json == null) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(json, NetworkDbVersionDTO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
