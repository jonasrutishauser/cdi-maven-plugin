package test;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@ApplicationScoped
public class HealthChecks {

    @Produces
    @Liveness
    HealthCheck check1() {
        return () -> HealthCheckResponse.named("test").state(true).build();
    }

}
