package com.coviam.controller;

import com.coviam.model.CandidateDetails;
import com.coviam.service.CrawlerService;
import com.coviam.service.LinkedinSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@CrossOrigin
@Slf4j
@RequestMapping("/")
public class CrawlerController {

  @Autowired
  private CrawlerService crawlerService;

  @Autowired
  private LinkedinSearchService linkedinSearchService;

  @RequestMapping(method = RequestMethod.GET, value = "getCandidateDetails")
  public CandidateDetails crawler(@RequestParam(value = "url") String url) {
    return crawlerService.getCandidateDetails(url);
  }

  @RequestMapping(method = RequestMethod.GET, value = "searchCandidate")
  public Map<String, Integer> getCandidateUrls(@RequestParam(value = "params") String params) {
    List<String> paramsList = new ArrayList<String>(Arrays.asList(params.split(",")));
    if(CollectionUtils.isNotEmpty(paramsList)){
      return linkedinSearchService.getCandidateUrlsWithNumber(paramsList);
    }else{
      log.error("Error in parsing params from request");
      return null;
    }
  }
}
