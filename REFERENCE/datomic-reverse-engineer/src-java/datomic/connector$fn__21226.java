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
import datomic.connector$fn__21226$__GT_TransactorHornetConnector__21280;
import java.util.Arrays;

public final class connector$fn__21226
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.connector", (String)"->TransactorHornetConnector");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster-conf"), (Object)Symbol.intern(null, (String)"transactor-endpoint"), (Object)Symbol.intern(null, (String)"hornet-factory")))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.connector.TransactorHornetConnector");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.connector.TransactorHornetConnector"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new connector$fn__21226$__GT_TransactorHornetConnector__21280());
        return const__6;
    }

    public Object invoke() {
        return connector$fn__21226.invokeStatic();
    }
}

