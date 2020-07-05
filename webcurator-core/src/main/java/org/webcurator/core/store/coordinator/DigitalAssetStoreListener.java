package org.webcurator.core.store.coordinator;

import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.dto.SeedHistorySetDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DigitalAssetStoreListener {
    /**
     * Query history seeds from the Core
     *
     * @param targetInstanceOid The OID of the instance being archived.
     * @param harvestNumber     The order number of the harvest.
     * @return The history seeds
     */
    SeedHistorySetDTO dasQuerySeedHistory(long targetInstanceOid, int harvestNumber);

    /**
     * Notify the core that the modification is finished
     *
     * @param targetInstanceOid The OID of the instance being archived.
     * @param harvestNumber     The order number of the harvest.
     */
    void dasModificationComplete(long targetInstanceOid, int harvestNumber);

    /**
     * Download the original imported file from the core
     *
     * @param targetInstanceOid The OID of the instance being archived.
     * @param harvestNumber     The order number of the harvest.
     * @param fileName          The name of the file to be downloaded, e.g.: file name
     * @param req               Http Request
     * @param rsp               Http Response
     */
    void dasDownloadFile(long targetInstanceOid, int harvestNumber, String fileName, HttpServletRequest req, HttpServletResponse rsp);

    void dasHeartBeat(List<HarvestResultDTO> harvestResultDTOList);

    void dasUpdateHarvestResultStatus(HarvestResultDTO hrDTO);
}
