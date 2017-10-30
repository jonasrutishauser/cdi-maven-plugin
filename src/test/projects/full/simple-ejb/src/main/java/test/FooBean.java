package test;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

@Stateless
public class FooBean implements Foo {

    @Override
    public void test() {
    }

}
