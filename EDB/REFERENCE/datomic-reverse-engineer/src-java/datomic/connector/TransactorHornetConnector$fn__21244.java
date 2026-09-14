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

public final class TransactorHornetConnector$fn__21244
extends AFunction {
    Object session;
    Object result_queue;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__1 = RT.var((String)"datomic.artemis-client", (String)"delete-queue");
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"report");

    public TransactorHornetConnector$fn__21244(Object object, Object object2) {
        this.session = object;
        this.result_queue = object2;
    }

    public Object invoke() {
        Object object;
        try {
            this.session = null;
            this.result_queue = null;
            object = ((IFn)((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), this.session)).invoke(this.result_queue);
        }
        catch (Throwable t__708__auto__2) {
            Object t__708__auto__2 = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)t__708__auto__2);
        }
        return object;
    }
}

