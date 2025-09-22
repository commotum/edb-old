/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.query$process_find_bindings$fn__19365;
import java.util.List;

public final class query$process_find_bindings
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"find");
    public static final Object const__5 = 1L;
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__12 = RT.keyword(null, (String)"find-bindings");
    public static final AFn const__13 = (AFn)Symbol.intern(null, (String)"first-tuple");
    public static final AFn const__15 = (AFn)Symbol.intern(null, (String)"...");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"take");
    public static final AFn const__18 = (AFn)Symbol.intern(null, (String)"one-column");
    public static final AFn const__19 = (AFn)Symbol.intern(null, (String)".");
    public static final AFn const__20 = (AFn)Symbol.intern(null, (String)"one-value");
    public static final Keyword const__21 = RT.keyword(null, (String)"default");

    public static Object invokeStatic(Object p__19363) {
        Object object;
        boolean and__5236__auto__19369;
        Object map__19364;
        Object object2;
        Object object3 = p__19363;
        p__19363 = null;
        Object map__193642 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__193642);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__193642;
            map__193642 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__193642;
            map__193642 = null;
        }
        Object qmap = map__19364 = object2;
        Object object6 = map__19364;
        map__19364 = null;
        Object find = RT.get((Object)object6, (Object)const__3);
        boolean and__5236__auto__19370 = Util.equiv((long)1L, (long)RT.count((Object)find));
        Object object7 = and__5236__auto__19370 ? ((and__5236__auto__19369 = ((IFn)const__9.getRawRoot()).invoke(find) instanceof List) ? ((IFn)const__10.getRawRoot()).invoke((Object)new query$process_find_bindings$fn__19365(), ((IFn)const__9.getRawRoot()).invoke(find)) : (and__5236__auto__19369 ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__19370 ? Boolean.TRUE : Boolean.FALSE);
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = qmap;
            qmap = null;
            Object object9 = find;
            find = null;
            object = ((IFn)const__11.getRawRoot()).invoke(object8, (Object)const__3, ((IFn)const__9.getRawRoot()).invoke(object9), (Object)const__12, (Object)const__13);
        } else {
            boolean and__5236__auto__19371;
            boolean and__5236__auto__19372;
            boolean and__5236__auto__19373 = Util.equiv((long)1L, (long)RT.count((Object)find));
            boolean bl = and__5236__auto__19373 ? ((and__5236__auto__19372 = ((IFn)const__9.getRawRoot()).invoke(find) instanceof List) ? ((and__5236__auto__19371 = Util.equiv((long)2L, (long)RT.count((Object)((IFn)const__9.getRawRoot()).invoke(find)))) ? Util.equiv((Object)const__15, (Object)((IFn)const__16.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(find))) : and__5236__auto__19371) : and__5236__auto__19372) : and__5236__auto__19373;
            if (bl) {
                Object object10 = qmap;
                qmap = null;
                Object object11 = find;
                find = null;
                object = ((IFn)const__11.getRawRoot()).invoke(object10, (Object)const__3, ((IFn)const__17.getRawRoot()).invoke(const__5, ((IFn)const__9.getRawRoot()).invoke(object11)), (Object)const__12, (Object)const__18);
            } else {
                boolean and__5236__auto__19374 = Util.equiv((long)2L, (long)RT.count((Object)find));
                if (and__5236__auto__19374 ? Util.equiv((Object)const__19, (Object)((IFn)const__16.getRawRoot()).invoke(find)) : and__5236__auto__19374) {
                    Object object12 = qmap;
                    qmap = null;
                    Object object13 = find;
                    find = null;
                    object = ((IFn)const__11.getRawRoot()).invoke(object12, (Object)const__3, ((IFn)const__17.getRawRoot()).invoke(const__5, object13), (Object)const__12, (Object)const__20);
                } else {
                    Keyword keyword = const__21;
                    if (keyword != null && keyword != Boolean.FALSE) {
                        object = qmap;
                        qmap = null;
                    } else {
                        object = null;
                    }
                }
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$process_find_bindings.invokeStatic(object2);
    }
}

