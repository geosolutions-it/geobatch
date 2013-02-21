package it.geosolutions.tools.commons.generics;

public class StringCaster implements Caster<String> {
    public String cast(Object o) {
        if (o != null)
            return o.toString();
        else
            return null;
    }
}