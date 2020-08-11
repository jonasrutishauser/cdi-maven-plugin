package com.github.jonasrutishauser.cdi.maven.plugin.ear;

import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

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
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.enterprise.inject.spi.Extension;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

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
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.jonasrutishauser.cdi.maven.plugin.ArchiveUtil;
import com.github.jonasrutishauser.cdi.maven.plugin.war.WarUtil;
import com.github.jonasrutishauser.cdi.maven.plugin.weld.EarDeployment;
import com.github.jonasrutishauser.cdi.maven.plugin.weld.JarsBeanArchiveScanner;

public class EarUtil extends ArchiveUtil {

    private final ArchiverManager archiverManager;
    private final EjbUtil ejbUtil = new EjbUtil();

    private final Set<File> ejbs = new HashSet<>();
    private final Set<File> libraries = new HashSet<>();
    private final Set<WarUtil> wars = new HashSet<>();

    private ClassLoader earClassloader;

    public EarUtil(ArchiverManager archiverManager) {
        this.archiverManager = archiverManager;
    }

    public Deployment createDeployment(CDI11Bootstrap bootstrap) throws MojoExecutionException {
        ResourceLoader resourceLoader = new ClassLoaderResourceLoader(earClassloader);
        Set<Metadata<Extension>> extensions = getExtensions(bootstrap);
        addDefaultExtensions(extensions);
        TypeDiscoveryConfiguration typeDiscoveryConfiguration = bootstrap.startExtensions(extensions);
        return new EarDeployment(resourceLoader, bootstrap, getBeanDeploymentArchives(resourceLoader, bootstrap,
                typeDiscoveryConfiguration.getKnownBeanDefiningAnnotations()), extensions);
    }

    @Override
    public ClassLoader getClassLoader() {
        return earClassloader;
    }

    private Set<WeldBeanDeploymentArchive> getBeanDeploymentArchives(ResourceLoader resourceLoader,
            CDI11Bootstrap bootstrap, Set<Class<? extends Annotation>> beanDefiningAnnotations) {
        return Stream
                .of(getEjbArchives(resourceLoader, bootstrap, beanDefiningAnnotations),
                        getLibraryArchives(resourceLoader, bootstrap, beanDefiningAnnotations),
                        getWarArchives(bootstrap, beanDefiningAnnotations))
                .flatMap(Set::stream).collect(Collectors.toCollection(HashSet::new));
    }

    private Set<WeldBeanDeploymentArchive> getWarArchives(CDI11Bootstrap bootstrap,
            Set<Class<? extends Annotation>> beanDefiningAnnotations) {
        return wars.stream().map(util -> util.createArchives(bootstrap, beanDefiningAnnotations)).flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private Set<WeldBeanDeploymentArchive> getLibraryArchives(ResourceLoader resourceLoader, CDI11Bootstrap bootstrap,
            Set<Class<? extends Annotation>> beanDefiningAnnotations) {
        DiscoveryStrategy strategy = DiscoveryStrategyFactory.create(resourceLoader, bootstrap, beanDefiningAnnotations,
                false);
        strategy.setScanner(new JarsBeanArchiveScanner(bootstrap, libraries));
        Set<WeldBeanDeploymentArchive> archives = strategy.performDiscovery();
        archives.forEach(archive -> archive.getServices().add(EEModuleDescriptor.class,
                new EEModuleDescriptorImpl(archive.getId(), ModuleType.EAR)));
        return archives;
    }

    private Set<WeldBeanDeploymentArchive> getEjbArchives(ResourceLoader resourceLoader, CDI11Bootstrap bootstrap,
            Set<Class<? extends Annotation>> beanDefiningAnnotations) {
        Set<Class<? extends Annotation>> annotations = new HashSet<>(beanDefiningAnnotations);
        ejbUtil.addBeanDefiningAnnotations(annotations);
        DiscoveryStrategy strategy = DiscoveryStrategyFactory.create(resourceLoader, bootstrap, annotations, false);
        strategy.setScanner(new JarsBeanArchiveScanner(bootstrap, ejbs));
        return strategy.performDiscovery().stream().map(ejbUtil::addEjbDescriptors).collect(Collectors.toSet());
    }

    private Set<Metadata<Extension>> getExtensions(CDI11Bootstrap bootstrap) {
        return Stream
                .of(Collections.singleton(earClassloader),
                        wars.stream().map(WarUtil::getClassLoader).collect(Collectors.toSet()))
                .flatMap(Collection::stream)
                .flatMap(
                        classloader -> StreamSupport.stream(bootstrap.loadExtensions(classloader).spliterator(), false))
                .collect(Collectors.toSet());
    }

    @Override
    public void init(File workDirectory, File earFile, ClassLoader parentClassloader) throws MalformedURLException {
        UnArchiver unArchiver;
        try {
            unArchiver = archiverManager.getUnArchiver(earFile);
        } catch (NoSuchArchiverException e) {
            throw new IllegalStateException(e);
        }
        File extractedDir = new File(workDirectory, earFile.getName());
        extractedDir.mkdirs();
        unArchiver.setDestDirectory(extractedDir);
        unArchiver.setSourceFile(earFile);
        unArchiver.extract();
        File libDir = new File(extractedDir, "lib");
        Set<File> warFiles = new HashSet<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setAttribute(ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(ACCESS_EXTERNAL_SCHEMA, "");
            factory.setFeature(FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document doc = documentBuilder.parse(new File(extractedDir, "META-INF/application.xml"));
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList modules = (NodeList) xPath.evaluate("//module/*", doc, XPathConstants.NODESET);
            for (int i = 0; i < modules.getLength(); i++) {
                Element module = (Element) modules.item(i);
                if ("ejb".equals(module.getNodeName())) {
                    ejbs.add(new File(extractedDir, module.getTextContent()));
                } else if ("web".equals(module.getNodeName())) {
                    File warFile = new File(extractedDir,
                            (String) xPath.evaluate("web-uri/text()", module, XPathConstants.STRING));
                    warFiles.add(warFile);
                } else {
                    libraries.add(new File(extractedDir, module.getTextContent()));
                }
            }
            Element libraryDirectory = (Element) xPath.evaluate("//library-directory", doc, XPathConstants.NODE);
            if (libraryDirectory != null) {
                libDir = new File(extractedDir, libraryDirectory.getTextContent());
            }
        } catch (ParserConfigurationException | SAXException | XPathException | IOException e) {
            LoggerFactory.getLogger(getClass()).warn("failed to read deployment descriptor of ear", e);
        }
        File[] jars = libDir.listFiles(file -> file.getName().endsWith(".jar"));
        if (jars != null) {
            libraries.addAll(Arrays.asList(jars));
        }
        List<URL> urls = new ArrayList<>(ejbs.size() + libraries.size());
        for (File ejb : ejbs) {
            urls.add(ejb.toURI().toURL());
        }
        for (File library : libraries) {
            urls.add(library.toURI().toURL());
        }
        earClassloader = new URLClassLoader(urls.toArray(new URL[urls.size()]), parentClassloader);
        for (File warFile : warFiles) {
            wars.add(WarUtil.create(archiverManager, workDirectory, warFile, earClassloader));
        }
    }

}
