/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.db;

import clojure.lang.AFunction;
import datomic.Database;

public final class Db$fn__13468
extends AFunction {
    Object pred;

    public Db$fn__13468(Object object) {
        this.pred = object;
    }

    public Object invoke(Object db2, Object d) {
        Object object = db2;
        db2 = null;
        Object object2 = d;
        d = null;
        return ((Database.Predicate)this.pred).apply((Database)object, object2) ? Boolean.TRUE : Boolean.FALSE;
    }
}

