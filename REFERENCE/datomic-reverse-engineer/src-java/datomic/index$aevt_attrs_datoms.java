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
import datomic.index$aevt_attrs_datoms$fn__15545;

public final class index$aevt_attrs_datoms
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"datomic.iter", (String)"concat");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"second");

    public static Object invokeStatic(Object db2, Object attrids) {
        Object object = db2;
        db2 = null;
        Object object2 = attrids;
        attrids = null;
        Object datoms2 = ((IFn)const__0.getRawRoot()).invoke((Object)new index$aevt_attrs_datoms$fn__15545(object), object2);
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(const__4.getRawRoot(), datoms2)));
        Object object4 = datoms2;
        datoms2 = null;
        return Tuple.create((Object)object3, (Object)((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(const__5.getRawRoot(), object4))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$aevt_attrs_datoms.invokeStatic(object3, object4);
    }
}

