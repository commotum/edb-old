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
import java.nio.ByteBuffer;

public final class io$allocate_buffer
extends AFunction {
    public static Object invokeStatic(Object size) {
        Object object = size;
        size = null;
        return ByteBuffer.allocate(RT.intCast((Object)((Number)object))).flip();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$allocate_buffer.invokeStatic(object2);
    }
}

