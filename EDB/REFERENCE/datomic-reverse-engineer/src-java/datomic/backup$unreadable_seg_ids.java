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
import datomic.backup$unreadable_seg_ids$fn__20349;
import datomic.backup$unreadable_seg_ids$unreadable_QMARK___20347;

public final class backup$unreadable_seg_ids
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"keep");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seque");
    public static final Var const__3 = RT.var((String)"datomic.backup", (String)"pool-size");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"map");

    public static Object invokeStatic(Object lookup, Object seg_ids) {
        backup$unreadable_seg_ids$unreadable_QMARK___20347 unreadable_QMARK_;
        Object object = lookup;
        lookup = null;
        backup$unreadable_seg_ids$unreadable_QMARK___20347 backup$unreadable_seg_ids$unreadable_QMARK___20347 = unreadable_QMARK_ = new backup$unreadable_seg_ids$unreadable_QMARK___20347(object);
        unreadable_QMARK_ = null;
        Object object2 = seg_ids;
        seg_ids = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), ((IFn)const__4.getRawRoot()).invoke((Object)new backup$unreadable_seg_ids$fn__20349((Object)backup$unreadable_seg_ids$unreadable_QMARK___20347), object2)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$unreadable_seg_ids.invokeStatic(object3, object4);
    }
}

