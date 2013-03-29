package it.geosolutions.tools.commons.generics;

import java.util.Comparator;

public class SetComparator<T> implements Comparator<Object[]> {
    private Caster<T> caster;

    private int keyIndex;

    private boolean notReverse;

    public SetComparator(Caster<T> caster, int keyIndex, boolean reverse) {
        init(caster, keyIndex, reverse);
    }
    
    public SetComparator(Caster<T> caster, int keyIndex) {
        init(caster, keyIndex, false);
    }

    void init(Caster<T> caster, int keyIndex, boolean reverse) {
        if (caster == null)
            throw new IllegalArgumentException("Null caster");
        if (keyIndex < 0)
            throw new IllegalArgumentException("keyIndex < 0");

        this.notReverse = !reverse;
        this.keyIndex = keyIndex;
        this.caster = caster;
    }

    public T get(Object o) {
        return caster.cast(o);
    }

    public int compare(Object[] o1, Object[] o2) {
        if (keyIndex > o1.length || keyIndex > o2.length) {
            return o1.hashCode() > o2.hashCode() & notReverse ? 1 : -1;
        } else {
            return get(o1[keyIndex]).hashCode() > get(o2[keyIndex]).hashCode() & notReverse ? 1 : -1;
        }
    }

}
