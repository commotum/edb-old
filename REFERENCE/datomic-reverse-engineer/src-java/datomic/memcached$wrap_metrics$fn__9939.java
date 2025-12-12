/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;

public final class memcached$wrap_metrics$fn__9939
extends AFunction {
    Object succ;
    Object record_kv;
    long start;
    Object f;
    Object fail;

    public memcached$wrap_metrics$fn__9939(Object object, Object object2, long l, Object object3, Object object4) {
        this.succ = object;
        this.record_kv = object2;
        this.start = l;
        this.f = object3;
        this.fail = object4;
    }

    public Object invoke(Object result2) {
        Object object = result2;
        result2 = null;
        Object v = ((IFn)this.f).invoke(object);
        Object object2 = v;
        ((IFn)this.record_kv).invoke(object2 != null && object2 != Boolean.FALSE ? this.succ : this.fail, (Object)Numbers.num((long)Numbers.minus((long)System.nanoTime(), (long)this.start)));
        Object var2_2 = null;
        return v;
    }
}

