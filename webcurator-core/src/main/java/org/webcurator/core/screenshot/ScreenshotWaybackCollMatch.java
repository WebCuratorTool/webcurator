package org.webcurator.core.screenshot;

import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.warc.WARCConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.SeedHistoryDTO;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class ScreenshotWaybackCollMatch {
    protected Logger log = LoggerFactory.getLogger(getClass());
    private static final String RECORD_TYPE_METADATA=org.archive.format.warc.WARCConstants.WARCRecordType.metadata.toString();
    private String waybackViewerUrl;
    private String rootStorePath;
    private WctCoordinatorClient wctClient;

    public List<SeedHistoryDTO> getSeedWithTimestamps(HarvestResultDTO harvestResult) throws DigitalAssetStoreException {
        Set<SeedHistoryDTO> seedsHistory = wctClient.getSeedUrls(harvestResult.getTargetInstanceOid(), harvestResult.getHarvestNumber());

        File directory = new File(rootStorePath, harvestResult.getTargetInstanceOid() + File.separator + harvestResult.getHarvestNumber());
        File[] warcFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".warc") ||
                name.toLowerCase().endsWith(".warc.gz"));
        if (warcFiles == null || warcFiles.length == 0) {
            String err = "No warc files found in folder: " + directory.getAbsolutePath();
            log.error(err);
            throw new DigitalAssetStoreException(err);
        }

        try {
            extractTimestampFromHarvests(seedsHistory,warcFiles);
        } catch (IOException e) {
            String err ="Failed to extract timestamp for seed Urls";
            log.error(err,e);
            throw new DigitalAssetStoreException(err);
        }


    }

    private void extractTimestampFromHarvests(Set<SeedHistoryDTO> seeds, File[] warcFiles) throws IOException {
        for (File f: warcFiles){
            extractTimestampFromHarvests(seeds,f);
        }
    }

    private void extractTimestampFromHarvests(Set<SeedHistoryDTO> seeds,File warcFile) throws IOException {
        ArchiveReader reader = null;
        try {
            reader = ArchiveReaderFactory.get(warcFile);
            for (ArchiveRecord rec : reader){
                extractTimestampFromHarvests(seeds,rec);
            }
        } catch (Exception e) {
            String err = "Failed to open archive file: " + f.getAbsolutePath() + " with exception: " + e.getMessage();
            log.error(err, e);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void extractTimestampFromHarvests(Set<SeedHistoryDTO> seeds, ArchiveRecord rec){
        ArchiveRecordHeader headers=rec.getHeader();
        if (headers==null){
            return;
        }

        String type =headers.getHeaderValue(WARCConstants.HEADER_KEY_TYPE).toString();
        if (type==null|| !type.equals(RECORD_TYPE_METADATA)){
            return;
        }

        seeds.forEach(seed->{
            String target_uri=headers.getHeaderValue(WARCConstants.HEADER_KEY_URI).toString();
            if (target_uri!=null && target_uri.equals(seed.getSeed())){
                if (seed.getTimestamp()!=null){
                    String harvestedDateTime=headers.getHeaderValue(WARCConstants.HEADER_KEY_DATE).toString();
                    seed.setTimestamp(harvestedDateTimeTo14Timestamp(harvestedDateTime));
                }
            }
        });
    }

    private String harvestedDateTimeTo14Timestamp(String harvestedDateTime){
        LocalDateTime dt = LocalDateTime.parse(harvestedDateTime, DateTimeFormatter.ISO_INSTANT);
        return dt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}