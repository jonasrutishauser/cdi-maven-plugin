package test;

import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

@WebServlet("/test")
public class TestWarImpl extends HttpServlet {

    private final Foo foo;

    @Inject
    protected TestWarImpl(Foo foo) {
        this.foo = foo;
    }

}
