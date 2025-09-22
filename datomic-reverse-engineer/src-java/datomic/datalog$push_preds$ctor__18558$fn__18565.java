/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import datomic.datalog$push_preds$ctor__18558$fn__18565$fn__18566;

public final class datalog$push_preds$ctor__18558$fn__18565
extends AFunction {
    Object needs_source;
    Object f;
    Object consts;
    int arity;
    Object src;
    Object join_map;

    public datalog$push_preds$ctor__18558$fn__18565(Object object, Object object2, Object object3, int n, Object object4, Object object5) {
        this.needs_source = object;
        this.f = object2;
        this.consts = object3;
        this.arity = n;
        this.src = object4;
        this.join_map = object5;
    }

    public Object invoke() {
        Object[] args;
        Object[] objectArray = args = RT.object_array((Object)this.arity);
        args = null;
        return new datalog$push_preds$ctor__18558$fn__18565$fn__18566(this.needs_source, this.f, objectArray, this.consts, this.arity, this.src, this.join_map);
    }
}

