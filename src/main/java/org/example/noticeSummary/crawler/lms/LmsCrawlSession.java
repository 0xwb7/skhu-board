package org.example.noticeSummary.crawler.lms;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public record LmsCrawlSession(
        WebDriver driver,
        WebDriverWait driverWait
) implements AutoCloseable {

    @Override
    public void close() {
        driver.quit();
    }
}
