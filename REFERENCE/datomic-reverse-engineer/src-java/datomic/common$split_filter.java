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

public final class common$split_filter
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"remove");

    public static Object invokeStatic(Object pred2, Object coll) {
        Object object = ((IFn)const__0.getRawRoot()).invoke(pred2, coll);
        Object object2 = pred2;
        pred2 = null;
        Object object3 = coll;
        coll = null;
        return Tuple.create((Object)object, (Object)((IFn)const__1.getRawRoot()).invoke(object2, object3));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$split_filter.invokeStatic(object3, object4);
    }
}

