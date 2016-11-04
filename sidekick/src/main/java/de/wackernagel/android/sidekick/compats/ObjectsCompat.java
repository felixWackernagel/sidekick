package de.wackernagel.android.sidekick.compats;

import android.annotation.TargetApi;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class ObjectsCompat {

    private static final ObjectsCompatImpl IMPL;
    static {
        final int version = android.os.Build.VERSION.SDK_INT;
        if( version >= 19 ) {
            IMPL = new KitkatObjectsCompatImpl();
        } else {
            IMPL = new BaseObjectsCompatImpl();
        }
    }

    /**
     * Note that if one of the arguments is null, a NullPointerException may or may not be thrown depending on what ordering policy,
     * if any, the Comparator chooses to have for null values.
     *
     * @param a an Object
     * @param b an object to be compared with a
     * @param c the Comparator to compare the first two arguments
     * @return 0 if the arguments are identical and c.compare(a, b) otherwise.
     */
    public static <T> int compare(T a, T b, Comparator<? super T> c) {
        return IMPL.compare(a, b, c);
    }

    /**
     * Returns true if the arguments are deeply equal to each other and false otherwise. Two null values are deeply equal.
     * If both arguments are arrays, the algorithm in Arrays.deepEquals is used to determine equality.
     * Otherwise, equality is determined by using the equals method of the first argument.
     *
     * @param a an object
     * @param b an object to be compared with a for deep equality
     * @return true if the arguments are deeply equal to each other and false otherwise
     */
    public static boolean deepEquals(Object a, Object b) {
        return IMPL.deepEquals(a, b);
    }

    /**
     * Returns true if the arguments are equal to each other and false otherwise. Consequently, if both arguments are null,
     * true is returned and if exactly one argument is null, false is returned.
     * Otherwise, equality is determined by using the equals method of the first argument.
     *
     * @param a an object
     * @param b an object to be compared with a for equality
     * @return true if the arguments are equal to each other and false otherwise
     */
    public static boolean equals(Object a, Object b) {
        return IMPL.equals(a, b);
    }

    /**
     * Generates a hash code for a sequence of input values.
     * The hash code is generated as if all the input values were placed into an array, and that array were hashed by calling hashCode(Object[]).
     *
     * Warning:
     * When a single object reference is supplied, the returned value does not equal the hash code of that object reference.
     * This value can be computed by calling hashCode(Object).
     *
     * @param values the values to be hashed
     * @return a hash value of the sequence of input values
     */
    public static int hash(Object... values) {
        return IMPL.hash(values);
    }

    /**
     * Returns the hash code of a non-null argument and 0 for a null argument.
     *
     * @param o an object
     * @return the hash code of a non-null argument and 0 for a null argument
     */
    public static int hashCode(Object o) {
        return IMPL.hashCode(o);
    }

    /**
     * Checks that the specified object reference is not null and throws a customized NullPointerException if it is.
     * This method is designed primarily for doing parameter validation in methods and constructors with multiple parameters.
     *
     * @param obj the object reference to check for nullity
     * @param message detail message to be used in the event that a NullPointerException is thrown
     * @return obj if not null
     */
    public static <T> T requireNonNull(T obj, String message) {
        return IMPL.requireNonNull(obj, message);
    }

    /**
     * Checks that the specified object reference is not null.
     * This method is designed primarily for doing parameter validation in methods and constructors.
     *
     * @param obj the object reference to check for nullity
     * @return obj if not null
     */
    public static <T> T requireNonNull(T obj) {
        return IMPL.requireNonNull( obj );
    }

    /**
     * Returns the result of calling toString for a non-null argument and "null" for a null argument.
     *
     * @param o an object
     * @return the result of calling toString for a non-null argument and "null" for a null argument
     */
    public static String toString(Object o) {
        return IMPL.toString(o);
    }

    /**
     * Returns the result of calling toString on the first argument if the first argument is not null and returns the second argument otherwise.
     *
     * @param o an object
     * @param nullDefault string to return if the first argument is null
     * @return the result of calling toString on the first argument if it is not null and the second argument otherwise.
     */
    public static String toString(Object o, String nullDefault) {
        return IMPL.toString( o, nullDefault );
    }

    private interface ObjectsCompatImpl {
        <T> int compare(T a, T b, Comparator<? super T> c);
        boolean deepEquals(Object a, Object b);
        boolean equals(Object a, Object b);
        int hash(Object... values);
        int hashCode(Object o);
        <T> T requireNonNull(T obj, String message);
        <T> T requireNonNull(T obj);
        String toString(Object o);
        String toString(Object o, String nullDefault);
    }

    private static class BaseObjectsCompatImpl implements ObjectsCompatImpl {

        BaseObjectsCompatImpl() {
        }

        @Override
        public <T> int compare(T a, T b, Comparator<? super T> c) {
            return a == b ? 0 : c.compare( a, b );
        }

        @Override
        public boolean deepEquals(Object a, Object b) {
            if (a == null || b == null) {
                return a == b;
            } else if (a instanceof Object[] && b instanceof Object[]) {
                return Arrays.deepEquals(( Object[] ) a, ( Object[] ) b);
            } else if (a instanceof boolean[] && b instanceof boolean[]) {
                return Arrays.equals((boolean[]) a, (boolean[]) b);
            } else if (a instanceof byte[] && b instanceof byte[]) {
                return Arrays.equals((byte[]) a, (byte[]) b);
            } else if (a instanceof char[] && b instanceof char[]) {
                return Arrays.equals((char[]) a, (char[]) b);
            } else if (a instanceof double[] && b instanceof double[]) {
                return Arrays.equals((double[]) a, (double[]) b);
            } else if (a instanceof float[] && b instanceof float[]) {
                return Arrays.equals((float[]) a, (float[]) b);
            } else if (a instanceof int[] && b instanceof int[]) {
                return Arrays.equals((int[]) a, (int[]) b);
            } else if (a instanceof long[] && b instanceof long[]) {
                return Arrays.equals((long[]) a, (long[]) b);
            } else if (a instanceof short[] && b instanceof short[]) {
                return Arrays.equals((short[]) a, (short[]) b);
            }
            return a.equals(b);
        }

        @Override
        public boolean equals(Object a, Object b) {
            return a == null ? b == null : a.equals( b );
        }

        @Override
        public int hash(Object... values) {
            return Arrays.hashCode(values);
        }

        @Override
        public int hashCode(Object o) {
            return (o == null) ? 0 : o.hashCode();
        }

        @Override
        public <T> T requireNonNull(T obj, String message) {
            if( obj == null ) {
                throw new NullPointerException( message );
            }
            return obj;
        }

        @Override
        public <T> T requireNonNull(T obj) {
            if( obj == null ) {
                throw new NullPointerException();
            }
            return obj;
        }

        @Override
        public String toString(Object o) {
            return o == null ? "null" : o.toString();
        }

        @Override
        public String toString(Object o, String nullDefault) {
            return o == null ? nullDefault : o.toString();
        }
    }

    @TargetApi( 19 )
    private static class KitkatObjectsCompatImpl extends BaseObjectsCompatImpl {

        KitkatObjectsCompatImpl() {
        }

        @Override
        public <T> int compare(T a, T b, Comparator<? super T> c) {
            return Objects.compare(a, b, c);
        }

        @Override
        public boolean deepEquals(Object a, Object b) {
            return Objects.deepEquals(a, b);
        }

        @Override
        public boolean equals(Object a, Object b) {
            return Objects.equals(a, b);
        }

        @Override
        public int hash(Object... values) {
            return Objects.hash(values);
        }

        @Override
        public int hashCode(Object o) {
            return Objects.hashCode( o );
        }

        @Override
        public <T> T requireNonNull(T obj, String message) {
            return Objects.requireNonNull( obj, message );
        }

        @Override
        public <T> T requireNonNull(T obj) {
            return Objects.requireNonNull( obj );
        }

        @Override
        public String toString(Object o) {
            return Objects.toString( o );
        }

        @Override
        public String toString(Object o, String nullDefault) {
            return Objects.toString( o, nullDefault );
        }
    }
}
