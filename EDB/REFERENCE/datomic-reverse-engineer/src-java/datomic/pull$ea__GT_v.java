/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.iter.Iter;
import datomic.pull$ea__GT_v$fn__18926;
import datomic.pull$ea__GT_v$mk_iter__18919;

public final class pull$ea__GT_v
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.pull", (String)"ea->v");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__3 = RT.keyword(null, (String)"e");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Var const__7 = RT.var((String)"datomic.pull", (String)"nilify-empty");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"persistent!");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"transient");
    public static final Var const__11 = RT.var((String)"datomic.pull", (String)"limit-iterable");
    public static final Var const__12 = RT.var((String)"datomic.iter", (String)"iterable");

    public static Object invokeStatic(Object db2, Object e, Object attr, Object xf, Object limit2, Object valfn, Object use_aevt_QMARK_) {
        Object object;
        Object object2 = valfn;
        valfn = null;
        IFn iFn = (IFn)object2;
        Object object3 = attr;
        if (object3 != null && object3 != Boolean.FALSE) {
            Object temp__5457__auto__18929;
            Object object4 = e;
            e = null;
            Object eid = ((IFn)const__1.getRawRoot()).invoke(db2, object4);
            Object attrid = ((Attribute)attr).id();
            Object d = ((IFn)const__2.getRawRoot()).invoke(db2, (Object)const__3, eid, (Object)const__4, attrid);
            Object object5 = db2;
            db2 = null;
            Object object6 = attrid;
            attrid = null;
            Object object7 = eid;
            eid = null;
            Object object8 = use_aevt_QMARK_;
            use_aevt_QMARK_ = null;
            Object object9 = d;
            d = null;
            pull$ea__GT_v$mk_iter__18919 mk_iter = new pull$ea__GT_v$mk_iter__18919(object5, object6, object7, object8, object9);
            Object object10 = temp__5457__auto__18929 = ((IFn)mk_iter).invoke();
            if (object10 != null && object10 != Boolean.FALSE) {
                Object object11 = temp__5457__auto__18929;
                temp__5457__auto__18929 = null;
                Object iter2 = object11;
                Object object12 = attr;
                attr = null;
                if (Util.equiv((long)36L, (Object)((Attribute)object12).cardinality)) {
                    Object object13 = xf;
                    xf = null;
                    Object object14 = limit2;
                    limit2 = null;
                    pull$ea__GT_v$mk_iter__18919 pull$ea__GT_v$mk_iter__18919 = mk_iter;
                    mk_iter = null;
                    object = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke((Object)new pull$ea__GT_v$fn__18926(object13), ((IFn)const__10.getRawRoot()).invoke((Object)PersistentVector.EMPTY), ((IFn)const__11.getRawRoot()).invoke(object14, ((IFn)const__12.getRawRoot()).invoke((Object)pull$ea__GT_v$mk_iter__18919)))));
                } else {
                    Object object15 = xf;
                    xf = null;
                    Object object16 = iter2;
                    iter2 = null;
                    object = ((IFn)object15).invoke(((Iter)object16).get());
                }
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        return iFn.invoke(object);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        Object object8 = object;
        object = null;
        Object object9 = object2;
        object2 = null;
        Object object10 = object3;
        object3 = null;
        Object object11 = object4;
        object4 = null;
        Object object12 = object5;
        object5 = null;
        Object object13 = object6;
        object6 = null;
        Object object14 = object7;
        object7 = null;
        return pull$ea__GT_v.invokeStatic(object8, object9, object10, object11, object12, object13, object14);
    }

    public static Object invokeStatic(Object db2, Object e, Object attr, Object xf, Object limit2, Object valfn) {
        Object object = db2;
        db2 = null;
        Object object2 = e;
        e = null;
        Object object3 = attr;
        attr = null;
        Object object4 = xf;
        xf = null;
        Object object5 = limit2;
        limit2 = null;
        Object object6 = valfn;
        valfn = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, object4, object5, object6, (Object)Boolean.FALSE);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return pull$ea__GT_v.invokeStatic(object7, object8, object9, object10, object11, object12);
    }
}

