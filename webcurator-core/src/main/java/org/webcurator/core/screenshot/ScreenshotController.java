package org.webcurator.core.screenshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.webcurator.core.exceptions.DigitalAssetStoreException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class ScreenshotController implements ScreenshotService {
    private static final Logger log = LoggerFactory.getLogger(ScreenshotController.class);
    @Autowired
    private ScreenshotClient screenshotClient;

    @Override
    @PostMapping(path = ScreenshotPaths.CREATE_SCREENSHOT)
    public Boolean createScreenshots(@RequestBody ScreenshotIdentifierCommand identifiers) throws DigitalAssetStoreException {
        log.debug("Create screenshot: {}", identifiers);
        return screenshotClient.createScreenshots(identifiers);
    }

    @Override
    @GetMapping(path = ScreenshotPaths.BROWSE_SCREENSHOT+"/**")
    public void browseScreenshotImage(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        screenshotClient.browseScreenshotImage(req, rsp);
    }
}
