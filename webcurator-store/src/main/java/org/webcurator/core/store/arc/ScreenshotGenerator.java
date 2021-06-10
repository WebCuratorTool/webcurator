package org.webcurator.core.store.arc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ScreenshotGenerator {
    private static Log log = LogFactory.getLog(ScreenshotGenerator.class);
    String windowSizeCommand, screenSizeCommand, fullpageSizeCommand;

    public String getFullpageSizeCommand() {
        return fullpageSizeCommand;
    }

    public String getScreenSizeCommand() {
        return screenSizeCommand;
    }

    public String getWindowSizeCommand() {
        return windowSizeCommand;
    }

    public ScreenshotGenerator(String windowSizeCommand, String screenSizeCommand, String fullpageSizeCommand) {
        this.log = log;
        this.windowSizeCommand = windowSizeCommand;
        this.screenSizeCommand = screenSizeCommand;
        this.fullpageSizeCommand = fullpageSizeCommand;
    }

    private void waitForScreenshot(File file) {
        try {
            for (int i = 0; i < 5; i++) {
                if (file.exists()) return;
                log.info(file.getName() + " has not been created yet.  Waiting...");
                Thread.sleep(10000);
            }
            log.info("Timed out waiting for file creation.");
        } catch (Exception e) {
        }
    }

    // The wayback banner may be problematic when getting full page screenshots, check against the live image dimensions
    // Allow some space for the wayback banner
    private boolean checkFullpageScreenshotSize(String outputPath, String filename, File liveImageFile, String url) {
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
                return true;
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
                    return false;
                }
                waitForScreenshot(toDelete);
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
        return true;
    }

    private boolean runCommand(String command) {
        log.info("Running command " + command);
        Thread processThread = null;
        try {
            List<String> commandList = Arrays.asList(command.split(" "));

            final Boolean[] threadFailed = {null};
            processThread = new Thread("processThread") {
                public void run() {
                    ProcessBuilder processBuilder = new ProcessBuilder(commandList);

                    // Command output gets printed to the same place as the application console output
                    try {
                        Process process = processBuilder.inheritIO().start();
                        int processStatus = process.waitFor();

                        if (processStatus != 0) {
                            throw new Exception("Process ended with a fail status: " + processStatus);
                        } else {
                            threadFailed[0] = false;
                        }
                    } catch (Exception e) {
                        log.error("Unable to process the command in a new thread.");
                        threadFailed[0] = true;
                    }
                }
            };

            processThread.start();
            processThread.join();
            processThread.stop();

            if (threadFailed[0]) return false;
        } catch (Exception e) {
            log.error("Unable to process command " + command, e);
            if (processThread != null) {
                processThread.stop();
            }
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

    private String replaceSectionInFilename(String filename, String replacement, int sectionIndex) {
        // File naming convention  ti_harvest_seedId_source_size.png
        String[] filenameSections = filename.split("_");
        filenameSections[sectionIndex] = replacement;
        String result = String.join("_", filenameSections);
        log.debug("Changing filename from " + filename + " to " + result);
        return result;
    }

    private void renameLiveFile(String liveDirectory, String outputDirectory, String harvestNumber, String filename) {
        // Then rename the "harvested" with "live"
        String liveFilename = replaceSectionInFilename(filename, "live", 3);
        File fullpageLiveFilePath = new File(liveDirectory + liveFilename);

        if (!fullpageLiveFilePath.exists()) {
            log.info("Could not find screenshot live file " + liveFilename);
            return;
        }

        if (harvestNumber == null) return;

        // Replace harvest humber in filename and move to permanent directory
        String newFilePath = outputDirectory + replaceSectionInFilename(liveFilename, harvestNumber, 1);
        if (!fullpageLiveFilePath.renameTo(new File(newFilePath))) {
            log.error("Unable to rename live file to include harvest number and seed.  File: " + filename);
        }
    }

    private void deleteTmpDir(String tmpDirectoryString) {
        // Delete the tmp directory if it's empty after all files have been renamed and moved
        File liveDirectory = new File(tmpDirectoryString);
        if (liveDirectory.isDirectory() && liveDirectory.list().length == 0) {
            if (!liveDirectory.delete()) {
                log.info("Unable to delete tmp directory" + tmpDirectoryString);
            }
            File parentDir = new File(liveDirectory.getParent());
            if (!parentDir.delete()) {
                log.info("Unable to delete tmp directory " + parentDir.getAbsolutePath());
            }
        }
    }

    // Returns an empty string when it can't retrieve the timestamp for the url
    private String getWaybackUrl(String seed, String outputPathString, Map identifiers, String waybackBaseUrl) {
        String result = "";
        // Get timestamp from warc file and use with harvest seed url
        File harvestDirectory = new File(new File(outputPathString).getParent());

        if (identifiers.get("timestamp") == null || identifiers.get("timestamp").equals("null")) {
            log.info("No valid timestamp to use");
            return result;
        }

        // warc and index files will contain a timestamp within the filename formatted to YYYYmmdd
        // In case the timing between the harvester collection and the screenshot generation does not match up
        // Search for the year in the filename to generate the wayback timestamp
        // In case the harvesting took place over new  year, search for the prior year as well
        String tsArg = String.valueOf(identifiers.get("timestamp"));
        String year = tsArg.substring(0, tsArg.length() -4);
        for (String fileString : harvestDirectory.list()) {
            if (!fileString.toLowerCase().endsWith(".warc")) continue;
            if (!fileString.contains(year)) {
                year = String.valueOf(Integer.parseInt(year) -1);
            }
            if (!fileString.contains(tsArg) && !fileString.contains(year)) continue;

            int tsIndex = fileString.indexOf(year);
            String timestamp = fileString.substring(tsIndex, tsIndex + 14);

            result = waybackBaseUrl + timestamp + "/" + seed;

            log.info("Using harvest url " + result + " to generate screenshots.");
            break;
        }
        return result;
    }

    private String getScreenshotToolName() {
        // Get the name of the tool used to get the screenshot
        String toolUsed = getFullpageSizeCommand().split("\\s+")[0];
        // If using a default tool, specify as default
        if (getFullpageSizeCommand().contains("SeleniumScreenshotCapture")) {
            toolUsed = "default";
            if (getFullpageSizeCommand().contains("python")) {
                toolUsed = toolUsed + "-python";
            }
            if (getFullpageSizeCommand().contains("java")) {
                toolUsed = toolUsed + "-java";
            }
        }
        if (toolUsed.contains(File.separator)) {
            toolUsed = toolUsed.substring(toolUsed.lastIndexOf(File.separator) + 1);
        }
        return toolUsed;
    }

    private boolean hasAllRequiredIdentifiers(Map identifiers) {
        for (String id : new String[]{"seed", "tiOid", "liveOrHarvested", "seedOid"}) {
            if (identifiers.get(id) == null || String.valueOf(identifiers.get(id)).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public Boolean createScreenshots(Map identifiers, String baseDir, String harvestWaybackViewerBaseUrl) {
        if (identifiers == null || identifiers.keySet().size() < 1) {
            log.info("No arguments available for the screenshot.");
            return false;
        }

        if (!hasAllRequiredIdentifiers(identifiers)) {
            log.info("Missing some required identifiers.");
            return false;
        }

        // file naming convention: ti_harvest_seedId_source_size.png
        String seedUrl = String.valueOf(identifiers.get("seed"));
        String targetInstanceOid = String.valueOf(identifiers.get("tiOid"));
        String liveOrHarvested = String.valueOf(identifiers.get("liveOrHarvested"));
        String seedId = String.valueOf(identifiers.get("seedOid"));
        String harvestNumber = String.valueOf(identifiers.get("harvestNumber"));

        if (harvestNumber.equals("null")) harvestNumber = "tmpDir";

        String outputPathString = baseDir + File.separator + targetInstanceOid + File.separator +
                harvestNumber + File.separator + "_resources" + File.separator;
        String tmpDirectoryString = baseDir + File.separator + targetInstanceOid + File.separator + "tmpDir" +
                File.separator + "_resources" + File.separator;

        // Make sure output path exists
        File destinationDir = new File(outputPathString);
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        String fullpageFilename =  targetInstanceOid + "_harvestNum_" + seedId + "_" + liveOrHarvested + "_fullpage.png";

        // Need to move the live screenshots and use the wayback indexed url instead of the seed url
        if (liveOrHarvested.equals("harvested")) {
            // Check if live screenshots exist in tmp directory
            for (String size : new String[]{"fullpage","screen","fullpage-thumbnail","screen-thumbnail"}){
                renameLiveFile(tmpDirectoryString, outputPathString, harvestNumber,
                        replaceSectionInFilename(fullpageFilename, size + ".png", 4));
            }
            deleteTmpDir(tmpDirectoryString);

            seedUrl = getWaybackUrl(seedUrl, outputPathString, identifiers, harvestWaybackViewerBaseUrl);

            if (seedUrl.equals("")) {
                log.error("Could not retrieve wayback url.");
                return false;
            }
        }

        // Populate the filenames and the placeholder values
        if (identifiers.get("harvestNumber") != null && !harvestNumber.equals("null")) {
            fullpageFilename = replaceSectionInFilename(fullpageFilename, harvestNumber, 1);
        }

        String screenFilename = replaceSectionInFilename(fullpageFilename, "screen.png", 4);
        String imagePlaceholder = "%image.png%";
        String urlPlaceholder = "%url%";

        String commandFullpage = getFullpageSizeCommand()
                .replace(urlPlaceholder, seedUrl.replaceAll("\\s+",""))
                .replace(imagePlaceholder, outputPathString + fullpageFilename);
        String commandScreen = getScreenSizeCommand()
                .replace(urlPlaceholder, seedUrl.replaceAll("\\s+",""))
                .replace(imagePlaceholder, outputPathString + screenFilename);

        // Get the name of the tool used to get the screenshot
        String toolUsed = getScreenshotToolName();

        log.info("Generating screenshots for job " + targetInstanceOid + " using " + toolUsed + "...");

        try {
            // Generate fullpage screenshots only if live or not using the default SeleniumScreenshotCapture executable for harvested screenshot
            // The size of harvested screenshots will be compared next
            if (liveOrHarvested.equals("live") || !commandFullpage.contains("SeleniumScreenshotCapture")) {
                if (runCommand(commandFullpage)) {
                    waitForScreenshot(new File(outputPathString + fullpageFilename));
                } else {
                    log.error("Unable to run command " + commandFullpage);
                    return false;
                }
            }

            String liveImageFilename = fullpageFilename;
            String[] filenameSections = fullpageFilename.split("_");
            if (filenameSections[3].equals("harvested")) {
                liveImageFilename = replaceSectionInFilename(fullpageFilename, "live", 3);
            }

            File liveImageFile = new File(outputPathString + File.separator + liveImageFilename);
            if (liveOrHarvested.equals("harvested") && !liveImageFile.exists()) {
                log.info("Live image file " + liveImageFilename + " does not exist, nothing to compare against.");
            }
            if (liveOrHarvested.equals("harvested") && liveImageFile.exists()) {
                // Generate wayback commands
                String commandWaybackFullpage = getWindowSizeCommand()
                        .replace(urlPlaceholder, seedUrl.replaceAll("\\s+", ""))
                        .replace(imagePlaceholder, outputPathString + fullpageFilename);

                if (commandWaybackFullpage.contains("SeleniumScreenshotCapture")) {
                    commandWaybackFullpage = commandWaybackFullpage.substring(0, commandWaybackFullpage.indexOf("width=")) + "--wayback";
                    if (runCommand(commandWaybackFullpage)) {
                        waitForScreenshot(new File(outputPathString + fullpageFilename));
                    }
                    // For non-default screenshot tools check the fullpage screenshot image size against the harvested screenshots
                } else {
                    if (!checkFullpageScreenshotSize(outputPathString, fullpageFilename, liveImageFile, seedUrl)) {
                        log.error("Unable to check fullpage screenshot size");
                        return false;
                    }
                }
            }

            // Generate the screen sized screenshot
            if (liveOrHarvested.equals("harvested") && commandScreen.contains("SeleniumScreenshotCapture")) {
                commandScreen = commandScreen.trim() + " --wayback";
            }
            if (runCommand(commandScreen)) {
                waitForScreenshot(new File(outputPathString + screenFilename));
            }

            // Generate thumbnails screenshots if not using the default screenshot tool
            if (!commandScreen.contains("SeleniumScreenshotCapture")) {
                generateThumbnailOrScreenSizeScreenshot(fullpageFilename, outputPathString,
                        "fullpage","fullpage-thumbnail",100, 100);
                waitForScreenshot(new File(outputPathString +
                        replaceSectionInFilename(fullpageFilename, "fullpage-thumbnail.png", 4)));
                generateThumbnailOrScreenSizeScreenshot(screenFilename, outputPathString,
                        "screen", "screen-thumbnail", 100, 100);
                waitForScreenshot(new File(outputPathString +
                        replaceSectionInFilename(screenFilename, "screen-thumbnail.png", 4)));
            }

            // Count the number of screenshots generated and add the tool name as a file attribute
            File dir = new File(outputPathString);
            int imageCounter = 0;
            for (File file : dir.listFiles()) {
                if (!file.toString().toLowerCase().endsWith(".png")) continue;
                if (!file.toString().contains(liveOrHarvested)) continue;
                imageCounter++;

                if (Files.getFileStore(file.toPath()) == null) continue;
                UserDefinedFileAttributeView attributeView = Files.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);
                attributeView.write("screenshotTool-" + toolUsed, Charset.defaultCharset().encode(toolUsed));
            }
            log.info(String.valueOf(imageCounter) + " " + liveOrHarvested + " screenshots have been generated for job " + targetInstanceOid);

        } catch (Exception e) {
            log.error("Failed to generate screenshots: " + e.getMessage(), e);
            return false;
        }
        return true;
    }
}