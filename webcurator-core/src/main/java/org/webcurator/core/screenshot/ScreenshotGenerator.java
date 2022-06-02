package org.webcurator.core.screenshot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.domain.model.core.SeedHistory;


public class ScreenshotGenerator {
    private static final Logger log = LoggerFactory.getLogger(ScreenshotGenerator.class);

    private final static String SCREENSHOT_FOLDER = "_screenshots";
    private final static int THUMBNAIL_WIDTH = 100;
    private final static int THUMBNAIL_HEIGHT = 100;
    private final String windowSizeCommand;
    private final String screenSizeCommand;
    private final String fullpageSizeCommand;

    private final String baseDir;
    private final String harvestWaybackViewerBaseUrl;


    public String getFullpageSizeCommand() {
        return fullpageSizeCommand;
    }

    public String getScreenSizeCommand() {
        return screenSizeCommand;
    }

    public String getWindowSizeCommand() {
        return windowSizeCommand;
    }

    public ScreenshotGenerator(String windowSizeCommand, String screenSizeCommand, String fullpageSizeCommand, String baseDir, String harvestWaybackViewerBaseUrl) {
        this.windowSizeCommand = windowSizeCommand;
        this.screenSizeCommand = screenSizeCommand;
        this.fullpageSizeCommand = fullpageSizeCommand;
        this.baseDir = baseDir;
        this.harvestWaybackViewerBaseUrl = harvestWaybackViewerBaseUrl;
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
            log.warn("Wakeup without unexpected", e);
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
            if (harvestedImage.getWidth() >= liveImageWidth && harvestedImage.getHeight() >= liveImageHeight) {
                harvestedImage.flush();
                return true;
            }

            String windowSizeCommand = this.windowSizeCommand
                    .replace("%width%", String.valueOf(liveImageWidth))
                    .replace("%height%", String.valueOf(liveImageHeight + 150))
                    .replace("%url%", url)
                    .replace("%image.png%", outputPath + filename);


            log.info("Harvested full page screenshot is smaller than live full page screenshot.  " +
                    "Getting a new screenshot using live image dimensions, command " + windowSizeCommand);

            // Delete the old harvested fullpage image and replace it with one with new dimensions
            File toDelete = new File(outputPath + File.separator + filename);
            if (toDelete.delete()) {
                if (!runCommand(windowSizeCommand)) {
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
                        log.error("Unable to process the command in a new thread.", e);
                        threadFailed[0] = true;
                    }
                }
            };

            processThread.start();
            processThread.join();
//            processThread.stop();

