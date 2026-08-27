/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import datomic.db.IndexSet;

public final class db$accept_index$fn__13644$fn__13650
extends AFunction {
    Object iset;
    Object adopt;
    Object new_memidx;

    public db$accept_index$fn__13644$fn__13650(Object object, Object object2, Object object3) {
        this.iset = object;
        this.adopt = object2;
        this.new_memidx = object3;
    }

    public Object invoke() {
        this_.adopt = null;
        this_.new_memidx = null;
        this_.iset = null;
        db$accept_index$fn__13644$fn__13650 this_ = null;
        return ((IFn)this_.adopt).invoke(((IndexSet)this_.new_memidx).avet, ((IndexSet)this_.iset).avet);
    }
}

