/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.log;

import clojure.lang.AFunction;
import datomic.core2.log.mem.Log;

public final class mem$fn__20869$__GT_Log__20882
extends AFunction {
    public Object invoke(Object items_ref) {
        Object object = items_ref;
        items_ref = null;
        return new Log(object);
    }
}

