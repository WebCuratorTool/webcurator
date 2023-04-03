package org.webcurator.core.screenshot;

import org.apache.commons.lang.StringUtils;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.format.warc.WARCConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.domain.model.core.SeedHistoryDTO;

import java.io.File;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScreenshotTimestampExtractor {
    protected static final Logger log = LoggerFactory.getLogger(ScreenshotTimestampExtractor.class);
    private static final String RECORD_TYPE_METADATA = org.archive.format.warc.WARCConstants.WARCRecordType.metadata.toString();


    public static List<SeedHistoryDTO> getSeedWithTimestamps(List<SeedHistoryDTO> seedsHistory, File directory) throws DigitalAssetStoreException {
        File[] warcFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".warc") ||
                name.toLowerCase().endsWith(".warc.gz"));
        if (warcFiles == null || warcFiles.length == 0) {
            String err = "No warc files found in folder: " + directory.getAbsolutePath();
            log.error(err);
            throw new DigitalAssetStoreException(err);
        }

        try {
            extractTimestampFromHarvests(seedsHistory, warcFiles);
        } catch (IOException e) {
            String err = "Failed to extract timestamp for seed Urls";
            log.error(err, e);
            throw new DigitalAssetStoreException(err);
        }

        return seedsHistory;
    }

    private static void extractTimestampFromHarvests(List<SeedHistoryDTO> seeds, File[] warcFiles) throws IOException {
        for (File f : warcFiles) {
            extractTimestampFromHarvests(seeds, f);
            if (isAllSeedsTimestampAvailable(seeds)) {
                break;
            }
        }
    }

    private static void extractTimestampFromHarvests(List<SeedHistoryDTO> seeds, File warcFile) throws IOException {
        try (ArchiveReader reader = ArchiveReaderFactory.get(warcFile)) {
            for (ArchiveRecord rec : reader) {
                extractTimestampFromHarvests(seeds, rec);
            }
        } catch (IOException e) {
            String err = "Failed to open archive file: " + warcFile.getAbsolutePath() + " with exception: " + e.getMessage();
            log.error(err, e);
            throw e;
        }
    }

    private static void extractTimestampFromHarvests(List<SeedHistoryDTO> seeds, ArchiveRecord rec) {
        ArchiveRecordHeader headers = rec.getHeader();
        if (headers == null) {
            return;
        }

        String type = headers.getHeaderValue(WARCConstants.HEADER_KEY_TYPE).toString();
        if (type == null || !type.equals(RECORD_TYPE_METADATA)) {
            return;
        }

        for (SeedHistoryDTO seed : seeds) {
            String target_uri = headers.getHeaderValue(WARCConstants.HEADER_KEY_URI).toString();
            if (target_uri != null && target_uri.equals(seed.getSeed())) {
                if (seed.getTimestamp() == null) {
                    String harvestedDateTime = headers.getHeaderValue(WARCConstants.HEADER_KEY_DATE).toString();
                    String timestamp = harvestedDateTimeTo14Timestamp(harvestedDateTime);
                    seed.setTimestamp(timestamp);
                }
                break;
            }
        }
    }

    private static String harvestedDateTimeTo14Timestamp(String harvestedDateTime) {
        Instant instant = Instant.parse(harvestedDateTime);
        OffsetDateTime odt = instant.atOffset(ZoneOffset.UTC);
        return odt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private static boolean isAllSeedsTimestampAvailable(List<SeedHistoryDTO> seeds) {
        long sizeOfAvailable = seeds.stream().filter(seed -> !StringUtils.isEmpty(seed.getTimestamp())).count();
        return sizeOfAvailable == seeds.size();
    }
}