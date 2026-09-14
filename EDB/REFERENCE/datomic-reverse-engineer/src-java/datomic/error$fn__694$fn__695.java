/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class error$fn__694$fn__695
extends AFunction {
    public Object invoke(Object msg) {
        Object object = msg;
        msg = null;
        return new RuntimeException((String)object);
    }
}

