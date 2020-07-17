package org.webcurator.core.visualization;

import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;
import org.archive.io.ArchiveRecord;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.warc.WARCConstants;
import org.archive.io.warc.WARCRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.networkmap.bdb.BDBNetworkMapPool;
import org.webcurator.domain.model.core.SeedHistoryDTO;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MockVisualizationCommonConfigureItems {
    protected static final Logger log = LoggerFactory.getLogger(MockVisualizationCommonConfigureItems.class);
    protected String baseDir = "/usr/local/wct/store";
    protected String baseLogDir = "logs";
    protected String baseReportDir = "reports";
    protected VisualizationDirectoryManager directoryManager = new VisualizationDirectoryManager(baseDir, baseLogDir, baseReportDir);

    protected BDBNetworkMapPool pool = new BDBNetworkMapPool(baseDir);

    protected long targetInstanceId = 5010;
    protected int harvestResultNumber = 1;
    protected int newHarvestResultNumber = 2;
    protected Set<SeedHistoryDTO> seeds = new HashSet<>();
    protected VisualizationProcessorManager processorManager;
    protected WctCoordinatorClient wctClient;

    public void initTest() throws IOException, DigitalAssetStoreException {
        SeedHistoryDTO seedHistoryPrimary = new SeedHistoryDTO(1, "http://www.google.com/", targetInstanceId, true);
        SeedHistoryDTO seedHistorySecondary = new SeedHistoryDTO(2, "http://www.baidu.com/", targetInstanceId, false);
        seeds.add(seedHistoryPrimary);
        seeds.add(seedHistorySecondary);
        wctClient = new WctCoordinatorClient("http", "localhost", 8080, new RestTemplateBuilder());
        processorManager = new VisualizationProcessorManager(directoryManager, wctClient, 3);
    }

    public boolean isUrlExistInWarcFile(File warcFile, List<String> urls) throws IOException {
        ArchiveReader reader = ArchiveReaderFactory.get(warcFile);
        for (ArchiveRecord rec : reader) {
            String mime = rec.getHeader().getMimetype();
            if (mime != null && mime.equals("text/dns")) {
                continue;
            }

            WARCRecord record = (WARCRecord) rec;
            ArchiveRecordHeader header = record.getHeader();
            if (header.getUrl() == null) {
                continue;
            }

            if (urls.contains(header.getUrl())) {
                return true;
            }
        }

        return false;
    }

    public List<String> getRandomUrlsFromWarcFile(File warcFile) throws IOException {
        int COUNT = 5, N = 0; //N: Current pointer
        String[] candidateUrls = new String[COUNT];
        ArchiveReader reader = ArchiveReaderFactory.get(warcFile);
        for (ArchiveRecord rec : reader) {
            String mime = rec.getHeader().getMimetype();
            if (mime.equals("text/dns")) {
                continue;
            }

            WARCRecord record = (WARCRecord) rec;
            ArchiveRecordHeader header = record.getHeader();
            if (header.getUrl() == null) {
                continue;
            }

            String type = rec.getHeader().getHeaderValue(WARCConstants.HEADER_KEY_TYPE).toString();
            if (!org.archive.format.warc.WARCConstants.WARCRecordType.request.toString().equals(type) &&
                    !org.archive.format.warc.WARCConstants.WARCRecordType.response.toString().equals(type) &&
                    !org.archive.format.warc.WARCConstants.WARCRecordType.metadata.toString().equals(type)) {
                continue;
            }

            String url = header.getUrl();
            for (int i = 0; i < COUNT; i++) {
                int randNum = this.random(N);
                if (randNum == 0) {
                    candidateUrls[i] = url;
                }
            }
            N++;
        }

        return Arrays.asList(candidateUrls);
    }

    public int random(int max) {
        Random random = new Random();
        return random.nextInt(max + 1);
    }

    public ModifyApplyCommand getApplyCommand() {
        ModifyApplyCommand cmd = new ModifyApplyCommand();
        cmd.setTargetInstanceId(targetInstanceId);
        cmd.setHarvestResultNumber(harvestResultNumber);
        cmd.setNewHarvestResultNumber(newHarvestResultNumber);
        return cmd;
    }

    public String getModifiedWarcFileName(File warcFileFrom) {
        String fromFileName = warcFileFrom.getName();
        int idx = fromFileName.indexOf('.');
        if (idx <= 0) {
            return null;
        }

        String part1 = fromFileName.substring(0, idx);
        String part2 = fromFileName.substring(idx + 1);

        return String.format("%s~%d.%s", part1, newHarvestResultNumber, part2);
    }
}
