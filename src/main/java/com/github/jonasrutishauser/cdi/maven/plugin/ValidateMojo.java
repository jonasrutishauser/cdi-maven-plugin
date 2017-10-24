package com.github.jonasrutishauser.cdi.maven.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.slf4j.LoggerFactory;

import com.github.jonasrutishauser.cdi.maven.plugin.ear.ArchiveUtil;
import com.github.jonasrutishauser.cdi.maven.plugin.ear.EarUtil;
import com.github.jonasrutishauser.cdi.maven.plugin.ear.WarUtil;

@Mojo(name = "validate", defaultPhase = LifecyclePhase.VERIFY,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresProject = true, threadSafe = true)
public class ValidateMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}/cdi", readonly = true)
    private File workDirectory;

    @Component
    private ArchiverManager archiverManager;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ArchiveUtil util;
        if ("ear".equals(project.getPackaging())) {
            util = new EarUtil(archiverManager);
        } else if ("war".equals(project.getPackaging())) {
            util = new WarUtil(archiverManager);
        } else {
            return;
        }
        try {
            util.init(workDirectory, project.getArtifact().getFile());
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("failed to load ear", e);
        }
        System.setProperty("org.jboss.logging.provider", "slf4j");
        CDI11Bootstrap bootstrap = new WeldBootstrap();
        try {
            bootstrap.startContainer(UUID.randomUUID().toString(), Environments.EE, util.createDeployment(bootstrap));
            bootstrap.startInitialization();
            bootstrap.deployBeans();
            bootstrap.validateBeans();
            bootstrap.endInitialization();
        } catch (Throwable e) {
            LoggerFactory.getLogger(getClass()).error(e.getMessage());
            throw new MojoFailureException("CDI error found: " + e.getMessage(), e);
        } finally {
            bootstrap.shutdown();
        }
    }

}
