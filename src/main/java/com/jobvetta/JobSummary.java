package com.jobvetta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/** A compact job record returned by a search. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JobSummary(
        @JsonProperty("job_id") String jobId,
        String title,
        String company,
        String location,
        String url,
        @JsonProperty("work_model") String workModel,
        @JsonProperty("employment_type") String employmentType,
        JsonNode salary) {}
