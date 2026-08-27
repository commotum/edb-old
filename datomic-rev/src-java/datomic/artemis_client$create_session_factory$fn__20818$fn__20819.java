/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.apache.activemq.artemis.api.core.client.ServerLocator
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import org.apache.activemq.artemis.api.core.client.ServerLocator;

public final class artemis_client$create_session_factory$fn__20818$fn__20819
extends AFunction {
    Object loc;
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"report");

    public artemis_client$create_session_factory$fn__20818$fn__20819(Object object) {
        this.loc = object;
    }

    public Object invoke() {
        Object object;
        try {
            this.loc = null;
            ((ServerLocator)this.loc).close();
            object = null;
        }
        catch (Throwable t__708__auto__2) {
            Object t__708__auto__2 = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)t__708__auto__2);
        }
        return object;
    }
}

