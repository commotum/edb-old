/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.sql.SQLException;

public final class kv_sql$constraint_violation_QMARK_
extends AFunction {
    public static Object invokeStatic(Object e) {
        Object object = e;
        e = null;
        return ((SQLException)object).getSQLState().startsWith("23") ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_sql$constraint_violation_QMARK_.invokeStatic(object2);
    }
}

