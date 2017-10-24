package com.github.jonasrutishauser.cdi.maven.plugin.weld.services;

/*
 * Copyright (C) 2017 Jonas Rutishauser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/lgpl-3.0.txt>.
 */

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