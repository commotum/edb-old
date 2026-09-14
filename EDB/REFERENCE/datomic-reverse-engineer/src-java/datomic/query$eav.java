/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.db.IDb;
import datomic.impl.db.IDatum;
import datomic.iter.Iter;
import datomic.query$eav$fn__19148;
import datomic.query$eav$fn__19153;
import datomic.query$eav$maybe_bind__19151;

public final class query$eav
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"reverse-lookup?");
    public static final Var const__1 = RT.var((String)"datomic.query", (String)"rae");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"subs");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"name");
    public static final Object const__6 = 1L;
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"windowed");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__10 = RT.keyword(null, (String)"e");
    public static final Keyword const__11 = RT.keyword(null, (String)"a");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__16 = RT.var((String)"datomic.iter", (String)"reduce");

    public static Object invokeStatic(Object db2, Object e, Object a) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(db2, a);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = db2;
            db2 = null;
            Object object4 = e;
            e = null;
            Object object5 = ((IFn)const__3.getRawRoot()).invoke(a);
            Object object6 = a;
            a = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, object4, ((IFn)const__2.getRawRoot()).invoke(object5, ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object6), const__6)));
        } else {
            Object ref_QMARK_;
            Object object7;
            Object vtypeid;
            Object and__5236__auto__19158;
            Object object8;
            Object attr;
            Object and__5236__auto__19157;
            Object object9;
            Object and__5236__auto__19156;
            Object object10 = a;
            a = null;
            Object attrid = ((IFn)const__7.getRawRoot()).invoke(db2, object10);
            Object object11 = e;
            e = null;
            Object eid = ((IFn)const__7.getRawRoot()).invoke(db2, object11);
            query$eav$fn__19148 query$eav$fn__19148 = new query$eav$fn__19148(eid, attrid);
            Object object12 = eid;
            eid = null;
            Object iter2 = ((IFn)const__8.getRawRoot()).invoke(db2, (Object)query$eav$fn__19148, (Object)((IDb)db2).seekEAVT((IDatum)((IFn)const__9.getRawRoot()).invoke(db2, (Object)const__10, object12, (Object)const__11, attrid)));
            Object object13 = and__5236__auto__19156 = attrid;
            if (object13 != null && object13 != Boolean.FALSE) {
                Object object14 = attrid;
                attrid = null;
                object9 = ((IFn)const__12.getRawRoot()).invoke(db2, object14);
            } else {
                object9 = and__5236__auto__19156;
                and__5236__auto__19156 = null;
            }
            Object object15 = and__5236__auto__19157 = (attr = object9);
            if (object15 != null && object15 != Boolean.FALSE) {
                object8 = ((Attribute)attr).vtypeid;
            } else {
                object8 = and__5236__auto__19157;
                and__5236__auto__19157 = null;
            }
            Object object16 = and__5236__auto__19158 = (vtypeid = object8);
            if (object16 != null && object16 != Boolean.FALSE) {
                Object object17 = vtypeid;
                vtypeid = null;
                object7 = Util.equiv((Object)object17, (long)20L) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object7 = and__5236__auto__19158;
                and__5236__auto__19158 = null;
            }
            Object object18 = ref_QMARK_ = object7;
            ref_QMARK_ = null;
            Object object19 = db2;
            db2 = null;
            query$eav$maybe_bind__19151 maybe_bind = new query$eav$maybe_bind__19151(object18, object19);
            Object object20 = iter2;
            if (object20 != null && object20 != Boolean.FALSE) {
                Object object21;
                Object and__5236__auto__19159;
                Object object22 = and__5236__auto__19159 = attr;
                if (object22 != null && object22 != Boolean.FALSE) {
                    Object object23 = attr;
                    attr = null;
                    object21 = Util.equiv((long)36L, (Object)((Attribute)object23).cardinality) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    object21 = and__5236__auto__19159;
                    and__5236__auto__19159 = null;
                }
                if (object21 != null && object21 != Boolean.FALSE) {
                    query$eav$maybe_bind__19151 query$eav$maybe_bind__19151 = maybe_bind;
                    maybe_bind = null;
                    Object object24 = iter2;
                    iter2 = null;
                    object = ((IFn)const__16.getRawRoot()).invoke((Object)new query$eav$fn__19153((Object)query$eav$maybe_bind__19151), (Object)PersistentHashSet.EMPTY, object24);
                } else {
                    query$eav$maybe_bind__19151 query$eav$maybe_bind__19151 = maybe_bind;
                    maybe_bind = null;
                    Object object25 = iter2;
                    iter2 = null;
                    object = ((IFn)query$eav$maybe_bind__19151).invoke(((IDatum)((Iter)object25).get()).getV());
                }
            } else {
                object = null;
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return query$eav.invokeStatic(object4, object5, object6);
    }
}

