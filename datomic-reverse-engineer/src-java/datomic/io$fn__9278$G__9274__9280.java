/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.io.ByteSource;

public final class io$fn__9278$G__9274__9280
extends AFunction {
    public Object invoke(Object gf_____9279) {
        Object object = gf_____9279;
        gf_____9279 = null;
        return ((ByteSource)object).slurp_bytes();
    }
}

