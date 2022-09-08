package org.webcurator.core.screenshot;

import org.webcurator.core.exceptions.DigitalAssetStoreException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ScreenshotService {
    /**
     * Create live or screenshots for each seed in the harvest
     *
     * @param identifiers
     * @return true if successfully created screenshots or screenshots disabled
     * @throws DigitalAssetStoreException
     */
    Boolean createScreenshots(ScreenshotIdentifierCommand identifiers) throws DigitalAssetStoreException;

    //    void browseScreenshotImage(long  tiOid, int harvestNumber, long seedOid);
    void browseScreenshotImage(HttpServletRequest req, HttpServletResponse rsp) throws IOException;
}
