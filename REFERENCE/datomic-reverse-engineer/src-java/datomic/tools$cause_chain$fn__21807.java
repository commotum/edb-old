/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;

public final class tools$cause_chain$fn__21807
extends AFunction {
    public Object invoke(Object p1__21806_SHARP_) {
        Object object = p1__21806_SHARP_;
        p1__21806_SHARP_ = null;
        return ((Throwable)object).getCause();
    }
}

