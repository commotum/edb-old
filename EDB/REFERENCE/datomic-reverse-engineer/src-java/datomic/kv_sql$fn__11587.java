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
import java.sql.SQLException;

public final class kv_sql$fn__11587
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not=");
    public static final Object const__1 = 28000L;

    public static Object invokeStatic(Object x) {
        Object object = x;
        x = null;
        int code = ((SQLException)object).getErrorCode();
        return ((IFn)const__0.getRawRoot()).invoke((Object)code, const__1);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_sql$fn__11587.invokeStatic(object2);
    }
}

