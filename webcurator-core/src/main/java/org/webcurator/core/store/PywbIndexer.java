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
    private boolean useSymLinkForArchive = false;

    public PywbIndexer() {
        super();
    }

    public PywbIndexer(PywbIndexer original) {
        super(original);
        this.result = original.result;
        this.directory = original.directory;
        this.enabled = original.enabled;
        this.pywbManagerColl = original.pywbManagerColl;
        this.pywbManagerStoreDir = original.pywbManagerStoreDir;
        this.isIndividualCollectionMode = original.isIndividualCollectionMode;
        this.useSymLinkForArchive = original.useSymLinkForArchive;
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
        log.info("Going to execute pywb indexing: {}", this.directory);
        if (!this.isEnabled()) {
            return;
        }

        String collName = this.pywbManagerColl;
        if (this.isIndividualCollectionMode) {
            collName = String.format("%d-%d", this.result.getTargetInstanceOid(), this.result.getHarvestNumber());
            File collPath = new File(pywbManagerStoreDir, "collections" + File.separator + collName);

            //Create the collection with "wb-manager" command
            log.info("Going to clean the individual directory: {}", collPath);
            boolean ret = WctUtils.cleanDirectory(collPath);
            if (!ret || collPath.exists()) {
                log.info("Failed to clean the individual directory normally, will force to delete the directory: {}", collPath);
                ProcessBuilderUtils.forceDeleteDirectory(collPath);
                log.info("The individual directory is force deleted: {}", collPath);
            } else {
                log.info("The individual directory is cleaned normally: {}", collPath);
            }
            log.info("The individual directory is cleaned: {}", collPath);

            log.info("Going to init collection: {} {}", pywbManagerStoreDir, collName);
            if (ProcessBuilderUtils.wbManagerInitCollection(pywbManagerStoreDir, collName) != 0) {
                log.error("Failed to init collection: {} {}", pywbManagerStoreDir, collName);
                return;
            } else {
                log.info("Initialed the collection: {} {}", pywbManagerStoreDir, collName);
            }

            if (this.useSymLinkForArchive) {
                // Remove original 'archive' folder in the new collection directory
                File archivePath = new File(pywbManagerStoreDir, "collections" + File.separator + collName + File.separator + "archive");

                log.info("Going to clean the directory: {}", archivePath);

                //Remove archive folder within collection directory
                ret = WctUtils.cleanDirectory(archivePath);
                if (!ret || archivePath.exists()) {
                    log.info("Failed to clean the archive directory normally, will force to delete the directory: {}", archivePath);
                    ProcessBuilderUtils.forceDeleteDirectory(archivePath);
                    log.info("The archive directory is force deleted: {}", archivePath);
                } else {
                    log.info("The archive directory is cleaned normally: {}", archivePath);
                }
                log.info("The archive directory is cleaned: {}", archivePath);

                // Create a symbolic link to the WARC files in the Target Instance + Harvest Result directory
                log.info("Going to create symlink: {} {} [archive]", collPath, this.directory);
                ret = ProcessBuilderUtils.createSymLink(collPath, this.directory, "archive");
                if (!ret) {
                    log.error("Failed to create Archive folder SymLink for collection: {} {}", this.directory, collName);
                    return;
                }
            }

        }

        // If Symlinking is used for the archive folder, then use wb-manager reindex as the WARC files don't need to be copied.
        if (this.useSymLinkForArchive) {
            log.info("Reindexing with symlink created: {}-{}", pywbManagerStoreDir, collName);
            // Index collection
            int ret = ProcessBuilderUtils.wbManagerReindexCollection(pywbManagerStoreDir, collName);
            if (ret != 0) {
                log.error("Failed to index collection: {}", collName);
            } else {
                log.info("Reindex finished with symlink created: {}-{}", pywbManagerStoreDir, collName);
            }
        } else {
            log.info("Going to add files to pywb collections: {}", this.directory);

            //Added the WARC files to pywb, and pywb will index automatically
            File[] warcFiles = this.directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".warc") ||
                    name.toLowerCase().endsWith(".warc.gz"));
            if (warcFiles == null || warcFiles.length == 0) {
                log.error("No warc files found in folder: {}", this.directory);
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
    }

    @Override
    public void removeIndex(Long harvestResultOid) {
        //Remove the Archive files from the pywb's input folder
        log.info("Removing indexes for {}-{}", getResult().getTargetInstanceOid(), getResult().getHarvestNumber());
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

    public boolean isUseSymLinkForArchive() {
        return this.useSymLinkForArchive;
    }

    public void setUseSymLinkForArchive(boolean useSymLinkForArchive) {
        this.useSymLinkForArchive = useSymLinkForArchive;
    }
}
