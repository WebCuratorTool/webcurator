package org.webcurator.visualization.app;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class VisualizationServiceController {
    // The base directory for the arc store
    @Value("${arcDigitalAssetStoreService.baseDir}")
    private String arcDigitalAssetStoreServiceBaseDir;

    @Value("${qualityReviewToolController.archiveUrl}")
    private String openWayBack;

    @Autowired
    private NetworkMapClient networkMapClient;

    final private AtomicLong hrOid = new AtomicLong(0);

    @RequestMapping(path = "/curator/vis/all_hr_results", method = {RequestMethod.POST, RequestMethod.GET})
    public List<VisTargetInstance> getAllHarvestResults() {
        return this.listTargetInstances();
    }

    private List<VisTargetInstance> listTargetInstances() {
        final List<VisTargetInstance> tiList = new ArrayList<>();

        File tiDirectories = new File(arcDigitalAssetStoreServiceBaseDir);
        if (!tiDirectories.exists()) {
            return tiList;
        }

        Arrays.asList(Objects.requireNonNull(tiDirectories.listFiles())).forEach(tiDir -> {
            if (tiDir.isDirectory() && NumberUtils.isNumber(tiDir.getName())) {
                VisTargetInstance visTi = new VisTargetInstance();
                visTi.setTiId(Long.parseLong(tiDir.getName()));
                List<VisHarvestResult> hrList = this.listHarvestResults(tiDir);
                visTi.setHrList(hrList);
                tiList.add(visTi);
            }
        });

        return tiList;
    }

    private List<VisHarvestResult> listHarvestResults(File hrDirectories) {
        final List<VisHarvestResult> hrList = new ArrayList<>();

        if (hrDirectories == null || !hrDirectories.exists()) {
            return hrList;
        }
        Arrays.asList(Objects.requireNonNull(hrDirectories.listFiles())).forEach(hrDir -> {
            if (hrDir.isDirectory() && NumberUtils.isNumber(hrDir.getName())) {
                VisHarvestResult visHr = new VisHarvestResult();
                visHr.setHrId(hrOid.incrementAndGet());
                visHr.setHrNumber(Integer.parseInt(hrDir.getName()));

                File indexDir = new File(hrDir, "_resource");
                visHr.setIndexed(indexDir.exists());
                hrList.add(visHr);
            }
        });
        return hrList;
    }
}
