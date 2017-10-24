package com.github.jonasrutishauser.cdi.maven.plugin.ear;

import java.io.File;
import java.net.MalformedURLException;

import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;

public interface ArchiveUtil {

    void init(File workDirectory, File archive) throws MalformedURLException;

    Deployment createDeployment(CDI11Bootstrap bootstrap);

}
