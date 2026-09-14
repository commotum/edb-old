/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.atom;

import clojure.lang.AFunction;
import datomic.core2.atom.spi.DurableAtom;

public final class spi$fn__20399$G__20393__20403
extends AFunction {
    public Object invoke(Object gf_____20400, Object gf__f__20401, Object gf__ch__20402) {
        Object object = gf_____20400;
        gf_____20400 = null;
        Object object2 = gf__f__20401;
        gf__f__20401 = null;
        Object object3 = gf__ch__20402;
        gf__ch__20402 = null;
        return ((DurableAtom)object)._swap_vals_BANG_(object2, object3);
    }
}

