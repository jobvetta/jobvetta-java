package com.jobvetta;

/** Optional filters accepted by {@link JobvettaClient#searchJobs(SearchOptions)}. */
public record SearchOptions(String query, String location, Integer days, Integer limit) {
    /** Creates a search without filters. */
    public SearchOptions() {
        this(null, null, null, null);
    }

    public SearchOptions withQuery(String value) {
        return new SearchOptions(value, location, days, limit);
    }

    public SearchOptions withLocation(String value) {
        return new SearchOptions(query, value, days, limit);
    }

    public SearchOptions withDays(Integer value) {
        return new SearchOptions(query, location, value, limit);
    }

    public SearchOptions withLimit(Integer value) {
        return new SearchOptions(query, location, days, value);
    }
}
