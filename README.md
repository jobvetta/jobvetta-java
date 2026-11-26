# Jobvetta for Java (`1.2.0`)

Typed Java 17+ client for the [Jobvetta Jobs API](https://www.jobvetta.com/api): search **6.5M+ active job openings** across **238,000+ official employer career portals** in **242 countries** and retrieve structured 70-field job details.

## Install with JitPack

```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.github.jobvetta</groupId>
  <artifactId>jobvetta-java</artifactId>
  <version>1.2.0</version>
</dependency>
```

## Quickstart

```java
import com.jobvetta.JobvettaClient;
import com.jobvetta.SearchOptions;

var client = new JobvettaClient(System.getenv("JOBVETTA_API_KEY"));
var results = client.searchJobs(
    new SearchOptions()
        .withQuery("java developer")
        .withLocation("United States")
        .withDays(7)
        .withLimit(25)
);

results.jobs().forEach(job ->
    System.out.printf("%s — %s (%s): %s%n", job.title(), job.company(), job.location(), job.url())
);
```

Generate a free Bearer key (`25 requests / UTC day`) at [jobvetta.com/dashboard#/api](https://www.jobvetta.com/dashboard#/api).

## License

MIT
