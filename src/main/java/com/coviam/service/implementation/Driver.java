package com.coviam.service.implementation;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@PropertySource("classpath:linkedin.properties")
public class Driver {

  @Value("${chrome.driver.path}")
  private String chromeDriverPath;


  /**
   * Get chrome options
   *
   * @return
   */
  private ChromeOptions getChromeOptions() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("start-maximized");
    options.addArguments("--js-flags=--expose-gc");
    options.addArguments("--enable-precise-memory-info");
    options.addArguments("--disable-popup-blocking");
    options.addArguments("disable-infobars");
    options.addArguments("headless");
    options.addArguments("window-size=1200,1100");
    options.addExtensions(new File("config/notaRobot.crx"));
    return options;
  }

  /**
   * Get chrome driver service
   *
   * @return
   */
  private ChromeDriverService getChromeDriverService() {
    return new ChromeDriverService.Builder().usingDriverExecutable(new File(chromeDriverPath))
        .usingAnyFreePort().build();
  }

  /**
   * Intialize instance drivers
   *
   * @return
   */
  public WebDriver getWebDriver() {
    ChromeOptions options = getChromeOptions();
    DesiredCapabilities cap = new DesiredCapabilities();
    options.merge(cap);
    return new ChromeDriver(getChromeDriverService(), options);
  }
}
