package com.coviam.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CandidateDetails {
	private String name;
	private String location;
	private List<String> educationDetails;
	private List<String> workDetails;
	private List<String> technicalSkills;
}
