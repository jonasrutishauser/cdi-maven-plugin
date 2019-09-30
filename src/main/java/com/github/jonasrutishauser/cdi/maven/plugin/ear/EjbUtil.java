package com.github.jonasrutishauser.cdi.maven.plugin.ear;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ejb.MessageDriven;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.jboss.weld.bootstrap.spi.EEModuleDescriptor;
import org.jboss.weld.bootstrap.spi.EEModuleDescriptor.ModuleType;
import org.jboss.weld.bootstrap.spi.helpers.EEModuleDescriptorImpl;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.environment.deployment.WeldBeanDeploymentArchive;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.github.jonasrutishauser.cdi.maven.plugin.weld.SimpleEjbDescriptor;
import com.github.jonasrutishauser.cdi.maven.plugin.weld.WarBeanArchiveScanner;

public class EjbUtil {

    public void addBeanDefiningAnnotations(Set<Class<? extends Annotation>> annotations) {
        annotations.add(Stateless.class);
        annotations.add(Stateful.class);
        annotations.add(Singleton.class);
        annotations.add(MessageDriven.class);
    }

    public WeldBeanDeploymentArchive addEjbDescriptors(WeldBeanDeploymentArchive archive) {
        archive.getServices().add(EEModuleDescriptor.class,
                new EEModuleDescriptorImpl(archive.getId(), ModuleType.EJB_JAR));
        Predicate<Class<?>> isEjb = type -> type.isAnnotationPresent(Stateless.class)
                || type.isAnnotationPresent(Stateful.class) //
                || type.isAnnotationPresent(Singleton.class) //
                || type.isAnnotationPresent(MessageDriven.class);
        ResourceLoader resourceLoader = archive.getServices().get(ResourceLoader.class);
        Map<String, EjbDescriptor<?>> descriptors = archive.getBeanClasses().stream().map(resourceLoader::classForName)
                .filter(isEjb).collect(Collectors.toMap(Class::getSimpleName, SimpleEjbDescriptor::new, (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                }, HashMap::new));
        String archivePath = archive.getId();
        if (archivePath.endsWith(WarBeanArchiveScanner.CLASSES)) {
            addEjbsFromDescriptor(descriptors, resourceLoader,
                    new File(new File(archivePath).getParentFile(), "ejb-jar.xml").toURI().toString());
        } else {
            addEjbsFromDescriptor(descriptors, resourceLoader,
                    "jar:" + new File(archivePath).toURI() + "!/META-INF/ejb-jar.xml");
        }
        WeldBeanDeploymentArchive weldBeanDeploymentArchive = new WeldBeanDeploymentArchive(archive.getId(),
                archive.getBeanClasses(), archive.getBeansXml()) {
            @Override
            public Collection<EjbDescriptor<?>> getEjbs() {
                return descriptors.values();
            }
        };
        weldBeanDeploymentArchive.getServices().addAll(archive.getServices().entrySet());
        return weldBeanDeploymentArchive;
    }

    private void addEjbsFromDescriptor(Map<String, EjbDescriptor<?>> ejbs, ResourceLoader resourceLoader,
            String descriptorUrl) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document doc = documentBuilder.parse(descriptorUrl);
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList beans = (NodeList) xPath.evaluate("//enterprise-beans/*", doc, XPathConstants.NODESET);
            for (int i = 0; i < beans.getLength(); i++) {
                Element bean = (Element) beans.item(i);
                String name = getValue(bean, "ejb-name").orElseThrow(
                        () -> new IllegalArgumentException("required 'ejb-name' not found in deployment descriptor"));
                SimpleEjbDescriptor<?> descriptor = (SimpleEjbDescriptor<?>) ejbs.computeIfAbsent(name,
                        beanName -> new SimpleEjbDescriptor<>(
                                resourceLoader.classForName(
                                        getValue(bean, "ejb-class").orElseThrow(() -> new IllegalArgumentException(
                                                "required 'ejb-class' not found in deployment descriptor"))),
                                beanName));
                boolean hasInterface = hasInterface(bean);
                if (hasInterface) {
                    descriptor.getLocalBusinessInterfaces().clear();
                    if (getValue(bean, "local-bean").isPresent()) {
                        descriptor.addLocalInterface(descriptor.getBeanClass());
                    }
                    NodeList interfaces = bean.getElementsByTagName("business-local");
                    for (int j = 0; j < interfaces.getLength(); j++) {
                        descriptor.addLocalInterface(resourceLoader.classForName(interfaces.item(j).getTextContent()));
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | XPathException e) {
            LoggerFactory.getLogger(getClass()).warn("failed to read deployment descriptor in " + descriptorUrl, e);
        } catch (IOException e) {
            // ignore
        }
    }

    private boolean hasInterface(Element bean) {
        Set<String> interfaces = new HashSet<>(Arrays.asList("home", "remote", "local-home", "local", "business-local",
                "business-remote", "local-bean"));
        for (int i = 0; i < bean.getChildNodes().getLength(); i++) {
            Node node = bean.getChildNodes().item(i);
            if (node instanceof Element && interfaces.contains(node.getNodeName())) {
                return true;
            }
        }
        return false;
    }

    private Optional<String> getValue(Element element, String elementName) {
        NodeList elements = element.getElementsByTagName(elementName);
        if (elements.getLength() == 1) {
            return Optional.of(elements.item(0).getTextContent());
        }
        return Optional.empty();
    }

}
