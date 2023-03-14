package org.webcurator.core.screenshot;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class SeleniumScreenshotCaptureTest {
    @Ignore
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

    @Ignore
    @Test
    public void testHarvestedScreenshot() {
        String url = "http://localhost:9090/my-web-archive/20230208222848/https://www.rnz.co.nz/";
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
