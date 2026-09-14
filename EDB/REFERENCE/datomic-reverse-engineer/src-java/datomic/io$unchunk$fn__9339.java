/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.nio.Buffer;

public final class io$unchunk$fn__9339
extends AFunction {
    public Object invoke(Object p1__9338_SHARP_) {
        Object object = p1__9338_SHARP_;
        p1__9338_SHARP_ = null;
        return ((Buffer)object).remaining();
    }
}

