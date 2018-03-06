package com.github.jonasrutishauser.cdi.maven.plugin.weld.bootstrap;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.util.annotated.ForwardingAnnotatedType;

public class PredefinedBeansExtension implements Extension {

    private final Set<Type> types;

    public PredefinedBeansExtension(Type... types) {
        this.types = new HashSet<>(Arrays.asList(types));
    }

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery event, BeanManager beanManager) {
        AnnotatedType<Object> type = beanManager.createAnnotatedType(Object.class);
        event.addAnnotatedType(new ForwardingAnnotatedType<Object>() {
            @Override
            public AnnotatedType<Object> delegate() {
                return type;
            }

            @Override
            public Set<Type> getTypeClosure() {
                return types;
            }
        });
    }

}
