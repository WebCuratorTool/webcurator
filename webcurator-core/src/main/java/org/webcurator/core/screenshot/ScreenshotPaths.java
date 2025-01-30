package org.webcurator.core.screenshot;

import java.io.File;

public class ScreenshotPaths {
    public static final String ROOT_PATH = "/screenshot";
    public static final String CREATE_SCREENSHOT = ROOT_PATH + "/create-screenshot";
    public static final String BROWSE_SCREENSHOT = ROOT_PATH + "/browse-screenshot";
    public static final String CHECK_SCREENSHOT_STATE = ROOT_PATH + "/check-screenshot-state";

    private final static String SCREENSHOT_FOLDER = "_snapshots";

    public static String getImageName(long tiOid, int harvestNumber, String seedOid, ScreenshotType liveOrHarvested, String suffix) {
        return String.format("%d_%d_%s_%s_%s.png", tiOid, harvestNumber, seedOid, liveOrHarvested.name(), suffix);
    }

    public static String getImagePath(long tiOid, int harvestNumber) {
        return tiOid + File.separator + harvestNumber + File.separator + SCREENSHOT_FOLDER;
    }
}
