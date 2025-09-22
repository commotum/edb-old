/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.peer;

import clojure.lang.AFunction;
import java.util.concurrent.LinkedBlockingQueue;

public final class LocalConnection$fn__21604
extends AFunction {
    public Object invoke(Object p1__21598_SHARP_) {
        Object object;
        Object object2 = p1__21598_SHARP_;
        if (object2 != null && object2 != Boolean.FALSE) {
            object = p1__21598_SHARP_;
            p1__21598_SHARP_ = null;
        } else {
            object = new LinkedBlockingQueue();
        }
        return object;
    }
}

