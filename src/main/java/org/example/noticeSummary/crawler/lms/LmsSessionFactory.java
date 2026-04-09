package org.example.noticeSummary.crawler.lms;

import lombok.RequiredArgsConstructor;
import org.example.noticeSummary.util.CrawlerSupport;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class LmsSessionFactory {

    private final CrawlerSupport crawlerSupport;

    public LmsCrawlSession createLoggedInSession() {
        WebDriver driver = crawlerSupport.createWebDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        crawlerSupport.login(driver, wait);

        return new LmsCrawlSession(driver, wait);
    }
}
