package com.github.jonasrutishauser.cdi.maven.plugin.weld.services;

import org.jboss.weld.ejb.api.SessionObjectReference;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.ejb.spi.InterceptorBindings;

public class MockEjbServices implements EjbServices {

    @Override
    public void cleanup() {
        // nothing
    }

    @Override
    public SessionObjectReference resolveEjb(EjbDescriptor<?> ejbDescriptor) {
        return null;
    }

    @Override
    public void registerInterceptors(EjbDescriptor<?> ejbDescriptor, InterceptorBindings interceptorBindings) {
        // nothing
    }

}
