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
import datomic.datalog$adorned_pred$fn__18423;

public final class datalog$adorned_pred
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__3 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"rest");

    public static Object invokeStatic(Object query2, Object bindset) {
        Object object = ((IFn)const__0.getRawRoot()).invoke(query2);
        Object object2 = bindset;
        bindset = null;
        Object object3 = query2;
        query2 = null;
        return Tuple.create((Object)object, (Object)((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)new datalog$adorned_pred$fn__18423(object2), ((IFn)const__4.getRawRoot()).invoke(object3))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datalog$adorned_pred.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object query2) {
        Object object = ((IFn)const__0.getRawRoot()).invoke(query2);
        Object object2 = query2;
        query2 = null;
        return Tuple.create((Object)object, (Object)((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), ((IFn)const__4.getRawRoot()).invoke(object2))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$adorned_pred.invokeStatic(object2);
    }
}

