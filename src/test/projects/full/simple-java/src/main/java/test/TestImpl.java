package test;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.validation.Validator;

@Dependent
public class TestImpl {

    private final Foo foo;

    @Inject
    protected TestImpl(Foo foo, Validator validator) {
        this.foo = foo;
    }

}
