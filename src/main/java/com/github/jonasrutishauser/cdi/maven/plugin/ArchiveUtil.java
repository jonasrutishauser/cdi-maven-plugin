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
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Set;

import javax.enterprise.inject.spi.Extension;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.maven.plugin.MojoExecutionException;
import org.jboss.weld.bootstrap.api.CDI11Bootstrap;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.bootstrap.spi.helpers.MetadataImpl;

import com.github.jonasrutishauser.cdi.maven.plugin.weld.bootstrap.PredefinedBeansExtension;

public abstract class ArchiveUtil {

    public abstract void init(File workDirectory, File archive, ClassLoader parentClassLoader)
            throws MalformedURLException;

    public abstract Deployment createDeployment(CDI11Bootstrap bootstrap) throws MojoExecutionException;

    public abstract ClassLoader getClassLoader();

    protected void addDefaultExtensions(Set<Metadata<Extension>> extensions) throws MojoExecutionException {
        extensions.add(MetadataImpl.from(new PredefinedBeansExtension(Validator.class, ValidatorFactory.class)));
        try {
            extensions.add(
                    MetadataImpl.from(getClassLoader().loadClass("io.smallrye.jwt.auth.cdi.SmallRyeJWTAuthCDIExtension")
                            .asSubclass(Extension.class).getDeclaredConstructor().newInstance()));
        } catch (ClassNotFoundException e) {
            // no microprofile
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException e) {
            throw new MojoExecutionException("Failed to create JWT extension", e);
        }
    }

}
