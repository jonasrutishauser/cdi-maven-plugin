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