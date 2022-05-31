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
package org.webcurator.core.store.arc;

import static org.webcurator.core.archive.Constants.ARC_FILE;
import static org.webcurator.core.archive.Constants.LOG_FILE;
import static org.webcurator.core.archive.Constants.REPORT_FILE;
import static org.webcurator.core.archive.Constants.ROOT_FILE;

import it.unipi.di.util.ExternalSort;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.io.FileUtils;
import org.archive.format.warc.WARCConstants;
import org.archive.io.*;
import org.archive.io.arc.ARCRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.common.util.SafeSimpleDateFormat;
import org.webcurator.core.archive.Archive;
import org.webcurator.core.archive.ArchiveFile;
import org.webcurator.core.archive.file.FileArchive;
import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.core.coordinator.WctCoordinatorPaths;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.store.Constants;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.reader.LogProvider;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.store.Indexer;
import org.webcurator.core.util.WctUtils;
import org.webcurator.core.visualization.VisualizationAbstractProcessor;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.VisualizationDirectoryManager;
import org.webcurator.core.visualization.VisualizationProcessorManager;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyResult;
import org.webcurator.core.visualization.modification.processor.ModifyProcessorWarc;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeDTO;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapUrl;
import org.webcurator.core.visualization.networkmap.processor.IndexProcessorWarc;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.domain.model.core.*;

import javax.imageio.ImageIO;

/**
 * The ArcDigitalAssetStoreService is used for storing and accessing the
 * completed harvest data from the digital asset store.
 *
 * @author bbeaumont
 */
