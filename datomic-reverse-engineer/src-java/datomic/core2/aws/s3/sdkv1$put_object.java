/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.amazonaws.services.s3.AmazonS3Client
 *  com.amazonaws.services.s3.model.ObjectMetadata
 */
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.InputStream;

public final class sdkv1$put_object
extends AFunction {
    public static Object invokeStatic(Object s32, Object bucket, Object path2, Object stream, Object metadata) {
        Object object = s32;
        s32 = null;
        Object object2 = bucket;
        bucket = null;
        Object object3 = path2;
        path2 = null;
        Object object4 = stream;
        stream = null;
        Object object5 = metadata;
        metadata = null;
        return ((AmazonS3Client)object).putObject((String)object2, (String)object3, (InputStream)object4, (ObjectMetadata)object5);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return sdkv1$put_object.invokeStatic(object6, object7, object8, object9, object10);
    }
}

