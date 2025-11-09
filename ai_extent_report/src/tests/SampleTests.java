package tests;

import java.util.Iterator;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SampleTests {
	private WebDriver driver;

	@BeforeClass
	public void setUp() {
		// ensure chromedriver binary is on PATH or use WebDriverManager in real
		// projects
		driver = new ChromeDriver();

		// Launch website
		driver.get("https://www.toolsqa.com/");

		// Get title of page
		String pageName = driver.getTitle();

		// Verify website
		if (pageName.equals("Tools QA")) {
			System.out.println("\n--- Tools QA page verified successfully ---");
		} else {
			System.out.println("--- Failed to varify Tools QA page ---");
			driver.quit();
		}

		// Maximize the window
		driver.manage().window().maximize();
	}

	@Test(description = "Tools QA Scenario")
	public void toolsqaDemo() {

		waitForElement();

		// Search selenium in search box
		By searchSeleniumField = By.xpath("(//input[@name='keyword'])[2]");
		WebElement searchSelenium = driver.findElement(searchSeleniumField);
		searchSelenium.clear();
		searchSelenium.sendKeys("selenium");
		searchSelenium.sendKeys(Keys.ENTER);

		waitForElement();

		// Verify selenium search success or not
		By verifySearchSeleniumLocator = By.xpath("//h1[@class='articles__list--heading']");
		WebElement verifySearchSelenium = driver.findElement(verifySearchSeleniumLocator);
		String str = verifySearchSelenium.getText();
		openAndVerifyElements(str, "selenium", "SeleniumSearchSuccess.png");

		// Click on automation frameworks
		By clickOnAutomationFrameworksLocator = By.xpath("//div[@class='articles']//a[7]");
		WebElement elementclickOnAutomationFrameworks = driver.findElement(clickOnAutomationFrameworksLocator);
		String elementclickOnAutomationFrameworksText = elementclickOnAutomationFrameworks.getText();
		JavascriptExecutor js = (JavascriptExecutor) driver;
		// This will scroll the page till the element is found
		js.executeScript("arguments[0].scrollIntoView();", elementclickOnAutomationFrameworks);
		waitForElement();
		elementclickOnAutomationFrameworks.click();

		// Verify automation frameworks click
		By verifyClickOnAutomationFrameworksLocator = By.xpath("//h1[@class='article-meta-data__title']");
		WebElement verifyClickOnAutomationFrameworks = driver.findElement(verifyClickOnAutomationFrameworksLocator);
		String verifyClickOnAutomationFrameworksText = verifyClickOnAutomationFrameworks.getText();
		openAndVerifyElements(elementclickOnAutomationFrameworksText, verifyClickOnAutomationFrameworksText,
				"AutomationFrameworksSearchSuccess.png");

		// It will return the parent window name as a String
		String parent = driver.getWindowHandle();

		// Click on demo site
		By demoSiteLocator = By.xpath("//ul[@class='navbar__links d-none d-lg-flex']//li[3]");
		WebElement demoSite = driver.findElement(demoSiteLocator);
		demoSite.click();

		waitForElement();

		Set<String> s = driver.getWindowHandles();

		// Now iterate using Iterator
		Iterator<String> I1 = s.iterator();

		while (I1.hasNext()) {
			String child_window = I1.next();

			if (!parent.equals(child_window)) {
				driver.switchTo().window(child_window);
				String demoSiteText = driver.getTitle();

				if (demoSiteText.equals("DEMOQA")) {
					System.out.println("\n--- Demo QA page verified successfully ---");

					// Click on element
					By clickElementsLocator = By.xpath("//div[@class='card mt-4 top-card'][1]");
					WebElement clickElements = driver.findElement(clickElementsLocator);
					String clickElementsText = clickElements.getText();
					js.executeScript("arguments[0].scrollIntoView();", clickElements);
					waitForElement();
					clickElements.click();

					// Verify element click
					By verifyElementsLocator = By.xpath("//div[@class='main-header']");
					WebElement verifyElements = driver.findElement(verifyElementsLocator);
					String verifyElementsText = verifyElements.getText();
					openAndVerifyElements(clickElementsText, verifyElementsText, "ElementsSearchSuccess.png");

				} else {
					System.out.println("--- Failed to varify Demo QA page ---");
				}
				driver.close();
			}
		}
		// switch to the parent window
		driver.switchTo().window(parent);
	}

	/**
	 * Verify page and take screenshot
	 * 
	 * @param str
	 * @param text
	 * @param fileName
	 */
	public void openAndVerifyElements(String str, String text, String fileName) {
		if (str.contains(text)) {
			System.out.println("\n--- " + text + " search success ---");
		} else {
			System.out.println("--- Failed to search " + text + " ---");
		}
	}

	/**
	 * To sleep website for 5000 milliseconds
	 */
	public void waitForElement() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public void tearDown() {
		if (driver != null)
			driver.quit();
	}
}
