package org.aksw.jenax.web.server.boot;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.GenericWebApplicationContext;

public interface ServletBuilder {
    WebApplicationInitializer build(GenericWebApplicationContext rootContext);
}
