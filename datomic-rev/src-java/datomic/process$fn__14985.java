/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class process$fn__14985
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.process", (String)"instance");
    public static final AFn const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
    public static final Var const__5 = RT.var((String)"datomic.process", (String)"create-instance");
    public static final Object const__6 = 30000L;

    public static Object invokeStatic() {
        Var var;
        Var v__6457__auto__14987;
        Var var2 = const__0;
        var2.setMeta((IPersistentMap)const__3);
        Var var3 = v__6457__auto__14987 = var2;
        v__6457__auto__14987 = null;
        if (var3.hasRoot()) {
            var = null;
        } else {
            Var var4 = const__0;
            var4.setMeta((IPersistentMap)const__4);
            var = var4;
            var4.bindRoot(((IFn)const__5.getRawRoot()).invoke(const__6, (Object)Boolean.TRUE));
        }
        return var;
    }

    public Object invoke() {
        return process$fn__14985.invokeStatic();
    }
}

