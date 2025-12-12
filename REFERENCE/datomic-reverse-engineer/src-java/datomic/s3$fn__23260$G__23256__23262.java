/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.s3.Name;

public final class s3$fn__23260$G__23256__23262
extends AFunction {
    public Object invoke(Object gf__x__23261) {
        Object object = gf__x__23261;
        gf__x__23261 = null;
        return ((Name)object).s3_name();
    }
}

