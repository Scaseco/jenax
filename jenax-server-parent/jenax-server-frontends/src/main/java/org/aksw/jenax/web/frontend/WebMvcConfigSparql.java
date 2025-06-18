package org.aksw.jenax.web.frontend;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * A default WebMvcConfig for the SPARQL ecosystem.
 */
@Configuration
public class WebMvcConfigSparql
     extends WebMvcConfigurationSupport
{
    @Override
    public void configureDefaultServletHandling(
            DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/view").setViewName("redirect:/view/");
        registry.addViewController("/view/").setViewName("forward:/view/resource.html");

        registry.addViewController("/graph-explorer").setViewName("redirect:/graph-explorer/");
        registry.addViewController("/graph-explorer/").setViewName("forward:/graph-explorer/index.html");

        registry.addViewController("/snorql").setViewName("redirect:/snorql/");
        registry.addViewController("/snorql/").setViewName("forward:/snorql/index.html");

        registry.addViewController("/").setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/yasgui/**").addResourceLocations("classpath:/static/yasgui/");
//        registry.addResourceHandler("/view/**").addResourceLocations("classpath:/static/yasgui/");
//        registry.addResourceHandler("/snorql/**").addResourceLocations("classpath:/static/snorql/");
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }
}
