package org.webcurator.core.harvester.coordinator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.webcurator.core.coordinator.WctCoordinatorPaths;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.util.WctUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
public class HarvestStoreDownloadController {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @RequestMapping(path = WctCoordinatorPaths.DOWNLOAD, method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/octet-stream")
    public void externalDownload(@RequestParam("filePath") String filePath,
                                 HttpServletRequest req,
                                 HttpServletResponse rsp) throws DigitalAssetStoreException {
        log.debug("Get file download request, filePath: {}", filePath);

        File file = new File(filePath);
        try {
            WctUtils.copy(Files.newInputStream(file.toPath()), rsp.getOutputStream());
        } catch (IOException e) {
            log.error("Failed to copy file to stream, file: {}", file.getAbsolutePath());
            throw new DigitalAssetStoreException(e);
        }
    }
}
