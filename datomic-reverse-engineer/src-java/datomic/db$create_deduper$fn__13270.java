/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.HashSet;

public final class db$create_deduper$fn__13270
extends AFunction {
    Object dset;

    public db$create_deduper$fn__13270(Object object) {
        this.dset = object;
    }

    public Object invoke(Object d) {
        Object object = d;
        d = null;
        return ((HashSet)this.dset).add(object) ? Boolean.TRUE : Boolean.FALSE;
    }
}

