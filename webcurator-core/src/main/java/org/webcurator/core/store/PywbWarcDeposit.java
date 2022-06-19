package org.webcurator.core.store;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.SeedHistoryDTO;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PywbWarcDeposit extends AbstractRestClient {
    private WctCoordinatorClient wctClient;
    private String rootStorePath;
    private String pywbManagerColl;

    private File pywbManagerStoreDir;
    private String pywbCDXQueryUrl;
    private long maxTrySeconds = 1800L;

    private boolean pywbEnabled = true;

    public PywbWarcDeposit() {
    }

    public PywbWarcDeposit(String baseUrl, RestTemplateBuilder restTemplateBuilder) {
        super(baseUrl, restTemplateBuilder);
    }


    public void depositWarc(HarvestResultDTO harvestResult) throws Exception {
        if (!this.pywbEnabled) {
            return;
        }

        File directory = new File(rootStorePath, harvestResult.getTargetInstanceOid() + File.separator + harvestResult.getHarvestNumber());
        File[] warcFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".warc") ||
                name.toLowerCase().endsWith(".warc.gz"));
        if (warcFiles == null) {
            return;
        }

        //Added all warc files to the PYWB
        for (File warc : warcFiles) {
            String[] commands = {"wb-manager", "add", pywbManagerColl, warc.getAbsolutePath()};
            List<String> commandList = Arrays.asList(commands);
            ProcessBuilder processBuilder = new ProcessBuilder(commandList);
            processBuilder.directory(pywbManagerStoreDir);
            try {
                Process process = processBuilder.inheritIO().start();
                int processStatus = process.waitFor();

                if (processStatus != 0) {
                    throw new Exception("Process ended with a failed status: " + processStatus);
                }
            } catch (Exception e) {
                log.error("Unable to process the command in a new thread.", e);
                throw e;
            }
        }
    }

    public List<SeedHistoryDTO> getSeedWithTimestamps(HarvestResultDTO harvestResult) throws DigitalAssetStoreException {
        if (!this.pywbEnabled) {
            return null;
        }

        File directory = new File(rootStorePath, harvestResult.getTargetInstanceOid() + File.separator + harvestResult.getHarvestNumber());
        File[] warcFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".warc") ||
                name.toLowerCase().endsWith(".warc.gz"));
        if (warcFiles == null) {
            return null;
        }

        Map<String, File> mappedWarcFiles = new HashMap<>();

        //Added all warc files to the PYWB
        for (File warc : warcFiles) {
            mappedWarcFiles.put(warc.getName(), warc);
        }

        File[] cdxFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".cdx"));
        if (cdxFiles == null) {
            return null;
        }

        LocalDateTime minTimestamp = LocalDateTime.now(), maxTimestamp = LocalDateTime.now().minusYears(5L);
        for (File cdx : cdxFiles) {
            List<String> lines = null;
            try {
                lines = FileUtils.readLines(cdx);
            } catch (IOException e) {
                throw new DigitalAssetStoreException(e);
            }
            for (int idx = 1; idx < lines.size(); idx++) {
                String line = lines.get(idx);
                int pos = line.indexOf(' ');
                if (pos <= 1) {
                    continue;
                }
                String strTimestamp = line.substring(0, pos - 1);
                LocalDateTime currTimestamp = LocalDateTime.parse(strTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                if (currTimestamp.compareTo(minTimestamp) < 0) {
                    minTimestamp = currTimestamp;
                }
                if (currTimestamp.compareTo(maxTimestamp) > 0) {
                    maxTimestamp = currTimestamp;
                }
            }
        }
        String from = minTimestamp.format(DateTimeFormatter.BASIC_ISO_DATE) + "000000";
        String to = maxTimestamp.format(DateTimeFormatter.BASIC_ISO_DATE) + "235959";

        // Query the CDX of the seeds
        List<SeedHistoryDTO> seedWithTimestamp = new ArrayList<>();
        Set<SeedHistoryDTO> seedsHistory = wctClient.getSeedUrls(harvestResult.getTargetInstanceOid(), harvestResult.getHarvestNumber());
        long timeUsed = 0;
        while (timeUsed < maxTrySeconds && seedWithTimestamp.size() < seedsHistory.size()) {
            Iterator<SeedHistoryDTO> iterator = seedsHistory.iterator();
            boolean needRetry = false;
            while (iterator.hasNext()) {
                SeedHistoryDTO seed = iterator.next();
                String timestamp = getTimestampOfSeed(seed.getSeed(), from, to, mappedWarcFiles);
                if (timestamp != null) {
                    seed.setTimestamp(timestamp);
                    seedWithTimestamp.add(seed);
                } else {
                    needRetry = true;
                    break;
                }
            }
            if (needRetry) {
                try {
                    TimeUnit.SECONDS.sleep(15L);
                } catch (InterruptedException e) {
                    throw new DigitalAssetStoreException(e);
                }
                timeUsed += 15;
            }
        }
        mappedWarcFiles.clear();
        seedsHistory.clear();
        return seedWithTimestamp;
    }

    public String getTimestampOfSeed(String seed, String from, String to, Map<String, File> mappedWarcFiles) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.pywbCDXQueryUrl)
                .queryParam("url", seed)
                .queryParam("from", from)
                .queryParam("to", to)
                .queryParam("output", "json");
        RestTemplate restTemplate = restTemplateBuilder.build();
        URI uri = uriComponentsBuilder.buildAndExpand().toUri();
        String jsonText = restTemplate.getForObject(uri, String.class);
        if (jsonText == null) {
            return null;
        }
        String[] jsonLines = jsonText.split("\n");

        for (String line : jsonLines) {
            JSONObject obj = new JSONObject(line);
            CDXResponse cdx = new CDXResponse();
            cdx.setUrl(obj.getString("url"));
            cdx.setTimestamp(obj.getString("timestamp"));
            cdx.setWarcName(obj.getString("filename"));
            if (StringUtils.equals(seed, cdx.getUrl()) && mappedWarcFiles.containsKey(cdx.getWarcName())) {
                return cdx.getTimestamp();
            }
        }
        return null;
    }

    public void setWctClient(WctCoordinatorClient wctClient) {
        this.wctClient = wctClient;
    }

    public void setRootStorePath(String rootStorePath) {
        this.rootStorePath = rootStorePath;
    }

    public void setPywbManagerColl(String pywbManagerColl) {
        this.pywbManagerColl = pywbManagerColl;
    }

    public void setPywbManagerStoreDir(File pywbManagerStoreDir) {
        this.pywbManagerStoreDir = pywbManagerStoreDir;
    }

    public void setPywbCDXQueryUrl(String pywbCDXQueryUrl) {
        this.pywbCDXQueryUrl = pywbCDXQueryUrl;
    }

    public void setMaxTrySeconds(long maxTrySeconds) {
        this.maxTrySeconds = maxTrySeconds;
    }

    public void setPywbEnabled(boolean pywbEnabled) {
        this.pywbEnabled = pywbEnabled;
    }
}

class CDXResponse {
    private String url;
    private String timestamp;
    private String warcName;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getWarcName() {
        return warcName;
    }

    public void setWarcName(String warcName) {
        this.warcName = warcName;
    }
}