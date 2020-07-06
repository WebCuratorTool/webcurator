package org.webcurator.core.coordinator;

import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.util.List;

public interface HarvestResultManager {
    void addHarvestResult(HarvestResultDTO hrDTO);

    void removeHarvestResult(HarvestResultDTO hrDTO);

    void removeHarvestResult(long targetInstanceId, int harvestResultNumber);

    HarvestResultDTO getHarvestResultDTO(long targetInstanceId, int harvestResultNumber) throws WCTRuntimeException;

    void updateHarvestResultStatus(long targetInstanceId, int harvestResultNumber, int state, int status);

    void updateHarvestResultStatus(HarvestResultDTO hrDTOFrom);

    void updateHarvestResultsStatus(List<HarvestResultDTO> harvestResultDTOList);
}
