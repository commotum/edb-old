/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic.valcache;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.valcache.IServer;
import java.util.concurrent.Semaphore;

public final class Server
implements AutoCloseable,
IServer,
IType {
    public final Object sem;
    public final long concurrency;
    public final Object socket_registry;
    public final Object handled;
    public final Object host;
    public final Object port;
    public final Object path;
    public final Object shutdown_fn;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"deref");

    public Server(Object object, long l, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.sem = object;
        this.concurrency = l;
        this.socket_registry = object2;
        this.handled = object3;
        this.host = object4;
        this.port = object5;
        this.path = object6;
        this.shutdown_fn = object7;
    }

    public static IPersistentVector getBasis() {
        return RT.vector((Object[])new Object[]{((IObj)Symbol.intern(null, (String)"sem")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Semaphore")})), ((IObj)Symbol.intern(null, (String)"concurrency")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), Symbol.intern(null, (String)"socket-registry"), Symbol.intern(null, (String)"handled"), Symbol.intern(null, (String)"host"), Symbol.intern(null, (String)"port"), Symbol.intern(null, (String)"path"), Symbol.intern(null, (String)"shutdown-fn")});
    }

    public Object handled_count() {
        Server this_ = null;
        return ((IFn)const__2.getRawRoot()).invoke(this_.handled);
    }

    public Object pending_count() {
        return ((Semaphore)this.sem).getQueueLength();
    }

    public Object running_count() {
        Server this_ = null;
        return Numbers.num((long)Numbers.minus((long)this_.concurrency, (long)((Semaphore)this_.sem).availablePermits()));
    }

    public Object connection_count() {
        Server this_ = null;
        return RT.count((Object)this_.socket_registry);
    }

    public void close() throws Exception {
        Server this_ = null;
        ((IFn)this_.shutdown_fn).invoke();
    }
}

