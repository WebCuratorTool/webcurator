import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


public class SeleniumScreenshotCapture {

	private static void createThumbnail(File input, String output) {
		try {
			BufferedImage sourceImage = ImageIO.read(input);
			BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
			Image scaledImage = sourceImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
			image.createGraphics().drawImage(scaledImage, 0, 0, null);
			ImageIO.write(image, "jpg", new File(output));
		} catch (Exception e) {
			System.out.println("Unable to create thumbnail: " + e);
		}
	}

	private static void waitForFile(File filepath) {
		try {
			for (int i = 0; i < 5; i++) {
				if (!filepath.exists()) {
					System.out.println("Waiting for screenshot file " + filepath.getName() + " to generate...");
					Thread.sleep(10000);
				}
			}
		} catch (Exception e) {

		}
	}

	// Generate fullpage, screen-sized, and thumbnail screenshots for a given url
	// Should always have an argument for the url=%url% filepath=%image.jpg% output filepath
	// For screen screenshot, inlcude width=val height=val arguments e.g. width=1400 height=800
	// For wayback fullpage screenshot, include --wayback argument
	public static void main(String[] args) throws IOException, InterruptedException {
		String url = null;
		String filepath = null;
		String imageWidth = null;
		String imageHeight = null;
		boolean isWayback = false;

		// Assign variable values based on arguments
		for (String arg : args) {
			String[] keyValues = arg.split("=");
			if (keyValues[0].equals("url")) url = keyValues[1];
			else if (keyValues[0].equals("filepath")) filepath = keyValues[1];
			else if (keyValues[0].equals("width")) imageWidth = keyValues[1];
			else if (keyValues[0].equals("height")) imageHeight = keyValues[1];
			else if (keyValues[0].equals("--wayback")) isWayback = true;
			else {
				System.out.println("Unrecognised argument '" + arg + "'.  Cannot generate screenshot.");
				System.exit(1);
			}
		}

		try {
			// Prepare the driver
			String chromeDriver = Paths.get("chromedriver").toFile().getAbsolutePath();

			System.setProperty("webdriver.chrome.driver", chromeDriver);

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

			driver.get(url);

			// Change the focus out of the wayback banner
			if (isWayback) {
				// Remove pywb banner
				try {
					JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
					WebElement banner = driver.findElement(By.id("_wb_frame_top_banner"));
					jsExecutor.executeScript("arguments[0].parentNode.removeChild(arguments[0])", banner);

					// Modify iframe padding
					WebElement frame = driver.findElement(By.id("wb_iframe_div"));
					jsExecutor.executeScript("arguments[0].setAttribute('style', 'padding:0px 0px 0px 0px')", frame);
				} catch (Exception e) {
				}

				WebDriverWait wait = new WebDriverWait(driver, 4000);
				wait.until(ExpectedConditions.visibilityOfElementLocated((By.id("replay_iframe"))));
				driver.switchTo().frame("replay_iframe");
			}

			// Generate the screenshot
			if (imageWidth != null && imageHeight != null) {
				File newFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
				File outputFile = new File(filepath);
				Files.copy(newFile.toPath(), Paths.get(filepath), StandardCopyOption.REPLACE_EXISTING);
				waitForFile(outputFile);

				// for fullpage
			} else {
				Screenshot fullScreen = new AShot().shootingStrategy(ShootingStrategies.viewportPasting(1000)).takeScreenshot(driver);
				ImageIO.write(fullScreen.getImage(), "jpg", new File(filepath));
				waitForFile(new File(filepath));
			}
			driver.quit();

			// Generate a thumbnail for the screenshot
			String size = "";
			if (filepath.contains("fullpage")) {
				size = "fullpage";
			} else if (filepath.contains("screen")) {
				size = "screen";
			}
			createThumbnail(new File(filepath), filepath.replace(size, size + "-thumbnail"));

			System.exit(0);
		} catch (Exception e) {
			System.out.println("Unable to capture the screenshot.  " + e.getMessage());
		}
	}
}
