package test;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class TestBean {

    @Inject
    private Foo foo;

    public void test() {
    }

}
