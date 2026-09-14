/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class datalog$fn__18046
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"query-pool");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"java.util.concurrent.ExecutorService"), RT.keyword(null, (String)"column"), 1});
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"java.util.concurrent.ExecutorService"), RT.keyword(null, (String)"column"), 1});
    public static final Var const__7 = RT.var((String)"datomic.common", (String)"thread-pool");
    public static final Keyword const__8 = RT.keyword(null, (String)"nthreads");
    public static final Var const__9 = RT.var((String)"datomic.config", (String)"property");
    public static final Keyword const__10 = RT.keyword(null, (String)"name");
    public static final Keyword const__11 = RT.keyword(null, (String)"metrics?");

    public static Object invokeStatic() {
        Var var;
        Var v__6457__auto__18048;
        Var var2 = const__0;
        var2.setMeta((IPersistentMap)const__5);
        Var var3 = v__6457__auto__18048 = var2;
        v__6457__auto__18048 = null;
        if (var3.hasRoot()) {
            var = null;
        } else {
            Var var4 = const__0;
            var4.setMeta((IPersistentMap)const__6);
            var = var4;
            var4.bindRoot(((IFn)const__7.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__8, ((IFn)const__9.getRawRoot()).invoke((Object)"datomic.queryPool"), const__10, "query-40e7d292-b60a-40ef-b6d8-db96660e415b-", const__11, Boolean.FALSE})));
        }
        return var;
    }

    public Object invoke() {
        return datalog$fn__18046.invokeStatic();
    }
}

