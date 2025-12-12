/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class backup$roots_path
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"join");

    public static Object invokeStatic(Object t) {
        Object object = t;
        t = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)"/", (Object)Tuple.create((Object)"roots", (Object)object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$roots_path.invokeStatic(object2);
    }
}

