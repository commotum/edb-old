/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.concurrent.atomic.AtomicLong;

public final class db$use_transactor_tempid_range
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"nextid");

    public static Object invokeStatic() {
        ((AtomicLong)const__0.getRawRoot()).set(1000000000001L);
        return null;
    }

    public Object invoke() {
        return db$use_transactor_tempid_range.invokeStatic();
    }
}

