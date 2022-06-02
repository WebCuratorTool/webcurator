package org.webcurator.core.screenshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.store.DigitalAssetStorePaths;

@RestController
public class ScreenshotController implements ScreenshotService {
    private static final Logger log = LoggerFactory.getLogger(ScreenshotController.class);
    @Autowired
    private ScreenshotClient screenshotClient;

    @Override
    @PostMapping(path = DigitalAssetStorePaths.CREATE_SCREENSHOT)
    public Boolean createScreenshots(@RequestBody ScreenshotIdentifierCommand identifiers) throws DigitalAssetStoreException {
        log.debug("Create screenshot: {}", identifiers);
        return screenshotClient.createScreenshots(identifiers);
    }
}
