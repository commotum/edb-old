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

public final class artemis_client$create_rpc_server$fn__20927$fn__20928
extends AFunction {
    Object request_queue;
    Object dq;
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"report");

    public artemis_client$create_rpc_server$fn__20927$fn__20928(Object object, Object object2) {
        this.request_queue = object;
        this.dq = object2;
    }

    public Object invoke() {
        Object object;
        try {
            this.dq = null;
            this.request_queue = null;
            object = ((IFn)this.dq).invoke(this.request_queue);
        }
        catch (Throwable t__708__auto__2) {
            Object t__708__auto__2 = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)t__708__auto__2);
        }
        return object;
    }
}

