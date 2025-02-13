package org.webcurator.core.screenshot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Arrays;
import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.domain.model.core.SeedHistoryDTO;


public class ScreenshotGenerator {
    private static final Logger log = LoggerFactory.getLogger(ScreenshotGenerator.class);
    private final static int THUMBNAIL_WIDTH = 100;
    private final static int THUMBNAIL_HEIGHT = 100;
    private String windowSizeCommand;
    private String screenSizeCommand;
    private String fullpageSizeCommand;

    private String baseDir;
    private String harvestWaybackViewerBaseUrl;

    private String waybackName = "pywb";
    private String waybackVersion = "2.7.3";
    private boolean isIndividualCollectionMode = true;

    private void waitForScreenshot(File file) {
        try {
            for (int i = 0; i < 120; i++) {
                if (file.exists()) return;
                log.info(file.getName() + " has not been created yet.  Waiting...");
                Thread.sleep(1000);
            }
            log.info("Timed out waiting for file creation.");
        } catch (Exception e) {
            log.warn("Wakeup without unexpected", e);
        }
    }

    private boolean isIndexReady(ScreenshotIdentifierCommand identifiers) {
        //The wb-manager is used to add the WARC files to collections, and index synchronized.
        if (this.waybackName.equalsIgnoreCase("pywb")) {
            return this.isIndexReadyPywb(identifiers);
        } else {
            return this.isIndexReadyWayback(identifiers);
        }
    }

    private boolean isIndexReadyWayback(ScreenshotIdentifierCommand identifiers) {
        List<SeedHistoryDTO> seeds = identifiers.getSeeds();
        for (SeedHistoryDTO seed : seeds) {
            String targetUrl = getWaybackUrl(seed.getSeed(), seed.getTimestamp(), identifiers);
            try {
                URI uri = new URI(targetUrl);
                HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();

                conn.connect();

                boolean is_indexed = conn.getResponseCode() == HttpURLConnection.HTTP_OK;
                conn.disconnect();

                if (!is_indexed) {
                    return false;
                }
            } catch (IOException | URISyntaxException e) {
                log.error("Failed to connect to: {}", targetUrl, e);
                return false;
            }
        }
        return true;
    }

