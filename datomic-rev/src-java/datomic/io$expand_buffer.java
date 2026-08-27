/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class io$expand_buffer
extends AFunction
implements IFn.OLO {
    public static final Var const__2 = RT.var((String)"datomic.io", (String)"unflipped");

    public static Object invokeStatic(Object buf, long extra) {
        Object object;
        long available = Numbers.minus((long)((Buffer)buf).capacity(), (long)((Buffer)buf).limit());
        if (extra < available) {
            Object object2 = buf;
            buf = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object2);
        } else {
            ByteBuffer new_buf;
            long new_length = Numbers.max((long)Numbers.multiply((long)2L, (long)((Buffer)buf).capacity()), (long)Numbers.add((long)extra, (long)((Buffer)buf).capacity()));
            ByteBuffer byteBuffer = new_buf = ((ByteBuffer)buf).isDirect() ? ByteBuffer.allocateDirect(RT.intCast((long)new_length)) : ByteBuffer.allocate(RT.intCast((long)new_length));
            new_buf = null;
            Object object3 = buf;
            buf = null;
            object = byteBuffer.put(((ByteBuffer)object3).duplicate());
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        return io$expand_buffer.invokeStatic(object3, RT.longCast((Object)((Number)object2)));
    }

    public final Object invokePrim(Object object, long l) {
        Object object2 = object;
        object = null;
        return io$expand_buffer.invokeStatic(object2, l);
    }
}

