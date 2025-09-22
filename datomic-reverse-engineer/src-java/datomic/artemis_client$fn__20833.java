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

public final class artemis_client$fn__20833
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.promise", (String)"delivered");

    public static Object invokeStatic(Object this_) {
        Object object = this_;
        this_ = null;
        ((ClientSessionFactory)object).close();
        return ((IFn)const__0.getRawRoot()).invoke((Object)Boolean.TRUE);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return artemis_client$fn__20833.invokeStatic(object2);
    }
}

