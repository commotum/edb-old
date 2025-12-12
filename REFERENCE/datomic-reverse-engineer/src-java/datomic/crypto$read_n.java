/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import java.nio.ByteBuffer;

public final class crypto$read_n
extends AFunction {
    public static Object invokeStatic(Object bb, Object n) {
        Object object = n;
        n = null;
        byte[] dest = Numbers.byte_array((Object)object);
        Object object2 = bb;
        bb = null;
        ((ByteBuffer)object2).get(dest);
        Object var2_2 = null;
        return dest;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return crypto$read_n.invokeStatic(object3, object4);
    }
}

