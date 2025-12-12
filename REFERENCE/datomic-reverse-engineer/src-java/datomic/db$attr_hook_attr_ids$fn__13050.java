/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Database;

public final class db$attr_hook_attr_ids$fn__13050
extends AFunction {
    Object db;

    public db$attr_hook_attr_ids$fn__13050(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__13049_SHARP_) {
        Object object = p1__13049_SHARP_;
        p1__13049_SHARP_ = null;
        return ((Database)this.db).entid(object);
    }
}

