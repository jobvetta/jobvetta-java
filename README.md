# Jobvetta for Java

A compact Java 17 client for backend services, schedulers, and data pipelines
that need typed access to Jobvetta's employer-sourced India job records. It uses
the JDK HTTP client and exposes Jackson-mapped response models.

## Install with JitPack

Add JitPack to your repositories:

```xml
<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>
```

Then add the SDK:

```xml
<dependency>
  <groupId>com.github.jobvetta</groupId>
  <artifactId>jobvetta-java</artifactId>
  <version>1.0.1</version>
</dependency>
```

## Search jobs

```java
import com.jobvetta.JobvettaClient;
import com.jobvetta.SearchOptions;

var client = new JobvettaClient(System.getenv("JOBVETTA_API_KEY"));
var results = client.searchJobs(
    new SearchOptions()
        .withQuery("java developer")
        .withLocation("Pune")
        .withDays(7)
        .withLimit(10)
);

results.jobs().forEach(job ->
    System.out.printf("%s — %s: %s%n", job.title(), job.company(), job.url())
);
```

Create a free key in the [Jobvetta dashboard](https://www.jobvetta.com/dashboard#/mcp).
The allowance is 50 requests per UTC day, shared with the
[hosted MCP server](https://www.jobvetta.com/mcp). Searches cover India and
return up to 10 records per request.

## Requirements

- Java 17+
- Jackson Databind 2.22+

## License

MIT
