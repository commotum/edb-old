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

public final class io$unflipped
extends AFunction {
    public static Object invokeStatic(Object b) {
        Object object = b;
        b = null;
        return ((ByteBuffer)b).duplicate().position(((Buffer)b).limit()).limit(((Buffer)object).capacity());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$unflipped.invokeStatic(object2);
    }
}

