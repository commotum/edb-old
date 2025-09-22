/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class tools$peer_diagnostics
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"peer-config");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.config", (String)"properties-ref");
    public static final Keyword const__3 = RT.keyword(null, (String)"object-cache-count");
    public static final Var const__4 = RT.var((String)"datomic.cache", (String)"fast-count");
    public static final Var const__5 = RT.var((String)"datomic.domain", (String)"system-cache");
    public static final Keyword const__6 = RT.keyword(null, (String)"memory-mb");
    public static final Var const__7 = RT.var((String)"datomic.math", (String)"rounded-mb");

    public static Object invokeStatic() {
        return RT.mapUniqueKeys((Object[])new Object[]{const__0, ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot()), const__3, ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke()), const__6, ((IFn)const__7.getRawRoot()).invoke((Object)Numbers.num((long)Runtime.getRuntime().maxMemory()))});
    }

    public Object invoke() {
        return tools$peer_diagnostics.invokeStatic();
    }
}

