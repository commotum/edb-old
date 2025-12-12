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

public final class peer$revert_to_log_version_1
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"db-uri");
    public static final Var const__4 = RT.var((String)"datomic.coordination", (String)"create-db-cluster");
    public static final Var const__5 = RT.var((String)"datomic.coordination", (String)"resolve-db-name");
    public static final Var const__6 = RT.var((String)"datomic.uri", (String)"parse");
    public static final Var const__7 = RT.var((String)"datomic.log", (String)"convert-log-version");
    public static final Object const__8 = 1L;

    public static Object invokeStatic(Object p__21658) {
        Object cluster2;
        Object db_uri2;
        Object map__21659;
        Object object;
        Object object2 = p__21658;
        p__21658 = null;
        Object map__216592 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__216592);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__216592;
            map__216592 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__216592;
            map__216592 = null;
        }
        Object object5 = map__21659 = object;
        map__21659 = null;
        Object object6 = db_uri2 = RT.get((Object)object5, (Object)const__3);
        db_uri2 = null;
        Object object7 = cluster2 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(object6)));
        cluster2 = null;
        return ((IFn)const__7.getRawRoot()).invoke(object7, const__8);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$revert_to_log_version_1.invokeStatic(object2);
    }
}

