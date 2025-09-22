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

public final class Connection$fn__21505$fn__21511
extends AFunction {
    Object cluster_conf;
    Object lockee__5436__auto__;
    public static final Var const__0 = RT.var((String)"datomic.peer", (String)"stop-connection");

    public Connection$fn__21505$fn__21511(Object object, Object object2) {
        this.cluster_conf = object;
        this.lockee__5436__auto__ = object2;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public Object invoke() {
        synchronized (this.lockee__5436__auto__) {
            Object object = ((IFn)const__0.getRawRoot()).invoke(this.cluster_conf);
            return object;
        }
    }
}

