package org.webcurator.core.visualization.modification;

import org.archive.format.warc.WARCConstants;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCReader;
import org.archive.io.warc.*;
import org.archive.uid.UUIDGenerator;
import org.archive.util.anvl.ANVLRecord;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandRowMetadata;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PruneAndImportCoordinatorHeritrixWarc extends PruneAndImportCoordinator {
    public static final String ARCHIVE_TYPE = "WARC";
    private String strippedImpArcFilename = null;
    private final List<String> impArcHeader = new ArrayList<String>();
    private final AtomicInteger aint = new AtomicInteger();
    private List<File> dirs;
    private boolean compressed;


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
    protected void copyArchiveRecords(File fileFrom, List<String> urisToDelete, Map<String, PruneAndImportCommandRowMetadata> hrsToImport, int newHarvestResultNumber) throws IOException, URISyntaxException {
        if (!fileFrom.getName().toUpperCase().endsWith(ARCHIVE_TYPE)) {
            log.error("Unsupported file format: {}", fileFrom.getAbsolutePath());
            return;
        }

        // Get the reader for this ARC File
        ArchiveReader reader = ArchiveReaderFactory.get(fileFrom);
        if (!(reader instanceof WARCReader)) {
            log.error("Unsupported file format: {}", fileFrom.getAbsolutePath());
            return;
        }


        // Use the original filename
        strippedImpArcFilename = reader.getStrippedFileName();
        compressed = reader.isCompressed();
        Iterator<ArchiveRecord> archiveRecordsIt = reader.iterator();

        // Get a another reader for the warc header metadata
        ArchiveReader headerReader = ArchiveReaderFactory.get(fileFrom);
        Iterator<ArchiveRecord> headerRecordIt = headerReader.iterator();

        // Read the Meta Data
        WARCRecord headerRec = (WARCRecord) headerRecordIt.next();
        byte[] buff = new byte[BYTE_BUFF_SIZE];
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
                    .getHeaderValue(org.archive.format.warc.WARCConstants.HEADER_KEY_TYPE);
            String strRecordId = (String) header
                    .getHeaderValue(org.archive.format.warc.WARCConstants.HEADER_KEY_ID);
            URI recordId = new URI(strRecordId.substring(
                    strRecordId.indexOf("<") + 1,
                    strRecordId.lastIndexOf(">") - 1));
            long contentLength = header.getLength() - header.getContentBegin();

            if (WARCType.equals(org.archive.format.warc.WARCConstants.WARCRecordType.warcinfo.toString())) {
                continue;
            }
            if (urisToDelete.contains(header.getUrl()) || hrsToImport.containsKey(header.getUrl())) {
                continue;
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

            WARCRecordInfo warcRecordInfo = new WARCRecordInfo();
            switch (org.archive.format.warc.WARCConstants.WARCRecordType.valueOf(WARCType)) {
                case warcinfo:
                    warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.warcinfo);
                    break;
                case response:
                    warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.response);
                    warcRecordInfo.setUrl(header.getUrl());
                    break;
                case metadata:
                    warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.metadata);
                    warcRecordInfo.setUrl(header.getUrl());
                    break;
                case request:
                    warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.request);
                    warcRecordInfo.setUrl(header.getUrl());
                    break;
                case resource:
                    warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.resource);
                    warcRecordInfo.setUrl(header.getUrl());
                    break;
                case revisit:
                    warcRecordInfo.setType(WARCConstants.WARCRecordType.revisit);
                    warcRecordInfo.setUrl(header.getUrl());
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
            writer.writeRecord(warcRecordInfo);
        }
        writer.close();
        reader.close();
    }

    @Override
    protected void importFromFile(long job, int harvestResultNumber, Map<String, PruneAndImportCommandRowMetadata> hrsToImport) throws IOException {
        // Create a WARC Writer
        // Somewhat arbitrarily use the last filename from the list of original filenames
        // Compress the file if the (last) original file was compressed
        WARCWriterPoolSettings settings = new WARCWriterPoolSettingsData(strippedImpArcFilename + "-new", "${prefix}",
                WARCReader.DEFAULT_MAX_WARC_FILE_SIZE, compressed, dirs, impArcHeader, new UUIDGenerator());
        WARCWriter warcWriter = new WARCWriter(aint, settings);
        hrsToImport.values().stream().filter(f -> {
            return f.getLength() > 0L;
        }).forEach(fProps -> {
            try {
                File tempFile = this.modificationDownloadFile(job,harvestResultNumber,fProps);
                InputStream fin = Files.newInputStream(tempFile.toPath());
                URI recordId = new URI("urn:uuid:" + tempFile.getName());
                ANVLRecord namedFields = new ANVLRecord();
                namedFields.addLabelValue(WARCConstants.HEADER_KEY_IP, "0.0.0.0");
                WARCRecordInfo warcRecordInfo = new WARCRecordInfo();
                warcRecordInfo.setUrl(fProps.getName());
                warcRecordInfo.setCreate14DigitDate(writerDF.format(new Date()));
                warcRecordInfo.setMimetype(fProps.getContentType());
                warcRecordInfo.setRecordId(recordId);
                warcRecordInfo.setExtraHeaders(namedFields);
                warcRecordInfo.setContentStream(fin);
                warcRecordInfo.setContentLength(fProps.getLength());
                warcRecordInfo.setType(WARCConstants.WARCRecordType.response);
                warcWriter.writeRecord(warcRecordInfo);

                Files.deleteIfExists(tempFile.toPath());
            } catch (IOException | URISyntaxException e) {
                log.error(e.getMessage());
            }
        });
        warcWriter.close();
    }

