package datomic_jetty.impl;

import clojure.lang.IFn;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class ProxyHandler extends AbstractHandler {
    IFn fn;

    public ProxyHandler(IFn fn) {
        this.fn = fn;
    }

    @Override
    public void handle(String s,
                       Request base,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {
        fn.invoke(base, request, response);
    }
}
