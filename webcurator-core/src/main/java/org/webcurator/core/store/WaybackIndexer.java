package org.webcurator.core.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.util.WctUtils;
import org.webcurator.domain.model.core.HarvestResultDTO;

public class WaybackIndexer extends IndexerBase {

    //Static variables
    private static Logger log = LoggerFactory.getLogger(WaybackIndexer.class);

    public static enum FileStatus {INITIAL, COPIED, INDEXED, REMOVED, FAILED}

    ;

    //Passed in variables
    private HarvestResultDTO result;
    private File directory;

    //Spring initialised variables (to be copied in copy constructor)
    private String waybackInputFolder;
    private String waybackMergedFolder;
    private String waybackFailedFolder;
    private long waittime;
    private long timeout;
    private boolean enabled = false;

    //Internal variables
    private List<MonitoredFile> indexFiles = new ArrayList<MonitoredFile>();
    private boolean allIndexed = false;

    public WaybackIndexer() {
        super();
    }

    public WaybackIndexer(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }

    protected WaybackIndexer(WaybackIndexer original) {
        super(original);
        waybackInputFolder = original.waybackInputFolder;
        waybackMergedFolder = original.waybackMergedFolder;
        waittime = original.waittime;
        timeout = original.timeout;
        enabled = original.enabled;
    }

    @Override
    public RunnableIndex getCopy() {
        return new WaybackIndexer(this);
    }

    @Override
    protected HarvestResultDTO getResult() {
        return result;
    }

    @Override
    public Long begin() {
        buildIndexFileList();
        return getResult().getOid();
    }

    @Override
    public String getName() {
        return getClass().getCanonicalName();
    }

    @Override
    public void indexFiles(Long harvestResultOid) {
        //Copy the Archive files to the Wayback input folder
        log.info("Generating indexes for " + getResult().getTargetInstanceOid());
        boolean failed = false;
        allIndexed = false;
        if (indexFiles.size() <= 0) {
            log.error("Could not find any archive files in directory: " + directory.getAbsolutePath());
        } else {
            for (MonitoredFile f : indexFiles) {
                if (f.getStatus() == FileStatus.INITIAL) {
                    f.copyToInput();
                }
            }
        }

        MonitoredFile lastFileNotIndexed = null;

        //Watch the Wayback merged/failed folders until the files appear
        long maxloops = timeout / waittime;
        for (long count = 0; count < maxloops && !allIndexed && !failed; count++) {
            try {
                Thread.sleep(waittime);
            } catch (InterruptedException e) {
                log.warn("Wayback indexing thread was interrupted.", e);
                break; //out of count < maxloops
            }

            for (MonitoredFile f : indexFiles) {
                allIndexed = true;
                FileStatus status = f.getStatus();
                if (status != FileStatus.INDEXED) {
                    if (status == FileStatus.FAILED) {
                        failed = true;
                        log.warn("Archive file failed Wayback indexing: " + f.getPath());
                    }
                    lastFileNotIndexed = f;
                    if (log.isDebugEnabled()) {
                        log.debug("Found at least one archive file not indexed: " + f.getPath() +
                                " (this will mean that indexing will fail to complete in a timely manner).");
                    }
                    allIndexed = false;
                    break; //out of for MonitoredFile loop
                }
            }
        }
        if (allIndexed) {
            log.info("Completed indexing for job " + getResult().getTargetInstanceOid());
        } else {
            log.warn("Job " + getResult().getTargetInstanceOid() + " failed to complete indexing in a timely manner.");
            if (lastFileNotIndexed != null) {
                log.warn("Job " + getResult().getTargetInstanceOid() + " last file not indexed: " + lastFileNotIndexed.getPath());
            }
        }
    }

    @Override
    public void removeIndex(Long harvestResultOid) {
        //Remove the Archive files from the Wayback input folder
        log.info("Removing indexes for " + getResult().getTargetInstanceOid() + " HarvestNumber " + getResult().getHarvestNumber());
        if (indexFiles.size() <= 0) {
            log.error("Could not find any archive files in directory: " + directory.getAbsolutePath());
        } else {
            for (MonitoredFile f : indexFiles) {
                f.removeFromInput();
            }
        }
    }