            if (threadFailed[0]) return false;
        } catch (Exception e) {
            log.error("Unable to process command " + command, e);
            if (processThread != null && !processThread.isInterrupted()) {
//                processThread.stop();
                processThread.interrupt();
            }
            return false;
        }
        return true;
    }

    private void generateThumbnailOrScreenSizeScreenshot(String inputFilename, String outputPathString,
                                                         String inputSize, String outputSize) {
        log.info("Generating " + outputSize + " screenshot...");
        try {
            BufferedImage sourceImage = ImageIO.read(new File(outputPathString + File.separator + inputFilename));
            BufferedImage bufferedImage = new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Image scaledImage = sourceImage.getScaledInstance(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Image.SCALE_SMOOTH);
            bufferedImage.createGraphics().drawImage(scaledImage, 0, 0, null);
            BufferedImage thumbnailImage = bufferedImage.getSubimage(0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
            ImageIO.write(thumbnailImage, "png", new File(outputPathString + File.separator + inputFilename.replace(inputSize, outputSize)));
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

    // Returns an empty string when it can't retrieve the timestamp for the url
    private String getWaybackUrl(String seed, String timestamp, String waybackBaseUrl) {
        String result = waybackBaseUrl + timestamp + "/" + seed;
        log.info("Using harvest url {} to generate screenshots.", result);
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

    public Boolean createScreenshots(SeedHistory seed, long tiOid, ScreenshotType liveOrHarvested, int harvestNumber, String timestamp) {
        String outputPathString = baseDir + File.separator + tiOid + File.separator + harvestNumber + File.separator + SCREENSHOT_FOLDER + File.separator;

        // Make sure output path exists
        File destinationDir = new File(outputPathString);
        if (!destinationDir.exists()) {
            if (!destinationDir.mkdirs()) {
                log.error("Failed to make directory: {}", outputPathString);
                return false;
            }
        }

        String fullpageFilename = String.format("%d_%d_%d_%s_fullpage.png", tiOid, harvestNumber, seed.getOid(), liveOrHarvested.name());
        String seedUrl = seed.getSeed();
        // Need to move the live screenshots and use the wayback indexed url instead of the seed url
        if (liveOrHarvested == ScreenshotType.harvested) {
            seedUrl = getWaybackUrl(seedUrl, timestamp, harvestWaybackViewerBaseUrl);
            if (StringUtils.isEmpty(seedUrl)) {
                log.error("Could not retrieve wayback url.");
                return false;
            }
        }

        // Populate the filenames and the placeholder values
        fullpageFilename = replaceSectionInFilename(fullpageFilename, Integer.toString(harvestNumber), 1);

        String screenFilename = replaceSectionInFilename(fullpageFilename, "screen.png", 4);
        String imagePlaceholder = "%image.png%";
        String urlPlaceholder = "%url%";

        String commandFullpage = getFullpageSizeCommand()
                .replace(urlPlaceholder, seedUrl.replaceAll("\\s+", ""))
                .replace(imagePlaceholder, outputPathString + fullpageFilename);
        String commandScreen = getScreenSizeCommand()
                .replace(urlPlaceholder, seedUrl.replaceAll("\\s+", ""))
                .replace(imagePlaceholder, outputPathString + screenFilename);

        // Get the name of the tool used to get the screenshot
        String toolUsed = getScreenshotToolName();

        log.info("Generating screenshots for job " + tiOid + " using " + toolUsed + "...");

        try {
            // Generate fullpage screenshots only if live or not using the default SeleniumScreenshotCapture executable for harvested screenshot
            // The size of harvested screenshots will be compared next
            if (liveOrHarvested == ScreenshotType.live || !commandFullpage.contains("SeleniumScreenshotCapture")) {
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
            if (liveOrHarvested == ScreenshotType.harvested && !liveImageFile.exists()) {
                log.info("Live image file " + liveImageFilename + " does not exist, nothing to compare against.");
            }
            if (liveOrHarvested == ScreenshotType.harvested && liveImageFile.exists()) {
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
            if (liveOrHarvested == ScreenshotType.harvested && commandScreen.contains("SeleniumScreenshotCapture")) {
                commandScreen = commandScreen.trim() + " --wayback";
            }
            if (runCommand(commandScreen)) {
                waitForScreenshot(new File(outputPathString + screenFilename));
            }

            // Generate thumbnails screenshots if not using the default screenshot tool
            if (!commandScreen.contains("SeleniumScreenshotCapture")) {
                generateThumbnailOrScreenSizeScreenshot(fullpageFilename, outputPathString,
                        "fullpage", "fullpage-thumbnail");
                waitForScreenshot(new File(outputPathString +
                        replaceSectionInFilename(fullpageFilename, "fullpage-thumbnail.png", 4)));
                generateThumbnailOrScreenSizeScreenshot(screenFilename, outputPathString,
                        "screen", "screen-thumbnail");
                waitForScreenshot(new File(outputPathString +
                        replaceSectionInFilename(screenFilename, "screen-thumbnail.png", 4)));
            }

            // Count the number of screenshots generated and add the tool name as a file attribute
            File dir = new File(outputPathString);
            File[] listFiles = dir.listFiles();
            if (listFiles == null) {
                log.error("Could not list the directory: {}", outputPathString);
                return false;
            }
            int imageCounter = 0;
            for (File file : listFiles) {
                if (!file.toString().toLowerCase().endsWith(".png")) continue;
                if (!file.toString().contains(liveOrHarvested.name())) continue;
                imageCounter++;

                if (Files.getFileStore(file.toPath()) == null) continue;
                UserDefinedFileAttributeView attributeView = Files.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);
                attributeView.write("screenshotTool-" + toolUsed, Charset.defaultCharset().encode(toolUsed));
            }
            log.info("{} {} screenshots have been generated for job {}", imageCounter, liveOrHarvested, tiOid);

        } catch (Exception e) {
            log.error("Failed to generate screenshots:", e);
            return false;
        }
        return true;
    }
}