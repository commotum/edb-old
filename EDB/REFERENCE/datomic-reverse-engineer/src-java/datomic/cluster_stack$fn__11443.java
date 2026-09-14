/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class cluster_stack$fn__11443
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"add-watch");
    public static final Keyword const__2 = RT.keyword((String)"datomic.cluster-stack", (String)"closer");
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"closing-watch");

    public static Object invokeStatic() {
        Object G__11442 = ((IFn)const__0.getRawRoot()).invoke(null);
        ((IFn)const__1.getRawRoot()).invoke(G__11442, (Object)const__2, const__3.getRawRoot());
        Object var0 = null;
        return G__11442;
    }

    public Object invoke() {
        return cluster_stack$fn__11443.invokeStatic();
    }
}

