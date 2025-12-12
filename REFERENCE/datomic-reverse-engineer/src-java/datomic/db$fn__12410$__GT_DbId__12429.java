/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.DbId;

public final class db$fn__12410$__GT_DbId__12429
extends AFunction {
    public Object invoke(Object part2, Object idx) {
        Object object = part2;
        part2 = null;
        Object object2 = idx;
        idx = null;
        return new DbId(object, object2);
    }
}

