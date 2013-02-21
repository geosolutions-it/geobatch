package it.geosolutions.tools.commons.beans;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

public abstract class BeanUtils {

    /**
     * Print all properties for a given bean. It's useful for overwriting toString method.
     * 
     * @param bean Object to get all properties from.
     * @param showNulls Determine if you wether you want to show null properties or not.
     * @return String representing bean state.
     * @author andres santana
     */
    public static String toStringBean(Object bean, boolean showNulls) {
        if (bean == null)
            return null;
        StringBuilder sb = new StringBuilder(bean.getClass().getName()).append("[");
        // new ToStringCreator(this)
        try {
            BeanInfo bi = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] pd = bi.getPropertyDescriptors();
            for (int i = 0; i < pd.length; i++) {
                if (!"class".equals(pd[i].getName())) {
                    Object result = pd[i].getReadMethod().invoke(bean);
                    if (showNulls || result != null) {
                        sb.append(pd[i].getDisplayName()).append("=").append(result);
                        if (i == pd.length - 1)
                            continue;
                        sb.append(",");
                    }
                }
            }
        } catch (Exception ex) {
        }

        return sb.append("]").toString();
    }

    /**
     * Print all properties for a given bean. It's useful for overwriting toString method.
     * 
     * @param bean Object to get all properties from.
     * @param showNulls Determine if you wether you want to show null properties or not.
     * @return String representing bean state.
     * @author andres santana
     */
    public static int hashBean(Object bean) {
        if (bean == null)
            return -1;
        int hash = bean.getClass().hashCode();
        // new ToStringCreator(this)
        try {
            BeanInfo bi = Introspector.getBeanInfo(bean.getClass());
            PropertyDescriptor[] pd = bi.getPropertyDescriptors();
            for (int i = 0; i < pd.length; i++) {
                if (!"class".equals(pd[i].getName())) {
                    Object result = pd[i].getReadMethod().invoke(bean);
                    if (result != null) {
                        hash += result.hashCode();
                        if (i == pd.length - 1)
                            continue;
                    }
                }
            }
        } catch (Exception ex) {
            return -1;
        }

        return hash;
    }


}
