package org.webcurator.core.screenshot;
/*
 * Selenium Screenshot Tool
 * <p>
 * This is the default screenshot tool to be used with WCT for screenshot generation.
 * It uses selenium which requires a web driver.  In this case, it uses chromedriver.
 * <p>
 * To use it with WCT, download chromedriver from  https://chromedriver.chromium.org/downloads and save it to the same location that you have placed WCT store jar.
 * Then either copy the python file to the same location that you have placed the WCT Store jar
 * or run gradle clean build and copy the jar from build/libs to the same location that you have the WCT Store jar.
 * <p>
 * This tool assumes that WCT uses pywb as it's wayback access tool.
 * Generating screenshots with selenium has been problematic because the focus will be on the wayback banner.
 * To get around this, the tool changes the focus to the iframe_replay and removes the wayback banner.
 * <p>
 * The arguments that it requires are the filepath for the image, the url for the seed, and optionally the screen size for the image.
 * Another optional argument is --wayback, which tells the tool to change the focus and remove the wayback banner.
 */

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.util.ProcessBuilderUtils;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class SeleniumScreenshotCapture {
    private static final Logger log = LoggerFactory.getLogger(SeleniumScreenshotCapture.class);

    private static void createThumbnail(File input, String output) {
        try {
            BufferedImage sourceImage = ImageIO.read(input);
            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            Image scaledImage = sourceImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            image.createGraphics().drawImage(scaledImage, 0, 0, null);
            ImageIO.write(image, "png", new File(output));
        } catch (Exception e) {
            System.out.println("Unable to create thumbnail: " + e);
        }
    }

    private static boolean waitForFile(File filepath) {
        try {
            for (int i = 0; i < 60; i++) {
                if (!filepath.exists()) {
                    System.out.println("Waiting for screenshot file " + filepath.getName() + " to generate...");
                    Thread.sleep(1000);
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Not able to find screenshot file in {}", filepath.getName(), e);
        }
        return false;
    }


    // Generate fullpage, screen-sized, and thumbnail screenshots for a given url
    // Should always have an argument for the url=%url% filepath=%image.jpg% output filepath
    // For screen screenshot, inlcude width=val height=val arguments e.g. width=1400 height=800
    // For wayback fullpage screenshot, include --wayback argument
    public static boolean callChromeDriver(String[] args) {
        String url = null;
        String filepath = null;
        String imageWidth = null;
        String imageHeight = null;
        boolean isWayback = false;
        String waybackName = null;
        String waybackVersion = null;

        // Assign variable values based on arguments
        for (String arg : args) {
            String[] keyValues = arg.split("=");
            switch (keyValues[0]) {
                case "url":
                    url = keyValues[1];
                    break;
                case "filepath":
                    filepath = keyValues[1];
                    break;
                case "width":
                    imageWidth = keyValues[1];
                    break;
                case "height":
                    imageHeight = keyValues[1];
                    break;
                case "wayback-name":
                    waybackName = keyValues[1];
                    break;
                case "wayback-version":
                    waybackVersion = keyValues[1];
                    break;
                case "--wayback":
                    isWayback = true;
                    break;
                case "native":
                    break;
                default:
                    log.debug("Ignored unknown argument '{}'.", arg);
                    break;
            }
        }

        if (StringUtils.isEmpty(filepath)) {
            log.error("The filepath is null");
            return false;
        }

        // If the width or height is not restricted, then screenshot the whole scrollable page.
        boolean isScrollable = (imageHeight == null || imageWidth == null);

        try {
            // Prepare the driver
            String pathOfChromeDriver = ProcessBuilderUtils.getFullPathOfCommand("chromedriver");
            if (pathOfChromeDriver == null) {
                log.error("Failed to get the path of chromedriver");
                return false;
            }

            System.setProperty("webdriver.chrome.driver", pathOfChromeDriver);

            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--no-sandbox");

            if (imageHeight != null && imageWidth != null) {
                chromeOptions.addArguments("--window-size=" + imageWidth + "," + imageHeight);
            }

            WebDriver driver = new ChromeDriver(chromeOptions);

            if (imageWidth == null && imageHeight == null) {
                driver.manage().window().maximize();
            }

            // Change the focus out of the wayback banner
            if (isWayback) {
                if (waybackName.equalsIgnoreCase("pywb")) {
                    // Remove pywb banner: with a customized iframe to request the iframe generated by pywb.
                    String iFrameStyle = "display:block; overflow:hidden; height:100%; width:100%; margin:0px; border:0px; padding:0px;";
                    String iFrameHtml = String.format("<iframe id='replay_iframe' src='%s' frameborder='0' style='%s'></iframe>\n", url, iFrameStyle);
                    String html = "<!DOCTYPE html>\n" +
                            "<html style='width: 100wh; height: 100vh; margin:0px; border:0px; padding:0px; overflow: hidden;'>\n" +
                            "<body style='width: 100wh; height: 100vh; margin:0px; border:0px; padding:0px; overflow: hidden;'>\n" +
                            iFrameHtml +
                            "</body>\n" +
                            "</html>";
                    driver.get("data:text/html;charset=utf-8," + html);
                    WebDriverWait wait = new WebDriverWait(driver, 3600);
                    wait.until(ExpectedConditions.visibilityOfElementLocated((By.id("replay_iframe"))));
                    driver.switchTo().frame("replay_iframe");
                } else {
                    driver.get(url);
                    JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
                    WebElement banner = driver.findElement(By.id("wm-ipp"));
                    jsExecutor.executeScript("arguments[0].style.display='none';", banner);
                }
            } else {
                driver.get(url);
            }

            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");

            // Generate the screenshot
            boolean generated = false;
            if (!isScrollable) {
                File newFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                File outputFile = new File(filepath);
                Files.copy(newFile.toPath(), Paths.get(filepath), StandardCopyOption.REPLACE_EXISTING);
                generated = waitForFile(outputFile);
            } else { // for fullpage
                Screenshot fullScreen = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver);
                ImageIO.write(fullScreen.getImage(), "png", new File(filepath));
                generated = waitForFile(new File(filepath));
            }
            driver.quit();

            if (!generated) {
                return false;
            }
            // Generate a thumbnail for the screenshot
            String size = "";
            if (filepath.contains("fullpage")) {
                size = "fullpage";
            } else if (filepath.contains("screen")) {
                size = "screen";
            }
            createThumbnail(new File(filepath), filepath.replace(size, size + "-thumbnail"));
            return true;
        } catch (Exception e) {
            log.error("Unable to capture the screenshot.  " + e.getMessage());
            return false;
        }
    }
}
