package org.example.noticeSummary.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CrawlerSupport {

    @Value("${lms.id}")
    private String lmsId;

    @Value("${lms.password}")
    private String lmsPassword;

    private static final String LOGIN_URL = "https://lms.skhu.ac.kr/ilos/main/member/login_form.acl";

    public WebDriver createWebDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-size=1400,1200");

        return new ChromeDriver(options);
    }

    public void login(WebDriver driver, WebDriverWait wait) {
        driver.get(LOGIN_URL);

        WebElement normalLoginTab = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//li[contains(text(),'일반 로그인')]")
                )
        );
        normalLoginTab.click();

        WebElement idInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("usr_id"))
        );
        WebElement pwInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("usr_pwd"))
        );

        idInput.clear();
        idInput.sendKeys(lmsId);

        pwInput.clear();
        pwInput.sendKeys(lmsPassword);

        WebElement loginButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("login_btn"))
        );
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/ilos/main/main_form.acl"));
    }

    public String extractArticleId(String url) {
        Pattern pattern = Pattern.compile("ARTL_NUM=(\\d+)|/(\\d+)/");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            if (matcher.group(1) != null) {
                return matcher.group(1);
            }
            if (matcher.group(2) != null) {
                return matcher.group(2);
            }
        }

        return null;
    }

    public void enterDepartmentCourse(WebDriver driver, WebDriverWait wait, String courseText) {
        WebElement courseElement = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//em[contains(@class,'sub_open') and contains(normalize-space(.),'" + courseText + "')]")
                )
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", courseElement);

        wait.until(ExpectedConditions.urlContains("/ilos/st/course/submain_form.acl"));

        WebElement noticeMenu = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("menu_notice"))
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", noticeMenu);

        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("table tbody tr")
        ));
    }
}
