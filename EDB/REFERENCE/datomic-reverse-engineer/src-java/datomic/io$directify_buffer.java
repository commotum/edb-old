/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class io$directify_buffer
extends AFunction {
    public static Object invokeStatic(Object buf) {
        Object object;
        if (((ByteBuffer)buf).isDirect()) {
            object = buf;
            buf = null;
        } else {
            ByteBuffer ret = ByteBuffer.allocateDirect(((Buffer)buf).limit());
            Object object2 = buf;
            buf = null;
            ret.put(((ByteBuffer)object2).duplicate());
            ByteBuffer byteBuffer = ret;
            ret = null;
            object = byteBuffer.flip();
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$directify_buffer.invokeStatic(object2);
    }
}

