/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.kv_dynamo.KVDynamo;

public final class kv_dynamo$kv_dynamo
extends AFunction {
    public static Object invokeStatic(Object client2, Object table, Object prefix) {
        Object object = client2;
        client2 = null;
        Object object2 = table;
        table = null;
        Object object3 = prefix;
        prefix = null;
        return new KVDynamo(object, object2, object3);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return kv_dynamo$kv_dynamo.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object client2, Object table) {
        Object object = client2;
        client2 = null;
        Object object2 = table;
        table = null;
        return new KVDynamo(object, object2, null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return kv_dynamo$kv_dynamo.invokeStatic(object3, object4);
    }
}

