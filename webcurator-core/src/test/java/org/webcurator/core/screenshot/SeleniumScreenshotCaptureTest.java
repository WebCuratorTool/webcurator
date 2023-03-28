package org.webcurator.core.screenshot;


import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.webcurator.core.util.ProcessBuilderUtils;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class SeleniumScreenshotCaptureTest {
    @Ignore
    @Test
    public void testLiveScreenshot() {
        String url = "https://www.rnz.co.nz/";
        String imgFullPage = "ori_5000_1_4001_live_fullpage.png", imgFullPageThumbnail = "ori_5000_1_4001_live_fullpage-thumbnail.png";
        String imgScreen = "ori_5000_1_4001_live_screen.png", imgScreenThumbNail = "ori_5000_1_4001_live_screen-thumbnail.png";
        deleteFile(imgFullPage);
        deleteFile(imgFullPageThumbnail);
        deleteFile(imgScreen);
        deleteFile(imgScreenThumbNail);

        boolean ret;

        String[] argsFullPage = {"url=" + url, "filepath=/tmp/" + imgFullPage};
        ret = SeleniumScreenshotCapture.callChromeDriver(argsFullPage);
        assertTrue(ret);
        assertTrue(isFileExisting(imgFullPage));
        assertTrue(isFileExisting(imgFullPageThumbnail));

        String[] argsScreen = {"url=" + url, "filepath=/tmp/" + imgScreen, "width=1400", "height=800"};
        ret = SeleniumScreenshotCapture.callChromeDriver(argsScreen);
        assertTrue(ret);
        assertTrue(isFileExisting(imgScreen));
        assertTrue(isFileExisting(imgScreenThumbNail));
    }

    @Ignore
    @Test
    public void testHarvestedScreenshotOfPywb() {
        String url = "http://localhost:1080/my-web-archive/20230207222650mp_/https://www.rnz.co.nz/";
        String imgFullPage = "pywb_5000_1_4001_harvested_fullpage.png", imgFullPageThumbnail = "pywb_5000_1_4001_harvested_fullpage-thumbnail.png";
        String imgScreen = "pywb_5000_1_4001_harvested_screen.png", imgScreenThumbNail = "pywb_5000_1_4001_harvested_screen-thumbnail.png";
        deleteFile(imgFullPage);
        deleteFile(imgFullPageThumbnail);
        deleteFile(imgScreen);
        deleteFile(imgScreenThumbNail);

        boolean ret;

        String[] argsFullPage = {"url=" + url, "filepath=/tmp/" + imgFullPage, "--wayback=true", "wayback-name=pywb", "wayback-version=2.7.3"};
        ret = SeleniumScreenshotCapture.callChromeDriver(argsFullPage);
        assertTrue(ret);
        assertTrue(isFileExisting(imgFullPage));
        assertTrue(isFileExisting(imgFullPageThumbnail));

        String[] argsScreen = {"url=" + url, "filepath=/tmp/" + imgScreen, "width=1400", "height=800", "--wayback=true", "wayback-name=pywb", "wayback-version=2.7.3"};
        ret = SeleniumScreenshotCapture.callChromeDriver(argsScreen);
        assertTrue(ret);
        assertTrue(isFileExisting(imgScreen));
        assertTrue(isFileExisting(imgScreenThumbNail));
    }

    @Ignore
    @Test
    public void testHarvestedScreenshotOfOpenWayback() {
        String url = "http://localhost:8080/wayback/20230207222650/https://www.rnz.co.nz/";
        String imgFullPage = "owb_5000_1_4001_harvested_fullpage.png", imgFullPageThumbnail = "owb_5000_1_4001_harvested_fullpage-thumbnail.png";
        String imgScreen = "owb_5000_1_4001_harvested_screen.png", imgScreenThumbNail = "owb_5000_1_4001_harvested_screen-thumbnail.png";
        deleteFile(imgFullPage);
        deleteFile(imgFullPageThumbnail);
        deleteFile(imgScreen);
        deleteFile(imgScreenThumbNail);

        boolean ret;

        String[] argsFullPage = {"url=" + url, "filepath=/tmp/" + imgFullPage, "--wayback=true", "wayback-name=owb", "wayback-version=2.4.0"};
        ret = SeleniumScreenshotCapture.callChromeDriver(argsFullPage);
        assertTrue(ret);
        assertTrue(isFileExisting(imgFullPage));
        assertTrue(isFileExisting(imgFullPageThumbnail));

        String[] argsScreen = {"url=" + url, "filepath=/tmp/" + imgScreen, "width=1400", "height=800", "--wayback=true", "wayback-name=owb", "wayback-version=2.4.0"};
        ret = SeleniumScreenshotCapture.callChromeDriver(argsScreen);
        assertTrue(ret);
        assertTrue(isFileExisting(imgScreen));
        assertTrue(isFileExisting(imgScreenThumbNail));
    }


    @Ignore
    @Test
    public void testProxyMechanism() throws IOException {
        String chromeDriverPath = ProcessBuilderUtils.getFullPathOfCommand("chromedriver");
        if (chromeDriverPath == null) {
            return;
        } else {
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        }

        ChromeOptions chromeOptions = new ChromeOptions();
//        chromeOptions.addArguments("--headless");
//        chromeOptions.addArguments("--no-sandbox");
        WebDriver driver = new ChromeDriver(chromeOptions);

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body style='width: 100wh; height: 100vh; overflow: hidden;'>\n" +
                "  <iframe id='wrapped_replay_iframe' src='http://localhost:1080/my-web-archive/20230207222650mp_/https://www.rnz.co.nz/' frameborder='0' style='overflow:hidden;height:100%;width:100%' height='100%' width='100%'></iframe>\n" +
                "</body>\n" +
                "</html>";
        driver.get("data:text/html;charset=utf-8," + html);

        WebDriverWait wait = new WebDriverWait(driver, 4000);
        wait.until(ExpectedConditions.visibilityOfElementLocated((By.id("wrapped_replay_iframe"))));
//        driver.switchTo().frame("wrapped_replay_iframe");

        String filepath = "a.png";
        Screenshot fullScreen = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver);
        ImageIO.write(fullScreen.getImage(), "png", new File(filepath));

        driver.quit();

    }


    private boolean isFileExisting(String fileName) {
        File f = new File("/tmp/" + fileName);
        return f.exists();
    }

    private void deleteFile(String fileName) {
        if (!isFileExisting(fileName)) {
            return;
        }
        try {
            FileUtils.forceDelete(new File("/tmp/" + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
