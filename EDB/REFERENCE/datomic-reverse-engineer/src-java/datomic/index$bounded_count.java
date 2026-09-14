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

public final class index$bounded_count
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object n, Object coll) {
        long i = 0L;
        Object object = coll;
        coll = null;
        Object s = ((IFn)const__1.getRawRoot()).invoke(object);
        while (true) {
            Object object2;
            Object and__5236__auto__15332;
            Object object3 = and__5236__auto__15332 = s;
            if (object3 != null && object3 != Boolean.FALSE) {
                object2 = Numbers.lt((long)i, (Object)n) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object2 = and__5236__auto__15332;
                and__5236__auto__15332 = null;
            }
            if (object2 == null || object2 == Boolean.FALSE) break;
            Object object4 = s;
            s = null;
            s = ((IFn)const__4.getRawRoot()).invoke(object4);
            ++i;
        }
        return Numbers.num((long)i);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$bounded_count.invokeStatic(object3, object4);
    }
}

