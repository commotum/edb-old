/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.integrity$validate_log_cli$progress__22381$fn__22382;

public final class integrity$validate_log_cli$progress__22381
extends AFunction {
    Object count;

    public integrity$validate_log_cli$progress__22381(Object object) {
        this.count = object;
    }

    public Object invoke(Object p1__22380_SHARP_) {
        Object object = p1__22380_SHARP_;
        p1__22380_SHARP_ = null;
        return new integrity$validate_log_cli$progress__22381$fn__22382(this.count, object);
    }
}

