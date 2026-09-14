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

public final class index$fred
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"rest");

    public static Object invokeStatic(Object f, Object ret, Object coll) {
        while (true) {
            Object temp__5455__auto__15374;
            Object object = coll;
            coll = null;
            Object object2 = temp__5455__auto__15374 = ((IFn)const__0.getRawRoot()).invoke(object);
            if (object2 == null || object2 == Boolean.FALSE) break;
            Object object3 = temp__5455__auto__15374;
            temp__5455__auto__15374 = null;
            Object xs = object3;
            Object x = ((IFn)const__1.getRawRoot()).invoke(xs);
            Object object4 = xs;
            xs = null;
            Object rst = ((IFn)const__2.getRawRoot()).invoke(object4);
            Object object5 = f;
            Object object6 = f;
            f = null;
            Object object7 = ret;
            ret = null;
            Object object8 = x;
            x = null;
            Object object9 = rst;
            rst = null;
            coll = object9;
            ret = ((IFn)object6).invoke(object7, object8);
            f = object5;
        }
        Object var1_1 = null;
        return ret;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$fred.invokeStatic(object4, object5, object6);
    }
}

