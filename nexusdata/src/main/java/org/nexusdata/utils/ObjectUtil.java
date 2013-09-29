package org.nexusdata.utils;

public class ObjectUtil {

    public static boolean objectsEqual(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    public static Comparable<?> toComparable(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Comparable) {
            return (Comparable<?>) object;
        } else if (object instanceof StringBuffer) {
            return object.toString();
        } else if (object instanceof char[]) {
            return new String((char[]) object);
        } else {
            throw new ClassCastException("Could not get a valid Comparable instance for class:" + object.getClass().getName());
        }
    }
}
