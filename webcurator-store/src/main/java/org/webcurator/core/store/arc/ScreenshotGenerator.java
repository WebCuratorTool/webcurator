package org.webcurator.core.store.arc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import java.util.Map;

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
    private void checkFullpageScreenshotSize(String outputPath, String filename, File liveImageFile, String url) {
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
                if (toDelete.exists()) {
                    log.info("Fullpage screenshot of harvest replaced.");
                } else {
                    throw new Exception("Unable to replace fullpage harvest screenshot.");
                }
            } else {
                throw new Exception("Unable to delete harvest fullpage screenshot for replacement.");
            }
        } catch (Exception e) {
            log.error("Failed to resize fullpage harvest screenshot: " + e.getMessage(), e);
        } finally {
            if (harvestedImage != null) {
                harvestedImage.flush();
            }
        }
    }

    private boolean runCommand(String command) {
        log.info("Running command " + command);
        try {
            List<String> commandList = Arrays.asList(command.split(" "));

/*            // Add java class path
            for (String arg : commandList) {
                if (arg.equals("-cp") || arg.equals("-classpath")) {
                    int index = commandList.indexOf(arg) + 1;
                    commandList.set(index, commandList.get(index) + ":" + "\"" + System.getProperty("java.class.path") + "\"");
                }
            }*/

            ProcessBuilder processBuilder = new ProcessBuilder(commandList);

            // Command output gets printed to the same place as the application console output
            Process process = processBuilder.inheritIO().start();
            int processStatus = process.waitFor();

            if (processStatus != 0) {
                log.info("Process ended with a fail status: " + processStatus);
                return false;
            }
        } catch (Exception e) {
            log.error("Unable to process command " + command, e);
            return false;
        }
        return true;
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
            log.error("Unable to generate " + inputSize + " to " + outputSize + " screenshot.");
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

    public Boolean createScreenshots(Map identifiers, String baseDir, String screenshotCommandFullpage,
                                     String screenshotCommandWindowsize, String screenshotCommandScreen,
                                     String harvestWaybackViewerBaseUrl) {
        if (identifiers == null || identifiers.keySet().size() < 1) {
            log.info("No arguments available for the screenshot.");
            return false;
        }

        // file naming convention: ti_harvest_seedId_source_tool.png
        String seedUrl = String.valueOf(identifiers.get("seed"));
        String targetInstanceOid = String.valueOf(identifiers.get("tiOid"));
        String liveOrHarvested = String.valueOf(identifiers.get("liveOrHarvested"));
        String seedId = String.valueOf(identifiers.get("seedOid"));
        String harvestNumber = String.valueOf(identifiers.get("harvestNumber"));
        String outputPathString = baseDir.toString() + File.separator + targetInstanceOid + File.separator + harvestNumber + File.separator;
        String nullDirectoryString = baseDir.toString() + File.separator + targetInstanceOid + File.separator + "null" + File.separator;
        String toolUsed = screenshotCommandFullpage.split("\\s+")[0];

        // Make sure output path exists
        File destinationDir = new File(outputPathString);
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        // If using a java class, use the class name
        if (screenshotCommandFullpage.contains("SeleniumScreenshotCapture")) {
            toolUsed = "default";
        }

        // Get the name of the tool used to get the screenshot
        if (toolUsed.contains(File.separator)) toolUsed = toolUsed.substring(toolUsed.lastIndexOf(File.separator) + 1);

        String fullpageFilename =  targetInstanceOid + "_harvestNum_seedID_" + liveOrHarvested + "_" + toolUsed.toLowerCase() + "_fullpage.png";

        // Need to move the live screenshots and use the wayback indexed url instead of the seed url
        if (liveOrHarvested.equals("harvested")) {
            // Check if live screenshots exist in null directory
            for (String size : new String[]{"fullpage","screen","thumbnail"}){
                renameLiveFile(nullDirectoryString, outputPathString, seedId, harvestNumber, fullpageFilename.replace("fullpage", size));
            }
            // Delete the null directory if it's empty after all files have been renamed and moved
            File liveDirectory = new File(nullDirectoryString);
            if (liveDirectory.isDirectory() && liveDirectory.list().length == 0) {
                if (!liveDirectory.delete()) {
                    log.info("Unable to delete null directory.");
                }
            }

            // Get timestamp from warc file and use in harvest seed url
            File harvestDirectory = new File(outputPathString);
            String tsArg = String.valueOf(identifiers.get("timestamp"));

            for (String fileString : harvestDirectory.list()) {
                if (identifiers.get("timestamp") == null || identifiers.get("timestamp").equals("null")) {
                    log.info("No valid timestamp to use");
                    break;
                }

                if (!fileString.endsWith(".warc")) continue;
                if (!fileString.contains(tsArg)) continue;

                int tsIndex = fileString.indexOf(tsArg);
                String timestamp = fileString.substring(tsIndex, tsIndex + 14);

                seedUrl = harvestWaybackViewerBaseUrl + timestamp + "/" + seedUrl;

                log.info("Using harvest url " + seedUrl + " to generate screenshots.");
                break;
            }
        }

        if (identifiers.get("seedOid") != null && !seedId.equals("null")) {
            fullpageFilename = fullpageFilename.replace("seedID", seedId);
        }
        if (identifiers.get("harvestNumber") != null && !harvestNumber.equals("null")) {
            fullpageFilename = fullpageFilename.replace("harvestNum", harvestNumber);
        }

        String screenFilename = fullpageFilename.replace("fullpage", "screen");
        String imagePlaceholder = "%image.png%";
        String urlPlaceholder = "%url%";

        String commandFullpage = screenshotCommandFullpage
                .replace(urlPlaceholder, seedUrl.replaceAll("\\s+",""))
                .replace(imagePlaceholder, outputPathString + fullpageFilename);
        String commandScreen = screenshotCommandScreen
                .replace(urlPlaceholder, seedUrl.replaceAll("\\s+",""))
                .replace(imagePlaceholder, outputPathString + screenFilename);

        log.info("Generating screenshots for job " + targetInstanceOid + " using " + toolUsed + "...");

        try {
            ScreenshotGenerator screenshotGenerator = new ScreenshotGenerator(log, screenshotCommandWindowsize, screenshotCommandScreen, screenshotCommandFullpage);

            // Generate fullpage screenshots only if live or not using the default SeleniumScreenshotCapture executable for harvested screenshot
            // The size of harvested screenshots will be compared next
            if (liveOrHarvested.equals("live") || !commandFullpage.contains("SeleniumScreenshotCapture")) {
                if (screenshotGenerator.runCommand(commandFullpage)) {
                    screenshotGenerator.waitForScreenshot(new File(outputPathString + fullpageFilename), fullpageFilename);
                } else {
                    log.error("Unable to run command " + commandFullpage);
                    return false;
                }
            }

            File liveImageFile = new File(outputPathString + File.separator + fullpageFilename.replace("harvested", "live"));
            if (liveOrHarvested.equals("harvested") && liveImageFile.exists()) {
                String commandWaybackFullpage = screenshotCommandWindowsize
                        .replace(urlPlaceholder, seedUrl.replaceAll("\\s+", ""))
                        .replace(imagePlaceholder, outputPathString + fullpageFilename);
                if (commandWaybackFullpage.contains("SeleniumScreenshotCapture")) {
                    commandWaybackFullpage = commandWaybackFullpage.substring(0, commandWaybackFullpage.indexOf("width=")) + "--wayback";
                    if (screenshotGenerator.runCommand(commandWaybackFullpage)) {
                        screenshotGenerator.waitForScreenshot(new File(outputPathString + fullpageFilename), fullpageFilename);
                    }
                    // For non-default screenshot tools check the fullpage screenshot image size against the harvested screenshots
                } else {
                    screenshotGenerator.checkFullpageScreenshotSize(outputPathString, fullpageFilename, liveImageFile, seedUrl);
                }
            } else if (liveOrHarvested.equals("harvested") && !liveImageFile.exists()) {
                log.info("Live image file does not exist, nothing to compare against.");
            }

            // Generate the screen sized screenshot
            if (liveOrHarvested.equals("harvested") && commandScreen.contains("SeleniumScreenshotCapture")) {
                commandScreen = commandScreen.trim() + " --wayback";
            }
            if (screenshotGenerator.runCommand(commandScreen)) {
                screenshotGenerator.waitForScreenshot(new File(outputPathString + screenFilename), screenFilename);
            }
            // Generate thumbnail from fullpage screenshot if not using the default screenshot tool
            if (!commandScreen.contains("SeleniumScreenshotCapture")) {
                screenshotGenerator.generateThumbnailOrScreenSizeScreenshot(screenFilename, outputPathString, "screen",
                        "thumbnail",100, 100);
            }

            File dir = new File(outputPathString);
            int imageCounter = 0;
            for (File file : dir.listFiles()) {
                if (file.toString().toLowerCase().endsWith(".png") && file.toString().contains(liveOrHarvested)) {
                    imageCounter++;
                }
            }
            if (imageCounter == 3) {
                log.info("Screenshots generated.");
            } else {
                log.info("Screenshot files are missing.");
            }

        } catch (Exception e) {
            log.error("Failed to generate screenshots: " + e.getMessage(), e);
            return false;
        }
        return true;
    }
}
