package it.geosolutions.tools.commons.generics;

public class IntegerCaster implements Caster<Integer> {
    public Integer cast(Object o) {
        return o!=null?Integer.parseInt(o.toString()):-1;
    }
}