package com.github.jonasrutishauser.cdi.maven.plugin.weld;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.EEModuleDescriptor;
import org.jboss.weld.bootstrap.spi.EEModuleDescriptor.ModuleType;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.resources.spi.ResourceLoader;

public final class EarDeployment extends AbstractDeployment {

    public EarDeployment(ResourceLoader resourceLoader, Bootstrap bootstrap,
            Set<WeldBeanDeploymentArchive> beanDeploymentArchives, Iterable<Metadata<Extension>> extensions) {
        super(resourceLoader, bootstrap, beanDeploymentArchives, extensions);
    }

    @Override
    protected void setBeanDeploymentArchivesAccessibility() {
        getBeanDeploymentArchives().stream().map(WeldBeanDeploymentArchive.class::cast)
                .forEach(archive -> archive.setAccessibleBeanDeploymentArchives(getBeanDeploymentArchives().stream()
                        .filter(Predicate.isEqual(archive).negate()).map(WeldBeanDeploymentArchive.class::cast)
                        .filter(other -> isNotWarArchive(other) || isSameResourceLoader(archive, other))
                        .collect(Collectors.toSet())));
    }

    private boolean isSameResourceLoader(WeldBeanDeploymentArchive archive, WeldBeanDeploymentArchive other) {
        return archive.getServices().get(ResourceLoader.class) == other.getServices().get(ResourceLoader.class);
    }

    private boolean isNotWarArchive(WeldBeanDeploymentArchive archive) {
        EEModuleDescriptor moduleDescriptor = archive.getServices().get(EEModuleDescriptor.class);
        return moduleDescriptor == null || moduleDescriptor.getType() != ModuleType.WEB;
    }

}
