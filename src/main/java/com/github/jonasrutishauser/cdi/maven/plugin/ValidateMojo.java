package com.github.jonasrutishauser.cdi.maven.plugin;

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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolver;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.api.Environments;

import com.github.jonasrutishauser.cdi.maven.plugin.ear.EarUtil;
import com.github.jonasrutishauser.cdi.maven.plugin.war.WarUtil;

/**
 * Validates the CDI configuration of a war or ear.
 * 
 * @author Jonas Rutishauser
 */
@Mojo(name = "validate", defaultPhase = LifecyclePhase.VERIFY, requiresProject = true, threadSafe = true)
public class ValidateMojo extends AbstractMojo {

    private static final String SMALLRYE_GROUP_ID = "io.smallrye";

    private final MavenSession session;

    private final MavenProject project;

    private final ArchiverManager archiverManager;

    private final DependencyResolver dependencyResolver;

    private final Map<String, String> oldSystemProperties = new HashMap<>();

    @Parameter(defaultValue = "${project.build.directory}/cdi", readonly = true)
    private File workDirectory;

    @Parameter
    private Map<String, String> systemProperties = new HashMap<>();

    @Parameter
    private boolean microprofile = false;

    @Inject
    public ValidateMojo(MavenSession session, MavenProject project, ArchiverManager archiverManager,
            DependencyResolver dependencyResolver) {
        this.session = session;
        this.project = project;
        this.archiverManager = archiverManager;
        this.dependencyResolver = dependencyResolver;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        fixCdiApiInClassloader();
        ArchiveUtil util;
        if ("ear".equals(project.getPackaging())) {
            util = new EarUtil(archiverManager);
        } else if ("war".equals(project.getPackaging())) {
            util = new WarUtil(archiverManager);
        } else {
            return;
        }
        try {
            util.init(workDirectory, project.getArtifact().getFile(), getParentClassLoader());
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("failed to load ear", e);
        }
        systemProperties.forEach((key, value) -> oldSystemProperties.put(key, System.setProperty(key, value)));
        oldSystemProperties.put("org.jboss.logging.provider",
                System.setProperty("org.jboss.logging.provider", "slf4j"));
        CDI11Bootstrap bootstrap = new WeldBootstrap();
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(util.getClassLoader());
            bootstrap.startContainer(UUID.randomUUID().toString(), Environments.EE, util.createDeployment(bootstrap));
            bootstrap.startInitialization();
            bootstrap.deployBeans();
            bootstrap.validateBeans();
            bootstrap.endInitialization();
        } catch (Exception e) {
            throw new MojoFailureException("CDI error found: " + e.getMessage(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
            bootstrap.shutdown();
            oldSystemProperties.forEach((key, value) -> {
                if (value == null) {
                    System.clearProperty(key);
                } else {
                    System.setProperty(key, value);
                }
            });
        }
    }

    private void fixCdiApiInClassloader() {
        if (getClass().getClassLoader() instanceof ClassRealm) {
            ClassRealm ownRealm = (ClassRealm) getClass().getClassLoader();
            if (ownRealm.getImportClassLoader("javax.enterprise.inject") instanceof ClassRealm) {
                ownRealm.importFrom(new EmptyClassLoader(), "javax.enterprise.inject");
            }
        }
    }

    private ClassLoader getParentClassLoader() throws MojoExecutionException {
        if (microprofile) {
            List<URL> urls = new ArrayList<>();
            try {
                List<Dependency> managedDependencies = project.getDependencyManagement() == null ? new ArrayList<>() : project.getDependencyManagement().getDependencies();
                for (ArtifactResult resolved : dependencyResolver.resolveDependencies(
                        session.getProjectBuildingRequest(), getMicroprofileImplementations(),
                        managedDependencies, null)) {
                    urls.add(resolved.getArtifact().getFile().toURI().toURL());
                }
            } catch (DependencyResolverException | MalformedURLException e) {
                throw new MojoExecutionException("Failed to retrieve microprofile dependencies", e);
            }
            return new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());
        }
        return getClass().getClassLoader();
    }

    private Collection<Dependency> getMicroprofileImplementations() {
        Collection<Dependency> implementations = new ArrayList<>();
        Dependency configImpl = new Dependency();
        configImpl.setGroupId(SMALLRYE_GROUP_ID);
        configImpl.setArtifactId("smallrye-config");
        configImpl.setVersion("1.5.0");
        implementations.add(configImpl);
        Dependency faultToleranceApi = new Dependency();
        faultToleranceApi.setGroupId("org.eclipse.microprofile.fault-tolerance");
        faultToleranceApi.setArtifactId("microprofile-fault-tolerance-api");
        faultToleranceApi.setVersion("2.1.1");
        implementations.add(faultToleranceApi);
        Dependency healthApi = new Dependency();
        healthApi.setGroupId("org.eclipse.microprofile.health");
        healthApi.setArtifactId("microprofile-health-api");
        healthApi.setVersion("2.2");
        implementations.add(healthApi);
        Dependency metricsApi = new Dependency();
        metricsApi.setGroupId("org.eclipse.microprofile.metrics");
        metricsApi.setArtifactId("microprofile-metrics-api");
        metricsApi.setVersion("2.3");
        implementations.add(metricsApi);
        Dependency metricsImpl = new Dependency();
        metricsImpl.setGroupId(SMALLRYE_GROUP_ID);
        metricsImpl.setArtifactId("smallrye-metrics");
        metricsImpl.setVersion("2.4.2");
        implementations.add(metricsImpl);
        Dependency jwtImpl = new Dependency();
        jwtImpl.setGroupId(SMALLRYE_GROUP_ID);
        jwtImpl.setArtifactId("smallrye-jwt");
        jwtImpl.setVersion("2.2.0");
        implementations.add(jwtImpl);
        Dependency restClientImpl = new Dependency();
        restClientImpl.setGroupId(SMALLRYE_GROUP_ID);
        restClientImpl.setArtifactId("smallrye-rest-client");
        restClientImpl.setVersion("1.2.2");
        implementations.add(restClientImpl);
        return implementations;
    }

}
