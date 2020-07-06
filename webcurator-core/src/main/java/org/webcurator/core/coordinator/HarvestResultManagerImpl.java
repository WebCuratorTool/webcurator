package org.webcurator.core.coordinator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.core.visualization.VisualizationProgressView;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.TargetInstance;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("harvestResultManager")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class HarvestResultManagerImpl implements HarvestResultManager {
    private static final Logger log = LoggerFactory.getLogger(HarvestResultManagerImpl.class);
    private final Map<String, HarvestResultDTO> harvestResults = Collections.synchronizedMap(new HashMap<>());

    @Autowired
    private TargetInstanceManager targetInstanceManager;

    @Override
    public void addHarvestResult(HarvestResultDTO hrDTO) {
        harvestResults.put(hrDTO.getKey(), hrDTO);
    }

    @Override
    public void removeHarvestResult(HarvestResultDTO hrDTO) {
        harvestResults.remove(hrDTO.getKey());
    }

    @Override
    public void removeHarvestResult(long targetInstanceId, int harvestResultNumber) {
        String key = PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber);
        harvestResults.remove(key);
    }

    @Override
    public HarvestResultDTO getHarvestResultDTO(long targetInstanceId, int harvestResultNumber) throws WCTRuntimeException {
        String key = PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber);
        HarvestResultDTO hrDTO = harvestResults.get(key);
        if (hrDTO == null) {
            TargetInstance ti = targetInstanceManager.getTargetInstance(targetInstanceId);
            if (ti == null) {
                log.warn("TargetInstance does not exist: {}", targetInstanceId);
                throw new WCTRuntimeException("TargetInstance does not exist, targetInstanceId: " + targetInstanceId);
            }

            HarvestResult hr = ti.getHarvestResult(harvestResultNumber);
            if (hr == null) {
                log.warn("TargetInstance does not exist: {} {}", targetInstanceId, harvestResultNumber);
                throw new WCTRuntimeException("TargetInstance does not exist, targetInstanceId: " + targetInstanceId + ", harvestResultNumber: " + harvestResultNumber);
            }

            hrDTO = new HarvestResultDTO();
            hrDTO.setTargetInstanceOid(targetInstanceId);
            hrDTO.setHarvestNumber(harvestResultNumber);
            hrDTO.setState(hr.getState());
            if (hr.getState() == HarvestResult.STATE_CRAWLING
                    || hr.getState() == HarvestResult.STATE_MODIFYING
                    || hr.getState() == HarvestResult.STATE_INDEXING) {
                hrDTO.setStatus(HarvestResult.STATUS_SCHEDULED);
            } else {
                hrDTO.setStatus(HarvestResult.STATUS_UNASSESSED);
            }
            hrDTO.setCreatedByFullName(hr.getCreatedBy().getFullName());
            hrDTO.setDerivedFrom(hr.getDerivedFrom());
            hrDTO.setProvenanceNote(hr.getProvenanceNote());
            hrDTO.setCreationDate(hr.getCreationDate());

            harvestResults.put(hrDTO.getKey(), hrDTO);
        }

        return hrDTO;
    }

    @Override
    public void updateHarvestResultStatus(long targetInstanceId, int harvestResultNumber, int state, int status) {
        HarvestResultDTO hrDTO = getHarvestResultDTO(targetInstanceId, harvestResultNumber);
        if (hrDTO != null) {
            hrDTO.setState(state);
            hrDTO.setStatus(status);
        }
    }

    @Override
    public void updateHarvestResultStatus(HarvestResultDTO hrDTO) {
        harvestResults.put(hrDTO.getKey(), hrDTO);
    }

    @Override
    public void updateHarvestResultsStatus(List<HarvestResultDTO> harvestResultDTOList) {
        harvestResults.clear();
        for (HarvestResultDTO hrDTO : harvestResultDTOList) {
            harvestResults.put(hrDTO.getKey(), hrDTO);
        }
    }
}
