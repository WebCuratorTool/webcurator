package org.webcurator.core.visualization.modification.processor;

import org.archive.format.warc.WARCConstants;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCReader;
import org.archive.io.warc.*;
import org.archive.uid.UUIDGenerator;
import org.archive.util.anvl.ANVLRecord;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.VisualizationProgressBar;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyRowFullData;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ModifyProcessorWarc extends ModifyProcessor {
    public static final String ARCHIVE_TYPE_WARC = "WARC";
    public static final String ARCHIVE_TYPE_WARC_GZ = "WARC.GZ";

    private WarcFilenameTemplate warcFilenameTemplate = null;
    private final List<String> impArcHeader = new ArrayList<>();
    private final AtomicInteger aint = new AtomicInteger();
    private boolean compressed;

    public ModifyProcessorWarc(ModifyApplyCommand cmd) {
        super(cmd);
    }

    @Override
    public void initialFileNameTemplate(File fileFrom) throws Exception {
        if (!isArchiveType(fileFrom.getName())) {
            log.warn("Unsupported file format: {}", fileFrom.getAbsolutePath());
            return;
        }

        // Get the reader for this ARC File
        ArchiveReader reader = ArchiveReaderFactory.get(fileFrom);
        if (!(reader instanceof WARCReader)) {
            log.warn("Unsupported file format: {}", fileFrom.getAbsolutePath());
            return;
        }
        this.writeLog(String.format("Start to copy and prune a WARC file: %s size: %d", fileFrom.getName(), fileFrom.length()));

        // Use the original filename
        String strippedImpArcFilename = reader.getStrippedFileName();
        if (this.warcFilenameTemplate == null) {
            this.warcFilenameTemplate = new ModifyProcessor.WarcFilenameTemplate(strippedImpArcFilename);
        }

        reader.close();
    }

    /**
     * Post 1.6.1 code.
     * <p>
     * Problem:
     * The correct number of bytes/characters are being read from the header record, and saved in the
     * buffer array. But the input stream appears (for some unknown reason) to read or mark one character further
     * than the length that was read into the array.
     * <p>
     * For example, with content-length: 398, the stream should be stopping at the <|> below. So the next character read
     * would be a carriage return "\r". This is what the WarcReader (line 65 - gotoEOR()) is expecting in order to move
     * the marker to the start of the next record.
     * <p>
     * http-header-from: youremail@yourdomain.com\r\n
     * \r\n<|>
     * \r\n
     * \r\n
     * WARC/0.18\r\n
     * <p>
     * Instead the stream is reading up until the marker in the following example, and throwing a runtime error.
     * <p>
     * http-header-from: youremail@yourdomain.com\r\n
     * \r\n
     * \r<|>\n
     * \r\n
     * WARC/0.18\r\n
     * <p>
     * <p>
     * Workaround/Fix:
     * Create a duplicate ArchiveReader (headerRecordIt) for just the warc header metadata, that is then closed after
     * the metadata is read. The archiveRecordsIt ArchiveReader is still used to read the rest of the records. However
     * the first record (which we read with the other ArchiveReader) still has an issue with the iterator hasNext()
     * call. So it is skipped before entering the loop that copies each record.
     */
    @Override
    public void copyArchiveRecords(File fileFrom) throws Exception {
        if (!isArchiveType(fileFrom.getName())) {
            log.warn("Unsupported file format: {}", fileFrom.getAbsolutePath());
            return;
        }

        // Get the reader for this ARC File
        ArchiveReader reader = ArchiveReaderFactory.get(fileFrom);
        if (!(reader instanceof WARCReader)) {
            log.warn("Unsupported file format: {}", fileFrom.getAbsolutePath());
            return;
        }
        this.writeLog(String.format("Start to copy and prune a WARC file: %s size: %d", fileFrom.getName(), fileFrom.length()));

        // Use the original filename
        String strippedImpArcFilename = reader.getStrippedFileName();
        if (this.warcFilenameTemplate == null) {
            this.warcFilenameTemplate = new ModifyProcessor.WarcFilenameTemplate(strippedImpArcFilename);
        }

        if (urisToDelete.size() == 0) {
            //Copy file directly
            File destDir = this.dirs.get(0);
            File fileTo = new File(destDir, fileFrom.getName());
            org.apache.hadoop.thirdparty.guava.common.io.Files.copy(fileFrom, fileTo);
            return;
        }

        //Summary
        ModifyProcessor.StatisticItem statisticItem = new ModifyProcessor.StatisticItem();
        statisticItems.add(statisticItem);
        statisticItem.setFromFileName(fileFrom.getName());
        statisticItem.setFromFileLength(fileFrom.length());

        //Progress
        VisualizationProgressBar.ProgressItem progressItem = this.progressBar.getProgressItem(fileFrom.getName());

        compressed = reader.isCompressed();
        Iterator<ArchiveRecord> archiveRecordsIt = reader.iterator();

        // Get a another reader for the warc header metadata
        ArchiveReader headerReader = ArchiveReaderFactory.get(fileFrom);
        Iterator<ArchiveRecord> headerRecordIt = headerReader.iterator();

        // Read the Meta Data
        WARCRecord headerRec = (WARCRecord) headerRecordIt.next();
        byte[] buff = new byte[BYTE_BUFF_SIZE];
        StringBuilder metaData = new StringBuilder();
        int bytesRead;
        while ((bytesRead = headerRec.read(buff)) != -1) {
            metaData.append(new String(buff, 0, bytesRead));
        }

        List<String> l = new ArrayList<>();
        l.add(metaData.toString());

        if (impArcHeader.isEmpty()) {
            impArcHeader.add(metaData.toString());
        }

        headerRec.close();
        headerReader.close();

        // Bypass warc header metadata as it has been read above from a different ArchiveReader
        archiveRecordsIt.next();

        // Create a WARC Writer
        WARCWriterPoolSettings settings = new WARCWriterPoolSettingsData(strippedImpArcFilename + "~" + cmd.getNewHarvestResultNumber(), "${prefix}",
                ARCReader.DEFAULT_MAX_ARC_FILE_SIZE, compressed, dirs, l, new UUIDGenerator());
        WARCWriter writer = new WARCWriter(aint, settings);

        this.writeLog("Create a new WARC file, file name: " + writer.getFile());

        // Iterate through all the records, skipping deleted or imported URLs.
        while (archiveRecordsIt.hasNext()) {
            this.tryBlock();

            WARCRecord record = (WARCRecord) archiveRecordsIt.next();
            ArchiveRecordHeader header = record.getHeader();
            String WARCType = (String) header.getHeaderValue(org.archive.format.warc.WARCConstants.HEADER_KEY_TYPE);
            if (WARCType.equals(org.archive.format.warc.WARCConstants.WARCRecordType.warcinfo.toString())) {
                this.writeLog("Skip [warcinfo] record");
                statisticItem.increaseSkippedRecords();
                continue;
            }

            //TODO: to confirm should imported urls to be pruned: hrsToImport.containsKey(header.getUrl())
            if (urisToDelete.contains(header.getUrl())) {
                this.writeLog(String.format("Prune [%s] record: %s", WARCType, header.getUrl()));
                statisticItem.increasePrunedRecords();
                continue;
            }

            ANVLRecord namedFields = new ANVLRecord();
            header.getHeaderFields().forEach((key, value) -> {
                if (key.equals(org.archive.format.warc.WARCConstants.ABSOLUTE_OFFSET_KEY)) {
                    value = Long.toString(writer.getPosition());
                }
                // we exclude all but three fields to avoid duplication / erroneous data
                if (key.equals("WARC-IP-Address") || key.equals("WARC-Payload-Digest") || key.equals("WARC-Concurrent-To")) {
                    namedFields.addLabelValue(key, value.toString());
                }
            });

            WARCRecordInfo warcRecordInfo = createWarcRecordInfo(record, header, namedFields, header.getUrl());

            writer.writeRecord(warcRecordInfo);
            statisticItem.increaseCopiedRecords();
            this.writeLog(String.format("Copy [%s] record: %s", WARCType, header.getUrl()));

            progressItem.setCurLength(header.getOffset()); //Increase Progress
        }

        this.writeLog(String.format("End to copy and prune from: %s size: %d", fileFrom.getName(), fileFrom.length()));
        if (writer.getFile() != null) {
            this.writeLog(String.format("End to copy and prune to: %s size: %d", writer.getFile().getName(), writer.getFile().length()));

            statisticItem.setToFileName(writer.getFile().getName());
            statisticItem.setToFileLength(writer.getFile().length());
        }
        writer.close();
        reader.close();
    }

    @Override
    public void importFromFile() throws Exception {
        this.writeLog("Start to import from file");
        ModifyProcessor.StatisticItem statisticItem = new ModifyProcessor.StatisticItem();
        statisticItems.add(statisticItem);

        VisualizationProgressBar.ProgressItem progressItemFileImported = progressBar.getProgressItem("ImportedFiles");

        // Create a WARC Writer
        LocalDateTime timestamp = LocalDateTime.now();
        if (this.warcFilenameTemplate == null) {
            this.warcFilenameTemplate = new WarcFilenameTemplate("WCT-yyyymmddHHMMSS-FILE.warc");
        }
        this.warcFilenameTemplate.setTimestamp(timestamp.format(fTimestamp17));
        this.warcFilenameTemplate.setSerialNo(aint.getAndIncrement());
        this.warcFilenameTemplate.setHeritrixInfo("mod~import~file");
        String strippedImpArcFilename = this.warcFilenameTemplate.toString();

        // Create a WARC Writer
        // Somewhat arbitrarily use the last filename from the list of original filenames
        // Compress the file if the (last) original file was compressed
        WARCWriterPoolSettings settings = new WARCWriterPoolSettingsData(strippedImpArcFilename + "~" + cmd.getNewHarvestResultNumber(), "${prefix}",
                WARCReader.DEFAULT_MAX_WARC_FILE_SIZE, compressed, dirs, impArcHeader, new UUIDGenerator());
        WARCWriter warcWriter = new WARCWriter(aint, settings);

        for (ModifyRowFullData fProps : hrsToImport.values()) {
            this.tryBlock();

            if (!fProps.getOption().equalsIgnoreCase(ModifyApplyCommand.OPTION_FILE)) {
                continue;
            }

            if (this.isAllowedPrune(cmd.getTargetInstanceId(), cmd.getHarvestResultNumber(), fProps.getId())) {
                this.urisToDelete.add(fProps.getUrl());
            }

            File tempFile = null;
            try {
                Date warcDate = new Date();
                if (fProps.getModifiedMode().equalsIgnoreCase("FILE") || fProps.getModifiedMode().equalsIgnoreCase("CUSTOM")) {
                    warcDate.setTime(fProps.getLastModifiedDate());
                }
                log.debug("WARC-Date: {}", writerDF.format(warcDate));

                tempFile = this.downloadFile(cmd.getTargetInstanceId(), cmd.getHarvestResultNumber(), fProps);

                InputStream fin = Files.newInputStream(tempFile.toPath());
                URI recordId = new URI("urn:uuid:" + tempFile.getName());
                ANVLRecord namedFields = new ANVLRecord();
                namedFields.addLabelValue(org.archive.format.warc.WARCConstants.HEADER_KEY_IP, "0.0.0.0");
                WARCRecordInfo warcRecordInfo = new WARCRecordInfo();
                warcRecordInfo.setUrl(fProps.getUrl());
                warcRecordInfo.setCreate14DigitDate(writerDF.format(warcDate));
                warcRecordInfo.setMimetype(fProps.getContentType());
                warcRecordInfo.setRecordId(recordId);
                warcRecordInfo.setExtraHeaders(namedFields);
                warcRecordInfo.setContentStream(fin);
                warcRecordInfo.setContentLength(tempFile.length());
                warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.response);
                warcWriter.writeRecord(warcRecordInfo);

                this.writeLog(String.format("[INFO] Imported a record from file, name: %s, size: %d", tempFile.getName(), tempFile.length()));
                statisticItem.increaseCopiedRecords();

                progressItemFileImported.setCurLength(progressItemFileImported.getCurLength() + fProps.getUploadFileLength());
            } catch (IOException | URISyntaxException | DigitalAssetStoreException e) {
                log.error(e.getMessage());
                this.writeLog(String.format("[ERROR] Imported a record from file, name: %s, size: %d. Error: %s", fProps.getUploadFileName(), fProps.getUploadFileLength(), e.getMessage()));
                statisticItem.increaseFailedRecords();
                throw e;
            } finally {
                if (tempFile != null) {
                    Files.deleteIfExists(tempFile.toPath());
                }
            }
        }

        if (warcWriter.getFile() != null) {
            this.writeLog(String.format("End to import files, to: %s size: %d", warcWriter.getFile().getName(), warcWriter.getFile().length()));
            statisticItem.setToFileName(warcWriter.getFile().getName());
            statisticItem.setToFileLength(warcWriter.getFile().length());
        } else {
            this.writeLog("End to import files");
            statisticItems.remove(statisticItem);
        }
        warcWriter.close();
    }


    @Override
    public void importFromPatchHarvest(File fileFrom) throws IOException, URISyntaxException {
        if (!isArchiveType(fileFrom.getName())) {
            log.warn("Unsupported file format: {}", fileFrom.getAbsolutePath());
            return;
        }

        // Get the reader for this ARC File
        ArchiveReader reader = ArchiveReaderFactory.get(fileFrom);
        if (!(reader instanceof WARCReader)) {
            log.warn("Unsupported file format: {}", fileFrom.getAbsolutePath());
            return;
        }
        this.writeLog(String.format("Start to import from source URLs, a source WARC file: %s size: %d", fileFrom.getName(), fileFrom.length()));

        //Summary
        ModifyProcessor.StatisticItem statisticItem = new ModifyProcessor.StatisticItem();
        statisticItems.add(statisticItem);
        statisticItem.setFromFileName(fileFrom.getName());
        statisticItem.setFromFileLength(fileFrom.length());

        //Progress
        VisualizationProgressBar.ProgressItem progressItem = this.progressBar.getProgressItem(fileFrom.getName());

        String strippedImpArcFilename = reader.getStrippedFileName();
        Iterator<ArchiveRecord> archiveRecordsIt = reader.iterator();

        // Get a another reader for the warc header metadata
        ArchiveReader headerReader = ArchiveReaderFactory.get(fileFrom);
        Iterator<ArchiveRecord> headerRecordIt = headerReader.iterator();

        // Read the Meta Data
        WARCRecord headerRec = (WARCRecord) headerRecordIt.next();
        byte[] buff = new byte[BYTE_BUFF_SIZE];
        StringBuilder metaData = new StringBuilder();
        int bytesRead;
        while ((bytesRead = headerRec.read(buff)) != -1) {
            metaData.append(new String(buff, 0, bytesRead));
        }

        List<String> l = new ArrayList<>();
        l.add(metaData.toString());

        if (impArcHeader.isEmpty()) {
            impArcHeader.add(metaData.toString());
        }

        headerRec.close();
        headerReader.close();

        // Bypass warc header metadata as it has been read above from a different ArchiveReader
        archiveRecordsIt.next();

        WARCWriterPoolSettings settings = new WARCWriterPoolSettingsData(strippedImpArcFilename + "~" + cmd.getNewHarvestResultNumber(), "${prefix}",
                ARCReader.DEFAULT_MAX_ARC_FILE_SIZE, compressed, dirs, l, new UUIDGenerator());
        WARCWriter writer = new WARCWriter(aint, settings);

        // Iterate through all the records, skipping deleted URLs.
        while (archiveRecordsIt.hasNext()) {
            this.tryBlock();

            WARCRecord record = (WARCRecord) archiveRecordsIt.next();
            ArchiveRecordHeader header = record.getHeader();
            String WARCType = (String) header.getHeaderValue(org.archive.format.warc.WARCConstants.HEADER_KEY_TYPE);
            if (WARCType.equals(org.archive.format.warc.WARCConstants.WARCRecordType.warcinfo.toString())) {
                this.writeLog("Skip [warcinfo] record");
                statisticItem.increaseSkippedRecords();
                continue;
            }

            /*If the url is to be pruned, but not to be imported*/
            if (urisToDelete.contains(header.getUrl()) && !urisToImportByRecrawl.contains(header.getUrl())) {
                this.writeLog(String.format("Prune [%s] record: %s", WARCType, header.getUrl()));
                continue;
            }

            if (this.isAllowedPrune(cmd.getTargetInstanceId(), cmd.getHarvestResultNumber(), header.getUrl())) {
                this.urisToDelete.add(header.getUrl());
            }

            ANVLRecord namedFields = new ANVLRecord();
            header.getHeaderFields().forEach((key, value) -> {
                if (key.equals(org.archive.format.warc.WARCConstants.ABSOLUTE_OFFSET_KEY)) {
                    value = Long.toString(writer.getPosition());
                }
                // we exclude all but three fields to avoid
                // duplication / erroneous data
                if (key.equals("WARC-IP-Address") || key.equals("WARC-Payload-Digest") || key.equals("WARC-Concurrent-To")) {
                    namedFields.addLabelValue(key, value.toString());
                }
            });

            WARCRecordInfo warcRecordInfo = createWarcRecordInfo(record, header, namedFields, header.getUrl());
            writer.writeRecord(warcRecordInfo);

            this.writeLog(String.format("Import [%s] record: %s", WARCType, header.getUrl()));
            statisticItem.increaseCopiedRecords();

            progressItem.setCurLength(header.getOffset());
        }

        this.writeLog(String.format("End to import URLs, from: %s size: %d", fileFrom.getName(), fileFrom.length()));
        if (writer.getFile() != null) {
            this.writeLog(String.format("End to import URLs, to: %s size: %d", writer.getFile().getName(), writer.getFile().length()));
            statisticItem.setToFileName(writer.getFile().getName());
            statisticItem.setToFileLength(writer.getFile().length());
        }
        writer.close();
        reader.close();
    }

    private WARCRecordInfo createWarcRecordInfo(WARCRecord record, ArchiveRecordHeader header, ANVLRecord namedFields, String targetUrl) throws URISyntaxException {
        String WARCType = (String) header.getHeaderValue(org.archive.format.warc.WARCConstants.HEADER_KEY_TYPE);
        //        URI recordId = new URI(String.format("urn:uuid:%s", UUID.randomUUID().toString()));
        String strRecordId = (String) header.getHeaderValue(org.archive.format.warc.WARCConstants.HEADER_KEY_ID);
        URI recordId = new URI(strRecordId.substring(strRecordId.indexOf("<") + 1, strRecordId.lastIndexOf(">") - 1));

        long contentLength = header.getLength() - header.getContentBegin();

        WARCRecordInfo warcRecordInfo = new WARCRecordInfo();
        switch (org.archive.format.warc.WARCConstants.WARCRecordType.valueOf(WARCType)) {
            case warcinfo:
                warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.warcinfo);
                break;
            case response:
                warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.response);
                warcRecordInfo.setUrl(targetUrl);
                break;
            case metadata:
                warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.metadata);
                warcRecordInfo.setUrl(targetUrl);
                break;
            case request:
                warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.request);
                warcRecordInfo.setUrl(targetUrl);
                break;
            case resource:
                warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.resource);
                warcRecordInfo.setUrl(targetUrl);
                break;
            case revisit:
                warcRecordInfo.setType(WARCConstants.WARCRecordType.revisit);
                warcRecordInfo.setUrl(targetUrl);
                break;
            default:
                log.warn("Ignoring unrecognised type for WARCRecord: " + WARCType);
        }
        warcRecordInfo.setCreate14DigitDate(header.getDate());
        warcRecordInfo.setMimetype(header.getMimetype());
        warcRecordInfo.setRecordId(recordId);
        warcRecordInfo.setExtraHeaders(namedFields);
        warcRecordInfo.setContentStream(record);
        warcRecordInfo.setContentLength(contentLength);

        return warcRecordInfo;
    }

    public boolean isArchiveType(String fileName) {
        return fileName.toUpperCase().endsWith(ARCHIVE_TYPE_WARC) || fileName.toUpperCase().endsWith(ARCHIVE_TYPE_WARC_GZ);
    }
}
