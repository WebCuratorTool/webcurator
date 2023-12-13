package org.webcurator.core.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.util.WctUtils;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WaybackIndexer extends IndexerBase {

    //Static variables
    private final static Logger log = LoggerFactory.getLogger(WaybackIndexer.class);
    private final static String extensionRegex = "((\\.arc)|(\\.arc.gz)|(\\.warc)|(\\.warc.gz))$";

    public enum FileStatus {INITIAL, COPIED, INDEXED, REMOVED, FAILED}


    //Passed in variables
    private HarvestResultDTO result;
    private File directory;

    //Spring initialised variables (to be copied in copy constructor)
    private String waybackInputFolder;
    private String waybackMergedFolder;
    private String waybackFailedFolder;
    private long waittime;
    private long timeout;
    private boolean useSymLinks = false;
    private boolean enabled = false;

    //Internal variables
    private final List<MonitoredFile> indexFiles = new ArrayList<>();
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
        waybackFailedFolder = original.waybackFailedFolder;
        waittime = original.waittime;
        timeout = original.timeout;
        useSymLinks = original.useSymLinks;
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
        long tiId = getResult().getTargetInstanceOid();

        //Copy the Archive files to the Wayback input folder
        log.info("Generating indexes for " + getResult().getTargetInstanceOid());
        boolean failed = false;
        allIndexed = false;
        log.trace("{}, in directory {}, indexFiles.size={}", tiId, this.directory, indexFiles.size());
        if (indexFiles.isEmpty()) {
            log.error("Could not find any archive files in directory: " + this.directory);
        } else {
            log.trace("{}, going to copy files.", tiId);
            for (MonitoredFile f : indexFiles) {
                log.trace("{}, going to copy file: {} {}", tiId, f.theFile, f.status.name());
                if (f.getStatus() == FileStatus.INITIAL) {
                    log.trace("{}, the file {} is going to be copied: {}", tiId, f.theFile, f.status.name());
                    f.copyToInput();
                    log.trace("{}, copied the file: {} {}", tiId, f.theFile, f.status.name());
                }
            }
        }

        log.trace("{}, inspecting file copying result: {} -> {}", tiId, this.directory, this.waybackInputFolder);
        File[] originalFileList = this.directory.listFiles(new ARCFilter());
        if (originalFileList == null) {
            log.error("{}, Nothing found in directory: {}", tiId, this.directory);
            return;
        }
        for (File f : originalFileList) {
            File fInWaybackInputDirectory = new File(this.waybackInputFolder, getVersionedName(f.getName(), result.getHarvestNumber()));
            log.trace("{}, file copying result: {} -> {}, exists={}", tiId, f, fInWaybackInputDirectory, fInWaybackInputDirectory.exists());
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

    public boolean isUseSymLinks() {
        return useSymLinks;
    }

    public void setUseSymLinks(boolean useSymLinks) {
        this.useSymLinks = useSymLinks;
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

    public static String getVersionedName(String fileName, int hrNum) {
        String[] splitName = fileName.split(extensionRegex);
        if (splitName.length > 0) {
            String extension = fileName.substring(splitName[0].length());
            return splitName[0] + ".ver" + hrNum + extension;
        } else {
            return fileName;
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

        protected MonitoredFile(File theFile) {
            this.theFile = theFile;
            checkStatus(); //set the initial status
            log.trace("Created monitored file: {}, status: {}", this.theFile, this.status);
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
            int hrNum = result.getHarvestNumber();
            return WaybackIndexer.getVersionedName(fileName, hrNum);
        }


        protected String getPath() {
            return theFile.getPath();
        }

        protected void copyToInput() {
            long tiId = getResult().getTargetInstanceOid();
            File inputFile = new File(waybackInputFolder + "/" + getVersionedName());
            log.trace("inputFile:{}", inputFile.getAbsolutePath());
            try {
                File parentDirectory = inputFile.getParentFile();
                if (!parentDirectory.exists()) {
                    boolean ret = directory.mkdirs();
                    log.info("{}, tried to make the directory: {} {}", tiId, parentDirectory, ret);
                }
                log.trace("{}, useSymLinks={}", tiId, useSymLinks);
                if (!useSymLinks) {
                    log.trace("{}, try to copy file: {} -> {}", tiId, theFile, inputFile);
                    copyFile(theFile, inputFile);
                } else {
                    // Create symbolic link instead of copy
                    Path target = Paths.get(theFile.getAbsolutePath());
                    Path link = Paths.get(inputFile.getAbsolutePath());
                    log.trace("{}, tried to make link: {} -> {}", tiId, link, target);
                    Files.createSymbolicLink(link, target);
                }
                this.status = FileStatus.COPIED;
            } catch (IOException e) {
                log.error("Unable to copy: " + theFile.getAbsolutePath() + " to: " + inputFile.getAbsolutePath(), e);
            } catch (Throwable e) {
                log.error("{}, failed to copy file: {} -> {}", tiId, theFile, inputFile, e);
            } finally {
                log.trace("{}, copied file: {} -> {}, status: {}", tiId, theFile, inputFile, this.status);
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
            log.debug("Copy file: {} -> {}", source.getAbsolutePath(), destination.getAbsolutePath());

            InputStream is = new BufferedInputStream(Files.newInputStream(source.toPath()));
            OutputStream os = new BufferedOutputStream(Files.newOutputStream(destination.toPath()));

            WctUtils.copy(is, os);
        }
    }
}
