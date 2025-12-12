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
import java.io.Writer;

public final class db$fn__12434
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object dbid, Object w) {
        Object object = w;
        w = null;
        Object object2 = dbid;
        dbid = null;
        ((Writer)object).write((String)((IFn)const__0.getRawRoot()).invoke(object2));
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$fn__12434.invokeStatic(object3, object4);
    }
}

