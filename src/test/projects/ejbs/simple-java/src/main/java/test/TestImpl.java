package test;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class TestImpl {

    private final Foo foo;

    @Inject
    protected TestImpl(Foo foo) {
        this.foo = foo;
    }

}
