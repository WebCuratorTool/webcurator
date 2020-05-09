package org.webcurator.core.networkmap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PruneAndImportController implements PruneAndImportService {

    //For store component, it's a localClient; For webapp component, it's a remote component
    @Autowired
    private PruneAndImportService client;

    @Override
    @RequestMapping(path = PruneAndImportServicePath.PATH_UPLOAD_FILE, method = RequestMethod.POST)
    public PruneAndImportCommandRowMetadata uploadFile(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("fileName") String fileName, @RequestParam("replaceFlag") boolean replaceFlag, @RequestBody byte[] doc) {
        return client.uploadFile(job, harvestResultNumber, fileName, replaceFlag, doc);
    }

    @Override
    @RequestMapping(path = PruneAndImportServicePath.PATH_DOWNLOAD_FILE, method = {RequestMethod.GET, RequestMethod.POST})
    public PruneAndImportCommandRow downloadFile(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("fileName") String fileName) {
        return client.downloadFile(job, harvestResultNumber, fileName);
    }

    @Override
    @RequestMapping(path = PruneAndImportServicePath.PATH_CHECK_FILES, method = RequestMethod.POST)
    public PruneAndImportCommandResult checkFiles(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestBody List<PruneAndImportCommandRowMetadata> items) {
        return client.checkFiles(job, harvestResultNumber, items);
    }

    @Override
    @RequestMapping(path = PruneAndImportServicePath.PATH_APPLY_PRUNE_IMPORT, method = RequestMethod.POST)
    public PruneAndImportCommandResult pruneAndImport(@RequestParam("job") long job, @RequestParam("harvestResultNumber") int harvestResultNumber, @RequestParam("newHarvestResultNumber") int newHarvestResultNumber, @RequestBody List<PruneAndImportCommandRowMetadata> dataset) {
        return client.pruneAndImport(job, harvestResultNumber, newHarvestResultNumber, dataset);
    }
}
