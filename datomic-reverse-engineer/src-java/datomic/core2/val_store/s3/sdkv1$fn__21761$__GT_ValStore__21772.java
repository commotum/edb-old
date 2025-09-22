/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.val_store.s3;

import clojure.lang.AFunction;
import datomic.core2.val_store.s3.sdkv1.ValStore;

public final class sdkv1$fn__21761$__GT_ValStore__21772
extends AFunction {
    public Object invoke(Object read_pool, Object write_pool, Object retry_fn2, Object client2, Object bucket, Object prefix) {
        Object object = read_pool;
        read_pool = null;
        Object object2 = write_pool;
        write_pool = null;
        Object object3 = retry_fn2;
        retry_fn2 = null;
        Object object4 = client2;
        client2 = null;
        Object object5 = bucket;
        bucket = null;
        Object object6 = prefix;
        prefix = null;
        return new ValStore(object, object2, object3, object4, object5, object6);
    }
}

