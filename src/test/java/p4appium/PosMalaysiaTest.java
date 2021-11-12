package p4appium;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.appium.java_client.MobileElement;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.offset.PointOption;

public class PosMalaysiaTest {

	public static AndroidDriver<MobileElement> driver;
	public static WebDriverWait                wait;		
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
	        System.out.println("Setting up driver instance...");
	       
	        // Local VM
	        URL url = new URL("http://192.168.100.41:4723/wd/hub");
	        
	        // For AWS EC2 + Docker
	        // URL url = new URL("http://stargate.erengu.info:4723/wd/hub");

	        DesiredCapabilities cap = new DesiredCapabilities();

	        cap.setCapability("platformName", "Android");
	        cap.setCapability("platformVersion", "12");
	        cap.setCapability("appPackage", "my.com.pos.posmobile.posmobileapps");
	        cap.setCapability("appActivity", "my.com.pos.posmobile.posmobileapps.MainActivity");

	        driver = new AndroidDriver<MobileElement>(url, cap);
	        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
	        SessionId sessionId = driver.getSessionId();
	        Thread.sleep(2000);
	        
			System.out.println("INFO: sessionId: " + sessionId + " created on " + driver.getRemoteAddress());
			Thread.sleep(3000);	   
			
			// Allow photo
			driver.findElement(By.xpath("//android.widget.Button[@text='Allow']")).click();
			Thread.sleep(3000);
		
	}


	
	
	@SuppressWarnings("deprecation")
	@Test
	public void ParcelRatesTest() throws InterruptedException {

		String loc;
		
		
		// Step 1: Click "Postage Calculator" 
		loc = "//android.widget.ImageView[contains(@content-desc, 'Postage')]";
		System.out.println("DEBUG: Going to click Postage Calculator icon");
		safeGetME(loc, "xpath").click();
		Thread.sleep(2000);

        
		// Step 2: Click "International" 
		loc = "//android.widget.Button[@content-desc='Domestic']";
		System.out.println("DEBUG: Going to select International");
		safeGetME(loc, "xpath").click();
		Thread.sleep(2000);
		loc = "//android.view.View[@content-desc='International']";
		safeGetME(loc, "xpath").click();
		Thread.sleep(2000);
		
		
		// Step 3: Select "Parcel" as Service 
		loc = "//android.widget.Button[@content-desc='Mail']";
		System.out.println("DEBUG: Going to select Parcel");
		safeGetME(loc, "xpath").click();
		Thread.sleep(2000);
		loc = "//android.view.View[@content-desc='Parcel']";
		safeGetME(loc, "xpath").click();
		Thread.sleep(2000);
		
		// Step 4: Select India from Destination Country
		loc = "//android.widget.Button[@content-desc='Afghanistan']";
		System.out.println("DEBUG: Going to select India");
		safeGetME(loc, "xpath").click();
		Thread.sleep(2000);		
		viewSelect("//android.widget.FrameLayout", "India");
		Thread.sleep(2000);
		
		// Step 5: Enter weight (need to click the element to enable edit)
		loc = "//android.widget.EditText[contains(@text, '(kg)')]";
		safeGetME(loc,"xpath").click();
		Thread.sleep(2000);
		System.out.println("DEBUG: Going to click Weight");
		
		// The element.sendKeys didn't work for this particular apps. 
		// So, we send the input via keyboard.
		driver.getKeyboard().sendKeys("1.88");
		
		driver.hideKeyboard();
		Thread.sleep(2000);
		
		// Step 6: Click Calculate
		loc = "//android.widget.Button[@content-desc='Calculate']";
		System.out.println("DEBUG: Going to click Calculate button");	
		safeGetME(loc,"xpath").click();
		Thread.sleep(2000);
		
		
		// Step 7: Verify Result
		loc = "//android.view.View[@index=11]";
		System.out.println("DEBUG: Getting rate for Air mode");	
		String AirRate = safeGetME(loc, "xpath").getAttribute("content-desc");
		System.out.println("RESULT: Parcel (Air) 6-14 days : RM" + AirRate);
		Thread.sleep(2000);

		String expectedRate = "171.00";
		
		// Assert 
		assertTrue(AirRate.equals(expectedRate));
		
		System.out.println("TEST COMPLETED!");
		
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	
	}
	
	
	
	
	/**
	 * Wrapper to get MobileElement by trying maximum of 3 times. This is general work-around for frequent StaleElementException due to timing issue.
	 * @param locValue String for locator. 
	 * @param type     "id" or "xpath"
	 * @return MobileElement 
	 */
	private static MobileElement safeGetME(String locValue, String type) {

		MobileElement tempME = null;
		int retry_max = 3;	// also indicate scroll max
		
		WebDriverWait wait2 = (WebDriverWait) new WebDriverWait(driver, 8)
				.ignoring(StaleElementReferenceException.class);

		while (retry_max-- > 0) {

			try {
				if ( type.equalsIgnoreCase("id") ) {
					tempME = (MobileElement) wait2.until(ExpectedConditions.presenceOfElementLocated(By.id(locValue)));
				}
				else {
					tempME = (MobileElement) wait2.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locValue)));
				}
				
				if ( tempME != null ) {
					return tempME;
				}
				else {
					System.out.println("DEBUG:safeGetME():: [" + locValue + "] not found! To retry..");
					continue;
				}
			}
			catch (StaleElementReferenceException e) {
				// not supposed, we already ignore it
				System.out.println("DEBUG:safeGetME():: Caught [StaleElementReferenceException] retrying.." );
			}
			catch (TimeoutException e) {
				System.out.println("DEBUG:safeGetME():: Caught [TimeoutException] for [" + locValue + "] retrying.." );
			}
			catch (Exception e) {
				System.out.println("DEBUG:safeGetME():: Caught [something else?!] for [" + locValue + "] retrying.." );
				e.printStackTrace();
			}
		}
		
		// exhausted retries
		System.out.println("ERROR:safeGetME()::Exhausted all retries. [" + locValue + "] not found!" );
		return null;
		
	}
	

	

	/**
	 * Iterate (scroll) a view list & click the elemClick item once found.
	 * @param parentXpath
	 * @param elemClick
	 * @throws InterruptedException
	 */
	private static void viewSelect(String parentXpath, String elemClick ) throws InterruptedException {
		
		int retry_max = 20;	
		MobileElement parent = null; 
		List<MobileElement> views = null;

		// we will do <retries times> & scrollUp
		// StaleElement exception seems very common here, we will do retry
		
		WebDriverWait wait2 = (WebDriverWait) new WebDriverWait(driver, 8)
				.ignoring(StaleElementReferenceException.class);		
		
		while (retry_max-- > 0) {

			parent = (MobileElement) wait2.until(ExpectedConditions.presenceOfElementLocated(By.xpath(parentXpath)));
			
			if ( parent != null ) {
				try {
					views = parent.findElements(By.xpath("//android.view.View[string-length(@content-desc) > 0 ]"));
					System.out.println("DEBUG:viewSelect():: Size of List View: " + views.size());
					for (MobileElement view : views) {
						System.out.println(" -> " + view.getAttribute("content-desc"));
						// iterate until the elemClick to click
						if (view.getAttribute("content-desc").equals(elemClick)) {
							System.out.println("     ^-- CLICK!");
							view.click();
							return ;
						}
					}	
					
					// if not found?!
					System.out.println("ERROR:viewSelect():: [" + elemClick + "] not found! To scrollUp & retry..");
					scrollUpDestination();
					continue;
					
				}
				catch (StaleElementReferenceException e) {
					System.out.println("DEBUG:viewSelect():: Caught [StaleElementReferenceException] retrying.." );
				}
				catch (Exception e) {
					System.out.println("DEBUG:viewSelect():: Caught [something else?!] retrying.." );
					e.printStackTrace();
				}

			}
			else {
				System.out.println("DEBUG:viewSelect():: parent is null! Retrying.." );
			}

		}

		// fall through
		fail("FATAL:viewSelect():: [" + elemClick + "] not found! Exhausted retries!");		
		
	}	

	public static void scrollUpDestination() {
		
		int height = driver.manage().window().getSize().getHeight();
		int width = driver.manage().window().getSize().getWidth();
		
		int startX = (int) (0.5 * width);
		int endX = (int) (0.5 * width);
		
		int startY = (int) (0.60 * height);
		int endY = (int) (0.15 * height);
		
		TouchAction action = new TouchAction(driver);
		
		action
		.longPress(PointOption.point(startX, startY))
		.moveTo(PointOption.point(endX, endY))
		.release()
		.perform();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		


	
}
