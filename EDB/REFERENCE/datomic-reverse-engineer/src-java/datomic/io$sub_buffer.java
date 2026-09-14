/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;
import java.nio.ByteBuffer;

public final class io$sub_buffer
extends AFunction {
    public static Object invokeStatic(Object bb, Object offset, Object length) {
        Object object = bb;
        bb = null;
        Object object2 = offset;
        offset = null;
        Object object3 = length;
        length = null;
        return ((ByteBuffer)object).duplicate().position(RT.intCast((Object)offset)).limit(RT.intCast((Object)Numbers.add((Object)object2, (Object)object3))).slice();
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return io$sub_buffer.invokeStatic(object4, object5, object6);
    }
}

