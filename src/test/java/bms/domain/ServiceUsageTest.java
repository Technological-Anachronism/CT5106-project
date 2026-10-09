package bms.domain;

import bms.domain.PortalDomain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Makes sure the Use rule works without opening the website.
// Run with .\mvnw.cmd test
class ServiceUsageTest {

    private final PortalService repairs = new PortalService(
            new ServiceId("repairs"),
            "Repairs",
            "Report a problem",
            List.of(
                    new PortalFeature(new FeatureId("create-ticket"), "Create ticket", true),
                    new PortalFeature(new FeatureId("export-invoice"), "Export invoice", false)
            )
    );

    // Happy path. Feature is on, so we get a STARTED usage.
    @Test
    void startsAUsageWhenTheFeatureIsEnabled() {
        ServiceUsage usage = ServiceUsage.start(
                new UsageId("usage-1"),
                "portal-user",
                repairs,
                new FeatureId("create-ticket")
        );

        assertEquals(UsageStatus.STARTED, usage.getStatus());
        assertEquals("Create ticket", usage.getFeatureName());
        assertEquals("portal-user", usage.getUserId());
    }

    // The one we care about for the demo. Off means error, not a saved use.
    @Test
    void rejectsADisabledFeature() {
        PortalException error = assertThrows(PortalException.class, () -> ServiceUsage.start(
                new UsageId("usage-2"),
                "portal-user",
                repairs,
                new FeatureId("export-invoice")
        ));

        assertEquals("Export invoice is not available", error.getMessage());
    }

    // Someone hits a feature id that isn't on this service at all.
    @Test
    void rejectsAFeatureThatIsNotOnTheService() {
        assertThrows(PortalException.class, () -> ServiceUsage.start(
                new UsageId("usage-3"),
                "portal-user",
                repairs,
                new FeatureId("missing")
        ));
    }
}
