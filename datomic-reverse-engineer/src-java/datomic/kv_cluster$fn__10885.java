/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IPersistentMap;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.kv_cluster$fn__10885$__GT_KVCluster__11058;
import java.util.Arrays;

public final class kv_cluster$fn__10885
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.kv-cluster", (String)"->KVCluster");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"kvs"), Symbol.intern(null, (String)"path-map"), Symbol.intern(null, (String)"exec"), Symbol.intern(null, (String)"retrying-write"), Symbol.intern(null, (String)"retrying-read"), Symbol.intern(null, (String)"retrying-delete"), Symbol.intern(null, (String)"protocol"), Symbol.intern(null, (String)"protocol-nsec-k"), Symbol.intern(null, (String)"pod-garbage-handler")}))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.kv_cluster.KVCluster");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.kv_cluster.KVCluster"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new kv_cluster$fn__10885$__GT_KVCluster__11058());
        return const__6;
    }

    public Object invoke() {
        return kv_cluster$fn__10885.invokeStatic();
    }
}

