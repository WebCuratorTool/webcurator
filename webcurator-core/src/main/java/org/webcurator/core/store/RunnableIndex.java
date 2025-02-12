package org.webcurator.core.store;

import java.io.File;
import java.util.concurrent.Callable;

import org.webcurator.domain.model.core.HarvestResultDTO;

public interface RunnableIndex extends Callable<Boolean> {

	public enum Mode {INDEX, REMOVE};
	String getName();
	RunnableIndex getCopy();
	void setMode(Mode mode);
	void initialise(HarvestResultDTO result, File directory);
	Long begin();
	void indexFiles(Long harvestResultOid);
//	void markComplete(Long harvestResultOid);
	void removeIndex(Long harvestResultOid);
	boolean isEnabled();
}
