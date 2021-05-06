package org.webcurator.core.store.arc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;


final class ScreenshotGenerator {
    Logger log;
    String windowSizeCommand, screenSizeCommand, fullpageSizeCommand;

    public ScreenshotGenerator(Logger log, String windowSizeCommand, String screenSizeCommand, String fullpageSizeCommand) {
        this.log = log;
        this.windowSizeCommand = windowSizeCommand;
        this.screenSizeCommand = screenSizeCommand;
        this.fullpageSizeCommand = fullpageSizeCommand;
    }

    public void waitForScreenshot(File file, String filename) {
        try {
            for (int i = 0; i < 5; i++) {
                if (file.exists()) return;
                log.info(filename + " has not been created yet.  Waiting...");
                Thread.sleep(10000);
            }
            log.info("Timed out waiting for file creation.");
        } catch (Exception e) {
        }
    }

    // The wayback banner may be problematic when getting full page screenshots, check against the live image dimensions
    // Allow some space for the wayback banner
    public void checkFullpageScreenshotSize(String outputPath, String filename, File liveImageFile, String url) {
        BufferedImage harvestedImage = null;
        try {
            BufferedImage liveImage = ImageIO.read(liveImageFile);
            int liveImageWidth = liveImage.getWidth();
            int liveImageHeight = liveImage.getHeight();
            liveImage.flush();

            // Only proceed if harvested fullpage image is smaller than live fullpage image
            harvestedImage = ImageIO.read(new File(outputPath + filename));
            if (harvestedImage.getWidth() >= liveImageWidth && harvestedImage.getHeight()>= liveImageHeight) {
                harvestedImage.flush();
                return;
            }

            String windowsizeCommand = windowSizeCommand
                    .replace("%width%", String.valueOf(liveImageWidth))
                    .replace("%height%", String.valueOf(liveImageHeight + 150))
                    .replace("%url%", url)
                    .replace("%image.png%", outputPath + filename);


            log.info("Harvested full page screenshot is smaller than live full page screenshot.  " +
                    "Getting a new screenshot using live image dimensions, command " + windowsizeCommand);

            // Delete the old harvested fullpage image and replace it with one with new dimensions
            File toDelete = new File(outputPath + File.separator + filename);
            if (toDelete.delete()) {
                if (!runCommand(windowsizeCommand)) {
                    log.info("Unable to run command to generate screenshot.");
                    harvestedImage.flush();
                    return;
                }
                waitForScreenshot(toDelete, filename);
                log.info("Fullpage screenshot of harvest replaced.");
            } else {
                log.info("Unable to replace harvest fullpage screenshot.");
            }
        } catch (Exception e) {
            log.error("Failed to resize fullpage harvest screenshot: " + e.getMessage(), e);
        } finally {
            if (harvestedImage != null) {
                harvestedImage.flush();
            }
        }
    }

    public boolean runCommand(String command) {
        log.info("Running command " + command);
        try {
            List<String> commandList = Arrays.asList(command.split(" "));

            // Add java class path
            for (String arg : commandList) {
                if (arg.equals("-cp") || arg.equals("-classpath")) {
                    int index = commandList.indexOf(arg) + 1;
                    commandList.set(index, commandList.get(index) + ":" + "\"" + System.getProperty("java.class.path") + "\"");
                }
            }

            ProcessBuilder processBuilder = new ProcessBuilder(commandList);

            // Command output gets printed to the same place as the application console output
            Process process = processBuilder.inheritIO().start();
            if (process.waitFor() != 0) {
                return false;
            }
        } catch (Exception e) {
            log.error("Unable to process command " + command, e);
            return false;
        }
        return true;
    }

    public void generateThumbnailOrScreenSizeScreenshot(String inputFilename, String outputPathString,
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
            log.error("Unable to generate " + inputSize + " to " + outputSize + " screenshot.");
        }
    }
}
