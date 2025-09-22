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

public final class math$mean
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"+");

    public static Object invokeStatic(Object coll) {
        Double d;
        Object object = ((IFn)const__0.getRawRoot()).invoke(coll);
        if (object != null && object != Boolean.FALSE) {
            double d2 = RT.doubleCast((Object)((IFn)const__3.getRawRoot()).invoke(const__4.getRawRoot(), coll));
            Object object2 = coll;
            coll = null;
            d = Numbers.divide((double)d2, (long)RT.count((Object)object2));
        } else {
            d = null;
        }
        return d;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return math$mean.invokeStatic(object2);
    }
}

