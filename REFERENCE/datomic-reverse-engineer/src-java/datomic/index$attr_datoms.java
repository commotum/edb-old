/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index$attr_datoms$fn__15542;

public final class index$attr_datoms
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.iter", (String)"take-while");
    public static final Var const__1 = RT.var((String)"datomic.btset", (String)"seek");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__3 = RT.keyword(null, (String)"a");

    public static Object invokeStatic(Object db2, Object index2, Object attrid) {
        index$attr_datoms$fn__15542 index$attr_datoms$fn__15542 = new index$attr_datoms$fn__15542(attrid);
        Object object = index2;
        index2 = null;
        Object object2 = db2;
        db2 = null;
        Object object3 = attrid;
        attrid = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)index$attr_datoms$fn__15542, ((IFn)const__1.getRawRoot()).invoke(object, ((IFn)const__2.getRawRoot()).invoke(object2, (Object)const__3, object3)));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$attr_datoms.invokeStatic(object4, object5, object6);
    }
}

