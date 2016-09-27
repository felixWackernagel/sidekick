package de.wackernagel.android.sidekick.compats;

import android.annotation.TargetApi;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

public class ObjectsCompat {

    static final ObjectsCompatImpl IMPL;
    static {
        final int version = android.os.Build.VERSION.SDK_INT;
        if( version >= 19 ) {
            IMPL = new KitkatObjectsCompatImpl();
        } else {
            IMPL = new BaseObjectsCompatImpl();
        }
    }

    public static <T> int compare(T a, T b, Comparator<? super T> c) {
        return IMPL.compare(a, b, c);
    }

    public static boolean deepEquals(Object a, Object b) {
        return IMPL.deepEquals(a, b);
    }

    public static boolean equals(Object a, Object b) {
        return IMPL.equals(a, b);
    }

    public static int hash(Object... values) {
        return IMPL.hash(values);
    }

    public static int hashCode(Object o) {
        return IMPL.hashCode(o);
    }

    public static <T> T requireNonNull(T obj, String message) {
        return IMPL.requireNonNull(obj, message);
    }

    public static <T> T requireNonNull(T obj) {
        return IMPL.requireNonNull( obj );
    }

    public static String toString(Object o) {
        return IMPL.toString(o);
    }

    public static String toString(Object o, String nullDefault) {
        return IMPL.toString( o, nullDefault );
    }

    interface ObjectsCompatImpl {
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

    static class BaseObjectsCompatImpl implements ObjectsCompatImpl {

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
    static class KitkatObjectsCompatImpl extends BaseObjectsCompatImpl {
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
