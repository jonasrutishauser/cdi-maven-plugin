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
