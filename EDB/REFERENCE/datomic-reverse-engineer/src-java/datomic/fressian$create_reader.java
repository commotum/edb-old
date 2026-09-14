/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.FressianReader
 *  org.fressian.handlers.ILookup
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.InputStream;
import org.fressian.FressianReader;
import org.fressian.handlers.ILookup;

public final class fressian$create_reader
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.fressian", (String)"create-reader");
    public static final Var const__3 = RT.var((String)"clojure.java.io", (String)"input-stream");
    public static final Var const__4 = RT.var((String)"datomic.fressian", (String)"as-lookup");

    public static Object invokeStatic(Object in, Object lookup, Object validate_checksum) {
        Object object;
        if (in instanceof InputStream) {
            object = in;
            in = null;
        } else {
            Object object2 = in;
            in = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object2);
        }
        Object object3 = lookup;
        lookup = null;
        Object object4 = validate_checksum;
        validate_checksum = null;
        return new FressianReader((InputStream)object, (ILookup)((IFn)const__4.getRawRoot()).invoke(object3), ((Boolean)object4).booleanValue());
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return fressian$create_reader.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object in, Object lookup) {
        Object object = in;
        in = null;
        Object object2 = lookup;
        lookup = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, (Object)Boolean.TRUE);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fressian$create_reader.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object in) {
        Object object = in;
        in = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, null);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fressian$create_reader.invokeStatic(object2);
    }
}

