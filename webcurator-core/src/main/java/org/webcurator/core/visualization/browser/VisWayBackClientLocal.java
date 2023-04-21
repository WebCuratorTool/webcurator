package org.webcurator.core.visualization.browser;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;
import org.archive.format.warc.WARCConstants;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.VisualizationDirectoryManager;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNodeUrlEntity;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapUrlCommand;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VisWayBackClientLocal implements VisWayBackClient {
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(VisWayBackClientLocal.class);

    private VisualizationDirectoryManager directoryManager;

    /**
     * the base directory for the digital asset stores harvest files.
     */
    private String baseDir = null;
    /**
     * Constant for the size of a buffer.
     */
    private final int BYTE_BUFF_SIZE = 1024;

    private NetworkMapClient networkMapClient;

    @Override
    public Path getResource(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        NetworkMapNodeUrlEntity resourceNode = this.queryUrlNode(targetInstanceId, harvestResultNumber, resourceUrl);

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
                source = new File(directoryManager.getArchiveRepository() + "/"
                        + targetInstanceId + "/"
                        + directoryManager.getArchiveArcDirectory() + "/"
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

    @Override
    public byte[] getSmallResource(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        NetworkMapNodeUrlEntity resourceNode = this.queryUrlNode(targetInstanceId, harvestResultNumber, resourceUrl);

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
                source = new File(directoryManager.getArchiveRepository() + "/"
                        + targetInstanceId + "/"
                        + directoryManager.getArchiveArcDirectory() + "/"
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
                if (record != null)
                    reader.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close reader failed " + ex.getMessage(), ex);
                }
            }
        }
        return new byte[0];
    }

    @Override
    public List<Header> getHeaders(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        log.debug("Start of getHeaders()");
        log.debug("Casting the DTO to HarvestResult");


        NetworkMapNodeUrlEntity resourceNode = this.queryUrlNode(targetInstanceId, harvestResultNumber, resourceUrl);

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

                source = new File(directoryManager.getArchiveRepository() + "/"
                        + targetInstanceId + "/"
                        + directoryManager.getArchiveArcDirectory() + "/"
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
                if (record != null)
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

    private NetworkMapNodeUrlEntity queryUrlNode(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        NetworkMapUrlCommand cmd = new NetworkMapUrlCommand();
        cmd.setUrlName(resourceUrl);
        NetworkMapResult result = networkMapClient.getUrlByName(targetInstanceId, harvestResultNumber, cmd);

        String err = String.format("Could not find NetworkMapNode with targetInstanceId=%d, harvestResultNumber=%d, resourceUrl=%s", targetInstanceId, harvestResultNumber, resourceUrl);
        if (result == null || result.getRspCode() != 0) {
            log.warn(err);
            throw new DigitalAssetStoreException(err);
        }

        String json = (String) result.getPayload();
        NetworkMapNodeUrlEntity node = networkMapClient.getNodeEntity(json);
        if (node == null) {
            log.warn(err);
            throw new DigitalAssetStoreException(err);
        }

        return node;
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
                if (c0 == '\r' && c1 == '\n') {
                    break;
                }
                c0 = c1;
            }
        }
    }

    private void skipHeaders(ArchiveRecord record) throws IOException {
        HttpParser.parseHeaders(record, WARCConstants.DEFAULT_ENCODING);
    }

    public VisualizationDirectoryManager getDirectoryManager() {
        return directoryManager;
    }

    public void setDirectoryManager(VisualizationDirectoryManager directoryManager) {
        this.directoryManager = directoryManager;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public NetworkMapClient getNetworkMapClient() {
        return networkMapClient;
    }

    public void setNetworkMapClient(NetworkMapClient networkMapClient) {
        this.networkMapClient = networkMapClient;
    }
}
