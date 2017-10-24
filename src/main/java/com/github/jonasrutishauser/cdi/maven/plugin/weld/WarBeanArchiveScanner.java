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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.AbstractWeldDeployment;

public final class WarBeanArchiveScanner extends JarsBeanArchiveScanner {

    public static final String CLASSES = "WEB-INF/classes/";
    private static final String BEANS_XML = "WEB-INF/beans.xml";

    private final File war;

    public WarBeanArchiveScanner(Bootstrap bootstrap, File war, URL[] classLoaderUrls) {
        super(bootstrap, Arrays.stream(classLoaderUrls).filter(WarBeanArchiveScanner::isWarLibrary).map(URL::toString)
                .map(URI::create).map(URI::getSchemeSpecificPart).map(File::new).collect(Collectors.toSet()));
        this.war = war;
    }

    private static boolean isWarLibrary(URL url) {
        return !url.getPath().endsWith(CLASSES);
    }

    @Override
    public List<ScanResult> scan() {
        File beansXmlFile = new File(war, BEANS_XML);
        if (!beansXmlFile.exists()) {
            beansXmlFile = new File(war, CLASSES + AbstractWeldDeployment.BEANS_XML);
        }
        BeansXml beansXml;
        try {
            beansXml = beansXmlFile.exists() ? parseBeansXml(beansXmlFile.toURI().toURL()) : null;
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        List<ScanResult> result = new ArrayList<>();
        result.add(new ScanResult(beansXml, new File(war, CLASSES).toString() + "/"));
        result.addAll(super.scan());
        return result;
    }

}
