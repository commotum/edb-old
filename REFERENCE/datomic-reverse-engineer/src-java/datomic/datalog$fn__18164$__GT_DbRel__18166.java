/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.datalog.DbRel;

public final class datalog$fn__18164$__GT_DbRel__18166
extends AFunction {
    public Object invoke(Object db2, Object isref, Object iskey, Object consts, Object starts, Object whiles) {
        Object object = db2;
        db2 = null;
        Object object2 = isref;
        isref = null;
        Object object3 = iskey;
        iskey = null;
        Object object4 = consts;
        consts = null;
        Object object5 = starts;
        starts = null;
        Object object6 = whiles;
        whiles = null;
        return new DbRel(object, object2, object3, object4, object5, object6);
    }
}

