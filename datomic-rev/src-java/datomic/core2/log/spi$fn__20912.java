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
package datomic.core2.log;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IPersistentMap;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class spi$fn__20912
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.log.spi", (String)"Delete");
    public static final AFn const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});

    public static Object invokeStatic() {
        Var var;
        Var v__6812__auto__20914;
        Var var2 = const__0;
        var2.setMeta((IPersistentMap)const__3);
        Var var3 = v__6812__auto__20914 = var2;
        v__6812__auto__20914 = null;
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
        return spi$fn__20912.invokeStatic();
    }
}

