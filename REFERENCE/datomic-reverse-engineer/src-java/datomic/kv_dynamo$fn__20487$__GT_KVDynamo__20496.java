/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_dynamo.KVDynamo;

public final class kv_dynamo$fn__20487$__GT_KVDynamo__20496
extends AFunction {
    public Object invoke(Object client2, Object table, Object prefix) {
        Object object = client2;
        client2 = null;
        Object object2 = table;
        table = null;
        Object object3 = prefix;
        prefix = null;
        return new KVDynamo(object, object2, object3);
    }
}

