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
import java.io.InputStream;
import java.util.Properties;

public final class common$load_properties
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.java.io", (String)"input-stream");
    public static final Var const__1 = RT.var((String)"clojure.java.io", (String)"file");

    public static Object invokeStatic(Object filename) {
        Properties G__9218 = new Properties();
        Object object = filename;
        filename = null;
        G__9218.load((InputStream)((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object)));
        Object var1_1 = null;
        return G__9218;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$load_properties.invokeStatic(object2);
    }
}

