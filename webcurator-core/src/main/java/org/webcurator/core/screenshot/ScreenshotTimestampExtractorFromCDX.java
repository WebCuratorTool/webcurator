package org.webcurator.core.screenshot;

import org.apache.commons.lang.StringUtils;
import org.netpreserve.jwarc.cdx.CdxReader;
import org.netpreserve.jwarc.cdx.CdxRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.domain.model.core.SeedHistoryDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScreenshotTimestampExtractorFromCDX {
    protected static final Logger log = LoggerFactory.getLogger(ScreenshotTimestampExtractorFromCDX.class);

    public static List<SeedHistoryDTO> getSeedWithTimestamps(List<SeedHistoryDTO> seedsHistory, File directory) throws DigitalAssetStoreException {
        File[] cdxFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".cdx"));
        if (cdxFiles == null || cdxFiles.length == 0) {
            String err = "No cdx files found in folder: " + directory.getAbsolutePath();
            log.error(err);
            throw new DigitalAssetStoreException(err);
        }

        try {
            extractTimestampFromHarvests(seedsHistory, cdxFiles);
        } catch (IOException e) {
            String err = "Failed to extract timestamp for seed Urls";
            log.error(err, e);
            throw new DigitalAssetStoreException(err);
        }

        return seedsHistory;
    }

    private static void extractTimestampFromHarvests(List<SeedHistoryDTO> seeds, File[] cdxFiles) throws IOException {
        for (File f : cdxFiles) {
            extractTimestampFromHarvests(seeds, f);
            if (isAllSeedsTimestampAvailable(seeds)) {
                break;
            }
        }
    }

    private static void extractTimestampFromHarvests(List<SeedHistoryDTO> seeds, File cdxFile) throws IOException {
        try (CdxReader cdxReader = new CdxReader(Files.newInputStream(cdxFile.toPath()))) {
            for (CdxRecord rec : cdxReader) {
                extractTimestampFromHarvests(seeds, rec);
            }
        }
    }

    private static void extractTimestampFromHarvests(List<SeedHistoryDTO> seeds, CdxRecord rec) {
        String target_uri = rec.target();
        if (StringUtils.isEmpty(target_uri)) {
            return;
        }
        for (SeedHistoryDTO seed : seeds) {
            if (target_uri.equals(seed.getSeed())) {
                String timestamp = harvestedDateTimeTo14Timestamp(rec);
                if (seed.getTimestamp() == null) {
                    seed.setTimestamp(timestamp);
                }
                break;
            }
        }
    }

    private static String harvestedDateTimeTo14Timestamp(CdxRecord rec) {
        Instant instant = rec.date();
        OffsetDateTime odt = instant.atOffset(ZoneOffset.UTC);
        return odt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private static boolean isAllSeedsTimestampAvailable(List<SeedHistoryDTO> seeds) {
        long sizeOfAvailable = seeds.stream().filter(seed -> !StringUtils.isEmpty(seed.getTimestamp())).count();
        return sizeOfAvailable == seeds.size();
    }
}
