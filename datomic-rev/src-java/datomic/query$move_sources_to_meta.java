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
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.query$move_sources_to_meta$fn__19258;

public final class query$move_sources_to_meta
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"in");
    public static final Keyword const__4 = RT.keyword(null, (String)"where");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"map");

    public static Object invokeStatic(Object p__19256) {
        Object object;
        Object map__19257;
        Object object2;
        Object object3 = p__19256;
        p__19256 = null;
        Object map__192572 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__192572);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__192572;
            map__192572 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__192572;
            map__192572 = null;
        }
        Object q2 = map__19257 = object2;
        Object srcs = RT.get((Object)map__19257, (Object)const__3);
        Object object6 = map__19257;
        map__19257 = null;
        Object clauses = RT.get((Object)object6, (Object)const__4);
        Object object7 = srcs;
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = srcs;
            srcs = null;
            Object sset = ((IFn)const__5.getRawRoot()).invoke(object8);
            Object object9 = q2;
            q2 = null;
            Object object10 = sset;
            sset = null;
            Object object11 = clauses;
            clauses = null;
            object = ((IFn)const__6.getRawRoot()).invoke(object9, (Object)const__4, ((IFn)const__7.getRawRoot()).invoke((Object)new query$move_sources_to_meta$fn__19258(object10), object11));
        } else {
            object = q2;
            Object var3_3 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$move_sources_to_meta.invokeStatic(object2);
    }
}

