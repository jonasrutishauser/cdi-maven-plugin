package com.github.jonasrutishauser.cdi.maven.plugin.weld;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.io.Serializable;

import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.Local;
import javax.ejb.LocalBean;
import javax.ejb.Remote;
import javax.ejb.RemoveException;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.jboss.weld.ejb.spi.BusinessInterfaceDescriptor;
import org.junit.Test;

public class SimpleEjbDescriptorTest {

    @Test
    public void collectLocalInterfaces_localBeanPresent() {
        SimpleEjbDescriptor<?> testee = new SimpleEjbDescriptor<>(LocalBeanBean.class);

        assertThat(testee.getLocalBusinessInterfaces(), contains(businessInterface(LocalBeanBean.class)));
    }

    @Test
    public void collectLocalInterfaces_noAnnotationPresent() {
        SimpleEjbDescriptor<?> testee = new SimpleEjbDescriptor<>(NoAnnotation.class);

        assertThat(testee.getLocalBusinessInterfaces(), contains(businessInterface(NoAnnotation.class)));
    }

    @Test
    public void collectLocalInterfaces_remoteAnnotationPresent() {
        SimpleEjbDescriptor<?> testee = new SimpleEjbDescriptor<>(RemoteBean.class);

        assertThat(testee.getLocalBusinessInterfaces(), is(empty()));
    }

    @Test
    public void collectLocalInterfaces_singleInterface() {
        SimpleEjbDescriptor<?> testee = new SimpleEjbDescriptor<>(SingleInteface.class);

        assertThat(testee.getLocalBusinessInterfaces(), contains(businessInterface(Comparable.class)));
    }

    @Test
    public void collectLocalInterfaces_remoteInterface() {
        SimpleEjbDescriptor<?> testee = new SimpleEjbDescriptor<>(RemoteInterfaceBean.class);

        assertThat(testee.getLocalBusinessInterfaces(), is(empty()));
    }

    @Test
    public void collectLocalInterfaces_localAnnotationWithInterfaces() {
        SimpleEjbDescriptor<?> testee = new SimpleEjbDescriptor<>(LocalInterfacesBean.class);

        assertThat(testee.getLocalBusinessInterfaces(),
                containsInAnyOrder(businessInterface(Comparable.class), businessInterface(RemoteInterface.class)));
    }

    @Test
    public void collectLocalInterfaces_localAnnotationPresent() {
        SimpleEjbDescriptor<?> testee = new SimpleEjbDescriptor<>(LocalAnnotationBean.class);

        assertThat(testee.getLocalBusinessInterfaces(), contains(businessInterface(RemoteInterface.class)));
    }

    @LocalBean
    private static class LocalBeanBean {}

    private static class NoAnnotation {}

    @Remote(Comparable.class)
    private static class RemoteBean {}

    @Remote
    private static interface RemoteInterface {}
    private static class RemoteInterfaceBean implements RemoteInterface {}

    @Local({Comparable.class, RemoteInterface.class})
    private static class LocalInterfacesBean {}

    @Local
    private static class LocalAnnotationBean implements RemoteInterface {}

    private static class SingleInteface implements Comparable<Object>, Serializable, EJBLocalObject {
        private static final long serialVersionUID = 1L;

        @Override
        public int compareTo(Object o) {
            return 0;
        }

        @Override
        public EJBLocalHome getEJBLocalHome() throws EJBException {
            return null;
        }

        @Override
        public Object getPrimaryKey() throws EJBException {
            return null;
        }

        @Override
        public void remove() throws RemoveException, EJBException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isIdentical(EJBLocalObject obj) throws EJBException {
            return false;
        }
    }

    private static Matcher<BusinessInterfaceDescriptor<?>> businessInterface(Class<?> interfaceClass) {
        return new BusinessInterfaceMatcher(interfaceClass);
    }

    private static class BusinessInterfaceMatcher extends CustomMatcher<BusinessInterfaceDescriptor<?>> {

        private final Class<?> interfaceClass;

        private BusinessInterfaceMatcher(Class<?> interfaceClass) {
            super("BusinessInterface " + interfaceClass.getName());
            this.interfaceClass = interfaceClass;
        }

        @Override
        public boolean matches(Object object) {
            return object instanceof BusinessInterfaceDescriptor
                    && ((BusinessInterfaceDescriptor<?>) object).getInterface() == interfaceClass;
        }
        
    }

}
