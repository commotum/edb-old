/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.amazonaws.AmazonServiceException
 *  com.amazonaws.services.s3.AmazonS3Client
 *  com.amazonaws.services.s3.model.AmazonS3Exception
 *  com.amazonaws.services.s3.model.S3Object
 */
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.S3Object;

public final class sdkv1$get_object
extends AFunction {
    public static Object invokeStatic(Object s32, Object bucket, Object path2) {
        S3Object s3Object;
        try {
            Object object = s32;
            s32 = null;
            Object object2 = bucket;
            bucket = null;
            Object object3 = path2;
            path2 = null;
            s3Object = ((AmazonS3Client)object).getObject((String)object2, (String)object3);
        }
        catch (AmazonS3Exception se2) {
            if (404L != (long)((AmazonServiceException)((Object)se2)).getStatusCode()) {
                Object se2 = null;
                throw (Throwable)se2;
            }
            s3Object = null;
        }
        return s3Object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return sdkv1$get_object.invokeStatic(object4, object5, object6);
    }
}

