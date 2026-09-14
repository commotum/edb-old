/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.nio.ByteBuffer;

public final class io$clear_buffer
extends AFunction {
    public static Object invokeStatic(Object bb) {
        Object object = bb;
        bb = null;
        return ((ByteBuffer)object).duplicate().clear().flip();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$clear_buffer.invokeStatic(object2);
    }
}

