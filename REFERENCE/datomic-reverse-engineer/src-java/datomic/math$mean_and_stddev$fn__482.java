/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;

public final class math$mean_and_stddev$fn__482
extends AFunction {
    Object m;

    public math$mean_and_stddev$fn__482(Object object) {
        this.m = object;
    }

    public Object invoke(Object v) {
        Number x;
        Object object = v;
        v = null;
        Number number = x = Numbers.minus((Object)object, (Object)this_.m);
        Number number2 = x;
        x = null;
        math$mean_and_stddev$fn__482 this_ = null;
        return Numbers.multiply((Object)number, (Object)number2);
    }
}

