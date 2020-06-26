/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.core.harvester.coordinator;

import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRow;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;
import org.webcurator.domain.model.dto.SeedHistorySetDTO;

/**
 * Callback interface for the DAS. The DAS calls this interface when it
 * has completed/failed to archive a harvest.
 *
 * @author beaumontb
 */
public interface DasCallback {
    /**
     * Advises the Core that the harvest has been archived successfully.
     *
     * @param targetInstanceOid The OID of the instance being archived.
     * @param archiveIID        The IID returned by the archive system.
     */
    public void completeArchiving(Long targetInstanceOid, String archiveIID);

    /**
     * Advises the Core that the harvest failed to be archived.
     *
     * @param targetInstanceOid The OID of the instance being archived.
     * @param message           The error message received from the archive.
     */
    public void failedArchiving(Long targetInstanceOid, String message);

    /**
     * Query history seeds from the Core
     *
     * @param targetInstanceOid The OID of the instance being archived.
     * @param harvestNumber     The order number of the harvest.
     * @return The history seeds
     */
    public SeedHistorySetDTO querySeedHistory(Long targetInstanceOid, Integer harvestNumber);

    /**
     * Notify the core that the modification is finished
     *
     * @param targetInstanceOid The OID of the instance being archived.
     * @param harvestNumber     The order number of the harvest.
     */
    public void modificationComplete(Long targetInstanceOid, Integer harvestNumber);

    /**
     * Download the original imported file from the core
     *
     * @param targetInstanceOid The OID of the instance being archived.
     * @param harvestNumber     The order number of the harvest.
     * @param cmd   The metadata of the file to be downloaded, e.g.: file name
     * @return The content and metadata of the file to be download
     */
    PruneAndImportCommandRow modificationDownloadFile(Long targetInstanceOid, Integer harvestNumber, PruneAndImportCommandRowMetadata cmd);
}
