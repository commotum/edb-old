/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.Database;

public final class db$install_attribute_errors$fn__13082
extends AFunction {
    Object after;

    public db$install_attribute_errors$fn__13082(Object object) {
        this.after = object;
    }

    public Object invoke(Object p1__13078_SHARP_) {
        Object object = p1__13078_SHARP_;
        p1__13078_SHARP_ = null;
        return ((Database)this.after).ident(object);
    }
}

