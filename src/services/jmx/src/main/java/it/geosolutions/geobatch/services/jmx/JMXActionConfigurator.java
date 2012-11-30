package it.geosolutions.geobatch.services.jmx;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

public class JMXActionConfigurator {

    public static ActionConfiguration configureAction(Logger LOGGER, final Map<String, String> config,
            ApplicationContext context) throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException,
            InvocationTargetException {

        final String serviceId = config.remove(ConsumerManager.SERVICE_ID_KEY);
        if (serviceId == null || serviceId.isEmpty())
            throw new IllegalArgumentException("Unable to locate the key "
                    + ConsumerManager.SERVICE_ID_KEY
                    + " matching the serviceId action in the passed paramether table");

        final ActionService service = (ActionService) context.getBean(serviceId);
        final Class serviceClass = service.getClass();

        ActionConfiguration actionConfig = null;
        for (Method method : serviceClass.getMethods()) {
            if (method.getName().equals("canCreateAction")) {
                final Class[] classes = method.getParameterTypes();

                Constructor constructor;

                // BeanUtils.instantiate(clazz)
                try {
                    constructor = classes[0].getConstructor(new Class[] {});
                    actionConfig = (ActionConfiguration) constructor.newInstance();
                } catch (NoSuchMethodException e) {
                    constructor = classes[0].getConstructor(new Class[] { String.class,
                            String.class, String.class });
                    actionConfig = (ActionConfiguration) constructor.newInstance(serviceId,
                            serviceId, serviceId);
                }
                actionConfig.setServiceID(serviceId);
                final Set<String> keys = config.keySet();
                final Iterator<String> it = keys.iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    try {
                        // TODO add a pluggable configurators from the context
                        String value = config.get(key);
                        if (value != null) {
                            smartCopy(LOGGER, actionConfig, key, value);
                        } else {
                            if (LOGGER.isWarnEnabled())
                                LOGGER.warn("Unable to find the parameter called: " + key);
                        }
                    } catch (Exception e) {
                        if (LOGGER.isErrorEnabled())
                            LOGGER.error(e.getLocalizedMessage(), e);
                        // TODO something else?
                    }
                }

                if (actionConfig != null)
                    break;
            }
        }

        return actionConfig;
    }

    private static <T> void smartCopy(final Logger LOGGER, final T bean, final String propertyName,
            final Object value) throws Exception {
        // special cases
        PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(bean, propertyName);
        // return null if there is no such descriptor
        if (pd == null) {
            return;
        }

        Class type = pd.getPropertyType();

        Object valueTo = value;
        if (type.isAssignableFrom(value.getClass())) {
            // try using setter
            if (pd.getWriteMethod() != null) {
                PropertyUtils.setProperty(bean, propertyName, valueTo);
                return;
            } else {
                // T interface doesn't declare setter method for this property
                // lets use getter methods to get the property reference
                Object property = PropertyUtils.getProperty(bean, propertyName);
                if (property == null) {
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error("Skipping unwritable property " + propertyName);
                } else {
                    if (Collection.class.isAssignableFrom(type)) {
                        ((Collection) property).addAll((Collection) value);
                    } else if (Map.class.isAssignableFrom(type)) {
                        ((Map) property).putAll((Map) value);
                    }
                }
            }
        } else if (Collection.class.isAssignableFrom(type)) {
            valueTo = adaptCollection(value);

        } else if (Map.class.isAssignableFrom(type)) {

            valueTo = adaptMap(value);

        } else {

            // fail
            // try quick way
            try {
                BeanUtils.copyProperty(bean, propertyName, valueTo);
                return;
            } catch (Exception e) {
                if (LOGGER.isWarnEnabled())
                    LOGGER.warn("Error using ");
            }
        }

        //
        // boolean status = true;
        // if (!status) {
        // if (LOGGER.isErrorEnabled())
        // LOGGER.error("Skipping unwritable property " + propertyName
        // + " unable to find the adapter for type " + pd.getPropertyType());
        // }
    }

    private static Map adaptMap(Object value) {
        Map<Object, Object> liveMap = new HashMap<Object, Object>();
        // value should be a list of key=value string ';' separated
        String[] listString = ((String) value).split(";");
        for (String kvString : listString) {
            String kv[] = kvString.split("=");
            liveMap.put(kv[0], kv[1]);
        }
        return liveMap;
    }

    private static Collection adaptCollection(Object value) {

        Collection<Object> liveCollection = new ArrayList<Object>();
        // value should be a list of string ',' separated
        String[] listString = ((String) value).split(",");
        for (String s : listString) {
            liveCollection.add(s);
        }
        return liveCollection;
    }

    /*
     * TODO make smart adapter extensible
     * 
     * public interface TypeAdapter<F, T> { public boolean adapt(Class<F> fromType, Class<T> toType, Object property, Object value); };
     * 
     * private final class StringToCollectionAdapter implements TypeAdapter<String, Map> {
     * 
     * @Override public boolean adapt(Class<String> fromType, Class<Map> toType, Object property, Object value) {
     * 
     * // check type of property to apply new value if (Map.class.isAssignableFrom(toType)) {
     * 
     * final Map<Object, Object> liveMap; if (property != null) { liveMap = (Map<Object, Object>)property; liveMap.clear(); } else { return false; }
     * if (fromType.isAssignableFrom(value.getClass())) { // value should be a list of key=value string ';' separated String[] listString =
     * ((String)value).split(";"); for (String kvString : listString) { String kv[] = kvString.split("="); liveMap.put(kv[0], kv[1]); } return true; }
     * }
     * 
     * return false; } }
     */

}
