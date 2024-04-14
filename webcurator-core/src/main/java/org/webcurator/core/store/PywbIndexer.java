package org.webcurator.core.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.util.ProcessBuilderUtils;
import org.webcurator.core.util.WctUtils;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.File;

public class PywbIndexer extends IndexerBase {
    private static final Logger log = LoggerFactory.getLogger(PywbIndexer.class);
    private HarvestResultDTO result;
    private File directory;
    private boolean enabled;
    private String pywbManagerColl;
    private File pywbManagerStoreDir;
    private boolean isIndividualCollectionMode = true;

    public PywbIndexer() {
        super();
    }

    public PywbIndexer(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }

    public PywbIndexer(PywbIndexer original) {
        super(original);
        this.result = original.result;
        this.directory = original.directory;
        this.enabled = original.enabled;
        this.pywbManagerColl = original.pywbManagerColl;
        this.pywbManagerStoreDir = original.pywbManagerStoreDir;
        this.isIndividualCollectionMode = original.isIndividualCollectionMode;
    }

    @Override
    protected HarvestResultDTO getResult() {
        return this.result;
    }

    @Override
    public String getName() {
        return getClass().getCanonicalName();
    }

    @Override
    public RunnableIndex getCopy() {
        return new PywbIndexer(this);
    }

    @Override
    public void initialise(HarvestResultDTO result, File directory) {
        this.result = result;
        this.directory = directory;
    }

    @Override
    public Long begin() {
        return this.getResult().getOid();
    }

    @Override
    public void indexFiles(Long harvestResultOid) {
        if (!this.isEnabled()) {
            return;
        }

        String collName = this.pywbManagerColl;
        if (this.isIndividualCollectionMode) {
            collName = String.format("%d-%d", this.result.getTargetInstanceOid(), this.result.getHarvestNumber());
            File collPath = new File(pywbManagerStoreDir, "collections" + File.separator + collName);

            //Create the collection with "wb-manager" command
            boolean ret = WctUtils.cleanDirectory(collPath);
            if (!ret || collPath.exists()) {
                ProcessBuilderUtils.forceDeleteDirectory(collPath);
            }

            if (ProcessBuilderUtils.wbManagerInitCollection(pywbManagerStoreDir, collName) != 0) {
                log.error("Failed to init collection: {} {}", pywbManagerStoreDir, collName);
                return;
            } else {
                log.info("Init the collection: {} {}", pywbManagerStoreDir, collName);
            }
        }

        //Added the WARC files to pywb, and pywb will index automatically
        File[] warcFiles = this.directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".warc") ||
                name.toLowerCase().endsWith(".warc.gz"));
        if (warcFiles == null || warcFiles.length == 0) {
            String err = "No warc files found in folder: " + this.directory.getAbsolutePath();
            log.error(err);
            return;
        }

        //Added all warc files to the PYWB
        for (File warc : warcFiles) {
            int ret = ProcessBuilderUtils.wbManagerAddWarcFile(pywbManagerStoreDir, collName, warc.getAbsolutePath());
            if (ret != 0) {
                log.error("Failed to add file: {} to collection: {}", warc.getAbsolutePath(), collName);
                break;
            }
        }
    }

    @Override
    public void removeIndex(Long harvestResultOid) {
        //Remove the Archive files from the Wayback input folder
        log.info("Removing indexes for " + getResult().getTargetInstanceOid() + " HarvestNumber " + getResult().getHarvestNumber());
        if (this.isIndividualCollectionMode) {
            String collName = String.format("%d-%d", this.result.getTargetInstanceOid(), this.result.getHarvestNumber());
            File collPath = new File(pywbManagerStoreDir, "collections" + File.separator + collName);

            //delete the individual collection folder
            boolean ret = WctUtils.cleanDirectory(collPath);
            if (!ret || collPath.exists()) {
                ProcessBuilderUtils.forceDeleteDirectory(collPath);
            }
        } else {
            File[] warcFiles = this.directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".warc") ||
                    name.toLowerCase().endsWith(".warc.gz"));
            if (warcFiles == null || warcFiles.length == 0) {
                String err = "No warc files found in folder: " + this.directory.getAbsolutePath();
                log.error(err);
                return;
            }
            for (File warc : warcFiles) {
                File archivedWarc = new File(pywbManagerStoreDir, "archive" + File.separator + warc.getName());
                if (archivedWarc.exists()) {
                    boolean ret = archivedWarc.delete();
                    if (!ret) {
                        log.error("Failed to delete the file: {}", archivedWarc.getAbsolutePath());
                    }
                }
            }
        }
    }

    public void setPywbManagerColl(String pywbManagerColl) {
        this.pywbManagerColl = pywbManagerColl;
    }

    public void setPywbManagerStoreDir(File pywbManagerStoreDir) {
        this.pywbManagerStoreDir = pywbManagerStoreDir;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isIndividualCollectionMode() {
        return isIndividualCollectionMode;
    }

    public void setIndividualCollectionMode(boolean individualCollectionMode) {
        isIndividualCollectionMode = individualCollectionMode;
    }
}
