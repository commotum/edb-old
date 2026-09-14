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
import datomic.log$max_eidx$fn__16521;

public final class log$max_eidx
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Object const__1 = 0L;

    public static Object invokeStatic(Object datoms2) {
        Object object = datoms2;
        datoms2 = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new log$max_eidx$fn__16521(), const__1, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$max_eidx.invokeStatic(object2);
    }
}

