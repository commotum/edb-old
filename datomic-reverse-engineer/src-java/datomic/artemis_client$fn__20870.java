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
import datomic.artemis_client$fn__20870$__GT_RpcClient__20874;
import java.util.Arrays;

public final class artemis_client$fn__20870
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.artemis-client", (String)"->RpcClient");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"session"), Symbol.intern(null, (String)"producer"), Symbol.intern(null, (String)"consumer"), Symbol.intern(null, (String)"serializer"), Symbol.intern(null, (String)"response-map"), Symbol.intern(null, (String)"producer-queue"), Symbol.intern(null, (String)"consumer-queue"), Symbol.intern(null, (String)"cleanup")}))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.artemis_client.RpcClient");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.artemis_client.RpcClient"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new artemis_client$fn__20870$__GT_RpcClient__20874());
        return const__6;
    }

    public Object invoke() {
        return artemis_client$fn__20870.invokeStatic();
    }
}

