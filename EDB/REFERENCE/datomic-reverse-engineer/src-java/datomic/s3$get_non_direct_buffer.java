/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.nio.ByteBuffer;

public final class s3$get_non_direct_buffer
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.s3", (String)"get-bytes");

    public static Object invokeStatic(Object s32, Object bucket, Object path2) {
        ByteBuffer byteBuffer;
        Object temp__5457__auto__23302;
        Object object = s32;
        s32 = null;
        Object object2 = bucket;
        bucket = null;
        Object object3 = path2;
        path2 = null;
        Object object4 = temp__5457__auto__23302 = ((IFn)const__0.getRawRoot()).invoke(object, object2, object3);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object b;
            Object object5 = temp__5457__auto__23302;
            temp__5457__auto__23302 = null;
            Object object6 = b = object5;
            b = null;
            byteBuffer = ByteBuffer.wrap((byte[])object6);
        } else {
            byteBuffer = null;
        }
        return byteBuffer;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return s3$get_non_direct_buffer.invokeStatic(object4, object5, object6);
    }
}

