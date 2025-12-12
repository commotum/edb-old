/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.s3_kv.S3Storage;

public final class s3_kv$fn__23327$__GT_S3Storage__23329
extends AFunction {
    public Object invoke(Object s32, Object bucket, Object base) {
        Object object = s32;
        s32 = null;
        Object object2 = bucket;
        bucket = null;
        Object object3 = base;
        base = null;
        return new S3Storage(object, object2, object3);
    }
}