    private boolean isIndexReadyPywb(ScreenshotIdentifierCommand identifiers) {
        List<SeedHistoryDTO> seeds = identifiers.getSeeds();
        for (SeedHistoryDTO seed : seeds) {
            String pywbUrl = this.getPywbCDXJServerApiUrl(seed.getSeed(), seed.getTimestamp(), identifiers);
            HttpURLConnection conn = null;
            try {
                URI uri = new URI(pywbUrl);
                conn = (HttpURLConnection) uri.toURL().openConnection();

                conn.connect();

                boolean is_indexed = conn.getResponseCode() == HttpURLConnection.HTTP_OK;

                if (!is_indexed) {
                    return false;
                }

                List<String> lines = IOUtils.readLines(conn.getInputStream(), Charset.defaultCharset());
                if (this.isIndividualCollectionMode) {
                    if (lines.isEmpty()) {
                        return false;
                    }
                } else {
                    boolean isValid = false;
                    for (String line : lines) {
                        String[] items = line.split(" ");
                        if (items.length >= 3) {
                            String timestamp = items[1];
                            if (StringUtils.equals(timestamp, seed.getTimestamp())) {
                                isValid = true;
                                break;
                            }
                        }
                    }
                    if (!isValid) {
                        return false;
                    }
                }
            } catch (IOException | URISyntaxException e) {
                log.error("Failed to connect to: {} {}", pywbUrl, e.getMessage());
                return false;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        return true;
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
        command = String.format("%s wayback-name=%s wayback-version=%s", command, this.waybackName, this.waybackVersion);
        log.info("Running command " + command);
        List<String> commandList = Arrays.asList(command.split(" "));
        if (commandList.size() < 2) {
            log.error("Invalid commandList, the commandList has no enough arguments.");
            return false;
        }

        if (StringUtils.equalsIgnoreCase(commandList.get(0), "native")) {
            String[] args = commandList.toArray(new String[0]);
            return SeleniumScreenshotCapture.callChromeDriver(args);
        }

        Thread processThread = null;
        try {
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
    private String getWaybackUrl(String seed, String timestamp, ScreenshotIdentifierCommand identifiers) {
        String result = this.harvestWaybackViewerBaseUrl;
        if (!result.endsWith("/")) {
            result += "/";
        }

        if (this.isIndividualCollectionMode) {
            String collName = String.format("%d-%d", identifiers.getTiOid(), identifiers.getHarvestNumber());
            result += collName;
            result += "/";
        }

        if (StringUtils.isEmpty(timestamp)) {
            result += seed;
        } else {
            if (this.waybackName.equalsIgnoreCase("pywb")) {
                result += timestamp + "mp_/" + seed;
            } else {
                result += timestamp + "/" + seed;
            }
        }
        log.info("Using harvest url {} to generate screenshots.", result);
        return result;
    }

    private String getPywbCDXJServerApiUrl(String seed, String timestamp, ScreenshotIdentifierCommand identifiers) {
        String rootUrl = StringUtils.removeEnd(this.harvestWaybackViewerBaseUrl, "/");
        if (this.isIndividualCollectionMode) {
            String collName = String.format("%d-%d", identifiers.getTiOid(), identifiers.getHarvestNumber());
            return String.format("%s/%s/cdx?url=%s", rootUrl, collName, seed);
        } else { //The coll name is contained inside rootUrl
            return String.format("%s/cdx?url=%s", rootUrl, seed);
        }
    }

    private String getScreenshotToolName() {
        String fullCommand = getFullpageSizeCommand();

        // Get the name of the tool used to get the screenshot
        String toolUsed = fullCommand.split("\\s+")[0];
        // If using a default tool, specify as default
        if (fullCommand.contains("native")) {
            toolUsed = "default-builtin";
        } else if (fullCommand.contains("SeleniumScreenshotCapture")) {
            toolUsed = "customized";
            if (fullCommand.contains(".py")) {
                toolUsed = toolUsed + "-python";
            }
            if (fullCommand.contains(".jar")) {
                toolUsed = toolUsed + "-java";
            }
        }
        if (toolUsed.contains(File.separator)) {
            toolUsed = toolUsed.substring(toolUsed.lastIndexOf(File.separator) + 1);
        }
        return toolUsed;
    }

    public boolean checkIndexState(ScreenshotIdentifierCommand identifiers) throws DigitalAssetStoreException {
        File directory = new File(baseDir, identifiers.getTiOid() + File.separator + identifiers.getHarvestNumber());
        List<SeedHistoryDTO> seedWithTimestamp = ScreenshotTimestampExtractorFromCDX.getSeedWithTimestamps(identifiers.getSeeds(), directory);
        if (seedWithTimestamp == null || seedWithTimestamp.size() != identifiers.getSeeds().size()) {
            log.error("Failed to append timestamp for seeds: {}, {}", identifiers.getTiOid(), identifiers.getHarvestNumber());
            return false;
        }
        identifiers.setSeeds(seedWithTimestamp);
        return this.isIndexReady(identifiers);
    }

    public Boolean createScreenshots(ScreenshotIdentifierCommand identifiers) throws DigitalAssetStoreException {
        if (identifiers.getScreenshotType() == ScreenshotType.harvested) {
            File directory = new File(baseDir, identifiers.getTiOid() + File.separator + identifiers.getHarvestNumber());
            List<SeedHistoryDTO> seedWithTimestamp = ScreenshotTimestampExtractorFromCDX.getSeedWithTimestamps(identifiers.getSeeds(), directory);
            if (seedWithTimestamp == null || seedWithTimestamp.size() != identifiers.getSeeds().size()) {
                log.error("Failed to extract timestamp for seeds: {}, {}", identifiers.getTiOid(), identifiers.getHarvestNumber());
                return false;
            }
            identifiers.setSeeds(seedWithTimestamp);

            for (int count = 0; count < 60; count++) {
                if (this.isIndexReady(identifiers)) {
                    break;
                }
                try {
                    Thread.sleep(30 * 1000);
                } catch (InterruptedException e) {
                    log.error("Sleep was interrupted");
                    return false;
                }
            }
        }

        boolean screenshotsSucceeded = Boolean.FALSE;
        try {
            for (SeedHistoryDTO seed : identifiers.getSeeds()) {
                screenshotsSucceeded = this.createScreenshots(seed, identifiers);
                if (!screenshotsSucceeded) {
                    break;
                }
            }
            screenshotsSucceeded = Boolean.TRUE;
        } catch (Exception e) {
            log.error("Failed to create screenshot.", e);
        }
        return screenshotsSucceeded;
    }

    private Boolean createScreenshots(SeedHistoryDTO seed, ScreenshotIdentifierCommand identifiers) {
        long tiOid = identifiers.getTiOid();
        ScreenshotType liveOrHarvested = identifiers.getScreenshotType();
        int harvestNumber = identifiers.getHarvestNumber();

        String outputPathString = baseDir + File.separator + ScreenshotPaths.getImagePath(tiOid, harvestNumber) + File.separator;

        // Make sure output path exists
        File destinationDir = new File(outputPathString);
        if (!destinationDir.exists()) {
            if (!destinationDir.mkdirs()) {
                log.error("Failed to make directory: {}", outputPathString);
                return false;
            }
        }

        String seedUrl = seed.getSeed();
        // Need to move the live screenshots and use the wayback indexed url instead of the seed url
        if (liveOrHarvested == ScreenshotType.harvested) {
            seedUrl = getWaybackUrl(seedUrl, seed.getTimestamp(), identifiers);
            if (StringUtils.isEmpty(seedUrl)) {
                log.error("Could not retrieve wayback url.");
                return false;
            }
        }

        // Populate the filenames and the placeholder values
        String fullpageFilename = ScreenshotPaths.getImageName(tiOid, harvestNumber, Long.toString(seed.getOid()), liveOrHarvested, "fullpage");
        String screenFilename = ScreenshotPaths.getImageName(tiOid, harvestNumber, Long.toString(seed.getOid()), liveOrHarvested, "screen");
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
            if (liveOrHarvested == ScreenshotType.live) {
                if (runCommand(commandFullpage)) {
                    waitForScreenshot(new File(outputPathString + fullpageFilename));
                } else {
                    log.error("Unable to run command: {}", commandFullpage);
                    return false;
                }
            } else if (liveOrHarvested == ScreenshotType.harvested) {
                // Generate wayback commands
                String commandWaybackFullpage = getWindowSizeCommand()
                        .replace(urlPlaceholder, seedUrl.replaceAll("\\s+", ""))
                        .replace(imagePlaceholder, outputPathString + fullpageFilename);
                commandWaybackFullpage = commandWaybackFullpage.substring(0, commandWaybackFullpage.indexOf("width=")) + "--wayback";
                if (runCommand(commandWaybackFullpage)) {
                    waitForScreenshot(new File(outputPathString + fullpageFilename));
                }

                // Generate the screen sized screenshot
                commandScreen = commandScreen.trim() + " --wayback";
            }

            if (runCommand(commandScreen)) {
                waitForScreenshot(new File(outputPathString + screenFilename));
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

                try {
                    if (Files.getFileStore(file.toPath()) == null) continue;
                    UserDefinedFileAttributeView attributeView = Files.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);
                    attributeView.write("screenshotTool-" + toolUsed, Charset.defaultCharset().encode(toolUsed));
                } catch (IOException e) {
                    log.warn("Failed to record attribute for file {} {}", file.getAbsoluteFile(), toolUsed);
                }
            }
            log.info("{} {} screenshots have been generated for job {}", imageCounter, liveOrHarvested, tiOid);

        } catch (Exception e) {
            log.error("Failed to generate screenshots:", e);
            return false;
        }
        return true;
    }

    public String getFullpageSizeCommand() {
        return fullpageSizeCommand;
    }

    public String getScreenSizeCommand() {
        return screenSizeCommand;
    }

    public String getWindowSizeCommand() {
        return windowSizeCommand;
    }

    public void setWindowSizeCommand(String windowSizeCommand) {
        this.windowSizeCommand = windowSizeCommand;
    }

    public void setScreenSizeCommand(String screenSizeCommand) {
        this.screenSizeCommand = screenSizeCommand;
    }

    public void setFullpageSizeCommand(String fullpageSizeCommand) {
        this.fullpageSizeCommand = fullpageSizeCommand;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public void setHarvestWaybackViewerBaseUrl(String harvestWaybackViewerBaseUrl) {
        this.harvestWaybackViewerBaseUrl = harvestWaybackViewerBaseUrl;
    }


    public void setWaybackName(String waybackName) {
        this.waybackName = waybackName;
    }


    public void setWaybackVersion(String waybackVersion) {
        this.waybackVersion = waybackVersion;
    }

    public boolean isIndividualCollectionMode() {
        return isIndividualCollectionMode;
    }

    public void setIndividualCollectionMode(boolean individualCollectionMode) {
        isIndividualCollectionMode = individualCollectionMode;
    }
}