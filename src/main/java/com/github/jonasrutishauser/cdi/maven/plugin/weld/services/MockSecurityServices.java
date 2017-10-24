package com.github.jonasrutishauser.cdi.maven.plugin.weld.services;

import java.security.Principal;

import org.jboss.weld.security.spi.SecurityServices;

public final class MockSecurityServices implements SecurityServices {
    @Override
    public void cleanup() {
        // nothing
    }

    @Override
    public Principal getPrincipal() {
        return null;
    }
}