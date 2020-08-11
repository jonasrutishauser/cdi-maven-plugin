package test;

import javax.ejb.Stateless;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Stateless
public class TestBean {

    @Inject
    @ConfigProperty(name="additional.test.property")
    private String foo;

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    private JsonWebToken callerPrincipal;
    @Inject
    @Claim(value="exp", standard=Claims.iat)
    private Long timeClaim;

    @Inject
    @RestClient
    private TestServiceClient client;

    @Retry
    public void test() {
    }

}
