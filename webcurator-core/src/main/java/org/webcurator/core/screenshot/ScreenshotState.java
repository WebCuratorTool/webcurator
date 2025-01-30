package org.webcurator.core.screenshot;

public class ScreenshotState {
    private boolean liveScreenshot;
    private boolean harvestedScreenshot;
    private boolean indexAvailable;

    public boolean isLiveScreenshot() {
        return liveScreenshot;
    }

    public void setLiveScreenshot(boolean liveScreenshot) {
        this.liveScreenshot = liveScreenshot;
    }

    public boolean isHarvestedScreenshot() {
        return harvestedScreenshot;
    }

    public void setHarvestedScreenshot(boolean harvestedScreenshot) {
        this.harvestedScreenshot = harvestedScreenshot;
    }

    public boolean isIndexAvailable() {
        return indexAvailable;
    }

    public void setIndexAvailable(boolean indexAvailable) {
        this.indexAvailable = indexAvailable;
    }
}
