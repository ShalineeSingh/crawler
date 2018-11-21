package com.coviam.service.implementation;

import com.coviam.service.LinkedinSearchService;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;

/**
 * Created by shalineesingh on 19/11/18 .
 */

@Slf4j
@Service("com.coviam.service.implementation.LinkedinSearchServiceImpl")
@PropertySource("classpath:linkedin.properties")
public class LinkedinSearchServiceImpl implements LinkedinSearchService {

  @Autowired
  private Driver driver;

  @Autowired
  private LinkedinImpl linkedinImpl;

  @Value("${search.url}")
  private String searchUrl;

  private int pageLimit = 20;

  @Override
  public Set<String> getCandidateUrls(String searchParam) {
    WebDriver webDriver = driver.getWebDriver();
    Set<String> candidateUrls = new HashSet<>();
    try {
      linkedinImpl.linkedInlogin(webDriver);
      searchByParam(webDriver, searchParam);
      //get urls for first page
      candidateUrls.addAll(extractCandidateUrlFromLinks(webDriver));
      for (int pagenum = 1; pagenum < pageLimit; pagenum++) {
        changePage(webDriver, pagenum);
        candidateUrls.addAll(extractCandidateUrlFromLinks(webDriver));
      }
    } catch (Exception e) {
      log.error("Exception in login method for baseURL: {}", e);
    }
    return candidateUrls;
  }

  @Override
  public Map<String, Integer> getCandidateUrlsWithNumber(List<String> searchParam) {
    Map<String, Integer> urlMapWithCount = new HashMap();
    Set<String> candidatePerPage = new HashSet<>();
    for (String param : searchParam) {
      candidatePerPage.addAll(getCandidateUrls(param));
      if(CollectionUtils.isNotEmpty(candidatePerPage)){
        for(String candidateUrl : candidatePerPage){
          if(urlMapWithCount.containsKey(candidateUrl)){
          Integer count = urlMapWithCount.get(candidateUrl);
          count = count+1;
          urlMapWithCount.put(candidateUrl, count);
          }else{
            urlMapWithCount.put(candidateUrl, 1);
          }
        }
      }
    }
    Map<String, Integer> sortedMap =
        urlMapWithCount.entrySet().stream().sorted(reverseOrder(Map.Entry.comparingByValue())).collect(Collectors
            .toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    return sortedMap;
  }

  private void changePage(WebDriver webDriver, int pageNum) {
    List<WebElement> pageNumElements= new ArrayList<>();
    try {
      ((JavascriptExecutor) webDriver)
          .executeScript("window.scrollTo(0, document.body.scrollHeight)");
      WebElement pageList = webDriver.findElement(By.className("page-list"));
      pageNumElements.addAll((pageList.findElement(By.tagName("ol")).findElements(By.tagName("li"))));
      if(CollectionUtils.isNotEmpty(pageNumElements)){
        pageNumElements.get(pageNum).click();
        Thread.sleep((int)Math.random() * 1000);
      }
    } catch (Exception e) {
      log.error("Exception in getting next page with page num : {}", e, pageNum);
    }
  }

  private void searchByParam(WebDriver webDriver, String searchParam) {
    try {
      webDriver.get(searchUrl + "&keywords=" + searchParam);
    } catch (Exception e) {
      log.error("Exception in getting the search results: {}", e);
    }
  }

  private Set extractCandidateUrlFromLinks(WebDriver webDriver) {
    Set<String> hrefElements = new HashSet<>();
    try {
      List<WebElement> pagelinks = (List<WebElement>) ((JavascriptExecutor) webDriver)
          .executeScript("return document.links");
      List<WebElement> userPageLinks = null;
      if (CollectionUtils.isNotEmpty(pagelinks)) {
        for (WebElement link : pagelinks) {
          userPageLinks = webDriver.findElements(By.className("search-result__result-link"));
        }
        if (CollectionUtils.isNotEmpty(userPageLinks)) {
          for (WebElement link : userPageLinks) {
            hrefElements.add(link.getAttribute("href"));
          }
        }
      } else {
        log.debug("Links could not be extracted");
      }
    } catch (Exception e) {
      log.error("Exception in parsing the list of candidates : {}", e);
    }
    return hrefElements;
  }
}