//    private InputStream wrapWarcContentInput(PruneAndImportCommandRowMetadata fProps) throws IOException {
//        String tempFileName = UUID.randomUUID().toString();
//        fProps.setTempFileName(tempFileName);
//
//        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(new File(this.fileDir, tempFileName)));
//
//        StringBuilder buf = new StringBuilder();
//        buf.append("HTTP/1.1 200 OK\n");
//        buf.append("Content-Type: ");
//        buf.append(fProps.getContentType()).append("\n");
//        buf.append("Content-Length: ");
//        buf.append(fProps.getLength()).append("\n");
//        LocalDateTime ldt = LocalDateTime.ofEpochSecond(fProps.getLastModified() / 1000, 0, ZoneOffset.UTC);
//        OffsetDateTime odt = ldt.atOffset(ZoneOffset.UTC);
//        buf.append("Date: ");
//        buf.append(odt.format(DateTimeFormatter.RFC_1123_DATE_TIME)).append("\n");
//        buf.append("Connection: close\n");
//
//        fos.write(buf.toString().getBytes());
//        fos.write("\n".getBytes());
//
//        File fin = new File(this.baseDir, fProps.getName());
//        fos.write(Files.readAllBytes(fin.toPath()));
//        fos.close();
//
//        return new FileInputStream(new File(this.fileDir, tempFileName));
//    }

    @Override
    protected void importFromRecorder(File fileFrom, List<String> urisToDelete, int newHarvestResultNumber) throws IOException, URISyntaxException {
        if (!fileFrom.getName().toUpperCase().endsWith(ARCHIVE_TYPE)) {
            log.error("Unsupported file format: {}", fileFrom.getAbsolutePath());
            return;
        }

        // Get the reader for this ARC File
        ArchiveReader reader = ArchiveReaderFactory.get(fileFrom);
        if (!(reader instanceof WARCReader)) {
            log.error("Unsupported file format: {}", fileFrom.getAbsolutePath());
            return;
        }

        Iterator<ArchiveRecord> archiveRecordsIt = reader.iterator();

        // Get a another reader for the warc header metadata
        ArchiveReader headerReader = ArchiveReaderFactory.get(fileFrom);
        Iterator<ArchiveRecord> headerRecordIt = headerReader.iterator();

        // Read the Meta Data
        WARCRecord headerRec = (WARCRecord) headerRecordIt.next();
        byte[] buff = new byte[BYTE_BUFF_SIZE];
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

        // Iterate through all the records, skipping deleted URLs.
        while (archiveRecordsIt.hasNext()) {
            WARCRecord record = (WARCRecord) archiveRecordsIt.next();
            ArchiveRecordHeader header = record.getHeader();
            String WARCType = (String) header.getHeaderValue(org.archive.format.warc.WARCConstants.HEADER_KEY_TYPE);
            String strRecordId = (String) header
                    .getHeaderValue(org.archive.format.warc.WARCConstants.HEADER_KEY_ID);
            URI recordId = new URI(strRecordId.substring(
                    strRecordId.indexOf("<") + 1,
                    strRecordId.lastIndexOf(">") - 1));
            long contentLength = header.getLength() - header.getContentBegin();

            if (WARCType.equals(org.archive.format.warc.WARCConstants.WARCRecordType.warcinfo.toString())) {
                continue;
            }
            if (urisToDelete.contains(header.getUrl())) {
                continue;
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

            WARCRecordInfo warcRecordInfo = new WARCRecordInfo();
            switch (org.archive.format.warc.WARCConstants.WARCRecordType.valueOf(WARCType)) {
                case warcinfo:
                    warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.warcinfo);
                    break;
                case response:
                    warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.response);
                    warcRecordInfo.setUrl(header.getUrl());
                    break;
                case metadata:
                    warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.metadata);
                    warcRecordInfo.setUrl(header.getUrl());
                    break;
                case request:
                    warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.request);
                    warcRecordInfo.setUrl(header.getUrl());
                    break;
                case resource:
                    warcRecordInfo.setType(org.archive.format.warc.WARCConstants.WARCRecordType.resource);
                    warcRecordInfo.setUrl(header.getUrl());
                    break;
                case revisit:
                    warcRecordInfo.setType(WARCConstants.WARCRecordType.revisit);
                    warcRecordInfo.setUrl(header.getUrl());
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

            writer.writeRecord(warcRecordInfo);
        }

        writer.close();

        reader.close();
    }

    @Override
    protected String archiveType() {
        return ARCHIVE_TYPE;
    }

    public String getStrippedImpArcFilename() {
        return strippedImpArcFilename;
    }

    public void setStrippedImpArcFilename(String strippedImpArcFilename) {
        this.strippedImpArcFilename = strippedImpArcFilename;
    }

    public List<String> getImpArcHeader() {
        return impArcHeader;
    }

    public AtomicInteger getAint() {
        return aint;
    }

    public List<File> getDirs() {
        return dirs;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public void setDirs(List<File> dirs) {
        this.dirs = dirs;
    }
}
