/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class backup$add_key_prefix
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"subs");

    public static Object invokeStatic(Object s) {
        Object object = ((IFn)const__1.getRawRoot()).invoke(s, (Object)Numbers.num((long)Numbers.minus((long)RT.count((Object)s), (long)2L)));
        Object object2 = s;
        s = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)"/", object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$add_key_prefix.invokeStatic(object2);
    }
}

