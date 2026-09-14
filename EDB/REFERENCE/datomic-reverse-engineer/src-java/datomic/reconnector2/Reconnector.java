/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IBlockingDeref
 *  clojure.lang.IDeref
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.reconnector2;

import clojure.lang.IBlockingDeref;
import clojure.lang.IDeref;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.common.AsyncShutdown;
import datomic.reconnector2.Reconnectable;
import datomic.reconnector2.Reconnector$fn__17129;
import datomic.reconnector2.Reconnector$fn__17134;
import datomic.reconnector2.Reconnector$fn__17136;

public final class Reconnector
implements IBlockingDeref,
Reconnectable,
IDeref,
AsyncShutdown,
IType {
    public final Object current_promise_ref;
    public final Object worker_ref;
    public final Object shutdown_state;
    public final Object reconnect_fn;
    public final Object cleanup_fn;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"future-call");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Object const__2 = 0L;
    public static final Keyword const__3 = RT.keyword(null, (String)"reconnecting");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__5 = RT.keyword(null, (String)"ok");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"reset!");
    public static final Var const__8 = RT.var((String)"datomic.promise", (String)"settable-future");

    public Reconnector(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.current_promise_ref = object;
        this.worker_ref = object2;
        this.shutdown_state = object3;
        this.reconnect_fn = object4;
        this.cleanup_fn = object5;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"current-promise-ref"), (Object)Symbol.intern(null, (String)"worker-ref"), (Object)Symbol.intern(null, (String)"shutdown-state"), (Object)Symbol.intern(null, (String)"reconnect-fn"), (Object)Symbol.intern(null, (String)"cleanup-fn"));
    }

    public Object reconnect() {
        Keyword keyword;
        Object lockee__5436__auto__17140 = this.worker_ref;
        try {
            synchronized (lockee__5436__auto__17140) {
                Keyword keyword2;
                Object object = ((IFn)const__1.getRawRoot()).invoke(this.worker_ref);
                if (object != null && object != Boolean.FALSE) {
                    keyword2 = const__5;
                } else {
                    Object state2 = ((IFn)const__1.getRawRoot()).invoke((Object)this);
                    if (Util.equiv((Object)state2, (Object)this.shutdown_state)) {
                        keyword2 = null;
                    } else {
                        Object object2 = state2;
                        state2 = null;
                        ((IFn)const__0.getRawRoot()).invoke((Object)new Reconnector$fn__17134(object2, this.cleanup_fn));
                        ((IFn)const__7.getRawRoot()).invoke(this.current_promise_ref, ((IFn)const__8.getRawRoot()).invoke());
                        ((IFn)const__7.getRawRoot()).invoke(this.worker_ref, ((IFn)const__0.getRawRoot()).invoke((Object)new Reconnector$fn__17136(this.current_promise_ref, this.worker_ref, this.reconnect_fn)));
                        keyword2 = const__5;
                    }
                }
                keyword = keyword2;
            }
        }
        finally {
            Object object = lockee__5436__auto__17140;
            lockee__5436__auto__17140 = null;
            // ** MonitorExit[v3] (shouldn't be in output)
        }
        {
            return keyword;
        }
    }

    public String toString() {
        Object obj;
        Object object = obj = ((IFn)const__1.getRawRoot()).invoke((Object)this_, const__2, (Object)const__3);
        obj = null;
        Reconnector this_ = null;
        return (String)((IFn)const__4.getRawRoot()).invoke((Object)"#<Reconnector: ", object, (Object)">");
    }

    public Object deref(long msec, Object object) {
        Object object2 = object;
        object = null;
        Reconnector this_ = null;
        return ((IFn)const__1.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this_.current_promise_ref), (Object)Numbers.num((long)msec), object2);
    }

    public Object deref() {
        Reconnector this_ = null;
        return ((IFn)const__1.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this_.current_promise_ref));
    }

    public Object async_shutdown() {
        Reconnector$fn__17129 reconnector$fn__17129 = new Reconnector$fn__17129(this_.current_promise_ref, this_.shutdown_state, this_.worker_ref, this_.cleanup_fn, this_);
        Reconnector this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)reconnector$fn__17129);
    }
}

