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
import datomic.db$distinct_last_by$step__12863;

public final class db$distinct_last_by
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");

    /*
     * WARNING - void declaration
     */
    public static Object invokeStatic(Object f, Object coll) {
        Object object;
        Object temp__5457__auto__12872;
        db$distinct_last_by$step__12863 db$distinct_last_by$step__12863 = null;
        db$distinct_last_by$step__12863 = new db$distinct_last_by$step__12863();
        Object object2 = temp__5457__auto__12872 = ((IFn)const__0.getRawRoot()).invoke(coll);
        if (object2 != null && object2 != Boolean.FALSE) {
            void step;
            temp__5457__auto__12872 = null;
            Object object3 = f;
            f = null;
            Object object4 = ((IFn)const__1.getRawRoot()).invoke(coll);
            Object object5 = coll;
            coll = null;
            object = ((IFn)step).invoke(object3, object4, ((IFn)const__2.getRawRoot()).invoke(object5));
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$distinct_last_by.invokeStatic(object3, object4);
    }
}

