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

public final class io$expand_byte_array
extends AFunction {
    public static Object invokeStatic(Object buf, Object valid_bytes, Object new_length) {
        Object object;
        if (Numbers.lte((Object)new_length, (long)RT.count((Object)buf))) {
            object = buf;
            buf = null;
        } else {
            Object object2 = new_length;
            new_length = null;
            byte[] expanded_buf = Numbers.byte_array((Object)Numbers.max((Object)object2, (long)Numbers.multiply((long)2L, (long)RT.count((Object)buf))));
            Object object3 = buf;
            buf = null;
            Object object4 = valid_bytes;
            valid_bytes = null;
            System.arraycopy(object3, RT.intCast((long)0L), expanded_buf, RT.intCast((long)0L), RT.intCast((Object)((Number)object4)));
            object = expanded_buf;
            Object var3_3 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return io$expand_byte_array.invokeStatic(object4, object5, object6);
    }
}

