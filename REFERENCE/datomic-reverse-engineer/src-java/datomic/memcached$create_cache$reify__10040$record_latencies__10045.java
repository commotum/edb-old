/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  datomic.spy.memcached.OperationTimeoutException
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.spy.memcached.OperationTimeoutException;

public final class memcached$create_cache$reify__10040$record_latencies__10045
extends AFunction {
    Object get_failed;
    Object record_kv;
    Object get_succeeded;
    Object get_missed;
    Object get_timeout;
    Object get_queue_full;
    Object io_latency;
    public static final Var const__3 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");

    public memcached$create_cache$reify__10040$record_latencies__10045(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.get_failed = object;
        this.record_kv = object2;
        this.get_succeeded = object3;
        this.get_missed = object4;
        this.get_timeout = object5;
        this.get_queue_full = object6;
        this.io_latency = object7;
    }

    public Object invoke(Object nanos, Object p__10044) {
        Object object;
        Object temp__5457__auto__10050;
        Object object2;
        Object object3 = p__10044;
        p__10044 = null;
        Object vec__10046 = object3;
        Object v = RT.nth((Object)vec__10046, (int)RT.intCast((long)0L), null);
        Object object4 = vec__10046;
        vec__10046 = null;
        Object ex = RT.nth((Object)object4, (int)RT.intCast((long)1L), null);
        Object object5 = v;
        if (object5 != null && object5 != Boolean.FALSE) {
        } else {
            ((IFn)this_.record_kv).invoke(this_.get_failed, nanos);
        }
        ((IFn.OLO)const__3.getRawRoot()).invokePrim(this_.io_latency, RT.longCast((Object)((Number)nanos)));
        Object object6 = v;
        v = null;
        if (object6 != null && object6 != Boolean.FALSE) {
            object2 = this_.get_succeeded;
        } else if (Util.identical((Object)ex, null)) {
            object2 = this_.get_missed;
        } else if (ex instanceof OperationTimeoutException) {
            object2 = this_.get_timeout;
        } else {
            Object object7 = ex;
            ex = null;
            object2 = object7 instanceof IllegalStateException ? this_.get_queue_full : null;
        }
        Object object8 = temp__5457__auto__10050 = object2;
        if (object8 != null && object8 != Boolean.FALSE) {
            Object k;
            Object object9 = temp__5457__auto__10050;
            temp__5457__auto__10050 = null;
            Object object10 = k = object9;
            k = null;
            Object object11 = nanos;
            nanos = null;
            memcached$create_cache$reify__10040$record_latencies__10045 this_ = null;
            object = ((IFn)this_.record_kv).invoke(object10, object11);
        } else {
            object = null;
        }
        return object;
    }
}

