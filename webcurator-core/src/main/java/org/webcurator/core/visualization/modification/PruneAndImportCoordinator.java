package org.webcurator.core.visualization.modification;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.store.WCTIndexer;
import org.webcurator.core.util.ApplicationContextFactory;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;
import org.webcurator.core.visualization.VisualizationConstants;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class PruneAndImportCoordinator {
    protected static final Logger log = LoggerFactory.getLogger(PruneAndImportCoordinator.class);
    protected static final int BYTE_BUFF_SIZE = 1024;
    /**
     * Arc files meta data date format.
     */
    protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    protected static final SimpleDateFormat writerDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    protected String fileDir; //Upload files
    protected String baseDir; //Harvest WARC files dir


    public String getArchiveType() {
        return archiveType();
    }

    abstract protected String archiveType();

    abstract protected void copyArchiveRecords(File fileFrom, List<String> urisToDelete, Map<String, PruneAndImportCommandRowMetadata> hrsToImport, int newHarvestResultNumber) throws IOException, URISyntaxException;

    abstract protected void importFromFile(long job, int harvestResultNumber, Map<String, PruneAndImportCommandRowMetadata> hrsToImport) throws IOException;

    abstract protected void importFromRecorder(File fileFrom, List<String> urisToDelete, int newHarvestResultNumber) throws IOException, URISyntaxException;

    protected File modificationDownloadFile(long job, int harvestResultNumber, PruneAndImportCommandRowMetadata metadata) {
        AbstractRestClient digitalAssetStoreClient = ApplicationContextFactory.getApplicationContext().getBean(WCTIndexer.class);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(digitalAssetStoreClient.getUrl(VisualizationConstants.PATH_DOWNLOAD_FILE))
                .queryParam("job", job)
                .queryParam("harvestResultNumber", harvestResultNumber);
        URI uri = uriComponentsBuilder.build().toUri();

        HttpEntity<String> reqEntity = digitalAssetStoreClient.createHttpRequestEntity(metadata);
        reqEntity.getBody();

        String tempFileName = UUID.randomUUID().toString();
        File tempFile = new File(fileDir, tempFileName);
        try {
            URL url = uri.toURL();
            URLConnection conn = url.openConnection();
            OutputStream connOutputStream = conn.getOutputStream();
            connOutputStream.write(Objects.requireNonNull(reqEntity.getBody()).getBytes());
            connOutputStream.flush();
            connOutputStream.close();

            OutputStream fos = Files.newOutputStream(tempFile.toPath());

            StringBuilder buf = new StringBuilder();
            buf.append("HTTP/1.1 200 OK\n");
            buf.append("Content-Type: ");
            buf.append(metadata.getContentType()).append("\n");
            buf.append("Content-Length: ");
            buf.append(metadata.getLength()).append("\n");
            LocalDateTime ldt = LocalDateTime.ofEpochSecond(metadata.getLastModified() / 1000, 0, ZoneOffset.UTC);
            OffsetDateTime odt = ldt.atOffset(ZoneOffset.UTC);
            buf.append("Date: ");
            buf.append(odt.format(DateTimeFormatter.RFC_1123_DATE_TIME)).append("\n");
            buf.append("Connection: close\n");

            fos.write(buf.toString().getBytes());
            fos.write("\n".getBytes());

            IOUtils.copy(conn.getInputStream(), fos);

            fos.close();
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }

        return tempFile;
    }
}
