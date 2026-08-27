/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.IDbImpl;
import datomic.db.Partition;

public final class db$explicit_partition
extends AFunction {
    public static Object invokeStatic(Object db2, Object eid) {
        Object object;
        Object temp__5457__auto__12680;
        Object object2 = db2;
        db2 = null;
        Object object3 = eid;
        eid = null;
        Object object4 = temp__5457__auto__12680 = ((IDbImpl)object2).elementAt(object3);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5457__auto__12680;
            temp__5457__auto__12680 = null;
            Object part2 = object5;
            if (part2 instanceof Partition) {
                object = part2;
                part2 = null;
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$explicit_partition.invokeStatic(object3, object4);
    }
}

