package com.github.jonasrutishauser.cdi.maven.plugin.weld;

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

import java.util.Set;

import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.environment.deployment.WeldDeployment;
import org.jboss.weld.injection.spi.InjectionServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.security.spi.SecurityServices;
import org.jboss.weld.transaction.spi.TransactionServices;

import com.github.jonasrutishauser.cdi.maven.plugin.weld.services.MockEjbServices;
import com.github.jonasrutishauser.cdi.maven.plugin.weld.services.MockInjectionServices;
import com.github.jonasrutishauser.cdi.maven.plugin.weld.services.MockSecurityServices;
import com.github.jonasrutishauser.cdi.maven.plugin.weld.services.MockTransactionServices;

public abstract class AbstractDeployment extends WeldDeployment {

    private final InjectionServices injectionServices = new MockInjectionServices();

    protected AbstractDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap,
            Set<WeldBeanDeploymentArchive> beanDeploymentArchives, Iterable<Metadata<Extension>> extensions) {
        super(resourceLoader, bootstrap, beanDeploymentArchives, extensions);
        beanDeploymentArchives
                .forEach(archive -> archive.getServices().add(InjectionServices.class, injectionServices));
        registerMockServices(getServices());
    }

    private void registerMockServices(ServiceRegistry services) {
        services.add(SecurityServices.class, new MockSecurityServices());
        services.add(TransactionServices.class, new MockTransactionServices());
        services.add(InjectionServices.class, injectionServices);
        services.add(EjbServices.class, new MockEjbServices());
    }

}
