/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class io$fill_buffer
extends AFunction {
    public static Object invokeStatic(Object is, Object bb) {
        boolean more = ((Buffer)bb).hasRemaining();
        while (more) {
            int n = ((InputStream)is).read();
            if (-1L == (long)n) {
                throw (Throwable)new Exception("Premature EOS");
            }
            ((ByteBuffer)bb).put(RT.uncheckedByteCast((int)n));
            more = ((Buffer)bb).hasRemaining();
        }
        Object object = bb;
        bb = null;
        return ((ByteBuffer)object).flip();
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return io$fill_buffer.invokeStatic(object3, object4);
    }
}

