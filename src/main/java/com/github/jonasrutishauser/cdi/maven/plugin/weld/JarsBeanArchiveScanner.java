package com.github.jonasrutishauser.cdi.maven.plugin.weld;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.AbstractWeldDeployment;
import org.jboss.weld.environment.deployment.discovery.AbstractBeanArchiveScanner;

public class JarsBeanArchiveScanner extends AbstractBeanArchiveScanner {
    private final Collection<File> jars;

    public JarsBeanArchiveScanner(Bootstrap bootstrap, Collection<File> jars) {
        super(bootstrap);
        this.jars = jars;
    }

    @Override
    public List<ScanResult> scan() {
        return jars.stream().map(this::getScanResult).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<ScanResult> getScanResult(File jar) {
        String beanArchiveRef = jar.getPath();
        try {
            BeansXml beansXml = parseBeansXml(new URL("jar:" + jar.toURI() + "!/" + AbstractWeldDeployment.BEANS_XML));
            return accept(beansXml) ? Optional.of(new ScanResult(beansXml, beanArchiveRef))
                    : Optional.empty();
        } catch (Exception e) {
            return Optional.of(new ScanResult(null, beanArchiveRef));
        }
    }
}