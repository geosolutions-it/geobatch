package it.geosolutions.tools.commons.generics;

public class NoCaster implements Caster<Object> {
    public Object cast(Object o) {
        return o;
    }
}