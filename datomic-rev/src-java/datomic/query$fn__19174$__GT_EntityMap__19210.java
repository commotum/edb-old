/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.query.EntityMap;

public final class query$fn__19174$__GT_EntityMap__19210
extends AFunction {
    public Object invoke(Object db2, Object eid, Object cache2, Object edits) {
        Object object = db2;
        db2 = null;
        Object object2 = eid;
        eid = null;
        Object object3 = cache2;
        cache2 = null;
        Object object4 = edits;
        edits = null;
        return new EntityMap(object, object2, object3, object4);
    }
}

