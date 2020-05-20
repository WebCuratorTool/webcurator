package org.webcurator.core.store;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandResult;
import org.webcurator.domain.model.core.CustomDepositFormCriteriaDTO;
import org.webcurator.domain.model.core.CustomDepositFormResultDTO;
import org.webcurator.domain.model.core.HarvestResourceDTO;
import org.webcurator.domain.model.core.HarvestResultDTO;

public class MockDigitalAssetStore implements DigitalAssetStore {

	private static Log log = LogFactory.getLog(MockDigitalAssetStore.class);
	private Boolean checkIndexing = false;
	private List<HarvestResultDTO> removedIndexes = new ArrayList<HarvestResultDTO>();
	
	public MockDigitalAssetStore() {
		// TODO Auto-generated constructor stub
	}

	public HarvestResultDTO copyAndPrune(String targetInstanceName,
                                         int originalHarvestResultNumber, int newHarvestResultNumber,
                                         List<String> urisToDelete, List<HarvestResourceDTO> harvestResourcesToImport) throws DigitalAssetStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Header> getHeaders(String targetInstanceName,
			int harvestResultNumber, HarvestResourceDTO resource)
			throws DigitalAssetStoreException {
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	public Path getResource(String targetInstanceName, int harvestResultNumber,
                            HarvestResourceDTO resource) throws DigitalAssetStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] getSmallResource(String targetInstanceName,
			int harvestResultNumber, HarvestResourceDTO resource)
			throws DigitalAssetStoreException {
		// TODO Auto-generated method stub
		return null;
	}

	public void initiateIndexing(HarvestResultDTO harvestResult)
			throws DigitalAssetStoreException {
		// TODO Auto-generated method stub

	}
	
	public void initiateRemoveIndexes(HarvestResultDTO harvestResult)
			throws DigitalAssetStoreException {
		log.info("Removing indexes for TargetInstance "+harvestResult.getTargetInstanceOid()+ " HarvestNumber "+harvestResult.getHarvestNumber());
		removedIndexes.add(harvestResult);
	}

	public void purge(List<String> targetInstanceNames)
			throws DigitalAssetStoreException {
		// TODO Auto-generated method stub

	}

	public void purgeAbortedTargetInstances(List<String> targetInstanceNames)
	throws DigitalAssetStoreException {
		// TODO Auto-generated method stub

}

	public void save(String targetInstanceName, List<Path> paths)
			throws DigitalAssetStoreException {
		// TODO Auto-generated method stub

	}

	public void save(String targetInstanceName, Path path)
			throws DigitalAssetStoreException {
		// TODO Auto-generated method stub

	}

	public void save(String targetInstanceName, String directory, List<Path> paths)
			throws DigitalAssetStoreException {
		// TODO Auto-generated method stub

	}

	public void save(String targetInstanceName, String directory, Path path)
			throws DigitalAssetStoreException {
		// TODO Auto-generated method stub

	}

	public void submitToArchive(String targetInstanceOid, String sip,
			Map attributes, int harvestNumber)
			throws DigitalAssetStoreException {
		// TODO Auto-generated method stub

	}

	public Boolean checkIndexing(Long harvestResultOid) throws DigitalAssetStoreException {
		return checkIndexing;
	}
	
	public void setCheckIndexingReturn(Boolean checkIndexing)
	{
		this.checkIndexing = checkIndexing;
	}
	
	public List<HarvestResultDTO> getRemovedIndexes() {
		return removedIndexes;
	}
	
	public CustomDepositFormResultDTO getCustomDepositFormDetails(CustomDepositFormCriteriaDTO criteria) throws DigitalAssetStoreException {
		return null;
	}

	@Override
	public PruneAndImportCommandResult pruneAndImport(PruneAndImportCommandApply cmd) {
		return null;
	}

}
