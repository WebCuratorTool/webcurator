package org.webcurator.core.screenshot;

import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.domain.model.core.*;
import org.webcurator.test.WCTTestUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotTimestampExtractorTest {
    private final static String archivePath = "/org/webcurator/core/store/archiveFiles";
    private static final String baseDir = "/usr/local/wct/store";
    private static final long tiOid = 19L;
    private static final int harvestNumber = 1;

    @Test
    public void testExtractTimestamp() throws DigitalAssetStoreException {
        List<SeedHistoryDTO> seeds = new ArrayList<>();
        SeedHistoryDTO seed = new SeedHistoryDTO();
        seed.setTargetInstanceOid(21L);
        seed.setSeed("https://www.rnz.co.nz/news/");
        seeds.add(seed);

        File directory = WCTTestUtils.getResourceAsFile(archivePath);
        ScreenshotTimestampExtractor.getSeedWithTimestamps(seeds, directory);

        for (SeedHistoryDTO e : seeds) {
            assert !StringUtils.isEmpty(e.getTimestamp());
        }
    }

    @Ignore
    @Test
    public void testExtractTimestampBulk() throws DigitalAssetStoreException {
        List<SeedHistoryDTO> seeds = new ArrayList<>();
        SeedHistoryDTO seed = new SeedHistoryDTO();
        seed.setTargetInstanceOid(tiOid);
        seed.setSeed("https://www.rnz.co.nz/news/");
        seeds.add(seed);

        File directory = new File(baseDir, tiOid + File.separator + harvestNumber);

        LocalDateTime dtStart = LocalDateTime.now();

        ScreenshotTimestampExtractor.getSeedWithTimestamps(seeds, directory);

        LocalDateTime dtFinished = LocalDateTime.now();
        long elapse = dtFinished.toEpochSecond(ZoneOffset.UTC) - dtStart.toEpochSecond(ZoneOffset.UTC);
        System.out.println("Time used: " + elapse);
    }
}
