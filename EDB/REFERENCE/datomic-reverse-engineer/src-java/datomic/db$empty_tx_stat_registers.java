/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$empty_tx_stat_registers$fn__13406;

public final class db$empty_tx_stat_registers
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"persistent!");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"transient");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"tx-stat-keys");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)new db$empty_tx_stat_registers$fn__13406(), ((IFn)const__2.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY), const__3.getRawRoot()));
    }

    public Object invoke() {
        return db$empty_tx_stat_registers.invokeStatic();
    }
}

