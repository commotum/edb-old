/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;

public final class math$create_exponential$fn__491
extends AFunction {
    double a;
    double b;

    public math$create_exponential$fn__491(double d, double d2) {
        this.a = d;
        this.b = d2;
    }

    public Object invoke(Object x) {
        Object object = x;
        x = null;
        math$create_exponential$fn__491 this_ = null;
        return Numbers.multiply((double)this_.a, (double)Math.pow(this_.b, RT.doubleCast((Object)((Number)object))));
    }
}

