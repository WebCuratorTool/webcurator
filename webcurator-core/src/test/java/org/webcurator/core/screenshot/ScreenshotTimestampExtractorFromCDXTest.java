package org.webcurator.core.screenshot;

import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.domain.model.core.SeedHistoryDTO;
import org.webcurator.test.WCTTestUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotTimestampExtractorFromCDXTest {
    private final static String archivePath = "/org/webcurator/core/store/archiveFiles";
    private static final String baseDir = "/usr/local/wct/store";
    private static final long tiOid = 19L;
    private static final int harvestNumber = 1;

    @Test
    public void testExtractTimestamp() throws DigitalAssetStoreException {
        List<SeedHistoryDTO> seeds = new ArrayList<>();
        SeedHistoryDTO seed1 = new SeedHistoryDTO();
        seed1.setTargetInstanceOid(tiOid);
        seed1.setSeed("https://www.rnz.co.nz/news/");
        seeds.add(seed1);

        SeedHistoryDTO seed2 = new SeedHistoryDTO();
        seed2.setTargetInstanceOid(tiOid);
        seed2.setSeed("https://www.rnz.co.nz/radios/");
        seeds.add(seed2);

        LocalDateTime dtStart = LocalDateTime.now();

        File directory = WCTTestUtils.getResourceAsFile(archivePath);
        ScreenshotTimestampExtractorFromCDX.getSeedWithTimestamps(seeds, directory);

        LocalDateTime dtFinished = LocalDateTime.now();
        long elapse = dtFinished.toEpochSecond(ZoneOffset.UTC) - dtStart.toEpochSecond(ZoneOffset.UTC);
        System.out.println("Time used: " + elapse);

        for (SeedHistoryDTO e : seeds) {
            assert !StringUtils.isEmpty(e.getTimestamp());
        }
    }
}
