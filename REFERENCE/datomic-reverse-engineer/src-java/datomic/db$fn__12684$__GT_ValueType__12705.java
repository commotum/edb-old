/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.ValueType;

public final class db$fn__12684$__GT_ValueType__12705
extends AFunction {
    public Object invoke(Object id, Object kw, Object fressian_tag) {
        Object object = id;
        id = null;
        Object object2 = kw;
        kw = null;
        Object object3 = fressian_tag;
        fressian_tag = null;
        return new ValueType(object, object2, object3);
    }
}

