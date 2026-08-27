/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.apache.activemq.artemis.api.core.client.ClientSessionFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;

public final class artemis_client$create_session_factory$fn__20822$fn__20823
extends AFunction {
    Object session_factory;
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"report");

    public artemis_client$create_session_factory$fn__20822$fn__20823(Object object) {
        this.session_factory = object;
    }

    public Object invoke() {
        Object object;
        try {
            this.session_factory = null;
            ((ClientSessionFactory)this.session_factory).close();
            object = null;
        }
        catch (Throwable t__708__auto__2) {
            Object t__708__auto__2 = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)t__708__auto__2);
        }
        return object;
    }
}

