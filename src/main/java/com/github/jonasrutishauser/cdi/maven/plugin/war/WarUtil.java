package com.github.jonasrutishauser.cdi.maven.plugin.war;

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
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.enterprise.inject.spi.Extension;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.api.TypeDiscoveryConfiguration;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.EEModuleDescriptor;
import org.jboss.weld.bootstrap.spi.EEModuleDescriptor.ModuleType;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.helpers.EEModuleDescriptorImpl;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.environment.deployment.discovery.DiscoveryStrategy;
import org.jboss.weld.environment.deployment.discovery.DiscoveryStrategyFactory;
import org.jboss.weld.resources.ClassLoaderResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;

import com.github.jonasrutishauser.cdi.maven.plugin.ArchiveUtil;
import com.github.jonasrutishauser.cdi.maven.plugin.ear.EjbUtil;
import com.github.jonasrutishauser.cdi.maven.plugin.weld.WarBeanArchiveScanner;
import com.github.jonasrutishauser.cdi.maven.plugin.weld.WarDeployment;

public class WarUtil extends ArchiveUtil {

    private final ArchiverManager archiverManager;
    private final EjbUtil ejbUtil = new EjbUtil();

    private File war;
    private URLClassLoader warClassloader;

    public WarUtil(ArchiverManager archiverManager) {
        this.archiverManager = archiverManager;
    }

    public static WarUtil create(ArchiverManager archiverManager, File workDirectory, File warFile,
            ClassLoader earClassloader) throws MalformedURLException {
        WarUtil util = new WarUtil(archiverManager);
        util.init(workDirectory, warFile, earClassloader);
        return util;
    }

    @Override
    public void init(File workDirectory, File warFile, ClassLoader parentClassloader) throws MalformedURLException {
        UnArchiver unArchiver;
        try {
            unArchiver = archiverManager.getUnArchiver(warFile);
        } catch (NoSuchArchiverException e) {
            throw new IllegalStateException(e);
        }
        File extractedDir = new File(workDirectory, warFile.getName());
        this.war = extractedDir;
        extractedDir.mkdirs();
        unArchiver.setDestDirectory(extractedDir);
        unArchiver.setSourceFile(warFile);
        unArchiver.extract();
        File libDir = new File(extractedDir, "WEB-INF/lib");
        File[] jars = libDir.listFiles(file -> file.getName().endsWith(".jar"));
        List<URL> urls = new ArrayList<>();
        urls.add(new File(extractedDir, WarBeanArchiveScanner.CLASSES).toURI().toURL());
        if (jars != null) {
            for (File jar : jars) {
                urls.add(jar.toURI().toURL());
            }
        }
        warClassloader = new URLClassLoader(urls.toArray(new URL[urls.size()]), parentClassloader);
    }

    @Override
    public Deployment createDeployment(CDI11Bootstrap bootstrap) throws MojoExecutionException {
        Set<Metadata<Extension>> extensions = StreamSupport
                .stream(bootstrap.loadExtensions(warClassloader).spliterator(), false).collect(Collectors.toSet());
        addDefaultExtensions(extensions);
        TypeDiscoveryConfiguration typeDiscoveryConfiguration = bootstrap.startExtensions(extensions);
        Set<WeldBeanDeploymentArchive> archives = createArchives(bootstrap,
                typeDiscoveryConfiguration.getKnownBeanDefiningAnnotations());
        ResourceLoader resourceLoader = archives.iterator().next().getServices().get(ResourceLoader.class);
        return new WarDeployment(resourceLoader, bootstrap, archives, extensions);
    }

    @Override
    public ClassLoader getClassLoader() {
        return warClassloader;
    }

    public Set<WeldBeanDeploymentArchive> createArchives(CDI11Bootstrap bootstrap,
            Set<Class<? extends Annotation>> beanDefiningAnnotations) {
        Set<Class<? extends Annotation>> annotations = new HashSet<>(beanDefiningAnnotations);
        ejbUtil.addBeanDefiningAnnotations(annotations);
        DiscoveryStrategy strategy = DiscoveryStrategyFactory.create(new ClassLoaderResourceLoader(warClassloader),
                bootstrap, annotations, false);
        strategy.setScanner(new WarBeanArchiveScanner(bootstrap, war, warClassloader.getURLs()));
        Set<WeldBeanDeploymentArchive> archives = strategy.performDiscovery().stream().map(ejbUtil::addEjbDescriptors)
                .collect(Collectors.toSet());
        archives.forEach(this::addServices);
        return archives;
    }

    private void addServices(WeldBeanDeploymentArchive archive) {
        archive.getServices().add(EEModuleDescriptor.class,
                new EEModuleDescriptorImpl(archive.getId(), ModuleType.WEB));
    }

}
