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
import datomic.Connection;

public final class peer$stop_connection
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"db-id");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Var const__6 = RT.var((String)"datomic.peer", (String)"connection-cache");
    public static final Var const__7 = RT.var((String)"datomic.cache", (String)"clear");
    public static final Var const__8 = RT.var((String)"datomic.coordination", (String)"db-cache");

    public static Object invokeStatic(Object p__21638) {
        block3: {
            Object conn;
            Object temp__5457__auto__21641;
            Object map__21639;
            Object object;
            Object object2 = p__21638;
            p__21638 = null;
            Object map__216392 = object2;
            Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__216392);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = map__216392;
                map__216392 = null;
                object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
            } else {
                object = map__216392;
                map__216392 = null;
            }
            Object resolved_cluster_conf = map__21639 = object;
            Object object5 = map__21639;
            map__21639 = null;
            RT.get((Object)object5, (Object)const__3);
            Keyword keyword = const__3;
            if (keyword == null || keyword == Boolean.FALSE) {
                throw (Throwable)((Object)new AssertionError(((IFn)const__4.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__5.getRawRoot()).invoke((Object)const__3))));
            }
            Object object6 = resolved_cluster_conf;
            resolved_cluster_conf = null;
            Object object7 = temp__5457__auto__21641 = RT.get((Object)const__6.getRawRoot(), (Object)object6);
            if (object7 == null || object7 == Boolean.FALSE) break block3;
            Object object8 = temp__5457__auto__21641;
            temp__5457__auto__21641 = null;
            Object object9 = conn = object8;
            conn = null;
            ((Connection)object9).release();
        }
        return ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return peer$stop_connection.invokeStatic(object2);
    }
}

