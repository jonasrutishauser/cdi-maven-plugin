package test;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@WebServlet("/test")
public class TestWarImpl extends HttpServlet {

    private final Foo foo;

    @Inject
    protected TestWarImpl(Foo foo, ServletContext context, HttpSession session, HttpServletRequest request) {
        this.foo = foo;
    }

}
