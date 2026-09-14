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

public final class math$median
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");

    public static Object invokeStatic(Object sorted_coll) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(sorted_coll);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = sorted_coll;
            Object object4 = sorted_coll;
            sorted_coll = null;
            object = RT.nth((Object)object3, (int)RT.intCast((Object)Numbers.divide((long)RT.count((Object)object4), (long)2L)));
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return math$median.invokeStatic(object2);
    }
}

