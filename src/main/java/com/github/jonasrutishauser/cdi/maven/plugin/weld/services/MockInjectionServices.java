package com.github.jonasrutishauser.cdi.maven.plugin.weld.services;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.weld.injection.spi.InjectionContext;
import org.jboss.weld.injection.spi.InjectionServices;

public final class MockInjectionServices implements InjectionServices {

    @Override
    public void cleanup() {
        // nothing
    }

    @Override
    public <T> void aroundInject(InjectionContext<T> injectionContext) {
        injectionContext.proceed();
    }

    @Override
    public <T> void registerInjectionTarget(InjectionTarget<T> injectionTarget, AnnotatedType<T> annotatedType) {
        // nothing
    }
    
}