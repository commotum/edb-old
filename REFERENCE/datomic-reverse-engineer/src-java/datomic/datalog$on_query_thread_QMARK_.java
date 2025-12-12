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

public final class datalog$on_query_thread_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"starts-with?");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke((Object)Thread.currentThread().getName(), (Object)"query-40e7d292-b60a-40ef-b6d8-db96660e415b-");
    }

    public Object invoke() {
        return datalog$on_query_thread_QMARK_.invokeStatic();
    }
}

