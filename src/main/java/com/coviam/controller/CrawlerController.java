package com.coviam.controller;

import com.coviam.model.CandidateDetails;
import com.coviam.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/")
public class CrawlerController {

  @Autowired
  private CrawlerService crawlerService;

  @RequestMapping(method = RequestMethod.GET, value = "getCandidateDetails")
  public CandidateDetails crawler(@RequestParam(value = "url") String url) {
    return crawlerService.getCandidateDetails(url);
  }
}
