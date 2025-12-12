/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Database;

public final class db$install_attribute_errors$fn__13088
extends AFunction {
    Object before;

    public db$install_attribute_errors$fn__13088(Object object) {
        this.before = object;
    }

    public Object invoke(Object p1__13079_SHARP_) {
        Object object = p1__13079_SHARP_;
        p1__13079_SHARP_ = null;
        return ((Database)this.before).ident(object);
    }
}

