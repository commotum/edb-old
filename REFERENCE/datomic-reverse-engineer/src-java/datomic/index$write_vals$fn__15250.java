/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.nio.Buffer;

public final class index$write_vals$fn__15250
extends AFunction {
    public Object invoke(Object p1__15249_SHARP_) {
        Object object = p1__15249_SHARP_;
        p1__15249_SHARP_ = null;
        return ((Buffer)object).remaining();
    }
}

