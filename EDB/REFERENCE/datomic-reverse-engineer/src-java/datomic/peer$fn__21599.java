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
 *  clojure.lang.Tuple
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
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.peer$fn__21599$__GT_LocalConnection__21609;
import java.util.Arrays;

public final class peer$fn__21599
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.peer", (String)"->LocalConnection");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"dbname"), (Object)Symbol.intern(null, (String)"db-ref"), (Object)Symbol.intern(null, (String)"tx-report-queue"), (Object)Symbol.intern(null, (String)"released"), (Object)Symbol.intern(null, (String)"tx-watcher")))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.peer.LocalConnection");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.peer.LocalConnection"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new peer$fn__21599$__GT_LocalConnection__21609());
        return const__6;
    }

    public Object invoke() {
        return peer$fn__21599.invokeStatic();
    }
}

