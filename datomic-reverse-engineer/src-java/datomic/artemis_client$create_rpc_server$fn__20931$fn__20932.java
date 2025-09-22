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

public final class artemis_client$create_rpc_server$fn__20931$fn__20932
extends AFunction {
    Object dq;
    Object response_queue;
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"report");

    public artemis_client$create_rpc_server$fn__20931$fn__20932(Object object, Object object2) {
        this.dq = object;
        this.response_queue = object2;
    }

    public Object invoke() {
        Object object;
        try {
            this.dq = null;
            this.response_queue = null;
            object = ((IFn)this.dq).invoke(this.response_queue);
        }
        catch (Throwable t__708__auto__2) {
            Object t__708__auto__2 = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)t__708__auto__2);
        }
        return object;
    }
}

