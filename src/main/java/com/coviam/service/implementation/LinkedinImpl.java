package com.coviam.service.implementation;

import com.coviam.model.CandidateDetails;
import com.coviam.service.CrawlerService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Slf4j
@Service("com.coviam.service.implementation.LinkedinImpl")
@PropertySource("classpath:linkedin.properties")
public class LinkedinImpl implements CrawlerService {

  @Autowired
  private Driver driver;

  @Value("${location.classname}")
  private String locationClassname;

  @Value("${name.classname}")
  private String nameClassname;

  @Value("${base.url}")
  private String baseUrl;

  @Value("${login.usename}")
  private String username;

  @Value("${login.password}")
  private String password;

  @Value("${education.classname}")
  private String educationClassname;

  @Value("${work.classname}")
  private String workClassname;

  @Value("${technical.classname}")
  private String technicalClassname;

  @Value("${dropdown.classname}")
  private String dropdownClassname;

  @Value("${login.usernamekey}")
  private String loginUsernameKey;

  @Value("${login.passwordkey}")
  private String loginPasswordKey;

  @Value("${login.signinkey}")
  private String loginSigninKey;

  @Value("${homepage.url}")
  private String homepageUrl;

  /**
   * getting candidate details from the linkedIn url of the candidate
   * candidate details include : (Personal, Technical, Work, Education) Details
   *
   * @param url
   * @return
   */
  @Override
  public CandidateDetails getCandidateDetails(String url) {
    CandidateDetails candidateDetails = null;
    WebDriver webDriver = driver.getWebDriver();
    try {
      linkedInlogin(webDriver);
      navigateToCandidateUrl(webDriver, url);
      candidateDetails = setCandidateDetails(url, webDriver);
    } catch (Exception e) {
      log.error("Exception in getCandidateDetails for url: {}", url, e);
    }
    log.info("Returning candidate details for url: {} to resume_parser service", url);
    return candidateDetails;
  }
  /**
   * Setting candidate details after scraping details from linkedIn Url
   *
   * @param url
   * @param webDriver
   * @return
   */
  private CandidateDetails setCandidateDetails(String url, WebDriver webDriver) {
    CandidateDetails candidateDetails = new CandidateDetails();
    ((JavascriptExecutor)webDriver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
    candidateDetails.setName(getName(webDriver, url));
    candidateDetails.setLocation(getLocation(webDriver, url));
    candidateDetails.setEducationDetails(getEducationDetails(webDriver, url));
    candidateDetails.setWorkDetails(getWorkDetails(webDriver, url));
    candidateDetails.setTechnicalSkills(getTechnicalDetails(webDriver, url));
    return candidateDetails;
  }

  /**
   * navigate to candidate url after successfully login
   *
   * @param webDriver
   * @param url
   */
  private void navigateToCandidateUrl(WebDriver webDriver, String url) {
    webDriver.get(url);
  }

  /**
   * scraping personal location from the linkedIn url of the candidate
   *
   * @param webDriver
   * @param url
   * @return
   */
  private String getLocation(WebDriver webDriver, String url) {
    log.info("Getting location from url: {}", url);
    String location = null;
    try {
      location = webDriver.findElement(By.className(locationClassname)).getText();
    } catch (Exception e) {
      log.error("Exception while finding location for url: {}", url, e);
    }
    return location;
  }

  /**
   * scraping  name of the candidate from candidate linkedInUrl
   *
   * @param webDriver
   * @param url
   * @return
   */
  private String getName(WebDriver webDriver, String url) {
    log.info("Getting name from url: {}", url);
    String name = null;
    try {
      name = webDriver.findElement(By.className(nameClassname)).getText();
    } catch (Exception e) {
      log.error("Exception while finding name for url: {}", url, e);
    }
    return name;
  }

  /**
   * Login using credentials(username, password) as a initial step
   * helps in searching candidate using candidate linkedIn url
   *
   * @param driver
   */
  public void linkedInlogin(WebDriver driver) {
    try {
      driver.manage().window().maximize();
      driver.get(baseUrl);
      driver.findElement(By.name(loginUsernameKey)).sendKeys(username);
      log.info("Username: {} added", username);
      driver.findElement(By.name(loginPasswordKey)).sendKeys(password);
      log.info("Password: {} added", password);
      driver.findElement(By.id(loginSigninKey)).click();
      String currentUrl = driver.getCurrentUrl();
      Boolean isLoginSuccessful = checkLoginIsSuccessful(currentUrl);
      if (isLoginSuccessful) {
        log.info("Login successful. for baseUrl: {} - Current url {}", baseUrl, currentUrl);
      } else {
        log.debug(
            "Exception in login with username: {} - password: {} - baseUrl: {} - currentUrl: {}",
            username, password, baseUrl, currentUrl);
      }

    } catch (Exception e) {
      log.error("Exception in login method for baseURL: {}", baseUrl, e);
    }
  }

  /**
   * check if login is successful using home page url
   *
   * @param currentUrl
   * @return
   */
  private Boolean checkLoginIsSuccessful(String currentUrl) {
    return currentUrl.equals(homepageUrl);
  }

  /**
   * scraping education details from linkedInUrl of the candidate
   * education details include (Degreee, College/School name, Department, Score, Year)
   *
   * @param webDriver
   * @param url
   * @return
   */
  private List<String> getEducationDetails(WebDriver webDriver, String url) {
    log.info("Getting education details from url: {}", url);
    List<String> educationDetails = new ArrayList<>();
    List<WebElement> descendantsEducation = getEducationWebElements(webDriver, url);
    if (CollectionUtils.isNotEmpty(descendantsEducation)) {
      for (WebElement education : descendantsEducation) {
        educationDetails.add(education.getText());
      }
    } else {
      log.debug("Education details are not present for url: {}", url);
    }
    return educationDetails;
  }

  /**
   * return list of education web elements using education classname
   *
   * @param webDriver
   * @param url
   * @return
   */
  private List<WebElement> getEducationWebElements(WebDriver webDriver, String url) {
    List<WebElement> descendantsEducation = null;
    try {
      descendantsEducation = webDriver.findElements(By.className(educationClassname));
    } catch (Exception e) {
      log.error("Exception while finding educationDetails for url: {}", url, e);
    }
    return descendantsEducation;
  }

  /**
   * scraping work details from candidate linkedInurl
   * work details include :
   * (Company name, Year/Month of experience, Designation, Location, Years(fromYear-toYear))
   *
   * @param webDriver
   * @param url
   * @return
   */
  private List<String> getWorkDetails(WebDriver webDriver, String url) {
    log.info("Getting work details from url: {}", url);
    List<String> workDetails = new ArrayList<>();
    List<WebElement> descendantsWork = getWorkWebElements(webDriver, url);
    if (CollectionUtils.isNotEmpty(descendantsWork)) {
      for (WebElement work : descendantsWork) {
        workDetails.add(work.getText());
      }
    } else {
      log.debug("Work details are not present for url: {}", url);
    }
    return workDetails;
  }

  /**
   * return list of work web elements using work classname
   *
   * @param webDriver
   * @param url
   * @return
   */
  private List<WebElement> getWorkWebElements(WebDriver webDriver, String url) {
    List<WebElement> descendantsWork = null;
    try {
      descendantsWork = webDriver.findElements(By.className(workClassname));
    } catch (Exception e) {
      log.error("Exception while finding workDetails for url: {}", url, e);
    }
    return descendantsWork;
  }

  /**
   * scraping technical skills from candidate linkedInUrl
   * eg: C, C++, java
   *
   * @param webDriver
   * @param url
   * @return
   */
  private List<String> getTechnicalDetails(WebDriver webDriver, String url) {
    log.info("Getting technical details from url: {}", url);
    List<String> technicalDetails = new ArrayList<>();
    List<WebElement> descendantsSkills = getSkillsWebElements(webDriver, url);
    if (CollectionUtils.isNotEmpty(descendantsSkills)) {
      for (WebElement skill : descendantsSkills) {
        technicalDetails.add(skill.getText());
      }
    } else {
      log.debug("Technical details are not present for url: {}", url);
    }
    return technicalDetails;
  }

  /**
   * return list of skills web elements using skill classname
   *
   * @param webDriver
   * @param url
   * @return
   */
  private List<WebElement> getSkillsWebElements(WebDriver webDriver, String url) {
    List<WebElement> descendantsSkills = null;
    openFullSkillsSection(webDriver);
    try {
      descendantsSkills = webDriver.findElements(By.className(technicalClassname));
    } catch (Exception e) {
      log.error("Exception while finding skills for url", url, e);
    }
    return descendantsSkills;
  }

  /**
   * click on the button to open full skill section
   *
   * @param webDriver
   */
  private void openFullSkillsSection(WebDriver webDriver) {
    try {
      webDriver.findElement(By.className(dropdownClassname)).click();
      Thread.sleep(4000);
    } catch (Exception e) {
      log.error("Exception while clicking skill icon to show all skills", e);
    }
  }
}
