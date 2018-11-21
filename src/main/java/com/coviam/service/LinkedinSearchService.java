package com.coviam.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by shalineesingh on 19/11/18 .
 */

public interface LinkedinSearchService {

  Set<String> getCandidateUrls(String searchParam);

  Map<String, Integer> getCandidateUrlsWithNumber(List<String> searchParam);
}
