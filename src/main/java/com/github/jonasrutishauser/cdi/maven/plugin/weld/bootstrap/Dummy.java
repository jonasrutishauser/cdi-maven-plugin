package com.github.jonasrutishauser.cdi.maven.plugin.weld.bootstrap;

import java.security.Principal;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;

@Typed({Validator.class, ValidatorFactory.class, Principal.class, UserTransaction.class})
class Dummy implements Validator, ValidatorFactory, Principal, UserTransaction {

    @Override
    public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value,
            Class<?>... groups) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExecutableValidator forExecutables() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Validator getValidator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValidatorContext usingContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MessageInterpolator getMessageInterpolator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TraversableResolver getTraversableResolver() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConstraintValidatorFactory getConstraintValidatorFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParameterNameProvider getParameterNameProvider() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void commit()
            throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rollback() throws SystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRollbackOnly() throws SystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatus() throws SystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

}
