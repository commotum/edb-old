/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class peer$fn__21498
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cluster-stack", (String)"start-kv-cache");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke();
    }

    public Object invoke() {
        return peer$fn__21498.invokeStatic();
    }
}

