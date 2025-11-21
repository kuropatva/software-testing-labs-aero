import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AllTests {

    private static final String link = "http://127.0.0.1:5000";
    private static final List<String> fields = List.of("flight-number", "airline", "aircraft-type", "departure-time", "arrival-time", "origin", "destination");
    private static final String username = "controller";
    private static final String password = "password123";


    WebDriver driver;

    @BeforeEach
    public void setup() {
        driver = new FirefoxDriver();
    }

    @AfterEach
    public void kill() {
        driver.quit();
    }

    @Test
    public void login() {
        login(driver);
        assertTrue(driver.getCurrentUrl().contains("dashboard"));
    }

    @Test
    public void emergency() {
        login(driver);
        var dialog = driver.findElement(By.id("emergency-dialog"));
        driver.findElement(By.id("emergency-alert-button")).click();
        var start = Instant.now();
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofMillis(501));
        wait.until(d -> dialog.isDisplayed());
        var diff = start.until(Instant.now());
        assertTrue(diff.toMillis() <= 500);
    }

    @Test
    public void checkList() throws InterruptedException {
        login(driver);
        click(driver, "flights-tab");
        var update = driver.findElement(By.id("flights-update-timestamp"));
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(u -> !update.getText().contains("-"));
        var oldNow = Instant.now();
        var oldText = update.getText();
        wait.until(u -> !update.getText().equals(oldText));
        var now = oldNow.until(Instant.now()).toMillis();
        assertTrue(now >= 4500);
        assertTrue(now <= 6500);
    }

    @Test
    public void checkFields() {
        login(driver);
        click(driver, "new-flight-tab");
        for (var field : fields) {
            var obj = driver.findElement(By.id(field));
            assertTrue(obj.isEnabled());
            assertTrue(obj.isDisplayed());
        }
    }

    @Test
    public void full() throws InterruptedException {
        login(driver);
        click(driver, "new-flight-tab");
        insertList(driver, fields,
                List.of("123", "123", "123", "2025-02-01T14:30", "2025-02-01T14:31", "123", "123"));
        click(driver, "submit-flight");
        Thread.sleep(500);
        assertFalse(isAlertPresent(driver));
    }

    public void login(WebDriver webDriver) {
        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500));
        driver.get(link);
        var username = driver.findElement(By.id("username"));
        var password = driver.findElement(By.id("password"));
        username.sendKeys(AllTests.username);
        password.sendKeys(AllTests.password);
        driver.findElement(By.id("login-button")).click();
    }

    public void click(WebDriver webDriver, String name) {
        webDriver.findElement(By.id(name)).click();
    }

    public void insert(WebDriver webDriver, String name, String input) {
        webDriver.findElement(By.id(name)).sendKeys(input);
    }

    public void insertList(WebDriver webDriver, List<String> nameList, List<String> inputList) {
        var iterInput = inputList.iterator();
        assert nameList.size() <= inputList.size();

        for (var name : nameList) {
            insert(webDriver, name, iterInput.next());
        }
    }

    public boolean isAlertPresent(WebDriver driver) {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

}
