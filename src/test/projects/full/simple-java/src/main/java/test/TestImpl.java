package test;

import java.security.Principal;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

@Dependent
public class TestImpl {

    private final Foo foo;

    @Inject
    protected TestImpl(Foo foo, Validator validator, ValidatorFactory validatorFactory, UserTransaction transaction,
            Principal principal) {
        this.foo = foo;
    }

}
