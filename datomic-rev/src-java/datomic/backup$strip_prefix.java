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

public final class backup$strip_prefix
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"subs");

    public static Object invokeStatic(Object prefix, Object k) {
        Object object = k;
        k = null;
        Object object2 = prefix;
        prefix = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)Numbers.num((long)Numbers.inc((long)RT.count((Object)object2))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$strip_prefix.invokeStatic(object3, object4);
    }
}

