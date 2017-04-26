import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.net.URL;

import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;
import static org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode.*;

/**
 * Simple example of plain Selenium usage.
 */
public class SeleniumContainerTest {

	@Rule
	public BrowserWebDriverContainer chrome = (BrowserWebDriverContainer) new BrowserWebDriverContainer()
																	.withDesiredCapabilities(DesiredCapabilities.chrome())
																	.withRecordingMode(SKIP, null)
																	// .withRecordingMode(RECORD_ALL, new File("target"))
																	.withEnv("DOCKER_HOST", "tcp://172.17.0.1:2376");

	@Test
	public void simplePlainSeleniumTest() {
		RemoteWebDriver driver = chrome.getWebDriver();

		Process p = runVnc();

		driver.get("https://wikipedia.org");
		WebElement searchInput = driver.findElementByName("search");

		searchInput.sendKeys("Rick Astley");
		searchInput.submit();

		WebElement otherPage = driver.findElementByLinkText("Rickrolling");
		otherPage.click();

		boolean expectedTextFound = driver.findElementsByCssSelector("p").stream()
				.anyMatch(element -> element.getText().contains("meme"));

		assertTrue("The word 'meme' is found on a page about rickrolling", expectedTextFound);
		exitVnc(p);
	}

	public String getVncIp() {
		String[] vncAddress = chrome.getVncAddress().split("@");
		return vncAddress[1];
	}

	public Process runVnc() {
		Process p = null;
		String vncIp = getVncIp();
		String pass = chrome.getPassword();

		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/bash", System.getProperty("user.dir") + "/noVNC/utils/launch.sh", "--vnc", vncIp);
			pb.redirectOutput(Redirect.INHERIT);
			pb.redirectError(Redirect.INHERIT);
			p = pb.start();

			try{
				Desktop.getDesktop().browse(
						new URL("http://localhost:6080/vnc.html?host=localhost&port=6080&autoconnect=true&password=" + pass).toURI());	
			} catch (Exception e) {
				e.printStackTrace();
			} 
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}

	public void exitVnc(Process p) {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		p.destroy();
	}
}
