package org.webcurator.core.screenshot;

//import io.netty.handler.codec.http.HttpResponse;
//import net.lightbody.bmp.BrowserMobProxy;
//import net.lightbody.bmp.BrowserMobProxyServer;
//import net.lightbody.bmp.filters.ResponseFilter;
//import net.lightbody.bmp.util.HttpMessageContents;
//import net.lightbody.bmp.util.HttpMessageInfo;

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
//    @Ignore
    @Test
    public void testLiveScreenshot() {
        String url = "https://www.rnz.co.nz/";
        String imgFullPage = "5000_1_4001_live_fullpage.png", imgFullPageThumbnail = "5000_1_4001_live_fullpage-thumbnail.png";
        String imgScreen = "5000_1_4001_live_screen.png", imgScreenThumbNail = "5000_1_4001_live_screen-thumbnail.png";
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

    //    @Ignore
    @Test
    public void testHarvestedScreenshot() {
        String url = "http://localhost:1080/my-web-archive/20230207222650mp_/https://www.rnz.co.nz/";
//        String html = "<!DOCTYPE html>\n" +
//                "<html>\n" +
//                "<body style='width: 100wh; height: 100vh; overflow: hidden;'>\n" +
//                "  <iframe id='wrapped_replay_iframe' src='" + url + "' frameborder='0' style='overflow:hidden;height:100%;width:100%' height='100%' width='100%'></iframe>\n" +
//                "</body>\n" +
//                "</html>";
//        url = "data:text/html;charset=utf-8," + html;

        String imgFullPage = "5000_1_4001_harvested_fullpage.png", imgFullPageThumbnail = "5000_1_4001_harvested_fullpage-thumbnail.png";
        String imgScreen = "5000_1_4001_harvested_screen.png", imgScreenThumbNail = "5000_1_4001_harvested_screen-thumbnail.png";
        deleteFile(imgFullPage);
        deleteFile(imgFullPageThumbnail);
        deleteFile(imgScreen);
        deleteFile(imgScreenThumbNail);

        boolean ret;

        String[] argsFullPage = {"url=" + url, "filepath=/tmp/" + imgFullPage, "--wayback=true"};
        ret = SeleniumScreenshotCapture.callChromeDriver(argsFullPage);
        assertTrue(ret);
        assertTrue(isFileExisting(imgFullPage));
        assertTrue(isFileExisting(imgFullPageThumbnail));

        String[] argsScreen = {"url=" + url, "filepath=/tmp/" + imgScreen, "width=1400", "height=800", "--wayback=true"};
        ret = SeleniumScreenshotCapture.callChromeDriver(argsScreen);
        assertTrue(ret);
        assertTrue(isFileExisting(imgScreen));
        assertTrue(isFileExisting(imgScreenThumbNail));
    }

//    @Test
//    public void testBrowserMobProxyMechanism() {
//        InetAddress bindAddress;
//        try {
//            bindAddress = Inet4Address.getLocalHost();
//        } catch (UnknownHostException e) {
//            throw new RuntimeException(e);
//        }
//        if (bindAddress == null) {
//            return;
//        }
//
//        // start the proxy
//        BrowserMobProxy proxy = new BrowserMobProxyServer();
//        proxy.setTrustAllServers(true);
//        proxy.start(1098, bindAddress);
//
//        ResponseFilter responseFilter = new ResponseFilter() {
//            @Override
//            public void filterResponse(HttpResponse response, HttpMessageContents contents, HttpMessageInfo messageInfo) {
//                System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@");
//                System.out.println(contents.getTextContents());
//                System.out.println("==========================");
//                System.out.println(response.getStatus());
//            }
//        };
//        proxy.addResponseFilter(responseFilter);
//
//        String proxyStr = bindAddress.getHostAddress() + ":" + proxy.getPort();
//
//
//        // get the Selenium proxy object
////        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
////        seleniumProxy.setAutodetect(false);
////        seleniumProxy.setHttpProxy(proxyStr);
////        seleniumProxy.setSslProxy(proxyStr);
////        proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);
//
//        Proxy proxyServer = new Proxy();
//        proxyServer.setAutodetect(false);
//        proxyServer.setHttpProxy(proxyStr);
//        proxyServer.setSslProxy(proxyStr);
//
//
//        ChromeOptions chromeOptions = new ChromeOptions();
//        chromeOptions.setCapability("proxy", proxyServer);
//
//        String chromeDriverPath = ProcessBuilderUtils.getFullPathOfCommand("chromedriver");
//        if (chromeDriverPath == null) {
//            return;
//        } else {
//            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
//        }
//
////        ChromeOptions chromeOptions = new ChromeOptions();
////        chromeOptions.addArguments("--headless");
////        chromeOptions.addArguments("--no-sandbox");
//        WebDriver driver = new ChromeDriver(chromeOptions);
//
//
//        driver.get("http://localhost:1080/my-web-archive/20230207222650/https://www.rnz.co.nz/");
////        driver.get("https://www.google.com/");
//
//        driver.quit();
//    }

    @Test
    public void testProxyMechanism() throws IOException {
//        ProxyServer proxyServer = new ProxyServer(1098);
//        Thread t = new Thread(proxyServer);
//        t.start();
//        String proxyStr = "localhost:1098";
//
//        Proxy proxy = new Proxy();
//        proxy.setAutodetect(false);
//        proxy.setHttpProxy(proxyStr);
//        proxy.setSslProxy(proxyStr);
//
//
//        ChromeOptions chromeOptions = new ChromeOptions();
////        chromeOptions.setCapability("proxy", proxy);
//        chromeOptions.addArguments("--proxy-server=" + proxyStr);

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

//        driver.get("http://localhost:1080/my-web-archive/20230207222650/https://www.rnz.co.nz/");
//        driver.get("https://www.google.com/");


        WebDriverWait wait = new WebDriverWait(driver, 4000);
        wait.until(ExpectedConditions.visibilityOfElementLocated((By.id("wrapped_replay_iframe"))));
//        driver.switchTo().frame("wrapped_replay_iframe");

        String filepath = "a.png";
        Screenshot fullScreen = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver);
        ImageIO.write(fullScreen.getImage(), "png", new File(filepath));

        driver.quit();

//        proxyServer.stop();
//        t.join();
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
