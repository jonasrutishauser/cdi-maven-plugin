package com.github.jonasrutishauser.cdi.maven.plugin.weld;

import java.util.Set;

import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.resources.spi.ResourceLoader;

public final class WarDeployment extends AbstractDeployment {

    public WarDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap,
            Set<WeldBeanDeploymentArchive> beanDeploymentArchives, Iterable<Metadata<Extension>> extensions) {
        super(resourceLoader, bootstrap, beanDeploymentArchives, extensions);
    }

}
