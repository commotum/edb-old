/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.cast2slf4j$fn__21290$fn__21291;

public final class cast2slf4j$fn__21290
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cast2slf4j", (String)"work");
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});

    public static Object invokeStatic() {
        Var var;
        Var v__6457__auto__21305;
        Var var2 = const__0;
        var2.setMeta((IPersistentMap)const__4);
        Var var3 = v__6457__auto__21305 = var2;
        v__6457__auto__21305 = null;
        if (var3.hasRoot()) {
            var = null;
        } else {
            Var var4 = const__0;
            var4.setMeta((IPersistentMap)const__5);
            var = var4;
            var4.bindRoot((Object)new Delay((IFn)new cast2slf4j$fn__21290$fn__21291()));
        }
        return var;
    }

    public Object invoke() {
        return cast2slf4j$fn__21290.invokeStatic();
    }
}

