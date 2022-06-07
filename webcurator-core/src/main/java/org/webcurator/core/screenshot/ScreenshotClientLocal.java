package org.webcurator.core.screenshot;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.domain.model.core.SeedHistory;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
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


    @Override
    public Boolean createScreenshots(ScreenshotIdentifierCommand identifiers) throws DigitalAssetStoreException {
        // Can continue with the harvest without taking a screenshot
        if (!enableScreenshots) return true;

        if (identifiers == null) {
            return false;
        }

        Boolean screenshotsSucceeded = Boolean.FALSE;
        try {
            for (SeedHistory seed : identifiers.getSeeds()) {
                screenshotsSucceeded = screenshotGenerator.createScreenshots(seed, identifiers.getTiOid(), identifiers.getScreenshotType(), identifiers.getHarvestNumber(), identifiers.getTimestamp());
                if (!screenshotsSucceeded) {
                    break;
                }
            }
            screenshotsSucceeded = Boolean.TRUE;
        } catch (Exception e) {
            log.error("Failed to create screenshot.", e);
        }

        if (!abortHarvestOnScreenshotFailure) return true;

        // Delete temporary screenshot directory if the screenshot didn't succeed and the harvest has been aborted
        if (!screenshotsSucceeded) {
            cleanUpDirOnAbort(new File(baseDir + File.separator + identifiers.getTiOid()));
        }

        return screenshotsSucceeded;
    }

    // The wayback banner may be problematic when getting full page screenshots, check against the live image dimensions
    // Allow some space for the wayback banner
    private void checkFullpageScreenshotSize(String outputPath, String filename, File liveImageFile, String url) {
        try {
            BufferedImage liveImage = ImageIO.read(liveImageFile);
            int liveImageWidth = liveImage.getWidth();
            int liveImageHeight = liveImage.getHeight();
            liveImage.flush();

            // Only proceed if harvested fullpage image is smaller than live fullpage image
            BufferedImage harvestedImage = ImageIO.read(new File(outputPath + File.separator + filename));
            if (harvestedImage.getWidth() >= liveImageWidth && harvestedImage.getHeight() >= liveImageHeight) {
                harvestedImage.flush();
                return;
            }

            String windowSizeCommand = screenshotCommandWindowSize
                    .replace("%width%", String.valueOf(liveImageWidth))
                    .replace("%height%", String.valueOf(liveImageHeight + 150))
                    .replace("%url%", url)
                    .replace("%image.png%", outputPath + File.separator + filename);


            log.info("Harvested full page screenshot is smaller than live full page screenshot.  " +
                    "Getting a new screenshot using live image dimensions, command " + windowSizeCommand);

            // Delete the old harvested fullpage image and replace it with one with new dimensions
            File toDelete = new File(outputPath + File.separator + filename);
            if (toDelete.delete()) {
                runCommand(windowSizeCommand);
                waitForScreenshot(toDelete, filename);
                log.info("Fullpage screenshot of harvest replaced.");
            } else {

                log.info("Unable to replace harvest fullpage screenshot.");
            }
            harvestedImage.flush();
        } catch (Exception e) {
            log.error("Failed to resize fullpage harvest screenshot: " + e.getMessage(), e);
        }
    }

    private void waitForScreenshot(File file, String filename) {
        try {
            for (int i = 0; i < 5; i++) {
                if (file.exists()) return;
                log.info(filename + " has not been created yet.  Waiting...");
                Thread.sleep(10000);
            }
            log.info("Timed out waiting for file creation.");
        } catch (Exception e) {
            log.error("The thread was interrupted:", e);
        }
    }

    private void runCommand(String command) {
        try {
            String harvestAgentH3SourceDir = "webcurator-harvest-agent-h3";
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            if (command.contains("SeleniumScreenshotCapture")) {
                String processDir = System.getProperty("user.dir");
                if (processDir.contains(harvestAgentH3SourceDir)) {
                    processDir = processDir.substring(0, processDir.indexOf(harvestAgentH3SourceDir));
                }
                processDir = processDir + File.separator + harvestAgentH3SourceDir + File.separator
                        + "build" + File.separator + "classes" + File.separator + "java" + File.separator + "main";
                processBuilder.directory(new File(processDir).getAbsoluteFile());
            }
            Process process = processBuilder.start();
        } catch (Exception e) {
            log.error("Unable to process command " + command, e);
        }
    }

    private void generateThumbnailOrScreenSizeScreenshot(String inputFilename, String outputPathString,
                                                         String inputSize, String outputSize, int width, int height) {
        log.info("Generating " + outputSize + " screenshot...");
        try {
            BufferedImage sourceImage = ImageIO.read(new File(outputPathString + File.separator + inputFilename));
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Image scaledImage = sourceImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            bufferedImage.createGraphics().drawImage(scaledImage, 0, 0, null);
            BufferedImage thumbnailImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            thumbnailImage = bufferedImage.getSubimage(0, 0, width, height);
            ImageIO.write(thumbnailImage, "png", new File(outputPathString + File.separator
                    + inputFilename.replace(inputSize, outputSize)));
            sourceImage.flush();
            bufferedImage.flush();
            thumbnailImage.flush();
        } catch (Exception e) {
            log.error("Unable to generate " + outputSize + " thumbnail.");
        }
    }


    private void renameLiveFile(String liveDirectory, String outputDirectory, String seed, String harvestNumber, String filename) {
        File fullpageLiveFilePath = new File(liveDirectory + filename.replace("harvested", "live"));
        if (!fullpageLiveFilePath.exists()) return;
        if (harvestNumber == null) return;
        if (seed == null) return;
        String newFilename = outputDirectory + filename.replace("seedID", seed).replace("harvestNum", harvestNumber);
        newFilename = newFilename.replace("harvested", "live");
        if (!fullpageLiveFilePath.renameTo(new File(newFilename))) {
            log.error("Unable to rename live file to include harvest number and seed.  File: " + filename);
        }
    }

    private boolean checkDirOnlyOneFile(File dir, String expectedDir) {
        if (Objects.requireNonNull(dir.list()).length == 0) return true;

        File[] dirFiles = dir.listFiles();

        // Return false if the directory contains more than 1 file
        if (dirFiles != null && dirFiles.length > 1) {
            log.info("There are more files in " + dir.toString() + " than the " + expectedDir + " directory.");
            return false;
        }

        // Return false if the child directory isn't the expected directory
        if (!Objects.requireNonNull(dirFiles)[0].getName().equals(expectedDir)) {
            log.info("There is an unexpected file in " + dir.toString());
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
                log.info("Unable to delete " + f.toString());
                return;
            }
        }

        // Delete _resources directory then tmpDir then harvest directory
        if (!resourcesDir.delete()) {
            log.info("Could not delete " + resourcesDir.toString());
            return;
        }
        if (!tmpDir.delete()) {
            log.info("Could not delete " + tmpDir.toString());
            return;
        }
        if (!harvestBaseDir.delete()) {
            log.info("Could not delete " + harvestBaseDir.toString());
        }
    }

    // Delete everything in _resources file then resources file then harvest file
    private void cleanUpTmpDir(File harvestTmpDir) {
        File tmpDir = harvestTmpDir.getParentFile();
        if (harvestTmpDir.exists()) {

            // Delete all files in all directories
            for (File file : Objects.requireNonNull(harvestTmpDir.listFiles())) {
                if (!file.delete()) {
                    log.info("Unable to delete file: " + file.getAbsolutePath());
                }
            }
            if (Objects.requireNonNull(harvestTmpDir.list()).length != 0) {
                log.info("Not all files have been removed from " + harvestTmpDir.toString());
                return;
            }
            if (!harvestTmpDir.delete()) {
                log.info("Unable to delete temporary screenshot directory.");
                return;
            }
            if (Objects.requireNonNull(tmpDir.list()).length > 0) {
                log.info("There are still files within " + tmpDir.toString());
                return;
            }
            if (!tmpDir.delete()) {
                log.info("Unable to delete " + tmpDir.getAbsolutePath());
            }
        }
    }

    @Override
    public void browseScreenshotImage(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        String imgPath = req.getRequestURI();
        File imgFilePath = new File(this.baseDir, imgPath);
        rsp.setContentType("image/png");
        IOUtils.copy(Files.newInputStream(imgFilePath.toPath()), rsp.getOutputStream());
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


    public void setScreenshotCommandWindowSize(String screenshotCommandWindowSize) {
        this.screenshotCommandWindowSize = screenshotCommandWindowSize;
    }
}
