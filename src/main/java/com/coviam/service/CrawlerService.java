package com.coviam.service;

import com.coviam.model.CandidateDetails;

public interface CrawlerService {
  CandidateDetails getCandidateDetails(String url);
}
