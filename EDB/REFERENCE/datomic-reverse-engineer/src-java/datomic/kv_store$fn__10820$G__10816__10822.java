/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_store.Retryable;

public final class kv_store$fn__10820$G__10816__10822
extends AFunction {
    public Object invoke(Object gf_____10821) {
        Object object = gf_____10821;
        gf_____10821 = null;
        return ((Retryable)object).retryable_QMARK_();
    }
}

