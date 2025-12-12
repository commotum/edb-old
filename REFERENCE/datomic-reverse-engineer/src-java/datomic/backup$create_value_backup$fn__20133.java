/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;

public final class backup$create_value_backup$fn__20133
extends AFunction {
    Object pace;

    public backup$create_value_backup$fn__20133(Object object) {
        this.pace = object;
    }

    public Object invoke() {
        backup$create_value_backup$fn__20133 this_ = null;
        Thread.sleep(RT.longCast((Object)((Number)this_.pace)));
        return null;
    }
}

