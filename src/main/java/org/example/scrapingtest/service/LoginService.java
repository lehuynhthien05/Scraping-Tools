package org.example.scrapingtest.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LoginService {

    public WebDriver login(String username, String password) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--headless");

        String headless = System.getProperty("scraper.headless", System.getenv().getOrDefault("SCRAPER_HEADLESS", "false"));
        if ("true".equalsIgnoreCase(headless)) {
            options.addArguments("--headless=new");
        }

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        try {
            // Login to get session
            driver.get("https://edusoftweb.hcmiu.edu.vn");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ContentPlaceHolder1_ctl00_ucDangNhap_txtTaiKhoa")))
                    .sendKeys(username);
            driver.findElement(By.id("ContentPlaceHolder1_ctl00_ucDangNhap_txtMatKhau"))
                    .sendKeys(password);
            driver.findElement(By.id("ContentPlaceHolder1_ctl00_ucDangNhap_btnDangNhap")).click();
            wait.until(ExpectedConditions.urlContains("default.aspx"));

            return driver;
        } catch (Exception e) {
            driver.quit();
            throw e;
        }
    }
}
