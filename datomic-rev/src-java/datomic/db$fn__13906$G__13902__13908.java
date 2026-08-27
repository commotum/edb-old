/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.LocalDb;

public final class db$fn__13906$G__13902__13908
extends AFunction {
    public Object invoke(Object gf__db__13907) {
        Object object = gf__db__13907;
        gf__db__13907 = null;
        return ((LocalDb)object).local_db();
    }
}

