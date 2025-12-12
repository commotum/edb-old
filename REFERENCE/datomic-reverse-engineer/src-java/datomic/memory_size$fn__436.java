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
import datomic.memory_size$fn__436$fn__438;

public final class memory_size$fn__436
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Object const__1 = 0L;

    public static Object invokeStatic(Object m) {
        Object object = m;
        m = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new memory_size$fn__436$fn__438(), const__1, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory_size$fn__436.invokeStatic(object2);
    }
}

