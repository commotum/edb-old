/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.process;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class events$fn__10839
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.process.events", (String)"subscribers-ref");
    public static final AFn const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"atom");

    public static Object invokeStatic() {
        Var var;
        Var v__6457__auto__10841;
        Var var2 = const__0;
        var2.setMeta((IPersistentMap)const__3);
        Var var3 = v__6457__auto__10841 = var2;
        v__6457__auto__10841 = null;
        if (var3.hasRoot()) {
            var = null;
        } else {
            Var var4 = const__0;
            var4.setMeta((IPersistentMap)const__4);
            var = var4;
            var4.bindRoot(((IFn)const__5.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY));
        }
        return var;
    }

    public Object invoke() {
        return events$fn__10839.invokeStatic();
    }
}

