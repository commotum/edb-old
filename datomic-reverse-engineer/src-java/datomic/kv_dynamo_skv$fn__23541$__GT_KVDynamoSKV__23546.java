/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_dynamo_skv.KVDynamoSKV;

public final class kv_dynamo_skv$fn__23541$__GT_KVDynamoSKV__23546
extends AFunction {
    public Object invoke(Object client2, Object table, Object skv, Object prefix) {
        Object object = client2;
        client2 = null;
        Object object2 = table;
        table = null;
        Object object3 = skv;
        skv = null;
        Object object4 = prefix;
        prefix = null;
        return new KVDynamoSKV(object, object2, object3, object4);
    }
}

