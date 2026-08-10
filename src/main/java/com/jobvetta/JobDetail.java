package com.jobvetta;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

/** Complete structured information for an individual job. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JobDetail(
        @JsonProperty("job_id") String jobId,
        String title,
        String company,
        String location,
        String url,
        @JsonProperty("work_model") String workModel,
        @JsonProperty("employment_type") String employmentType,
        JsonNode salary,
        @JsonProperty("normalized_title") String normalizedTitle,
        String description,
        @JsonProperty("experience_level") String experienceLevel,
        @JsonProperty("minimum_qualifications") List<String> minimumQualifications,
        @JsonProperty("preferred_qualifications") List<String> preferredQualifications,
        @JsonProperty("skills_required") List<String> skillsRequired,
        List<String> responsibilities,
        List<String> benefits,
        @JsonProperty("salary_min") Double salaryMin,
        @JsonProperty("salary_max") Double salaryMax,
        @JsonProperty("salary_currency") String salaryCurrency,
        @JsonProperty("created_at") Long createdAt,
        @JsonProperty("last_seen_date") Long lastSeenDate) {}
