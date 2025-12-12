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
import datomic.db$summarize_tx_stats$fn__13409;

public final class db$summarize_tx_stats
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"persistent!");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reduce-kv");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"transient");

    public static Object invokeStatic(Object tx_stat_registers) {
        Object object = tx_stat_registers;
        tx_stat_registers = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)new db$summarize_tx_stats$fn__13409(), ((IFn)const__2.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY), object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$summarize_tx_stats.invokeStatic(object2);
    }
}

