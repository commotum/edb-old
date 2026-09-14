/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import datomic.Database;

public final class db_io$most_current_db$basis__17069
extends AFunction {
    public static final Object const__0 = -1L;

    public Object invoke(Object db2) {
        Object object;
        Object object2 = db2;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = db2;
            db2 = null;
            object = Numbers.num((long)((Database)object3).basisT());
        } else {
            object = const__0;
        }
        return object;
    }
}

