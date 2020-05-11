package org.webcurator.core.networkmap.service;

import org.archive.format.warc.WARCConstants;
import org.archive.io.*;
import org.archive.io.arc.*;
import org.archive.io.warc.*;
import org.archive.uid.UUIDGenerator;
import org.archive.util.anvl.ANVLRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.store.Indexer;
import org.webcurator.domain.model.core.ArcHarvestFileDTO;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PruneAndImportLocalClient implements PruneAndImportService {
    private String fileDir; //Upload files
    private String baseDir; //Harvest WARC files
    private final int BYTE_BUFF_SIZE = 1024;

    /**
     * Arc files meta data date format.
     */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final SimpleDateFormat writerDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    /**
     * The Indexer
     */
    private Indexer indexer = null;


    @Override
    public PruneAndImportCommandRowMetadata uploadFile(long job, int harvestResultNumber, String fileName, boolean replaceFlag, byte[] doc) {
        PruneAndImportCommandRowMetadata cmd = new PruneAndImportCommandRowMetadata();
        cmd.setName(fileName);
        File uploadedFilePath = new File(fileDir, fileName);
        if (uploadedFilePath.exists()) {
            if (replaceFlag) {
                uploadedFilePath.deleteOnExit();
            } else {
                cmd.setRespCode(FILE_EXIST_YES);
                cmd.setRespMsg(String.format("File %s has been exist, return without replacement.", fileName));
                return cmd;
            }
        }

        try {
            Files.write(uploadedFilePath.toPath(), doc);
        } catch (IOException e) {
            log.error(e.getMessage());
            cmd.setRespCode(RESP_CODE_ERROR_FILE_IO);
            cmd.setRespMsg("Failed to write upload file to " + uploadedFilePath.getAbsolutePath());
            return cmd;
        }

        cmd.setRespCode(FILE_EXIST_YES);
        cmd.setRespMsg("OK");
        return cmd;
    }

    @Override
    public PruneAndImportCommandRow downloadFile(long job, int harvestResultNumber, String fileName) {
        PruneAndImportCommandRow result = new PruneAndImportCommandRow();
        File uploadedFilePath = new File(fileDir, fileName);
        try {
            result.setContent(Files.readAllBytes(uploadedFilePath.toPath()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return result;
    }

    @Override
    public PruneAndImportCommandResult checkFiles(long job, int harvestResultNumber, List<PruneAndImportCommandRowMetadata> items) {
        PruneAndImportCommandResult result=new PruneAndImportCommandResult();
        result.setRespCode(FILE_EXIST_YES);
        result.setRespMsg("OK");
        items.forEach(e -> {
            if (e.getOption().equalsIgnoreCase("file")) {
                File uploadedFilePath = new File(fileDir, e.getName());
                if (uploadedFilePath.exists()) {
                    e.setRespCode(FILE_EXIST_YES); //Exist
                    e.setRespMsg("OK");
                } else {
                    e.setRespCode(FILE_EXIST_NO); //Not exist
                    e.setRespMsg("File is not uploaded");
                    result.setRespCode(FILE_EXIST_NO);
                    result.setRespMsg("Not all files are uploaded");
                }
            } else {
                e.setRespCode(FILE_EXIST_YES); //For source urls, consider to exist
                e.setRespMsg("OK");
            }
        });

        result.setMetadataDataset(items);

        return result;
    }

    @Override
    public PruneAndImportCommandResult pruneAndImport(long job, long harvestResultId, int harvestResultNumber, int newHarvestResultNumber, PruneAndImportCommandApply cmd) {
        return null;
    }

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }
}

class PruneAndImportProcessor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(PruneAndImportProcessor.class);

    private String fileDir; //Upload files
    private String baseDir; //Harvest WARC files
    private final int BYTE_BUFF_SIZE = 1024;

    /**
     * Arc files meta data date format.
     */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final SimpleDateFormat writerDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    /**
     * The Indexer
     */
    private Indexer indexer = null;

    private long job;
    private int harvestResultNumber;
    private int newHarvestResultNumber;
    private List<PruneAndImportCommandRowMetadata> dataset;

    public PruneAndImportProcessor(String fileDir, String baseDir, long job, int harvestResultNumber, int newHarvestResultNumber, List<PruneAndImportCommandRowMetadata> dataset) {
        this.fileDir = fileDir;
        this.baseDir = baseDir;
        this.job = job;
        this.harvestResultNumber = harvestResultNumber;
        this.newHarvestResultNumber = newHarvestResultNumber;
        this.dataset = dataset;
    }


    @Override
    public void run() {
        try {
            pruneAndImport();
            log.info("copyAndPrune - Now returning the ArcHarvestResult: " + job);
        } catch (IOException | URISyntaxException | ParseException e) {
            e.printStackTrace();
        }

    }

    public void pruneAndImport() throws IOException, URISyntaxException, ParseException {
        final List<String> urisToDelete = new LinkedList<>();
        final Map<String, PruneAndImportCommandRowMetadata> hrsToImport = new HashMap<>();
        dataset.forEach(e -> {
            if (e.getOption().equalsIgnoreCase("prune")) {
                urisToDelete.add(e.getUrl());
            } else {
                hrsToImport.put(e.getUrl(), e);
            }
        });

        //TODO: launch a thread to download, prune and import

        // Calculate the source and destination directories.
        File sourceDir = new File(baseDir, job + File.separator + harvestResultNumber);
        File destDir = new File(baseDir, job + File.separator + newHarvestResultNumber);

        // Ensure the destination directory exists.
        if (!destDir.mkdirs()) {
            log.error("Make dir failed, path: {}", destDir.getAbsolutePath());
            return;
        }

        // Get all the files from the source dir.
        File[] arcFiles = sourceDir.listFiles();

        List<File> dirs = new LinkedList<File>();
        dirs.add(destDir);

        boolean compressed = false;
        AtomicInteger aint = new AtomicInteger();

        String impArcType = null;
        String strippedImpArcFilename = null;
        List<String> impArcHeader = new ArrayList<String>();

        // Copy them into the destination directory.
        for (int i = 0; i < Objects.requireNonNull(arcFiles).length; i++) {

            // If this is a CDX file, ignore it, another will be created for
            // the new file
            if (arcFiles[i].getName().toUpperCase().endsWith("CDX")) {
                continue;
            }

            // Get the reader for this ARC File
            ArchiveReader reader = ArchiveReaderFactory.get(arcFiles[i]);

            // Use the original filename
            strippedImpArcFilename = reader.getStrippedFileName();

            compressed = reader.isCompressed();

            Iterator<ArchiveRecord> archiveRecordsIt = reader.iterator();

            if (reader instanceof ARCReader) {
                if (impArcType == null) {
                    impArcType = "ARC";
                }

                // Read the Meta Data
                ARCRecord headerRec = (ARCRecord) archiveRecordsIt.next();
                byte[] buff = new byte[1024];
                StringBuffer metaData = new StringBuffer();
                int bytesRead = 0;
                while ((bytesRead = headerRec.read(buff)) != -1) {
                    metaData.append(new String(buff, 0, bytesRead));
                }
                List<String> l = new ArrayList<String>();
                l.add(metaData.toString());

                if (impArcHeader.isEmpty()) {
                    impArcHeader.add(metaData.toString());
                }

                // Create an ARC Writer
                WriterPoolSettings settings = new WriterPoolSettingsData(strippedImpArcFilename + "-" + newHarvestResultNumber, "${prefix}",
                        ARCReader.DEFAULT_MAX_ARC_FILE_SIZE, compressed, dirs, l);
                ARCWriter writer = new ARCWriter(aint, settings);

                // Iterate through all the records, skipping deleted or
                // imported URLs.
                while (archiveRecordsIt.hasNext()) {
                    ARCRecord record = (ARCRecord) archiveRecordsIt.next();
                    ARCRecordMetaData meta = record.getMetaData();
                    Date dt;
                    try {
                        dt = sdf.parse(meta.getDate());
                    } catch (ParseException ex) {
                        dt = new Date();
                        if (log.isWarnEnabled()) {
                            log.warn("Couldn't parse date from ARCRecord: "
                                    + record.getMetaData().getUrl(), ex);
                            log.warn("Setting to the current date.");
                        }
                    }

                    if (!urisToDelete.contains(meta.getUrl()) && !hrsToImport.containsKey(meta.getUrl())) {
                        // this record is not in the delete list so we should copy it
                        // forward to the arc file, but is there a match in the import list?
                        // If the record's Url is in the imports list, then the user
                        // is opting to replace the content for the specified Url so
                        // we won't copy this record forward into the target arc file,
                        // rather we'll add all imported Urls and their associated
                        // content into an additional newly created arc file at the end.
                        writer.write(meta.getUrl(), meta.getMimetype(),
                                meta.getIp(), dt.getTime(),
                                (int) meta.getLength(), record);
                    }
                }

                writer.close();
            } else if (reader instanceof WARCReader) {
                if (impArcType == null) {
                    impArcType = "WARC";
                }

                /*
                 * Post 1.6.1 code.
                 *
                 * Problem:
                 * The correct number of bytes/characters are being read from the header record, and saved in the
                 * buffer array. But the input stream appears (for some unknown reason) to read or mark one character further
                 * than the length that was read into the array.
                 *
                 * For example, with content-length: 398, the stream should be stopping at the <|> below. So the next character read
                 * would be a carriage return "\r". This is what the WarcReader (line 65 - gotoEOR()) is expecting in order to move
                 * the marker to the start of the next record.
                 *
                 * http-header-from: youremail@yourdomain.com\r\n
                 * \r\n<|>
                 * \r\n
                 * \r\n
                 * WARC/0.18\r\n
                 *
                 * Instead the stream is reading up until the marker in the following example, and throwing a runtime error.
                 *
                 * http-header-from: youremail@yourdomain.com\r\n
                 * \r\n
                 * \r<|>\n
                 * \r\n
                 * WARC/0.18\r\n
                 *
                 *
                 * Workaround/Fix:
                 * Create a duplicate ArchiveReader (headerRecordIt) for just the warc header metadata, that is then closed after
                 * the metadata is read. The archiveRecordsIt ArchiveReader is still used to read the rest of the records. However
                 * the first record (which we read with the other ArchiveReader) still has an issue with the iterator hasNext()
                 * call. So it is skipped before entering the loop that copies each record.
                 *
                 *
                 */

                // Get a another reader for the warc header metadata
                ArchiveReader headerReader = ArchiveReaderFactory.get(arcFiles[i]);
                Iterator<ArchiveRecord> headerRecordIt = headerReader.iterator();

                // Read the Meta Data
                WARCRecord headerRec = (WARCRecord) headerRecordIt.next();
                byte[] buff = new byte[1024];
                StringBuilder metaData = new StringBuilder();
                int bytesRead = 0;


                while ((bytesRead = headerRec.read(buff)) != -1) {
                    metaData.append(new String(buff, 0, bytesRead));
                }


                List<String> l = new ArrayList<String>();
                l.add(metaData.toString());

                if (impArcHeader.isEmpty()) {
                    impArcHeader.add(metaData.toString());
                }

                headerRec.close();
                headerReader.close();


                // Bypass warc header metadata as it has been read above from a different ArchiveReader
                archiveRecordsIt.next();

                // Create a WARC Writer
                WARCWriterPoolSettings settings = new WARCWriterPoolSettingsData(strippedImpArcFilename + "-" + newHarvestResultNumber, "${prefix}",
                        ARCReader.DEFAULT_MAX_ARC_FILE_SIZE, compressed, dirs, l, new UUIDGenerator());
                WARCWriter writer = new WARCWriter(aint, settings);

                // Iterate through all the records, skipping deleted or
                // imported URLs.
                while (archiveRecordsIt.hasNext()) {
                    WARCRecord record = (WARCRecord) archiveRecordsIt
                            .next();
                    ArchiveRecordHeader header = record.getHeader();
                    String WARCType = (String) header
                            .getHeaderValue(WARCConstants.HEADER_KEY_TYPE);
                    String strRecordId = (String) header
                            .getHeaderValue(WARCConstants.HEADER_KEY_ID);
                    URI recordId = new URI(strRecordId.substring(
                            strRecordId.indexOf("<") + 1,
                            strRecordId.lastIndexOf(">") - 1));
                    long contentLength = header.getLength()
                            - header.getContentBegin();


                    if (!WARCType.equals(WARCConstants.WARCRecordType.warcinfo)
                            && (urisToDelete.contains(header.getUrl()) || hrsToImport.containsKey(header.getUrl()))) {
                        continue;
                    }

                    ANVLRecord namedFields = new ANVLRecord();
                    header.getHeaderFields().forEach((key, value) -> {
                        if (key.equals(WARCConstants.ABSOLUTE_OFFSET_KEY)) {
                            value = Long.toString(writer.getPosition());
                        }
                        // we exclude all but three fields to avoid
                        // duplication / erroneous data
                        if (key.equals("WARC-IP-Address")
                                || key.equals("WARC-Payload-Digest")
                                || key.equals("WARC-Concurrent-To")) {
                            namedFields.addLabelValue(key, value.toString());
                        }
                    });

                    WARCRecordInfo warcRecordInfo = new WARCRecordInfo();
                    switch (WARCConstants.WARCRecordType.valueOf(WARCType)) {
                        case warcinfo:
                            warcRecordInfo.setType(WARCConstants.WARCRecordType.warcinfo);
                            break;
                        case response:
                            warcRecordInfo.setType(WARCConstants.WARCRecordType.response);
                            warcRecordInfo.setUrl(header.getUrl());
                            break;
                        case metadata:
                            warcRecordInfo.setType(WARCConstants.WARCRecordType.metadata);
                            warcRecordInfo.setUrl(header.getUrl());
                            break;
                        case request:
                            warcRecordInfo.setType(WARCConstants.WARCRecordType.request);
                            warcRecordInfo.setUrl(header.getUrl());
                            break;
                        case resource:
                            warcRecordInfo.setType(WARCConstants.WARCRecordType.resource);
                            warcRecordInfo.setUrl(header.getUrl());
                            break;
                        case revisit:
                            warcRecordInfo.setType(WARCConstants.WARCRecordType.revisit);
                            warcRecordInfo.setUrl(header.getUrl());
                            break;
                        default:
                            if (log.isWarnEnabled()) {
                                log.warn("Ignoring unrecognised type for WARCRecord: "
                                        + WARCType);
                            }
                    }
                    warcRecordInfo.setCreate14DigitDate(header.getDate());
                    warcRecordInfo.setMimetype(header.getMimetype());
                    warcRecordInfo.setRecordId(recordId);
                    warcRecordInfo.setExtraHeaders(namedFields);
                    warcRecordInfo.setContentStream(record);
                    warcRecordInfo.setContentLength(contentLength);

                    writer.writeRecord(warcRecordInfo);

                }

                writer.close();
            }

            reader.close();
        }

        // add any imported content to a new arc or warc file as
        // appropriate..
        if (!hrsToImport.isEmpty()) {
            if (impArcType.equals("ARC")) {
                // Create an ARC Writer somewhat arbitrarily use the last filename from the list
                // of original filenames compress the file if the (last) original file was compressed
                WriterPoolSettings settings = new WriterPoolSettingsData(strippedImpArcFilename + "-new", "${prefix}",
                        ARCReader.DEFAULT_MAX_ARC_FILE_SIZE, compressed, dirs, impArcHeader);
                ARCWriter arcWriter = new ARCWriter(aint, settings);

                hrsToImport.values().forEach(hr -> {
                    if (hr.getLength() > 0L) {
                        File fin = new File(this.baseDir, "/uploadedFiles/"
                                + hr.getTempFileName());
                        Date dtNow = new Date();
                        try {
                            arcWriter.write(hr.getName(), hr.getContentType(),
                                    "0.0.0.0", dtNow.getTime(), hr.getLength(),
                                    new FileInputStream(fin));
                        } catch (IOException e) {
                            log.error(e.getMessage());
                        }
                    }
                });

                arcWriter.close();

            } else {
                // Create a WARC Writer
                // Somewhat arbitrarily use the last filename from the list of original filenames
                // Compress the file if the (last) original file was compressed
                WARCWriterPoolSettings settings = new WARCWriterPoolSettingsData(strippedImpArcFilename + "-new", "${prefix}",
                        WARCReader.DEFAULT_MAX_WARC_FILE_SIZE, compressed, dirs, impArcHeader, new UUIDGenerator());
                WARCWriter warcWriter = new WARCWriter(aint, settings);
                hrsToImport.values().stream().filter(hr -> {
                    return hr.getLength() > 0L;
                }).forEach(hr -> {
                    File fin = new File(this.baseDir, hr.getTempFileName());
                    Date dtNow = new Date();
                    URI recordId = null;
                    try {
                        recordId = new URI("urn:uuid:" + hr.getTempFileName());
                        ANVLRecord namedFields = new ANVLRecord();
                        namedFields.addLabelValue(WARCConstants.HEADER_KEY_IP, "0.0.0.0");
                        WARCRecordInfo warcRecordInfo = new WARCRecordInfo();
                        warcRecordInfo.setUrl(hr.getName());
                        warcRecordInfo.setCreate14DigitDate(writerDF.format(dtNow));
                        warcRecordInfo.setMimetype(hr.getContentType());
                        warcRecordInfo.setRecordId(recordId);
                        warcRecordInfo.setExtraHeaders(namedFields);
                        warcRecordInfo.setContentStream(new java.io.FileInputStream(fin));
                        warcRecordInfo.setContentLength(hr.getLength());
                        warcRecordInfo.setType(WARCConstants.WARCRecordType.response);
                        warcWriter.writeRecord(warcRecordInfo);
                    } catch (IOException | URISyntaxException e) {
                        log.error(e.getMessage());
                    }
                });
                warcWriter.close();
            }
        }

        log.info("copyAndPrune - Now time to reindex.");
        // Now re-index the files.
        HarvestResultDTO ahr = new HarvestResultDTO();
        File[] fileList = destDir.listFiles();
        if (fileList == null) {
            log.error("");
            return;
        }
        Set<ArcHarvestFileDTO> fileset = new HashSet<ArcHarvestFileDTO>();
        for (File f : fileList) {
            ArcHarvestFileDTO ahf = new ArcHarvestFileDTO();
            ahf.setCompressed(compressed);
            ahf.setName(f.getName());
            fileset.add(ahf);
        }

        ahr.setArcFiles(fileset);
        ahr.setCreationDate(new Date());
        ahr.setHarvestNumber(newHarvestResultNumber);

        ahr.index(destDir);
    }
}