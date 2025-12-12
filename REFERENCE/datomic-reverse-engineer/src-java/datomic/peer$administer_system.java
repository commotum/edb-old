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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class peer$administer_system
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"uri");
    public static final Keyword const__4 = RT.keyword(null, (String)"action");
    public static final Keyword const__7 = RT.keyword(null, (String)"upgrade-schema");
    public static final Var const__8 = RT.var((String)"datomic.peer", (String)"connect-uri");
    public static final Var const__9 = RT.var((String)"datomic.peer", (String)"ensure-schema-level");
    public static final Keyword const__10 = RT.keyword(null, (String)"completed");
    public static final Keyword const__11 = RT.keyword(null, (String)"release-object-cache");
    public static final Var const__12 = RT.var((String)"datomic.domain", (String)"system-cache");
    public static final Var const__13 = RT.var((String)"datomic.cache", (String)"clear");
    public static final Keyword const__14 = RT.keyword(null, (String)"else");

    public static Object invokeStatic(Object p__21691) {
        Keyword keyword;
        Object object;
        Object object2 = p__21691;
        p__21691 = null;
        Object map__21692 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__21692);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__21692;
            map__21692 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__21692;
            map__21692 = null;
        }
        Object map__216922 = object;
        Object uri2 = RT.get((Object)map__216922, (Object)const__3);
        Object object5 = map__216922;
        map__216922 = null;
        Object action = RT.get((Object)object5, (Object)const__4);
        if (Util.identical((Object)action, null)) {
            throw (Throwable)new IllegalArgumentException("Invalid options map.");
        }
        boolean and__5236__auto__21694 = Util.equiv((Object)action, (Object)const__7);
        if (and__5236__auto__21694 ? Util.identical((Object)uri2, null) : and__5236__auto__21694) {
            throw (Throwable)new IllegalArgumentException("Invalid options map.");
        }
        boolean and__5236__auto__21695 = Util.equiv((Object)action, (Object)const__7);
        Object object6 = and__5236__auto__21695 ? uri2 : (and__5236__auto__21695 ? Boolean.TRUE : Boolean.FALSE);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object conn;
            Object object7 = uri2;
            uri2 = null;
            Object object8 = conn = ((IFn)const__8.getRawRoot()).invoke(object7);
            conn = null;
            ((IFn)const__9.getRawRoot()).invoke(object8);
            keyword = const__10;
        } else {
            Object object9 = action;
            action = null;
            if (Util.equiv((Object)object9, (Object)const__11)) {
                Object system_cache2;
                Object object10 = system_cache2 = ((IFn)const__12.getRawRoot()).invoke();
                system_cache2 = null;
                ((IFn)const__13.getRawRoot()).invoke(object10);
                keyword = const__10;
            } else {
                Keyword keyword2 = const__14;
                if (keyword2 != null && keyword2 != Boolean.FALSE) {
                    throw (Throwable)new IllegalArgumentException("Invalid options map.");
                }
                keyword = null;
            }
        }
        return keyword;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$administer_system.invokeStatic(object2);
    }
}

