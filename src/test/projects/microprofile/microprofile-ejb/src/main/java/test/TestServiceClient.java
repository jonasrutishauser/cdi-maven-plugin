package test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri="http://localhost/test")
public interface TestServiceClient {

    @GET
    @Path("/greet")
    String greet();

}