@SuppressWarnings("all")
@Component("arcDigitalAssetStoreService")
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class ArcDigitalAssetStoreService extends AbstractRestClient implements DigitalAssetStore, LogProvider {
    /**
     * The logger.
     */
    private static Logger log = LoggerFactory.getLogger(ArcDigitalAssetStoreService.class);

    @Autowired
    private VisualizationDirectoryManager directoryManager;

    /**
     * the base directory for the digital asset stores harvest files.
     */
    @Value("${arcDigitalAssetStoreService.baseDir}")
    private String baseDir = null;
    /**
     * Constant for the size of a buffer.
     */
    private final int BYTE_BUFF_SIZE = 1024;

    /**
     * Arc files meta data date format.
     */
    private static final SimpleDateFormat sdf = SafeSimpleDateFormat.getInstance("yyyyMMddHHmmss");
    private static final SimpleDateFormat writerDF = SafeSimpleDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'");
    /**
     * The Indexer
     */
    @Autowired
    private Indexer indexer = null;
    /**
     * The DAS File Mover
     */
    private DasFileMover dasFileMover = null;

    private FileArchive fileArchive = null;

    @Autowired
    private Archive arcDasArchive;

    @Autowired
    private NetworkMapClient networkMapClient;

    @Autowired
    private VisualizationProcessorManager visualizationProcessorManager;
    private ScreenshotGenerator screenshotGenerator;
    /**
     * the fullpage screenshot command.
     */
    private String screenshotCommandFullpage = null;
    /**
     * the screen screenshot command.
     */
    private String screenshotCommandScreen = null;
    /**
     * the windowsize screenshot command.
     */
    private String screenshotCommandWindowsize = null;


    @Autowired
    private BDBNetworkMapPool pool;

    @Autowired
    private WctCoordinatorClient wctCoordinatorClient;

    private String pageImagePrefix = "PageImage";
    private String aqaReportPrefix = "aqa-report";
    /**
     * the base url for the wayback viewer
     */
    private String harvestWaybackViewerBaseUrl;

    /**
     * Determines whether the harvest should stop if screenshots aren't generated
     */
    private boolean abortHarvestOnScreenshotFailure;

    /**
     * Determines whether or not to do screenshots
     */
    private boolean enableScreenshots;


    public ArcDigitalAssetStoreService() {
        super();
    }

    public ArcDigitalAssetStoreService(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        writerDF.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Read data from HTTP API and save to storage of store component
     *
     * @param targetInstanceName: Target Instance ID
     * @param directory:          Sub-directory of input files
     * @param fileName:           The file's name
     * @param inputStream:        The input stream
     * @throws DigitalAssetStoreException
     */
    public void save(String targetInstanceName, String directory, String fileName, InputStream inputStream) throws DigitalAssetStoreException {
        if (directory == null || directory.trim().length() == 0) {
            directory = "1";
        }

        // Target destination is always baseDir plus targetInstanceName.
        File targetDir = new File(baseDir, String.format("%s%s%s", targetInstanceName, File.separator, directory));

        // Create the target dir if is doesn't exist. This will also
        // create the parent if necessary.
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        // Move the ARC files into the /1 directory.
        boolean success = true;
        Exception failureException = null;

        // Loop through all the files, but stop if any of them fail.
        File destination = new File(targetDir, fileName);
        log.debug("Moving File to Store: " + fileName + " -> " + destination.getAbsolutePath());

        try {
            WctUtils.copy(inputStream, new BufferedOutputStream(new FileOutputStream(destination)));
        } catch (IOException ex) {
            log.error("Failed to move file " + fileName + " to " + destination.getAbsolutePath(), ex);
            failureException = ex;
            success = false;
        }

        // If the copy failed, throw an exception.
        if (!success) {
            throw new DigitalAssetStoreException("Failed to move Archive files to " + targetDir + "/" + directory, failureException);
        }
    }

    /**
     * @see DigitalAssetStore#save(String, String, List<java.nio.file.Path>).
     */
    private void save(String targetInstanceName, String directory, List<Path> paths) throws DigitalAssetStoreException {
        // Target destination is always baseDir plus targetInstanceName.
        File targetDir = new File(baseDir, targetInstanceName);
        String dir = directory + "/";

        // Create the target dir if is doesn't exist. This will also
        // create the parent if necessary.
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        // Move the ARC files into the /1 directory.
        new File(targetDir, dir).mkdirs();
        boolean success = true;
        Exception failureException = null;

        // Loop through all the files, but stop if any of them fail.
        for (Path path : paths) {
            File destination = new File(targetDir, "/" + dir + path.getFileName());
            log.debug("Moving File to Store: " + path.toString() + " -> " + destination.getAbsolutePath());

            try {
                // FileUtils.copyFile(files[i], destination);
                // DasFileMover fileMover = new InputStreamDasFileMover();
                dasFileMover.moveFile(path.toFile(), destination);
            } catch (IOException ex) {
                log.error("Failed to move file " + path.toString() + " to " + destination.getAbsolutePath(), ex);
                failureException = ex;
                success = false;
            }
        }

        // If the copy failed, throw an exception.
        if (!success) {
            throw new DigitalAssetStoreException("Failed to move Archive files to " + targetDir + "/" + dir, failureException);
        }
    }

    @Override
    public void save(String targetInstanceName, String directory, Path path)
            throws DigitalAssetStoreException {
        save(targetInstanceName, directory, Collections.singletonList(path));
    }

    /**
     * @see DigitalAssetStore#getResource(String, int, HarvestResourceDTO).
     */
    @SuppressWarnings("finally")
    public Path getResource(long targetInstanceId, int harvestResultNumber, String resourceUrl)
            throws DigitalAssetStoreException {
        NetworkMapNodeDTO resourceNode = this.queryUrlNode(targetInstanceId, harvestResultNumber, resourceUrl);

        FileOutputStream fos = null;
        ArchiveReader reader = null;
        ArchiveRecord record = null;
        File source = null;
        File dest = null;
        try {
            source = new File(this.baseDir, "/" + targetInstanceId + "/"
                    + harvestResultNumber + "/" + resourceNode.getFileName());

            try {
                reader = ArchiveReaderFactory.get(source);
            } catch (IOException ex) {
                if (log.isWarnEnabled()) {
                    log.warn("Failed to get resource : " + ex.getMessage());
                }
                source = new File(fileArchive.getArchiveRepository() + "/"
                        + targetInstanceId + "/"
                        + fileArchive.getArchiveArcDirectory() + "/"
                        + resourceNode.getFileName());
                if (log.isWarnEnabled()) {
                    log.info("trying filestore " + source.getAbsolutePath());
                }
                try {
                    reader = ArchiveReaderFactory.get(source);
                } catch (IOException e) {
                    throw new DigitalAssetStoreException(
                            "Failed to get resource : " + e.getMessage());
                }
            }

            record = reader.get(resourceNode.getOffset());

            dest = File.createTempFile("wct", "tmp");
            if (log.isDebugEnabled()) {
                log.debug("== Temp file: " + dest.getAbsolutePath());
            }

            fos = new FileOutputStream(dest);

            if (record instanceof ARCRecord) {
                ((ARCRecord) record).skipHttpHeader();
            } else {
                skipStatusLine(record);
                skipHeaders(record);
            }

            int bytesRead = 0;
            byte[] byteBuffer = new byte[BYTE_BUFF_SIZE];
            while ((bytesRead = record.read(byteBuffer, 0, BYTE_BUFF_SIZE)) != -1) {
                fos.write(byteBuffer, 0, bytesRead);
            }
            if (fos != null)
                fos.close();

        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to get resource : " + e.getMessage(), e);
            }
            throw new DigitalAssetStoreException("Failed to get resource : "
                    + e.getMessage());
        } catch (RuntimeException ex) {
            if (log.isErrorEnabled()) {
                log.error("Failed to get resource : " + ex.getMessage(), ex);
            }
            throw new DigitalAssetStoreException("Failed to get resource : "
                    + ex.getMessage());
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close fos failed " + ex.getMessage(), ex);
                }
            }
            try {
                if (record != null)
                    record.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close record failed " + ex.getMessage(), ex);
                }
            }
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close reader failed " + ex.getMessage(), ex);
                }
            }
        }

        return dest.toPath();
    }

    /**
     * @see DigitalAssetStore#getSmallResource(String, int, HarvestResourceDTO).
     */
    public byte[] getSmallResource(long targetInstanceId, int harvestResultNumber, String resourceUrl)
            throws DigitalAssetStoreException {
        NetworkMapNodeDTO resourceNode = this.queryUrlNode(targetInstanceId, harvestResultNumber, resourceUrl);

        ArchiveRecord record = null;
        ArchiveReader reader = null;
        File source = null;
        try {
            source = new File(this.baseDir, "/" + targetInstanceId + "/"
                    + harvestResultNumber + "/" + resourceNode.getFileName());
            try {
                reader = ArchiveReaderFactory.get(source);

            } catch (IOException e) {
                if (log.isWarnEnabled()) {

                    log.error("Failed to get resource from "
                            + source.getAbsolutePath() + " from local store");
                }
                source = new File(fileArchive.getArchiveRepository() + "/"
                        + targetInstanceId + "/"
                        + fileArchive.getArchiveArcDirectory() + "/"
                        + resourceNode.getFileName());
                if (log.isWarnEnabled()) {
                    log.info("trying filestore " + source.getAbsolutePath());
                }
                reader = ArchiveReaderFactory.get(source);
            }
            record = reader.get(resourceNode.getOffset());

            if (record instanceof ARCRecord) {
                ((ARCRecord) record).skipHttpHeader();
            } else {
                skipStatusLine(record);
                skipHeaders(record);
            }

            ByteArrayOutputStream fos = new ByteArrayOutputStream(1024 * 1024);

            int bytesRead = 0;
            byte[] byteBuffer = new byte[BYTE_BUFF_SIZE];
            while ((bytesRead = record.read(byteBuffer, 0, BYTE_BUFF_SIZE)) != -1) {
                fos.write(byteBuffer, 0, bytesRead);
            }
            fos.close();

            // 5. Return the result.
            return fos.toByteArray();
        } catch (IOException ex) {
            if (log.isWarnEnabled()) {
                log.warn("Failed to get resource : " + ex.getMessage());
            }
        } catch (RuntimeException ex) {
            if (log.isErrorEnabled()) {
                log.error("Failed to get resource : " + ex.getMessage(), ex);
            }
            throw new DigitalAssetStoreException("Failed to get resource : "
                    + ex.getMessage(), ex);
        } finally {
            try {
                if (record != null)
                    record.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close record failed " + ex.getMessage(), ex);
                }
            }
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close reader failed " + ex.getMessage(), ex);
                }
            }
        }
        return new byte[0];
    }

    /**
     * @see DigitalAssetStore#getHeaders(String, int, HarvestResourceDTO).
     */
    public List<Header> getHeaders(long targetInstanceId, int harvestResultNumber, String resourceUrl)
            throws DigitalAssetStoreException {
        log.debug("Start of getHeaders()");

        NetworkMapNodeDTO resourceNode = this.queryUrlNode(targetInstanceId, harvestResultNumber, resourceUrl);

        List<Header> headers = new ArrayList<>();
        ArchiveRecord record = null;
        ArchiveReader reader = null;

        log.debug("Determining the filename");

        File source = new File(this.baseDir, "/" + targetInstanceId + "/"
                + harvestResultNumber + "/" + resourceNode.getFileName());

        try {
            log.debug("Create the Archive File Reader");

            try {
                reader = ArchiveReaderFactory.get(source);
            } catch (IOException e) {
                log.warn("Could not read headers for ArchiveRecord from " + source.getAbsolutePath() + " from local store");

                source = new File(fileArchive.getArchiveRepository() + "/"
                        + targetInstanceId + "/"
                        + fileArchive.getArchiveArcDirectory() + "/"
                        + resourceNode.getFileName());
                log.info("trying filestore " + source.getAbsolutePath());

                reader = ArchiveReaderFactory.get(source);
            }

            log.debug("Skipping to the appropriate record at offset: " + resourceNode.getOffset());

            record = reader.get(resourceNode.getOffset());

            if (record instanceof ARCRecord) {
                log.debug("Reading the headers");
                ((ARCRecord) record).skipHttpHeader();
                Header[] headersArray = ((ARCRecord) record).getHttpHeaders();
                headers.addAll(Arrays.asList(headersArray));
            } else {
                log.debug("Reading the headers");
                skipStatusLine(record);
                Header[] headersArray = HttpParser.parseHeaders(record,
                        WARCConstants.DEFAULT_ENCODING);
                headers.addAll(Arrays.asList(headersArray));
            }

            Header httpResponseStatusCode = new Header("HTTP-RESPONSE-STATUS-CODE", Integer.toString(resourceNode.getStatusCode()));
            headers.add(httpResponseStatusCode);

            Header httpResponseContentLength = new Header("HTTP-RESPONSE-CONTENT_LENGTH", Long.toString(resourceNode.getContentLength()));
            headers.add(httpResponseContentLength);

            return headers;
        } catch (IOException ex) {
            if (log.isWarnEnabled()) {
                log.warn("Error reading headers from ArchiveRecord: "
                        + ex.getMessage());
            }
        } finally {
            try {
                if (record != null)
                    record.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close record failed " + ex.getMessage(), ex);
                }
            }
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close reader failed " + ex.getMessage(), ex);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("End of method");
            }
        }
        return null;
    }

    private NetworkMapNodeDTO queryUrlNode(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        NetworkMapUrl url = new NetworkMapUrl();
        url.setUrlName(resourceUrl);
        NetworkMapResult result = networkMapClient.getUrlByName(targetInstanceId, harvestResultNumber, url);

        String err = String.format("Could not find NetworkMapNode with targetInstanceId=%d, harvestResultNumber=%d, resourceUrl=%s", targetInstanceId, harvestResultNumber, resourceUrl);
        if (result == null || result.getRspCode() != 0) {
            log.warn(err);
            throw new DigitalAssetStoreException(err);
        }

        String json = (String) result.getPayload();
        NetworkMapNodeDTO node = networkMapClient.getNodeEntity(json);
        if (node == null) {
            log.warn(err);
            throw new DigitalAssetStoreException(err);
        }

        return node;
    }

    /**
     * Search the passed in list for an item matching the passed in Url.
     *
     * @param hrs - A list of HarvestResourceDTO objects
     * @param Url - The Url to check for.
     * @return true if a list item's name is equal to the Url, or else false
     */
    private boolean listContainsURL(List<HarvestResourceDTO> hrs, String Url) {
        for (Iterator<HarvestResourceDTO> it = hrs.iterator(); it.hasNext(); ) {
            HarvestResourceDTO hr = it.next();
            if (hr.getName().equals(Url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.webcurator.core.reader.LogProvider#getLogFile(java.lang.String,
     * java.lang.String)
     */
    public File getLogFile(String aJob, String aFileName) {
        JobItem jobItem = JobItem.getInstance(aJob);

        File file = null;

        if (!jobItem.isPatchedJob()) {
            File targetDir = new File(baseDir, jobItem.getJobName());
            File logsDir = new File(targetDir, Constants.DIR_LOGS);
            file = new File(logsDir, aFileName);
            if (!file.exists() && aFileName.equalsIgnoreCase(Constants.SORTED_CRAWL_LOG_FILE)) {
                // we need to create sorted crawl.log from crawl.log.
                createSortedCrawlLogFile(logsDir);
                file = new File(logsDir, aFileName);
            }
            if (!file.exists()) {
                logsDir = new File(targetDir, Constants.DIR_REPORTS);
                file = new File(logsDir, aFileName);
            }
            if (!file.exists()) {
                logsDir = new File(targetDir, Constants.DIR_CONTENT);
                file = new File(logsDir, aFileName);
            }
        } else {
            file = new File(directoryManager.getBaseLogDir(jobItem.getTargetInstancdId()), aFileName);
            if (!file.exists()) {
                file = new File(directoryManager.getBaseReportDir(jobItem.getTargetInstancdId()), aFileName);
            }
        }

        if (!file.exists()) {
            file = null;
        }
        return file;
    }

    private void createSortedCrawlLogFile(File logsDir) {

        // sort the crawl.log file to create a sorted crawl.log file in the same
        // directory.

        // write new 'stripped' crawl.log, replacing multiple spaces with a
        // single space in each record..
        try {

            BufferedReader inputStream = new BufferedReader(new FileReader(
                    logsDir.getAbsolutePath() + File.separator
                            + Constants.CRAWL_LOG_FILE));
            PrintWriter outputStream = new PrintWriter(new FileWriter(
                    logsDir.getAbsolutePath() + File.separator
                            + Constants.STRIPPED_CRAWL_LOG_FILE));

            String inLine = null;

            while ((inLine = inputStream.readLine()) != null) {
                outputStream.println(inLine.replaceAll(" +", " "));
            }

            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
            return;
        }

        // sort the 'stripped' crawl.log file to create a 'sorted' crawl.log
        // file...
        ExternalSort sort = new ExternalSort();
        try {
            sort.setInFile(logsDir.getAbsolutePath() + File.separator
                    + Constants.STRIPPED_CRAWL_LOG_FILE);
        } catch (FileNotFoundException e1) {
            return;
        }
        try {
            sort.setOutFile(logsDir.getAbsolutePath() + File.separator
                    + Constants.SORTED_CRAWL_LOG_FILE);
        } catch (FileNotFoundException e1) {
            return;
        }
        // sort on fourth column (url) then first column (timestamp)..
        int[] cols = {3, 0};
        sort.setColumns(cols);
        sort.setSeparator(' '); // space

        try {
            sort.run();
        } catch (IOException e1) {
            return;
        }
    }

    /**
     * @see org.webcurator.core.reader.LogProvider#getLogFileNames(java.lang.String)
     */
    public List<String> getLogFileNames(String aJob) {
        JobItem jobItem = JobItem.getInstance(aJob);

        List<String> logFiles = new ArrayList<String>();
        File logsDir = null;
        if (!jobItem.isPatchedJob()) {
            File targetDir = new File(baseDir, jobItem.getJobName());

            logsDir = new File(targetDir, Constants.DIR_LOGS);
            this.appendLogFileNames(logFiles, logsDir);

            logsDir = new File(targetDir, Constants.DIR_REPORTS);
            this.appendLogFileNames(logFiles, logsDir);
        } else {
            File patchedLogFile = new File(directoryManager.getBaseLogDir(jobItem.getTargetInstancdId()), directoryManager.getPatchLogFileName(jobItem.getPrefix(), jobItem.getHarvestNumber()));
            if (patchedLogFile.exists()) {
                logFiles.add(patchedLogFile.getName());
            }
            File patchedRepportFile = new File(directoryManager.getBaseReportDir(jobItem.targetInstancdId), directoryManager.getPatchReportFileName(jobItem.getPrefix(), jobItem.getHarvestNumber()));
            if (patchedRepportFile.exists()) {
                logFiles.add(patchedRepportFile.getName());
            }
        }

        return logFiles;
    }

    /**
     * @see org.webcurator.core.reader.LogProvider#getLogFileAttributes(java.lang.String)
     */
    public List<LogFilePropertiesDTO> getLogFileAttributes(String aJob) {
        JobItem jobItem = JobItem.getInstance(aJob);

        List<LogFilePropertiesDTO> logFiles = new ArrayList<LogFilePropertiesDTO>();
        File logsDir = null;
        if (!jobItem.isPatchedJob()) {
            File targetDir = new File(baseDir, jobItem.getJobName());

            logsDir = new File(targetDir, Constants.DIR_LOGS);
            this.appendLogFiles(logFiles, logsDir);

            logsDir = new File(targetDir, Constants.DIR_REPORTS);
            this.appendLogFiles(logFiles, logsDir);
        } else {
            File patchedLogFile = new File(directoryManager.getBaseLogDir(jobItem.getTargetInstancdId()), directoryManager.getPatchLogFileName(jobItem.getPrefix(), jobItem.getHarvestNumber()));
            if (patchedLogFile.exists()) {
                logFiles.add(new LogFilePropertiesDTO(patchedLogFile, pageImagePrefix, aqaReportPrefix));
            }
            File patchedRepportFile = new File(directoryManager.getBaseReportDir(jobItem.targetInstancdId), directoryManager.getPatchReportFileName(jobItem.getPrefix(), jobItem.getHarvestNumber()));
            if (patchedRepportFile.exists()) {
                logFiles.add(new LogFilePropertiesDTO(patchedRepportFile, pageImagePrefix, aqaReportPrefix));
            }
        }

        return logFiles;
    }

    private void appendLogFiles(List<LogFilePropertiesDTO> logFiles, File logsDir) {
        if (logFiles == null || logsDir == null || !logsDir.exists() || !logsDir.isDirectory()) {
            log.debug("Invalid input parameter");
            return;
        }
        File[] fileList = logsDir.listFiles();
        for (File f : fileList) {
            if (!f.isFile()) {
                continue;
            }
            logFiles.add(new LogFilePropertiesDTO(f, pageImagePrefix, aqaReportPrefix));
        }
    }

    private void appendLogFileNames(List<String> logFiles, File logsDir) {
        if (logFiles == null || logsDir == null || !logsDir.exists() || !logsDir.isDirectory()) {
            log.warn("Invalid input parameter");
            return;
        }
        File[] fileList = logsDir.listFiles();
        for (File f : fileList) {
            if (!f.isFile()) {
                continue;
            }
            logFiles.add(f.getName());
        }
    }

    private void clearTmpDir(File targetDir) {
        File tmpDir = new File(targetDir, "tmpDir");
        File resourcesDir = new File(tmpDir, "_resources");

        if (resourcesDir.exists()) {
            for (File f : resourcesDir.listFiles()) {
                if (!f.delete()) {
                    log.warn("Could not delete " + f.toString());
                }
            }
            if (!resourcesDir.delete()) {
                log.warn("Could not delete " + resourcesDir.toString());
            }
        }
        if (tmpDir.exists()) {
            for (File f : tmpDir.listFiles()) {
                if (!f.delete()) {
                    log.warn("Could not delete " + f.toString());
                }
            }
            if (!tmpDir.delete()) {
                log.warn("Could not delete " + tmpDir.toString());
            }
        }
    }

    /**
     * @see DigitalAssetStore#purge(List<String>).
     */
    public void purge(List<String> targetInstanceNames)
            throws DigitalAssetStoreException {
        if (null == targetInstanceNames || targetInstanceNames.size() == 0) {
            return;
        }

        try {
            for (String tiName : targetInstanceNames) {
                File toPurge = new File(baseDir, tiName);

                log.debug("About to purge dir " + toPurge.toString());

                // Screenshots may have been saved to a tmpDir/_resources folder
                // Make sure these are deleted as well
                clearTmpDir(toPurge);

                try {
                    FileUtils.deleteDirectory(toPurge);
                } catch (IOException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Unable to purge target instance folder: " + toPurge.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            throw new DigitalAssetStoreException("Failed to complete purge : "
                    + e.getMessage(), e);
        }
    }

    /**
     * @see DigitalAssetStore#purgeAbortedTargetInstances(List<String>).
     */
    public void purgeAbortedTargetInstances(List<String> targetInstanceNames)
            throws DigitalAssetStoreException {
        if (null == targetInstanceNames || targetInstanceNames.size() == 0) {
            return;
        }

        try {
            for (String tiName : targetInstanceNames) {
                File toPurge = new File(baseDir, tiName);

                log.debug("About to purge dir " + toPurge.toString());

                // Screenshots may have been saved to a tmpDir/_resources folder
                // Make sure these are deleted as well
                clearTmpDir(toPurge);

                try {
                    FileUtils.deleteDirectory(toPurge);
                } catch (IOException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Unable to purge target instance folder: " + toPurge.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            throw new DigitalAssetStoreException("Failed to complete purge : "
                    + e.getMessage(), e);
        }
    }

    /**
     * Return a list of all the ARC files for the specified target instance and
     * harvest result
     *
     * @param targetInstanceName  the name of the target instance
     * @param harvestResultNumber the harvest result number
     * @return the list of all the ARC files
     * @throws DigitalAssetStoreException throw if there is a problem
     */
    private List<File> getAllARCFiles(String targetInstanceName,
                                      int harvestResultNumber) throws DigitalAssetStoreException {
        ArrayList<File> arcFiles = new ArrayList<File>();
        try {
            File sourceDir = new File(this.baseDir, "/" + targetInstanceName
                    + "/" + harvestResultNumber);
            for (File file : sourceDir.listFiles()) {
                if (file.isFile()) {
                    arcFiles.add(file);
                }
            }
            return arcFiles;
        } catch (RuntimeException ex) {
            if (log.isErrorEnabled()) {
                log.error("Failed to get archive files : " + ex.getMessage(),
                        ex);
            }
            throw new DigitalAssetStoreException(
                    "Failed to get archive files : " + ex.getMessage(), ex);
        }
    }

    /**
     * Return a list of all the log files for the specified target instance.
     *
     * @param targetInstanceName the name of the target instance
     * @return the list of log files
     * @throws DigitalAssetStoreException thrown if there is an error
     */
    private List<File> getLogFiles(String targetInstanceName)
            throws DigitalAssetStoreException {
        List<File> logFiles = new ArrayList<File>();
        File targetDir = new File(baseDir, targetInstanceName);
        File logsDir = new File(targetDir, Constants.DIR_LOGS);
        File[] fileList = null;

        if (logsDir.exists()) {
            fileList = logsDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".log");
                }
            });
            for (File f : fileList) {
                logFiles.add(f);
            }
        }
        return logFiles;
    }

    /**
     * Return a list of report files for the specified target instance
     *
     * @param targetInstanceName the name of the target instance
     * @return the list of report files
     * @throws DigitalAssetStoreException thrown if there is an error
     */
    private List<File> getReportFiles(String targetInstanceName)
            throws DigitalAssetStoreException {
        List<File> reportFiles = new ArrayList<File>();
        File targetDir = new File(baseDir, targetInstanceName);
        File reportsDir = new File(targetDir, Constants.DIR_REPORTS);
        File[] fileList = null;

        if (reportsDir.exists()) {
            fileList = reportsDir.listFiles();
            for (File f : fileList) {
                reportFiles.add(f);
            }
        }
        return reportFiles;
    }

    /**
     * @see DigitalAssetStore#submitToArchive(String, String, Map, int).
     */
    public void submitToArchive(String targetInstanceOid, String SIP,
                                Map xAttributes, int harvestNumber)
            throws DigitalAssetStoreException {
        // Kick off the archiving in a separate thread.
        ArchivingThread thread = new ArchivingThread(targetInstanceOid, SIP,
                xAttributes, harvestNumber);
        new Thread(thread).start();
    }

    private class ArchivingThread implements Runnable {
        private String targetInstanceOid = null;
        private String SIP = null;
        private Map xAttributes = null;
        private int harvestNumber;

        public ArchivingThread(String targetInstanceOid, String sip, Map attributes, int harvestNumber) {
            super();
            this.targetInstanceOid = targetInstanceOid;
            SIP = sip;
            xAttributes = attributes;
            this.harvestNumber = harvestNumber;
        }

        public void run() {
            try {

                String targetID = targetInstanceOid + "";
                ArrayList<ArchiveFile> fileList = new ArrayList<ArchiveFile>();
                // Get log files
                for (File f : getLogFiles(targetID)) {
                    if (f.isDirectory()) {
                        continue;
                    }
                    fileList.add(new ArchiveFile(f, LOG_FILE));
                }
                // Get report files
                for (File f : getReportFiles(targetID)) {
                    if (f.isDirectory()) {
                        continue;
                    }
                    if (f.getName().endsWith("order.xml")) {
                        fileList.add(new ArchiveFile(f, ROOT_FILE));
                    } else {
                        fileList.add(new ArchiveFile(f, REPORT_FILE));
                    }
                }
                // Get arc files
                for (File f : getAllARCFiles(targetID, harvestNumber)) {
                    if (f.isDirectory()) {
                        continue;
                    }
                    fileList.add(new ArchiveFile(f, ARC_FILE));
                }

                String archiveIID = arcDasArchive.submitToArchive(targetInstanceOid, SIP, xAttributes, fileList);

                wctCoordinatorClient.completeArchiving(Long.parseLong(targetInstanceOid), archiveIID);
            } catch (Throwable t) {
                log.error("Could not archive " + targetInstanceOid, t);

                try {
                    wctCoordinatorClient.failedArchiving(Long.parseLong(targetInstanceOid), t.getMessage());
                } catch (Exception ex) {
                    log.error("Got error trying to send \"failedArchiving\" to server", ex);
                }
            }
        }
    }

    public CustomDepositFormResultDTO getCustomDepositFormDetails(
            CustomDepositFormCriteriaDTO criteria)
            throws DigitalAssetStoreException {
        return arcDasArchive.getCustomDepositFormDetails(criteria);
    }

    /**
     * @param baseDir the base directory for the digital asset stores harvest files.
     */
    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public void initiateIndexing(HarvestResultDTO harvestResult)
            throws DigitalAssetStoreException {
        VisualizationAbstractProcessor processor = new IndexProcessorWarc(pool, harvestResult.getTargetInstanceOid(), harvestResult.getHarvestNumber());
        try {
            visualizationProcessorManager.startTask(processor);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new DigitalAssetStoreException(e);
        }
    }

    public void initiateRemoveIndexes(HarvestResultDTO harvestResult)
            throws DigitalAssetStoreException {
        // Determine the source directory.
        File sourceDir = new File(this.baseDir, "/"
                + harvestResult.getTargetInstanceOid() + "/"
                + harvestResult.getHarvestNumber());

        // Kick of the indexer.
        indexer.removeIndex(harvestResult, sourceDir);
    }

    public Boolean checkIndexing(Long harvestResultOid)
            throws DigitalAssetStoreException {
        return indexer.checkIndexing(harvestResultOid);
    }

    /**
     * @param indexer the indexer to set
     */
    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }

    public void setWctCoordinatorClient(WctCoordinatorClient wctCoordinatorClient) {
        this.wctCoordinatorClient = wctCoordinatorClient;
    }

    /**
     * @param fileMover the fileMover to set
     */
    public void setDasFileMover(DasFileMover fileMover) {
        this.dasFileMover = fileMover;
    }

    public void setScreenshotGenerator(ScreenshotGenerator screenshotGenerator) {
        this.screenshotGenerator = screenshotGenerator;
    }

    // Moves the ArchiveRecord stream past the HTTP status line;
    // checks if it starts with HTTP and ends with CRLF
    private void skipStatusLine(ArchiveRecord record) throws IOException {
        if (record.available() > 0) {
            int i;
            char[] proto = new char[4];
            for (int c = 0; c < 4; c++) {
                if ((i = record.read()) == -1) {
                    throw new IOException("Malformed HTTP Status-Line");
                }
                proto[c] = (char) i;
            }
            if (!"HTTP".equals(new String(proto))) {
                throw new IOException("Malformed HTTP Status-Line");
            }
            char c0 = '0';
            char c1;
            while ((i = record.read()) != -1) {
                c1 = (char) i;
//                if (c0 == '\r' && c1 == '\n') {
                if (c1 == '\n') {
                    break;
                }
                c0 = c1;
            }
        }
    }

    private void skipHeaders(ArchiveRecord record) throws IOException {
        HttpParser.parseHeaders(record, WARCConstants.DEFAULT_ENCODING);
    }

    public void setPageImagePrefix(String pageImagePrefix) {
        this.pageImagePrefix = pageImagePrefix;
    }

    public String getPageImagePrefix() {
        return pageImagePrefix;
    }

    public void setAqaReportPrefix(String aqaReportPrefix) {
        this.aqaReportPrefix = aqaReportPrefix;
    }

    public String getAqaReportPrefix() {
        return aqaReportPrefix;
    }

    /**
     * @param fileArchive the fileArchive to set
     */
    public void setFileArchive(FileArchive fileArchive) {
        this.fileArchive = fileArchive;
    }

    public NetworkMapClient getNetworkMapClient() {
        return networkMapClient;
    }

    public void setNetworkMapClient(NetworkMapClient networkMapClient) {
        this.networkMapClient = networkMapClient;
    }

    public VisualizationProcessorManager getVisualizationProcessorManager() {
        return visualizationProcessorManager;
    }

    public void setVisualizationProcessorManager(VisualizationProcessorManager visualizationProcessorManager) {
        this.visualizationProcessorManager = visualizationProcessorManager;
    }

    public BDBNetworkMapPool getPool() {
        return pool;
    }

    public void setPool(BDBNetworkMapPool pool) {
        this.pool = pool;
    }

    @Override
    public ModifyResult initialPruneAndImport(ModifyApplyCommand cmd) {
        ModifyResult result = new ModifyResult();
        VisualizationAbstractProcessor processor = new ModifyProcessorWarc(cmd);
        try {
            visualizationProcessorManager.startTask(processor);
        } catch (IOException e) {
            result.setRespCode(VisualizationConstants.RESP_CODE_ERROR_SYSTEM_ERROR);
            result.setRespMsg(e.getMessage());
            log.error(e.getLocalizedMessage());
            return result;
        }

        result.setRespCode(VisualizationConstants.RESP_CODE_SUCCESS);
        result.setRespMsg("Modification task is accepted");
        return result;
    }

    @Override
    public void operateHarvestResultModification(String stage, String command, long targetInstanceId, int harvestNumber) throws DigitalAssetStoreException {
        log.info("stage: {}, command: {}, targetInstanceId: {}, harvestResultNumber:{} ", stage, command, targetInstanceId, harvestNumber);
        if (command.equalsIgnoreCase("pause")) {
            visualizationProcessorManager.pauseTask(stage, targetInstanceId, harvestNumber);
        } else if (command.equalsIgnoreCase("resume")) {
            visualizationProcessorManager.resumeTask(stage, targetInstanceId, harvestNumber);
        } else if (command.equalsIgnoreCase("terminate")) {
            visualizationProcessorManager.terminateTask(stage, targetInstanceId, harvestNumber);
        } else if (command.equalsIgnoreCase("delete")) {
            visualizationProcessorManager.deleteTask(stage, targetInstanceId, harvestNumber);
        }
    }

    public File getDownloadFileURL(String fileName, File downloadedFile) throws IOException {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(WctCoordinatorPaths.MODIFICATION_DOWNLOAD_IMPORTED_FILE))
                .queryParam("fileName", fileName);
        URI uri = uriComponentsBuilder.build().toUri();

        URL url = uri.toURL();
        URLConnection conn = url.openConnection();

        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadedFile));
        InputStream inputStream = conn.getInputStream();

        WctUtils.copy(inputStream, outputStream);

        return downloadedFile;
    }

    private static class JobItem {
        private boolean isPatchedJob;
        private String jobName;
        private String prefix;
        private long targetInstancdId;
        private int harvestNumber;

        public static JobItem getInstance(String aJob) {
            JobItem jobItem = new JobItem();
            if (aJob.indexOf('@') < 0) {
                jobItem.isPatchedJob = false;
                jobItem.jobName = aJob;
                jobItem.prefix = "";

                if (aJob.startsWith("mod")) {
                    String[] jobItems = aJob.split("_");
                    jobItem.targetInstancdId = Long.parseLong(jobItems[1]);
                    jobItem.harvestNumber = Integer.parseInt(jobItems[2]);
                } else {
                    jobItem.targetInstancdId = Long.parseLong(aJob);
                    jobItem.harvestNumber = 1;
                }
            } else {
                jobItem.isPatchedJob = true;

                String[] prefixItems = aJob.split("@");
                jobItem.prefix = prefixItems[0];
                jobItem.jobName = prefixItems[1];
                String[] jobItems = prefixItems[1].split("_");
                jobItem.targetInstancdId = Long.parseLong(jobItems[1]);
                jobItem.harvestNumber = Integer.parseInt(jobItems[2]);
            }

            return jobItem;
        }

        public boolean isPatchedJob() {
            return isPatchedJob;
        }

        public void setPatchedJob(boolean patchedJob) {
            isPatchedJob = patchedJob;
        }

        public String getJobName() {
            return jobName;
        }

        public void setJobName(String jobName) {
            this.jobName = jobName;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public long getTargetInstancdId() {
            return targetInstancdId;
        }

        public void setTargetInstancdId(long targetInstancdId) {
            this.targetInstancdId = targetInstancdId;
        }

        public int getHarvestNumber() {
            return harvestNumber;
        }

        public void setHarvestNumber(int harvestNumber) {
            this.harvestNumber = harvestNumber;
        }
    }

    private void waitForScreenshot(File file, String filename) {
        try {
            for (int i = 0; i < 5; i++) {
                if (file.exists()) return;
                log.info(filename + " has not been created yet.  Waiting...");
                Thread.sleep(10000);
            }
            log.info("Timed out waiting for file creation.");
        } catch (Exception e) {
        }
    }

    // The wayback banner may be problematic when getting full page screenshots, check against the live image dimensions
    // Allow some space for the wayback banner
    private void checkFullpageScreenshotSize(String outputPath, String filename, File liveImageFile, String url) {
        try {
            BufferedImage liveImage = ImageIO.read(liveImageFile);
            int liveImageWidth = liveImage.getWidth();
            int liveImageHeight = liveImage.getHeight();
            liveImage.flush();

            // Only proceed if harvested fullpage image is smaller than live fullpage image
            BufferedImage harvestedImage = ImageIO.read(new File(outputPath + File.separator + filename));
            if (harvestedImage.getWidth() >= liveImageWidth && harvestedImage.getHeight() >= liveImageHeight) {
                harvestedImage.flush();
                return;
            }

            String windowsizeCommand = screenshotCommandWindowsize
                    .replace("%width%", String.valueOf(liveImageWidth))
                    .replace("%height%", String.valueOf(liveImageHeight + 150))
                    .replace("%url%", url)
                    .replace("%image.png%", outputPath + File.separator + filename);


            log.info("Harvested full page screenshot is smaller than live full page screenshot.  " +
                    "Getting a new screenshot using live image dimensions, command " + windowsizeCommand);

            // Delete the old harvested fullpage image and replace it with one with new dimensions
            File toDelete = new File(outputPath + File.separator + filename);
            if (toDelete.delete()) {
                runCommand(windowsizeCommand);
                waitForScreenshot(toDelete, filename);
                log.info("Fullpage screenshot of harvest replaced.");
            } else {

                log.info("Unable to replace harvest fullpage screenshot.");
            }
            harvestedImage.flush();
        } catch (Exception e) {
            log.error("Failed to resize fullpage harvest screenshot: " + e.getMessage(), e);
        }
    }

    private void runCommand(String command) {
        try {
            String harvestAgentH3SourceDir = "webcurator-harvest-agent-h3";
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            if (command.contains("SeleniumScreenshotCapture")) {
                String processDir = System.getProperty("user.dir");
                if (processDir.contains(harvestAgentH3SourceDir)) {
                    processDir = processDir.substring(0, processDir.indexOf(harvestAgentH3SourceDir));
                }
                processDir = processDir + File.separator + harvestAgentH3SourceDir + File.separator
                        + "build" + File.separator + "classes" + File.separator + "java" + File.separator + "main";
                processBuilder.directory(new File(processDir).getAbsoluteFile());
            }
            Process process = processBuilder.start();
        } catch (Exception e) {
            log.error("Unable to process command " + command, e);
        }
    }

    private void generateThumbnailOrScreenSizeScreenshot(String inputFilename, String outputPathString,
                                                         String inputSize, String outputSize, int width, int height) {
        log.info("Generating " + outputSize + " screenshot...");
        try {
            BufferedImage sourceImage = ImageIO.read(new File(outputPathString + File.separator + inputFilename));
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Image scaledImage = sourceImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            bufferedImage.createGraphics().drawImage(scaledImage, 0, 0, null);
            BufferedImage thumbnailImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            thumbnailImage = bufferedImage.getSubimage(0, 0, width, height);
            ImageIO.write(thumbnailImage, "png", new File(outputPathString + File.separator
                    + inputFilename.replace(inputSize, outputSize)));
            sourceImage.flush();
            bufferedImage.flush();
            thumbnailImage.flush();
        } catch (Exception e) {
            log.error("Unable to generate " + outputSize + " thumbnail.");
        }
    }


    private void renameLiveFile(String liveDirectory, String outputDirectory, String seed, String harvestNumber, String filename) {
        File fullpageLiveFilePath = new File(liveDirectory + filename.replace("harvested", "live"));
        if (!fullpageLiveFilePath.exists()) return;
        if (harvestNumber == null) return;
        if (seed == null) return;
        String newFilename = outputDirectory + filename.replace("seedID", seed).replace("harvestNum", harvestNumber);
        newFilename = newFilename.replace("harvested", "live");
        if (!fullpageLiveFilePath.renameTo(new File(newFilename))) {
            log.error("Unable to rename live file to include harvest number and seed.  File: " + filename);
        }
    }

    // Delete everything in _resources file then resources file then harvest file
    private void cleanUpTmpDir(File harvestTmpDir) {
        File tmpDir = harvestTmpDir.getParentFile();
        if (harvestTmpDir.exists()) {

            // Delete all files in all directories
            for (File file : harvestTmpDir.listFiles()) {
                if (!file.delete()) {
                    log.info("Unable to delete file: " + file.getAbsolutePath());
                }
            }
            if (harvestTmpDir.list().length != 0) {
                log.info("Not all files have been removed from " + harvestTmpDir.toString());
                return;
            }
            if (!harvestTmpDir.delete()) {
                log.info("Unable to delete temporary screenshot directory.");
                return;
            }
            if (tmpDir.list().length > 0) {
                log.info("There are still files within " + tmpDir.toString());
                return;
            }
            if (!tmpDir.delete()) {
                log.info("Unable to delete " + tmpDir.getAbsolutePath());
            }
        }
    }

    private boolean checkDirOnlyOneFile(File dir, String expectedDir) {
        if (dir.list().length == 0) return true;

        File[] dirFiles = dir.listFiles();

        // Return false if the directory contains more than 1 file
        if (dirFiles.length > 1) {
            log.info("There are more files in " + dir.toString() + " than the " + expectedDir + " directory.");
            return false;
        }

        // Return false if the child directory isn't the expected directory
        if (!dirFiles[0].getName().equals(expectedDir)) {
            log.info("There is an unexpected file in " + dir.toString());
            return false;
        }

        return true;
    }

    private void cleanUpDirOnAbort(File harvestBaseDir) {
        if (!harvestBaseDir.exists()) return;

        // Harvest base dir should only contain tmpDir, do not delete otherwise
        if (!checkDirOnlyOneFile(harvestBaseDir, "tmpDir")) return;

        // tmpDir should only contain _resources, do not delete otherwise
        File tmpDir = harvestBaseDir.listFiles()[0];
        if (!checkDirOnlyOneFile(tmpDir, "_resources")) return;

        File resourcesDir = tmpDir.listFiles()[0];
        // Delete all files in _resources
        for (File f : resourcesDir.listFiles()) {
            if (!f.delete()) {
                log.info("Unable to delete " + f.toString());
                return;
            }
        }

        // Delete _resources directory then tmpDir then harvest directory
        if (!resourcesDir.delete()) {
            log.info("Could not delete " + resourcesDir.toString());
            return;
        }
        if (!tmpDir.delete()) {
            log.info("Could not delete " + tmpDir.toString());
            return;
        }
        if (!harvestBaseDir.delete()) {
            log.info("Could not delete " + harvestBaseDir.toString());
        }
    }

    /**
     * @param identifiers
     * @return the boolean to say if the screenshots have been generated
     * @throws DigitalAssetStoreException
     */
    public Boolean createScreenshots(Map identifiers) throws DigitalAssetStoreException {
        // Can continue with the harvest without taking a screenshot
        if (!enableScreenshots) return true;

        if (identifiers == null || identifiers.size() == 0) {
            return false;
        }

        Boolean screenshotsSucceeded = screenshotGenerator.createScreenshots(identifiers, baseDir, harvestWaybackViewerBaseUrl);

        if (!abortHarvestOnScreenshotFailure) return true;

        // Delete temporary screenshot directory if the screenshot didn't succeed and the harvest has been aborted
        if (!screenshotsSucceeded) {
            cleanUpDirOnAbort(new File(baseDir + File.separator + identifiers.get("tiOid").toString()));
        }

        return screenshotsSucceeded;
    }

    /**
     * @param aScreenshotFullpageCommand The screenshotFullpageCommand to set.
     */
    public void setScreenshotCommandFullpage(String aScreenshotCommandFullpage) {
        this.screenshotCommandFullpage = aScreenshotCommandFullpage;
    }

    /**
     * @param aScreenshotScreenCommand The screenshotScreenCommand to set.
     */
    public void setScreenshotCommandScreen(String aScreenshotCommandScreen) {
        this.screenshotCommandScreen = aScreenshotCommandScreen;
    }

    /**
     * @param aScreenshotWindowsizeCommand The screenshotWindowsizeCommand to set.
     */
    public void setScreenshotCommandWindowsize(String aScreenshotCommandWindowsize) {
        this.screenshotCommandWindowsize = aScreenshotCommandWindowsize;
    }

    /**
     * @param aHarvestWaybackViewerBaseUrl
     */
    public void setHarvestWaybackViewerBaseUrl(String aHarvestWaybackViewerBaseUrl) {
        this.harvestWaybackViewerBaseUrl = aHarvestWaybackViewerBaseUrl;
    }

    /**
     * @param aAbortHarvestOnScreenshotFailure
     */
    public void setAbortHarvestOnScreenshotFailure(boolean aAbortHarvestOnScreenshotFailure) {
        this.abortHarvestOnScreenshotFailure = aAbortHarvestOnScreenshotFailure;
    }

    /**
     * @param aEnableScreenshots
     */
    public void setEnableScreenshots(boolean aEnableScreenshots) {
        this.enableScreenshots = aEnableScreenshots;
    }
}
