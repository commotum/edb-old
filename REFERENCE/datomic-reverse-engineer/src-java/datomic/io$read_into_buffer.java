/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public final class io$read_into_buffer
extends AFunction {
    public static Object invokeStatic(Object bb, Object n, Object rc) {
        Object object = n;
        n = null;
        ((ByteBuffer)bb).clear().limit(RT.intCast((Object)object));
        boolean more = ((Buffer)bb).hasRemaining();
        while (more) {
            int n2 = ((ReadableByteChannel)rc).read((ByteBuffer)bb);
            boolean more2 = ((Buffer)bb).hasRemaining();
            boolean and__5236__auto__9364 = more2;
            if (and__5236__auto__9364 ? Util.equiv((long)-1L, (long)n2) : and__5236__auto__9364) {
                throw (Throwable)new IOException("Premature EOS, presumed disconnect");
            }
            more = more2;
        }
        Object object2 = bb;
        bb = null;
        return ((ByteBuffer)object2).flip();
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return io$read_into_buffer.invokeStatic(object4, object5, object6);
    }
}

