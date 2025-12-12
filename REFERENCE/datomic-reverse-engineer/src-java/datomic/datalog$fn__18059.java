/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IPersistentMap
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IPersistentMap;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class datalog$fn__18059
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"IJoin");
    public static final AFn const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});

    public static Object invokeStatic() {
        Var var;
        Var v__6457__auto__18061;
        Var var2 = const__0;
        var2.setMeta((IPersistentMap)const__3);
        Var var3 = v__6457__auto__18061 = var2;
        v__6457__auto__18061 = null;
        if (var3.hasRoot()) {
            var = null;
        } else {
            Var var4 = const__0;
            var4.setMeta((IPersistentMap)const__4);
            var = var4;
            var4.bindRoot((Object)PersistentArrayMap.EMPTY);
        }
        return var;
    }

    public Object invoke() {
        return datalog$fn__18059.invokeStatic();
    }
}

