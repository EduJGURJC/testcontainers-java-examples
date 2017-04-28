import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ProcessBuilder.Redirect;
import java.net.HttpURLConnection;
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
//			.withRecordingMode(SKIP, null)
			 .withRecordingMode(RECORD_ALL, new File("target"))
			.withEnv("DOCKER_HOST", "tcp://172.17.0.1:2376");

	@Test
	public void simplePlainSeleniumTest() {
		RemoteWebDriver driver = chrome.getWebDriver();

		Process p = runNoVncClient();

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

	public Process runNoVncClient() {
		Process p = null;
		String vncIp = getVncIp();
		String pass = chrome.getPassword();

		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/bash",
					System.getProperty("user.dir") + "/noVNC/utils/launch.sh", "--vnc", vncIp);
			pb.redirectOutput(Redirect.INHERIT);
			pb.redirectError(Redirect.INHERIT);
			p = pb.start();

			String url = "http://localhost:6080/vnc.html?host=localhost&port=6080&autoconnect=true&password=" + pass;
			try {
				Desktop.getDesktop().browse(new URL(url).toURI());
			} catch (Exception e) {
				e.printStackTrace();
			}		
			
			while(!validUrl(url)){System.out.println("Waiting for loading noVNC client");}
			System.out.println("urlvnc: "+ url);
			
			Thread.sleep(9000);
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

	public void writeFile(String path, String content) {
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "utf-8"));
			writer.write(content);
		} catch (IOException e) {
//			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
//				e.printStackTrace();
			}
		}
	}
	
    public static boolean validUrl(String URLName){
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con =  (HttpURLConnection) new URL(URLName).openConnection();
            con.setRequestMethod("HEAD");
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
