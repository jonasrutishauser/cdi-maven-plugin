package com.github.jonasrutishauser.cdi.maven.plugin.weld.bootstrap;

import java.security.Principal;

import javax.enterprise.inject.Produces;
import javax.transaction.UserTransaction;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

class Dummy {

    @Produces
    public Validator getValidator() {
        throw new UnsupportedOperationException();
    }

    @Produces
    public ValidatorFactory getValidatorFactory() {
        throw new UnsupportedOperationException();
    }

    @Produces
    public Principal getPrincipal() {
        throw new UnsupportedOperationException();
    }

    @Produces
    public UserTransaction getUserTransaction() {
        throw new UnsupportedOperationException();
    }

}
