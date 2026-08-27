/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.index$sparse_e_xf$fn__15398$fn__15399;

public final class index$sparse_e_xf$fn__15398
extends AFunction {
    Object vprev;
    Object olookup;
    Object idx;

    public index$sparse_e_xf$fn__15398(Object object, Object object2, Object object3) {
        this.vprev = object;
        this.olookup = object2;
        this.idx = object3;
    }

    public Object invoke(Object rf) {
        Object object = rf;
        rf = null;
        return new index$sparse_e_xf$fn__15398$fn__15399(this.vprev, this.olookup, this.idx, object);
    }
}

