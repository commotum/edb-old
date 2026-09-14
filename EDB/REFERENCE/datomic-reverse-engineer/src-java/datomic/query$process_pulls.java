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
import datomic.query$process_pulls$fn__19387;
import datomic.query$process_pulls$fn__19393;
import datomic.query$process_pulls$pull_QMARK___19384;

public final class query$process_pulls
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"find");
    public static final Keyword const__4 = RT.keyword(null, (String)"in");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__7 = RT.keyword(null, (String)"pull");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"mapv");

    public static Object invokeStatic(Object p__19382) {
        Object object;
        Object map__19383;
        Object object2;
        Object object3 = p__19382;
        p__19382 = null;
        Object map__193832 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__193832);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__193832;
            map__193832 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__193832;
            map__193832 = null;
        }
        Object qmap = map__19383 = object2;
        Object find = RT.get((Object)map__19383, (Object)const__3);
        Object object6 = map__19383;
        map__19383 = null;
        Object in = RT.get((Object)object6, (Object)const__4);
        query$process_pulls$pull_QMARK___19384 pull_QMARK_ = new query$process_pulls$pull_QMARK___19384();
        Object object7 = ((IFn)const__5.getRawRoot()).invoke((Object)pull_QMARK_, find);
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = qmap;
            qmap = null;
            Object object9 = in;
            in = null;
            Object object10 = ((IFn)const__8.getRawRoot()).invoke((Object)new query$process_pulls$fn__19387((Object)pull_QMARK_, object9), find);
            query$process_pulls$pull_QMARK___19384 query$process_pulls$pull_QMARK___19384 = pull_QMARK_;
            pull_QMARK_ = null;
            Object object11 = find;
            find = null;
            object = ((IFn)const__6.getRawRoot()).invoke(object8, (Object)const__7, object10, (Object)const__3, ((IFn)const__8.getRawRoot()).invoke((Object)new query$process_pulls$fn__19393((Object)query$process_pulls$pull_QMARK___19384), object11));
        } else {
            object = qmap;
            Object var3_3 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$process_pulls.invokeStatic(object2);
    }
}

