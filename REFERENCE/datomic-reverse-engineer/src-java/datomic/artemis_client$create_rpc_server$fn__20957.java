/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.common.AsyncShutdown;

public final class artemis_client$create_rpc_server$fn__20957
extends AFunction {
    Object session;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    public artemis_client$create_rpc_server$fn__20957(Object object) {
        this.session = object;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            v0 = this.session;
            this.session = null;
            v1 = v0;
            if (Util.classOf((Object)v0) == artemis_client$create_rpc_server$fn__20957.__cached_class__0) ** GOTO lbl9
            if (!(v1 instanceof AsyncShutdown)) {
                v1 = v1;
                artemis_client$create_rpc_server$fn__20957.__cached_class__0 = Util.classOf((Object)v1);
lbl9:
                // 2 sources

                v2 = artemis_client$create_rpc_server$fn__20957.const__0.getRawRoot().invoke(v1);
            } else {
                v2 = ((AsyncShutdown)v1).async_shutdown();
            }
            var1_1 = v2;
        }
        catch (Throwable t__708__auto__) {
            t__708__auto__ = null;
            var1_1 = ((IFn)artemis_client$create_rpc_server$fn__20957.const__1.getRawRoot()).invoke((Object)t__708__auto__);
        }
        return var1_1;
    }

    static {
        const__0 = RT.var((String)"datomic.common", (String)"async-shutdown");
        const__1 = RT.var((String)"datomic.error", (String)"report");
    }
}

