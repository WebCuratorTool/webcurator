package org.webcurator.core.screenshot;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.domain.model.core.SeedHistoryDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class ScreenshotClientLocal implements ScreenshotClient {
    private static final Logger log = LoggerFactory.getLogger(ScreenshotClientLocal.class);

    private boolean enableScreenshots;
    private boolean abortHarvestOnScreenshotFailure;
    private ScreenshotGenerator screenshotGenerator;
    private String baseDir;


    /**
     * the window size screenshot command.
     */
    private String screenshotCommandWindowSize;

    private byte[] unavailableImageThumbnail = new byte[0];
    private byte[] unavailableImageScreen = new byte[0];

    @Override
    public Boolean createScreenshots(ScreenshotIdentifierCommand identifiers) throws DigitalAssetStoreException {
        // Can continue with the harvest without taking a screenshot
        if (!enableScreenshots) return true;
        if (identifiers == null) {
            return false;
        }

        boolean screenshotsSucceeded = screenshotGenerator.createScreenshots(identifiers);

        if (!abortHarvestOnScreenshotFailure) return true;

        // Delete temporary screenshot directory if the screenshot didn't succeed and the harvest has been aborted
        if (!screenshotsSucceeded) {
            cleanUpDirOnAbort(new File(baseDir + File.separator + identifiers.getTiOid()));
        }

        return screenshotsSucceeded;
    }


    private boolean checkDirOnlyOneFile(File dir, String expectedDir) {
        if (Objects.requireNonNull(dir.list()).length == 0) return true;

        File[] dirFiles = dir.listFiles();

        // Return false if the directory contains more than 1 file
        if (dirFiles != null && dirFiles.length > 1) {
            log.info("There are more files in " + dir + " than the " + expectedDir + " directory.");
            return false;
        }

        // Return false if the child directory isn't the expected directory
        if (!Objects.requireNonNull(dirFiles)[0].getName().equals(expectedDir)) {
            log.info("There is an unexpected file in " + dir);
            return false;
        }

        return true;
    }

    private void cleanUpDirOnAbort(File harvestBaseDir) {
        if (!harvestBaseDir.exists()) return;

        // Harvest base dir should only contain tmpDir, do not delete otherwise
        if (!checkDirOnlyOneFile(harvestBaseDir, "tmpDir")) return;

        // tmpDir should only contain _resources, do not delete otherwise
        File tmpDir = Objects.requireNonNull(harvestBaseDir.listFiles())[0];
        if (!checkDirOnlyOneFile(tmpDir, "_resources")) return;

        File resourcesDir = Objects.requireNonNull(tmpDir.listFiles())[0];
        // Delete all files in _resources
        for (File f : Objects.requireNonNull(resourcesDir.listFiles())) {
            if (!f.delete()) {
                log.info("Unable to delete " + f);
                return;
            }
        }

        // Delete _resources directory then tmpDir then harvest directory
        if (!resourcesDir.delete()) {
            log.info("Could not delete " + resourcesDir);
            return;
        }
        if (!tmpDir.delete()) {
            log.info("Could not delete " + tmpDir);
            return;
        }
        if (!harvestBaseDir.delete()) {
            log.info("Could not delete " + harvestBaseDir);
        }
    }

    @Override
    public void browseScreenshotImage(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        String imgPath = req.getRequestURI();
        String imgContext = req.getContextPath();
        imgPath = imgPath.substring(imgContext.length());
        imgPath = imgPath.substring(ScreenshotPaths.BROWSE_SCREENSHOT.length());
        rsp.setContentType("image/png");
        File imgFilePath = new File(this.baseDir, imgPath);
        if (imgFilePath.exists()) {
            IOUtils.copy(Files.newInputStream(imgFilePath.toPath()), rsp.getOutputStream());
        } else {
            if (imgFilePath.getName().contains("thumbnail")) {
                IOUtils.write(unavailableImageThumbnail, rsp.getOutputStream());
            } else {
                IOUtils.write(unavailableImageScreen, rsp.getOutputStream());
            }
        }
    }

    @Override
    public ScreenshotState checkScreenshotState(ScreenshotIdentifierCommand identifiers) throws DigitalAssetStoreException {
        ScreenshotState ret = new ScreenshotState();
        ret.setIndexAvailable(screenshotGenerator.checkIndexState(identifiers));
        ret.setLiveScreenshot(this.isScreenshotOfSeedsAvailable(identifiers, ScreenshotType.live));
        ret.setHarvestedScreenshot(this.isScreenshotOfSeedsAvailable(identifiers, ScreenshotType.harvested));
        return ret;
    }

    private boolean isScreenshotOfSeedsAvailable(ScreenshotIdentifierCommand identifiers, ScreenshotType liveOrHarvested) {
        for (SeedHistoryDTO seed : identifiers.getSeeds()) {
            int count = this.countOfImages(identifiers.getTiOid(), identifiers.getHarvestNumber(), seed.getOid(), liveOrHarvested);
            if (count < 2) {
                return false;
            }
        }
        return true;
    }

    private int countOfImages(long tiOid, int harvestNumber, long seedOid, ScreenshotType liveOrHarvested) {
        File imgPath = new File(baseDir, ScreenshotPaths.getImagePath(tiOid, harvestNumber));
        String prefix = String.format("%d_%d_%s_%s", tiOid, harvestNumber, seedOid, liveOrHarvested.name());
        String[] fileNames = imgPath.list();
        if (fileNames == null) {
            return 0;
        }
        int count = 0;
        for (String fileName : fileNames) {
            if (fileName.startsWith(prefix)) {
                count++;
            }
        }
        return count;
    }

    public void setEnableScreenshots(boolean enableScreenshots) {
        this.enableScreenshots = enableScreenshots;
    }

    public void setAbortHarvestOnScreenshotFailure(boolean abortHarvestOnScreenshotFailure) {
        this.abortHarvestOnScreenshotFailure = abortHarvestOnScreenshotFailure;
    }

    public void setScreenshotGenerator(ScreenshotGenerator screenshotGenerator) {
        this.screenshotGenerator = screenshotGenerator;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }


    public void setUnavailableImageThumbnail(byte[] unavailableImageThumbnail) {
        this.unavailableImageThumbnail = unavailableImageThumbnail;
    }

    public void setUnavailableImageScreen(byte[] unavailableImageScreen) {
        this.unavailableImageScreen = unavailableImageScreen;
    }


    public void setScreenshotCommandWindowSize(String screenshotCommandWindowSize) {
        this.screenshotCommandWindowSize = screenshotCommandWindowSize;
    }
}
