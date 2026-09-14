/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Database;

public final class api$index_range
extends AFunction {
    public static Object invokeStatic(Object db2, Object attrid, Object start, Object end) {
        Object object = db2;
        db2 = null;
        Object object2 = attrid;
        attrid = null;
        Object object3 = start;
        start = null;
        Object object4 = end;
        end = null;
        return ((Database)object).indexRange(object2, object3, object4);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return api$index_range.invokeStatic(object5, object6, object7, object8);
    }
}

