/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.async.DiscardPolicy;

public final class async$fn__9437$__GT_DiscardPolicy__9439
extends AFunction {
    public Object invoke(Object f) {
        Object object = f;
        f = null;
        return new DiscardPolicy(object);
    }
}

