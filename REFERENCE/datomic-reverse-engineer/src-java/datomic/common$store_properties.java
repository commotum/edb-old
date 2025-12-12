/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.OutputStream;
import java.util.Properties;

public final class common$store_properties
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.java.io", (String)"output-stream");
    public static final Var const__1 = RT.var((String)"clojure.java.io", (String)"file");

    public static Object invokeStatic(Object props, Object filename) {
        Object object = props;
        props = null;
        Object object2 = filename;
        filename = null;
        ((Properties)object).store((OutputStream)((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object2)), "Generated Datomic Properties");
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$store_properties.invokeStatic(object3, object4);
    }
}

