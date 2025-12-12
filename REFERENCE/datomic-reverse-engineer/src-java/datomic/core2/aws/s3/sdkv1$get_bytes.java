/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.amazonaws.services.s3.model.S3Object
 *  com.amazonaws.services.s3.model.S3ObjectInputStream
 */
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public final class sdkv1$get_bytes
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"get-object");
    public static final Var const__2 = RT.var((String)"datomic.java.io", (String)"fill-from-stream!");

    public static Object invokeStatic(Object s32, Object bucket, Object path2) {
        Object object;
        Object temp__5804__auto__20539;
        Object object2 = s32;
        s32 = null;
        Object object3 = bucket;
        bucket = null;
        Object object4 = path2;
        path2 = null;
        Object object5 = temp__5804__auto__20539 = ((IFn)const__0.getRawRoot()).invoke(object2, object3, object4);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6;
            Object object7 = temp__5804__auto__20539;
            temp__5804__auto__20539 = null;
            Object obj = object7;
            S3ObjectInputStream is = ((S3Object)obj).getObjectContent();
            try {
                byte[] ba;
                Object object8 = obj;
                obj = null;
                byte[] byArray = ba = Numbers.byte_array((Object)Numbers.num((long)((S3Object)object8).getObjectMetadata().getContentLength()));
                ba = null;
                object6 = ((IFn)const__2.getRawRoot()).invoke((Object)byArray, (Object)is);
            }
            finally {
                S3ObjectInputStream s3ObjectInputStream = is;
                is = null;
                s3ObjectInputStream.close();
            }
            object = object6;
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return sdkv1$get_bytes.invokeStatic(object4, object5, object6);
    }
}

