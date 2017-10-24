package com.github.jonasrutishauser.cdi.maven.plugin.weld;

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

    public AbstractDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap,
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
