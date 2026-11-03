package bms.analytics.domain;

import java.util.Optional;

/** Storage boundary. In-memory for Assignment 2; database implementation can replace it later. */
public interface AnalyticsRepository {
    void save(AnalyticsReport report);
    Optional<AnalyticsReport> findLatest();
}
