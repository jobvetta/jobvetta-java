package com.jobvetta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** Job search results and the total reported by Jobvetta. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchJobsResponse(long total, List<JobSummary> jobs) {}
