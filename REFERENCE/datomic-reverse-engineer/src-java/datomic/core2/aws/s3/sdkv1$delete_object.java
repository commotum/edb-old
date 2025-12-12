/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.amazonaws.services.s3.AmazonS3Client
 */
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import com.amazonaws.services.s3.AmazonS3Client;

public final class sdkv1$delete_object
extends AFunction {
    public static Object invokeStatic(Object s32, Object bucket, Object path2) {
        Object object = s32;
        s32 = null;
        Object object2 = bucket;
        bucket = null;
        Object object3 = path2;
        path2 = null;
        ((AmazonS3Client)object).deleteObject((String)object2, (String)object3);
        return null;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return sdkv1$delete_object.invokeStatic(object4, object5, object6);
    }
}

