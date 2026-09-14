/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.connector;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.common.AsyncShutdown;

public final class TransactorHornetConnector$reify__21277
implements AsyncShutdown,
IObj {
    final IPersistentMap __meta;
    Object cleanup;
    public static final Var const__0 = RT.var((String)"datomic.promise", (String)"delivered");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");

    public TransactorHornetConnector$reify__21277(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.cleanup = object;
    }

    public TransactorHornetConnector$reify__21277(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new TransactorHornetConnector$reify__21277(iPersistentMap, this.cleanup);
    }

    public Object async_shutdown() {
        TransactorHornetConnector$reify__21277 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this_.cleanup));
    }
}

