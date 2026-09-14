/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public final class datalog$fn__18050
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"cancel-service");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"java.util.concurrent.ScheduledExecutorService"), RT.keyword(null, (String)"column"), 1});
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"java.util.concurrent.ScheduledExecutorService"), RT.keyword(null, (String)"column"), 1});

    public static Object invokeStatic() {
        Var var;
        Var v__6457__auto__18052;
        Var var2 = const__0;
        var2.setMeta((IPersistentMap)const__5);
        Var var3 = v__6457__auto__18052 = var2;
        v__6457__auto__18052 = null;
        if (var3.hasRoot()) {
            var = null;
        } else {
            Var var4 = const__0;
            var4.setMeta((IPersistentMap)const__6);
            var = var4;
            var4.bindRoot((Object)new ScheduledThreadPoolExecutor(RT.uncheckedIntCast((long)1L)));
        }
        return var;
    }

    public Object invoke() {
        return datalog$fn__18050.invokeStatic();
    }
}

