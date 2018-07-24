package com.coviam.service.implementation;

import com.coviam.model.CandidateDetails;
import com.coviam.service.CrawlerService;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import static com.coviam.constant.CrawlerConstants.LINKEDIN_LOGIN_PASS;
import static com.coviam.constant.CrawlerConstants.LINKEDIN_LOGIN_SIGNIN;
import static com.coviam.constant.CrawlerConstants.LINKEDIN_LOGIN_USERNAME;

@Slf4j
@Service
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

  /**
   * getting candidate details from the linkedIn url of the candidate
   * candidate details include : (Personal, Technical, Work, Education) Details
   *
   * @param url
   * @return
   */
  @Override
  public CandidateDetails getCandidateDetails(String url) {
    CandidateDetails candidateDetails = new CandidateDetails();
    long startTime = System.currentTimeMillis();
    WebDriver webDriver = driver.initializeDriverInstances();
    long endTime = System.currentTimeMillis();
    log.debug("Total time: {} taken to initialise driver", endTime - startTime);
    try {
      startTime = System.currentTimeMillis();
      linkedInlogin(webDriver);
      endTime = System.currentTimeMillis();
      log.debug("Total time: {} taken to login", endTime - startTime);
      long start = System.currentTimeMillis();
      webDriver.get(url);
      long end = System.currentTimeMillis();
      log.debug("Total time: {} to open url: {}", end - start, url);
    } catch (Exception e) {
      log.error("Exception in getCandidateDetails for url: {}", url, e);
    }
    startTime = System.currentTimeMillis();
    candidateDetails.setName(getName(webDriver, url));
    candidateDetails.setLocation(getLocation(webDriver, url));
    candidateDetails.setEducationDetails(getEducationDetails(webDriver, url));
    candidateDetails.setWorkDetails(getWorkDetails(webDriver, url));
    candidateDetails.setTechnicalSkills(getTechnicalDetails(webDriver, url));
    endTime = System.currentTimeMillis();
    log.debug("Total time: {} to scrap details", endTime - startTime);
    return candidateDetails;
  }

  /**
   * scraping personal location from the linkedIn url of the candidate
   *
   * @param webDriver
   * @param url
   * @return
   */
  private String getLocation(WebDriver webDriver, String url) {
    log.debug("Getting location from url: {}", url);
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
    log.debug("Getting name from url: {}", url);
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
  private void linkedInlogin(WebDriver driver) {
    String currentURL = driver.getCurrentUrl();
    try {
      driver.manage().window().maximize();
      driver.get(baseUrl);
      driver.findElement(By.name(LINKEDIN_LOGIN_USERNAME)).sendKeys(username);
      log.debug("Username: {} added", username);
      driver.findElement(By.name(LINKEDIN_LOGIN_PASS)).sendKeys(password);
      log.debug("Password: {} added", password);
      driver.findElement(By.id(LINKEDIN_LOGIN_SIGNIN)).click();
      log.debug("Login successful. Login url {}", currentURL);
    } catch (Exception e) {
      log.error("Exception in login method for baseURL: {} for currentURL: {}", baseUrl, currentURL,
          e);
    }
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
    log.debug("Getting education details from url: {}", url);
    List<String> educationDetails = new ArrayList<>();
    List<WebElement> descendantsEducation = null;
    try {
      descendantsEducation = webDriver.findElements(By.className(educationClassname));
    } catch (Exception e) {
      log.error("Exception while finding educationDetails for url: {}", url, e);
    }
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
   * scraping work details from candidate linkedInurl
   * work details include :
   * (Company name, Year/Month of experience, Designation, Location, Years(fromYear-toYear))
   *
   * @param webDriver
   * @param url
   * @return
   */
  private List<String> getWorkDetails(WebDriver webDriver, String url) {
    log.debug("Getting work details from url: {}", url);
    List<String> workDetails = new ArrayList<>();
    List<WebElement> descendantsWork = null;
    try {
      descendantsWork = webDriver.findElements(By.className(workClassname));
    } catch (Exception e) {
      log.error("Exception while finding workDetails for url: {}", url, e);
    }
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
   * scraping technical skills from candidate linkedInUrl
   * eg: C, C++, java
   *
   * @param webDriver
   * @param url
   * @return
   */
  private List<String> getTechnicalDetails(WebDriver webDriver, String url) {
    log.debug("Getting technical details from url: {}", url);
    List<String> technicalDetails = new ArrayList<>();
    List<WebElement> descendantsSkills = null;
    try {
      webDriver.findElement(By.className(dropdownClassname)).click();
    } catch (Exception e) {
      log.error("Exception while clicking skill icon to show all skills", e);
    }
    try {
      descendantsSkills = webDriver.findElements(By.className(technicalClassname));
    } catch (Exception e) {
      log.error("Exception while finding skills for url", url, e);
    }
    if (CollectionUtils.isNotEmpty(descendantsSkills)) {
      for (WebElement skill : descendantsSkills) {
        technicalDetails.add(skill.getText());
      }
    } else {
      log.debug("Technical details are not present for url: {}", url);
    }
    return technicalDetails;
  }
}