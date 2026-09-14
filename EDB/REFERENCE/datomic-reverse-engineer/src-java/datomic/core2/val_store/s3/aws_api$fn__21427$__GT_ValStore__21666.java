/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.core2.val_store.s3;

import clojure.lang.AFunction;
import datomic.core2.val_store.s3.aws_api.ValStore;

public final class aws_api$fn__21427$__GT_ValStore__21666
extends AFunction {
    public Object invoke(Object client2, Object bucket, Object prefix) {
        Object object = client2;
        client2 = null;
        Object object2 = bucket;
        bucket = null;
        Object object3 = prefix;
        prefix = null;
        return new ValStore(object, object2, object3);
    }
}

