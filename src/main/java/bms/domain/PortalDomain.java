package bms.domain;

import java.time.Instant;
import java.util.*;

// My part of the project (story 3.1). This file is the rules, not the web page.
// If you need to change what "using a feature" means, change it here, not in the controller.
public final class PortalDomain {

    // Just a holder so the classes below can live in one file. Don't new this up.
    private PortalDomain() {
    }

    // Tiny wrappers so a service id and a feature id can't get mixed up.
    // They're just strings underneath.
    public record ServiceId(String value) {
        public ServiceId {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("service id is required");
            }
        }
    }

    // Same idea as ServiceId, but for a feature inside a service.
    public record FeatureId(String value) {
        public FeatureId {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("feature id is required");
            }
        }
    }

    // One id per time someone hits Use. Made up in PortalUseCases with a UUID.
    public record UsageId(String value) {
        public UsageId {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("usage id is required");
            }
        }
    }

    // Only STARTED for now. Add FINISHED or CANCELLED here if we need them later.
    public enum UsageStatus {
        STARTED
    }

    // Normal error for "you can't do that". The controller turns this into the red message on the page.
    public static class PortalException extends RuntimeException {
        public PortalException(String message) {
            super(message);
        }
    }

    // One thing the user can do inside a service, like "Create ticket".
    // enabled = false means it still shows up, but the Use button stays off.
    public static class PortalFeature {
        private final FeatureId id;
        private final String name;
        private final boolean enabled;

        public PortalFeature(FeatureId id, String name, boolean enabled) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("feature name is required");
            }
            this.id = id;
            this.name = name;
            this.enabled = enabled;
        }

        public FeatureId getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    // The card on the portal. A service has a list of features.
    // Caoilfhionn, when your real service data exists, this is the shape the portal expects.
    public static class PortalService {
        private final ServiceId id;
        private final String name;
        private final String description;
        private final List<PortalFeature> features;

        public PortalService(ServiceId id, String name, String description, List<PortalFeature> features) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("service name is required");
            }
            this.id = id;
            this.name = name;
            this.description = description == null ? "" : description;
            this.features = List.copyOf(features);
        }

        public ServiceId getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public List<PortalFeature> getFeatures() {
            return features;
        }

        // Used before we let someone hit Use. If the feature isn't on this service, we stop.
        public Optional<PortalFeature> findFeature(FeatureId featureId) {
            return features.stream()
                    .filter(feature -> feature.getId().equals(featureId))
                    .findFirst();
        }
    }

    // One row of "this user started this feature". This is the bit that isn't just create/read/update/delete.
    // Abdulhaq, if Use should also open a ticket, call your code from start() after the checks pass.
    // Senlei, these saved usages are what you'd count for analytics.
    public static class ServiceUsage {
        private final UsageId id;
        private final String userId;
        private final ServiceId serviceId;
        private final FeatureId featureId;
        private final String serviceName;
        private final String featureName;
        private final UsageStatus status;
        private final Instant createdAt;

        private ServiceUsage(
                UsageId id,
                String userId,
                ServiceId serviceId,
                FeatureId featureId,
                String serviceName,
                String featureName,
                UsageStatus status,
                Instant createdAt
        ) {
            this.id = id;
            this.userId = userId;
            this.serviceId = serviceId;
            this.featureId = featureId;
            this.serviceName = serviceName;
            this.featureName = featureName;
            this.status = status;
            this.createdAt = createdAt;
        }

        // Don't skip this and just save a usage yourself. The checks are the whole point.
        // No user, wrong feature, or feature turned off = error, nothing saved.
        public static ServiceUsage start(UsageId id, String userId, PortalService service, FeatureId featureId) {
            if (userId == null || userId.isBlank()) {
                throw new PortalException("A user is required to use a service");
            }
            PortalFeature feature = service.findFeature(featureId)
                    .orElseThrow(() -> new PortalException("That feature is not part of this service"));
            if (!feature.isEnabled()) {
                throw new PortalException(feature.getName() + " is not available");
            }
            return new ServiceUsage(
                    id,
                    userId,
                    service.getId(),
                    feature.getId(),
                    service.getName(),
                    feature.getName(),
                    UsageStatus.STARTED,
                    Instant.now()
            );
        }

        public UsageId getId() {
            return id;
        }

        public String getUserId() {
            return userId;
        }

        public ServiceId getServiceId() {
            return serviceId;
        }

        public FeatureId getFeatureId() {
            return featureId;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getFeatureName() {
            return featureName;
        }

        public UsageStatus getStatus() {
            return status;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }
    }

    // "Give me the services." The portal doesn't care if they come from a list or a database.
    // Caoilfhionn, your storage can replace InMemoryPortalStore as long as it still does these two methods.
    public interface PortalCatalog {
        List<PortalService> findAll();

        Optional<PortalService> findById(ServiceId id);
    }

    // Where finished Uses get stored. Same idea as the catalog, separate list.
    // Called findAllUsages and not findAll because the catalog already has a findAll.
    public interface ServiceUsageRepository {
        void save(ServiceUsage usage);

        List<ServiceUsage> findAllUsages();
    }
}
