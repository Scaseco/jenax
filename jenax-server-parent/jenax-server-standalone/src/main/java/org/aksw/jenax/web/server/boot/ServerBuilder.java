package org.aksw.jenax.web.server.boot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.aksw.jenax.web.frontend.ServerUtils;
import org.aksw.jenax.web.util.WebAppInitUtils;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Server;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class ServerBuilder {
    protected Integer port;
    protected List<ServletBuilder> servletBuilders = new ArrayList<>();

    public int getPort() {
        return port;
    }

    public ServerBuilder setPort(int port) {
        this.port = port;

        return this;
    }

    public ServerBuilder addServletBuilder(ServletBuilder servletBuiler) {
        servletBuilders.add(servletBuiler);
        return this;
    }

    public Server create() {
        if (port == null) {
            port = 7531;
        }

        GenericWebApplicationContext rootContext = new GenericWebApplicationContext();
        List<WebApplicationInitializer> initializers = servletBuilders.stream().map(builder -> builder.build(rootContext)).collect(Collectors.toList());

        WebApplicationInitializer initializer = servletContext -> {
            WebAppInitUtils.defaultSetup(servletContext, rootContext);

            for (WebApplicationInitializer item : initializers) {
                item.onStartup(servletContext);
            }
        };

        Server result = ServerUtils.startServer(port, initializer);
        postProcess(result);

        return result;
    }

    public static void postProcess(Server server) {
        for (org.eclipse.jetty.server.Connector connector : server.getConnectors()) {
            if (connector instanceof AbstractConnector) {
                ((AbstractConnector) connector).setIdleTimeout(90 * 24 * 60 * 60);
            }
        }
    }

    public static ServerBuilder newBuilder() {
        return new ServerBuilder();
    }
}
