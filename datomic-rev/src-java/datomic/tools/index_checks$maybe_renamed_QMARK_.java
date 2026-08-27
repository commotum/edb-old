/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.tools;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;

public final class index_checks$maybe_renamed_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"e");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Keyword const__5 = RT.keyword(null, (String)"v");
    public static final Keyword const__6 = RT.keyword(null, (String)"tx");
    public static final Var const__7 = RT.var((String)"datomic.tools.index-checks", (String)"rename-from");
    public static final Var const__8 = RT.var((String)"datomic.tools.index-checks", (String)"rename-to");
    public static final Var const__11 = RT.var((String)"datomic.db", (String)"attribute");

    public static Object invokeStatic(Object db2, Object p__21910) {
        Object object;
        Object or__5238__auto__21916;
        Object object2;
        Object object3 = p__21910;
        p__21910 = null;
        Object map__21911 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__21911);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__21911;
            map__21911 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__21911;
            map__21911 = null;
        }
        Object map__219112 = object2;
        Object e = RT.get((Object)map__219112, (Object)const__3);
        Object a = RT.get((Object)map__219112, (Object)const__4);
        Object v = RT.get((Object)map__219112, (Object)const__5);
        Object object6 = map__219112;
        map__219112 = null;
        RT.get((Object)object6, (Object)const__6);
        Object object7 = or__5238__auto__21916 = ((IFn)const__7.getRawRoot()).invoke(db2, e);
        if (object7 != null && object7 != Boolean.FALSE) {
            object = or__5238__auto__21916;
            or__5238__auto__21916 = null;
        } else {
            Object or__5238__auto__21915;
            Object object8 = e;
            e = null;
            Object object9 = or__5238__auto__21915 = ((IFn)const__8.getRawRoot()).invoke(db2, object8);
            if (object9 != null && object9 != Boolean.FALSE) {
                object = or__5238__auto__21915;
                or__5238__auto__21915 = null;
            } else {
                Object object10 = a;
                a = null;
                boolean and__5236__auto__21914 = Util.equiv((long)20L, (Object)((Attribute)((IFn)index_checks$maybe_renamed_QMARK_.const__11.getRawRoot()).invoke((Object)db2, (Object)object10)).vtypeid);
                if (and__5236__auto__21914) {
                    Object or__5238__auto__21913;
                    Object object11 = or__5238__auto__21913 = ((IFn)const__8.getRawRoot()).invoke(db2, v);
                    if (object11 != null && object11 != Boolean.FALSE) {
                        object = or__5238__auto__21913;
                        or__5238__auto__21913 = null;
                    } else {
                        Object object12 = db2;
                        db2 = null;
                        Object object13 = v;
                        v = null;
                        object = ((IFn)const__7.getRawRoot()).invoke(object12, object13);
                    }
                } else {
                    object = and__5236__auto__21914 ? Boolean.TRUE : Boolean.FALSE;
                }
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index_checks$maybe_renamed_QMARK_.invokeStatic(object3, object4);
    }
}

