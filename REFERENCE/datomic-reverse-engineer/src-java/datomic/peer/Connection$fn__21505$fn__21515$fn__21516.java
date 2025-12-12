/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.peer;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class Connection$fn__21505$fn__21515$fn__21516
extends AFunction {
    Object shutdown;
    Object notifier;
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"report");

    public Connection$fn__21505$fn__21515$fn__21516(Object object, Object object2) {
        this.shutdown = object;
        this.notifier = object2;
    }

    public Object invoke() {
        Object object;
        try {
            this.shutdown = null;
            this.notifier = null;
            object = ((IFn)this.shutdown).invoke(this.notifier);
        }
        catch (Throwable t__708__auto__2) {
            Object t__708__auto__2 = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)t__708__auto__2);
        }
        return object;
    }
}

