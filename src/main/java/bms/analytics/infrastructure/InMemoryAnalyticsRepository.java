package bms.analytics.infrastructure;

import bms.analytics.domain.AnalyticsReport;
import bms.analytics.domain.AnalyticsRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Repository
public class InMemoryAnalyticsRepository implements AnalyticsRepository {
    // Just the latest snapshot is enough for this first version.
    private final AtomicReference<AnalyticsReport> latest = new AtomicReference<>();

    @Override
    public void save(AnalyticsReport report) {
        latest.set(report);
    }

    @Override
    public Optional<AnalyticsReport> findLatest() {
        return Optional.ofNullable(latest.get());
    }
}
