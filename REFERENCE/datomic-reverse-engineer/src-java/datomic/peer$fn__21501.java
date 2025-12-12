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
import datomic.peer$fn__21501$__GT_Connection__21574;
import java.util.Arrays;

public final class peer$fn__21501
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.peer", (String)"->Connection");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"db-id"), Symbol.intern(null, (String)"cluster"), Symbol.intern(null, (String)"olookup"), Symbol.intern(null, (String)"state-ref"), Symbol.intern(null, (String)"db-ref"), Symbol.intern(null, (String)"pending-txes"), Symbol.intern(null, (String)"unsent-updates-queue"), Symbol.intern(null, (String)"lucene-queue"), Symbol.intern(null, (String)"tx-report-queue"), Symbol.intern(null, (String)"tx-watcher"), Symbol.intern(null, (String)"idx-watcher"), Symbol.intern(null, (String)"bg-watcher")}))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.peer.Connection");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.peer.Connection"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new peer$fn__21501$__GT_Connection__21574());
        return const__6;
    }

    public Object invoke() {
        return peer$fn__21501.invokeStatic();
    }
}

