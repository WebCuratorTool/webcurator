package org.webcurator.core.screenshot;

import org.webcurator.core.exceptions.DigitalAssetStoreException;

public interface ScreenshotService {
    /**
     * Create live or screenshots for each seed in the harvest
     *
     * @param identifiers
     * @return true if successfully created screenshots or screenshots disabled
     * @throws DigitalAssetStoreException
     */
    Boolean createScreenshots(ScreenshotIdentifierCommand identifiers) throws DigitalAssetStoreException;
}
