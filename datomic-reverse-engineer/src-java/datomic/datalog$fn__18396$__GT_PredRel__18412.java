/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.datalog.PredRel;

public final class datalog$fn__18396$__GT_PredRel__18412
extends AFunction {
    public Object invoke(Object db2, Object f, Object arity, Object src_QMARK_, Object consts) {
        Object object = db2;
        db2 = null;
        Object object2 = f;
        f = null;
        Object object3 = arity;
        arity = null;
        Object object4 = src_QMARK_;
        src_QMARK_ = null;
        Object object5 = consts;
        consts = null;
        return new PredRel(object, object2, object3, object4, object5);
    }
}

