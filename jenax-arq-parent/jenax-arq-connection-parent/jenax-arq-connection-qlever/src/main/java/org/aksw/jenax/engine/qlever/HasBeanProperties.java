package org.aksw.jenax.engine.qlever;

import java.lang.reflect.InvocationTargetException;

import org.aksw.jenax.dataaccess.sparql.creator.HasProperties;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HasBeanProperties<X>
    extends HasProperties<X>
{
    @Override
    default X setProperty(String key, Object value) {
        X bean = self();
        try {
            PropertyUtils.setProperty(bean, key, value);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Logger logger = LoggerFactory.getLogger(HasBeanProperties.class);
            logger.warn("Could not set property: (" + key + ", " + value + ") on " + bean, e);
        }
        return bean;
    }

    @Override
    @SuppressWarnings("unchecked")
    default <T> T getProperty(String key) {
        X bean = self();
        Object r;
        try {
            r = PropertyUtils.getProperty(bean, key);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Logger logger = LoggerFactory.getLogger(HasBeanProperties.class);
            logger.warn("No such property " + key + " on " + bean, e);
            r = null;
        }
        return (T)r;
    }
}
