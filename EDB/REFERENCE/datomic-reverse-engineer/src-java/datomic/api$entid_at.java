/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Database;

public final class api$entid_at
extends AFunction {
    public static Object invokeStatic(Object db2, Object part2, Object t_or_date) {
        Object object = db2;
        db2 = null;
        Object object2 = part2;
        part2 = null;
        Object object3 = t_or_date;
        t_or_date = null;
        return ((Database)object).entidAt(object2, object3);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return api$entid_at.invokeStatic(object4, object5, object6);
    }
}

