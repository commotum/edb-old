/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.log;

import clojure.lang.AFunction;
import datomic.core2.log.ddb.Log;

public final class ddb$fn__20642$__GT_Log__20858
extends AFunction {
    public Object invoke(Object client2, Object table, Object p, Object chunk_size) {
        Object object = client2;
        client2 = null;
        Object object2 = table;
        table = null;
        Object object3 = p;
        p = null;
        Object object4 = chunk_size;
        chunk_size = null;
        return new Log(object, object2, object3, object4);
    }
}

