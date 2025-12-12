/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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
import java.nio.ByteBuffer;

public final class valcache$reply_with_error
extends AFunction {
    public static final Var const__3 = RT.var((String)"datomic.io", (String)"write-buffer");
    public static final Object const__6 = 0L;

    public static Object invokeStatic(Object opcode, Object status, Object msg, Object sc) {
        ByteBuffer bb;
        Object object = msg;
        msg = null;
        byte[] msg_bytes = ((String)object).getBytes("UTF-8");
        int mlen = msg_bytes.length;
        ByteBuffer byteBuffer = bb = ByteBuffer.allocate(RT.intCast((long)Numbers.add((long)24L, (long)mlen)));
        bb = null;
        Object object2 = opcode;
        opcode = null;
        Object object3 = status;
        status = null;
        byte[] byArray = msg_bytes;
        msg_bytes = null;
        Object object4 = sc;
        sc = null;
        return ((IFn)const__3.getRawRoot()).invoke((Object)byteBuffer.put(RT.byteCast((long)-127L)).put(RT.byteCast((Object)object2)).putShort(RT.shortCast((Object)((Number)const__6))).put(RT.byteCast((long)0L)).put(RT.byteCast((long)0L)).putShort(RT.shortCast((Object)((Number)object3))).putInt(mlen).putInt(RT.intCast((long)0L)).putLong(0L).put(byArray).flip(), object4);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return valcache$reply_with_error.invokeStatic(object5, object6, object7, object8);
    }
}

