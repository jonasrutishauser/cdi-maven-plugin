package com.github.jonasrutishauser.cdi.maven.plugin.weld;

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
