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
package org.webcurator.core.store;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyResult;
import org.webcurator.domain.model.core.CustomDepositFormCriteriaDTO;
import org.webcurator.domain.model.core.CustomDepositFormResultDTO;
import org.webcurator.domain.model.core.HarvestResultDTO;

/**
 * The <code>DigitalAssetStore</code> interface is used by the WCT Core and
 * WCT Harvest Agent to interact with the digital asset store component.
 */
public interface DigitalAssetStore {

    String FILE_UPLOAD_MODE_COPY = "copy";
    String FILE_UPLOAD_MODE_STREAM = "stream";


    /**
     * Save an array of files to the digital asset store. The files are
     * saved to the first harvest result, so this method should be used
     * only for files coming directly from the Harvest Agent.
     *
     * @param targetInstanceName The OID of the target instance to save the files to.
     * @param directory          The subdirectory under the target instance
     *                           directory to which to save the files.
     * @param path               The files to send to the asset store.
     * @throws DigitalAssetStoreException if there are any errors.
     */
    void save(String targetInstanceName, String directory, Path path) throws DigitalAssetStoreException;

    /**
     * Initiate the indexing of a Harvest Result.
     *
     * @param harvestResult The DTO of the Harvest Result to be indexed.
     */
    void initiateIndexing(HarvestResultDTO harvestResult) throws DigitalAssetStoreException;

    /**
     * Initiate the removal of indexes of the given HarvestResult.
     *
     * @param harvestResult The HarvestResult to remove indexes for.
     * @throws DigitalAssetStoreException if any errors occur.
     */
    void initiateRemoveIndexes(HarvestResultDTO harvestResult) throws DigitalAssetStoreException;

    /**
     * Check the indexing of a Harvest Result.
     *
     * @param harvestResultOid the harvest result to check
     * @return true if the harvestResult is currently indexing.
     */
    Boolean checkIndexing(Long harvestResultOid) throws DigitalAssetStoreException;

    /**
     * Submits a harvest result to the archive. This method will use a callback to
     * send the unique identifier returned from the archive.
     *
     * @param targetInstanceOid The OID of the target instance.
     * @param sip               The XML SIP file.
     * @param xAttributes       A Map of indexed attributes. This map may or
     *                          may not be used by the archive adapter
     *                          implementation.
     * @param harvestNumber     The number of the harvest result to submit.
     * @throws DigitalAssetStoreException if any errors occur.
     */
    void submitToArchive(String targetInstanceOid, String sip, Map xAttributes, int harvestNumber)
            throws DigitalAssetStoreException;

    /**
     * Purge all the data from the digital asset store for the target instances
     * specified in the list of target instance names.
     *
     * @param targetInstanceNames the target instances to purge
     * @throws DigitalAssetStoreException if any errors occur.
     */
    void purge(List<String> targetInstanceNames) throws DigitalAssetStoreException;

    /**
     * Purge all the data from the digital asset store for the target instances
     * specified in the list of **aborted** target instance names.
     *
     * @param targetInstanceNames the target instances to purge
     * @throws DigitalAssetStoreException if any errors occur.
     */
    void purgeAbortedTargetInstances(List<String> targetInstanceNames) throws DigitalAssetStoreException;

    /**
     * Determine whether a custom deposit form is required to be shown before
     * submitting a harvest to a specific digital asset store.
     *
     * @param criteria Provides parameters that are required for DAS to make this decision.
     * @return The response DTO that indicates whether a custom form is required,
     * and if so, the location/content of the custom form.
     * @throws Exception thrown if there is an error
     */
    CustomDepositFormResultDTO getCustomDepositFormDetails(CustomDepositFormCriteriaDTO criteria)
            throws DigitalAssetStoreException;

    /**
     * To modify ( prune and import) the harvest
     *
     * @param cmd The prune and import metadata list
     * @return the command is accepted or not
     */
    ModifyResult initialPruneAndImport(ModifyApplyCommand cmd);

    /**
     * To clear the patching Harvest Result, Index, and the Mod Harvest Files
     *
     * @param stage:            the stage of the request: crawling, modifying or indexing
     * @param command:          the action of the command
     * @param targetInstanceId: the ID of target instance
     * @param harvestNumber:    the number of harvest result
     * @throws DigitalAssetStoreException thrown if there is an error
     */
    void operateHarvestResultModification(String stage, String command, long targetInstanceId, int harvestNumber) throws DigitalAssetStoreException;
}
