/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.concurrent.atomic.AtomicLong;

public final class db$next_id
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"nextid");

    public static Object invokeStatic() {
        return Numbers.num((long)((AtomicLong)const__0.getRawRoot()).getAndIncrement());
    }

    public Object invoke() {
        return db$next_id.invokeStatic();
    }
}

