/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.connector;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class TransactorHornetConnector$fn__21230$fn__21231
extends AFunction {
    Object session;
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"sync-shutdown");
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"report");

    public TransactorHornetConnector$fn__21230$fn__21231(Object object) {
        this.session = object;
    }

    public Object invoke() {
        Object object;
        try {
            this.session = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this.session);
        }
        catch (Throwable t__708__auto__2) {
            Object t__708__auto__2 = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)t__708__auto__2);
        }
        return object;
    }
}

