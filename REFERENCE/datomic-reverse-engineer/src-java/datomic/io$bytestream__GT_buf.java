/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  org.fressian.impl.BytesOutputStream
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import java.nio.ByteBuffer;
import org.fressian.impl.BytesOutputStream;

public final class io$bytestream__GT_buf
extends AFunction {
    public static Object invokeStatic(Object stream) {
        Object object = stream;
        stream = null;
        return ByteBuffer.wrap(((BytesOutputStream)stream).internalBuffer(), RT.intCast((long)0L), ((BytesOutputStream)object).length());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$bytestream__GT_buf.invokeStatic(object2);
    }
}

