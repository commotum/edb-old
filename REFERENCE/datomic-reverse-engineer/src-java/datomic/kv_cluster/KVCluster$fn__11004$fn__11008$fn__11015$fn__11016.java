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
package datomic.kv_cluster;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.kv_cluster.KVCluster$fn__11004$fn__11008$fn__11015$fn__11016$fn__11018;

public final class KVCluster$fn__11004$fn__11008$fn__11015$fn__11016
extends AFunction {
    Object kvs;
    Object retrying_read;
    Object oldtail;
    public static final Keyword const__0 = RT.keyword(null, (String)"exponential");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"v");
    public static final Keyword const__5 = RT.keyword(null, (String)"prev");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"conj");

    public KVCluster$fn__11004$fn__11008$fn__11015$fn__11016(Object object, Object object2, Object object3) {
        this.kvs = object;
        this.retrying_read = object2;
        this.oldtail = object3;
    }

    public Object invoke() {
        Object ks = Tuple.create((Object)this.oldtail);
        while (true) {
            Object prev;
            Object object;
            Object map__11017 = ((IFn)this.retrying_read).invoke((Object)const__0, (Object)new KVCluster$fn__11004$fn__11008$fn__11015$fn__11016$fn__11018(ks, this.kvs));
            Object object2 = ((IFn)const__1.getRawRoot()).invoke(map__11017);
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object3 = map__11017;
                map__11017 = null;
                object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object3)));
            } else {
                object = map__11017;
                map__11017 = null;
            }
            Object map__110172 = object;
            RT.get((Object)map__110172, (Object)const__4);
            Object object4 = map__110172;
            map__110172 = null;
            Object object5 = prev = RT.get((Object)object4, (Object)const__5);
            if (object5 == null || object5 == Boolean.FALSE) break;
            IPersistentVector iPersistentVector = ks;
            ks = null;
            Object object6 = prev;
            prev = null;
            ks = ((IFn)const__6.getRawRoot()).invoke((Object)iPersistentVector, object6);
        }
        Object var1_1 = null;
        return ks;
    }
}

