/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.query;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class support$parse_as
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.query.support", (String)"query-map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"find");
    public static final Keyword const__5 = RT.keyword(null, (String)"keys");
    public static final Keyword const__6 = RT.keyword(null, (String)"strs");
    public static final Keyword const__7 = RT.keyword(null, (String)"syms");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__11 = RT.keyword(null, (String)"then");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__15 = RT.var((String)"datomic.query.support", (String)"incorrect!");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"dissoc");

    public static Object invokeStatic(Object q2) {
        IPersistentVector iPersistentVector;
        Object temp__5455__auto__19087;
        Object object;
        Object or__5238__auto__19084;
        Object map__19080;
        Object object2;
        Object object3 = q2;
        q2 = null;
        Object map__190802 = ((IFn)const__0.getRawRoot()).invoke(object3);
        Object object4 = ((IFn)const__1.getRawRoot()).invoke(map__190802);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__190802;
            map__190802 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__190802;
            map__190802 = null;
        }
        Object nq = map__19080 = object2;
        Object find = RT.get((Object)map__19080, (Object)const__4);
        Object keys = RT.get((Object)map__19080, (Object)const__5);
        Object strs = RT.get((Object)map__19080, (Object)const__6);
        Object object6 = map__19080;
        map__19080 = null;
        Object syms = RT.get((Object)object6, (Object)const__7);
        Object object7 = or__5238__auto__19084 = keys;
        if (object7 != null && object7 != Boolean.FALSE) {
            object = or__5238__auto__19084;
            or__5238__auto__19084 = null;
        } else {
            Object or__5238__auto__19083;
            Object object8 = or__5238__auto__19083 = strs;
            if (object8 != null && object8 != Boolean.FALSE) {
                object = or__5238__auto__19083;
                or__5238__auto__19083 = null;
            } else {
                object = syms;
            }
        }
        Object object9 = temp__5455__auto__19087 = object;
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10;
            Object or__5238__auto__19086;
            Object object11;
            Object object12;
            Object object13;
            Object asyms;
            Object object14 = temp__5455__auto__19087;
            temp__5455__auto__19087 = null;
            Object object15 = asyms = object14;
            asyms = null;
            Object G__19081 = object15;
            Object object16 = keys;
            if (object16 != null && object16 != Boolean.FALSE) {
                Object object17 = G__19081;
                G__19081 = null;
                object13 = ((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), object17);
            } else {
                object13 = G__19081;
                G__19081 = null;
            }
            Object G__190812 = object13;
            Object object18 = strs;
            if (object18 != null && object18 != Boolean.FALSE) {
                Object object19 = G__190812;
                G__190812 = null;
                object12 = ((IFn)const__8.getRawRoot()).invoke(const__10.getRawRoot(), object19);
            } else {
                object12 = G__190812;
                G__190812 = null;
            }
            Object G__190813 = object12;
            Keyword keyword = const__11;
            if (keyword != null && keyword != Boolean.FALSE) {
                Object object20 = G__190813;
                G__190813 = null;
                object11 = ((IFn)const__12.getRawRoot()).invoke(object20);
            } else {
                object11 = G__190813;
                G__190813 = null;
            }
            Object as = object11;
            Object object21 = keys;
            keys = null;
            Object object22 = or__5238__auto__19086 = object21;
            if (object22 != null && object22 != Boolean.FALSE) {
                object10 = or__5238__auto__19086;
                or__5238__auto__19086 = null;
            } else {
                Object or__5238__auto__19085;
                Object object23 = strs;
                strs = null;
                Object object24 = or__5238__auto__19085 = object23;
                if (object24 != null && object24 != Boolean.FALSE) {
                    object10 = or__5238__auto__19085;
                    or__5238__auto__19085 = null;
                } else {
                    object10 = syms;
                    syms = null;
                }
            }
            Object object25 = find;
            find = null;
            if ((long)RT.count((Object)object10) != (long)RT.count((Object)object25)) {
                throw (Throwable)((IFn)const__15.getRawRoot()).invoke((Object)"Count of :keys/:strs/:syms must match count of :find");
            }
            Object object26 = nq;
            nq = null;
            Object object27 = as;
            as = null;
            iPersistentVector = Tuple.create((Object)((IFn)const__16.getRawRoot()).invoke(object26, (Object)const__5, (Object)const__7, (Object)const__6), (Object)object27);
        } else {
            Object object28 = nq;
            nq = null;
            iPersistentVector = Tuple.create((Object)object28, null);
        }
        return iPersistentVector;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return support$parse_as.invokeStatic(object2);
    }
}

