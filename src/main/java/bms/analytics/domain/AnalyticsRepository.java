package bms.analytics.domain;

import java.util.Optional;

/** Storage boundary. In-memory for Assignment 2; database implementation can replace it later. */
/** Stores derived report snapshots independently of the ticket persistence adapter. */
public interface AnalyticsRepository {
    /** Replaces the latest derived snapshot.
     * @param report the snapshot to retain
     */
    void save(AnalyticsReport report);
    /** @return the latest report, or empty before the first calculation */
    Optional<AnalyticsReport> findLatest();
}
