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

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Remote;

import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.jboss.weld.ejb.spi.EjbDescriptor;

public class SimpleEjbDescriptor<T> implements EjbDescriptor<T> {

    private final Class<T> beanClass;
    private final String name;
    private final Collection<BusinessInterfaceDescriptor<?>> localBusinessInterfaces = new HashSet<>();

    public SimpleEjbDescriptor(Class<T> beanClass) {
        this(beanClass, beanClass.getSimpleName());
    }

    public SimpleEjbDescriptor(Class<T> beanClass, String name) {
        this.beanClass = beanClass;
        this.name = name;
        collectLocalInterfaces();
    }

    private void collectLocalInterfaces() {
        Local localAnnotation = beanClass.getAnnotation(Local.class);
        List<Class<?>> interfaces = Arrays.stream(beanClass.getInterfaces())
                .filter(Predicate.<Class<?>>isEqual(Serializable.class).negate()
                        .and(Predicate.<Class<?>>isEqual(Externalizable.class).negate()
                                .and(type -> !type.getName().startsWith("javax.ejb."))))
                .collect(Collectors.toList());
        if (localAnnotation != null && localAnnotation.value().length > 0) {
            for (Class<?> type : localAnnotation.value()) {
                localBusinessInterfaces.add(new BusinessInterface<>(type));
            }
        } else if (interfaces.size() == 1
                && (localAnnotation != null || !interfaces.get(0).isAnnotationPresent(Remote.class))) {
            localBusinessInterfaces.add(new BusinessInterface<>(beanClass.getInterfaces()[0]));
        }
        if (beanClass.isAnnotationPresent(LocalBean.class)
                || (localBusinessInterfaces.isEmpty() && noRemoteInterfaceDefined(interfaces))) {
            localBusinessInterfaces.add(new BusinessInterface<>(beanClass));
        }
    }

    private boolean noRemoteInterfaceDefined(List<Class<?>> interfaces) {
        return interfaces.isEmpty() && !beanClass.isAnnotationPresent(Remote.class);
    }

    @Override
    public Class<T> getBeanClass() {
        return beanClass;
    }

    @Override
    public Collection<BusinessInterfaceDescriptor<?>> getLocalBusinessInterfaces() {
        return localBusinessInterfaces;
    }

    @Override
    public Collection<BusinessInterfaceDescriptor<?>> getRemoteBusinessInterfaces() {
        return Collections.emptyList();
    }

    @Override
    public String getEjbName() {
        return name;
    }

    @Override
    public Collection<Method> getRemoveMethods() {
        return Collections.emptyList();
    }

    @Override
    public boolean isStateless() {
        return true;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public boolean isStateful() {
        return false;
    }

    @Override
    public boolean isMessageDriven() {
        return false;
    }

    @Override
    public boolean isPassivationCapable() {
        return false;
    }

    public void addLocalInterface(Class<?> type) {
        localBusinessInterfaces.add(new BusinessInterface<>(type));
    }

    private static class BusinessInterface<T> implements BusinessInterfaceDescriptor<T> {
        private final Class<T> type;

        public BusinessInterface(Class<T> type) {
            this.type = type;
        }

        @Override
        public Class<T> getInterface() {
            return type;
        }
    }

}