    @Override
    public void initialise(HarvestResultDTO result, File directory) {
        this.result = result;
        this.directory = directory;
    }

    public void setWaybackInputFolder(String waybackInputFolder) {
        this.waybackInputFolder = waybackInputFolder;
    }

    public String getWaybackInputFolder() {
        return waybackInputFolder;
    }

    public void setWaybackMergedFolder(String waybackMergedFolder) {
        this.waybackMergedFolder = waybackMergedFolder;
    }

    public String getWaybackMergedFolder() {
        return waybackMergedFolder;
    }

    public void setWaybackFailedFolder(String waybackFailedFolder) {
        this.waybackFailedFolder = waybackFailedFolder;
    }

    public String getWaybackFailedFolder() {
        return waybackFailedFolder;
    }

    public void setWaittime(long waittime) {
        this.waittime = waittime;
    }

    public long getWaittime() {
        return waittime;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    private void buildIndexFileList() {
        indexFiles.clear();

        File[] fileList = directory.listFiles(new ARCFilter());
        if (fileList != null) {
            for (File f : fileList) {
                indexFiles.add(new MonitoredFile(f));
            }
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    protected class MonitoredFile {
        private File theFile;
        private FileStatus status = FileStatus.INITIAL;
        private final String extensionRegex = "((\\.arc)|(\\.arc.gz)|(\\.warc)|(\\.warc.gz))$";

        protected MonitoredFile(File theFile) {
            this.theFile = theFile;
            checkStatus(); //set the initial status
        }

        protected FileStatus getStatus() {
            if (status == FileStatus.INITIAL || status == FileStatus.COPIED) {
                //Even with a file in the INITIAL state, there may already be a Wayback index
                checkStatus();
            }
            return status;
        }

        protected String getVersionedName() {
            String fileName = theFile.getName();
            String[] splitName = fileName.split(extensionRegex);
            if (splitName.length > 0) {
                String extension = fileName.substring(splitName[0].length(), fileName.length());
                return splitName[0] + ".ver" + result.getHarvestNumber() + extension;
            } else {
                return fileName;
            }
        }

        protected String getPath() {
            return theFile.getPath();
        }

        protected void copyToInput() {
            File inputFile = new File(waybackInputFolder + "/" + getVersionedName());
            try {
                File directory = inputFile.getParentFile();
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                copyFile(theFile, inputFile);
                status = FileStatus.COPIED;
            } catch (IOException e) {
                log.error("Unable to copy: " + theFile.getAbsolutePath() + " to: " + inputFile.getAbsolutePath(), e);
            }
        }

        protected void removeFromInput() {
            File inputFile = new File(waybackInputFolder + "/" + getVersionedName());
            if (inputFile.exists()) {
                if (inputFile.delete()) {
                    status = FileStatus.REMOVED;
                } else {
                    log.warn("Unable to remove Wayback indexed file: " + inputFile.getAbsolutePath());
                }
            } else {
                status = FileStatus.REMOVED;
            }
        }

        private void checkStatus() {
            File mergedFile = new File(waybackMergedFolder + "/" + getVersionedName());
            if (mergedFile.exists()) {
                status = FileStatus.INDEXED;
            }

            File failedFile = new File(waybackFailedFolder + "/" + getVersionedName());
            if (failedFile.exists()) {
                status = FileStatus.FAILED;
            }
        }

        private void copyFile(File source, File destination) throws IOException {
            log.debug("Copy file: {} --> {}", source.getAbsolutePath(), destination.getAbsolutePath());

            InputStream is = new BufferedInputStream(new FileInputStream(source));
            OutputStream os = new BufferedOutputStream(new FileOutputStream(destination));

            WctUtils.copy(is, os);
        }
    }
}
