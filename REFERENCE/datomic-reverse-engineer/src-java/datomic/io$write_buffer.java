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
import java.nio.channels.WritableByteChannel;

public final class io$write_buffer
extends AFunction {
    public static Object invokeStatic(Object bb, Object wc) {
        boolean more = ((Buffer)bb).hasRemaining();
        while (more) {
            Integer.valueOf(((WritableByteChannel)wc).write((ByteBuffer)bb));
            more = ((Buffer)bb).hasRemaining();
        }
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return io$write_buffer.invokeStatic(object3, object4);
    }
}

