/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  org.fressian.Writer
 */
package datomic;

import clojure.lang.AFunction;
import datomic.impl.db.IDatum;
import org.fressian.Writer;

public final class index$build_one_seg$proc__15317
extends AFunction {
    Object w;

    public index$build_one_seg$proc__15317(Object object) {
        this.w = object;
    }

    public Object invoke(Object d) {
        Object object = d;
        d = null;
        return ((Writer)this.w).writeObject(((IDatum)object).getV());
    }
}

