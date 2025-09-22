/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.nio.ByteBuffer;

public final class valcache$reply_empty_ok
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.io", (String)"write-buffer");
    public static final Object const__4 = 0L;

    public static Object invokeStatic(Object opcode, Object sc) {
        ByteBuffer bb;
        ByteBuffer byteBuffer = bb = ByteBuffer.allocate(RT.intCast((long)24L));
        bb = null;
        Object object = opcode;
        opcode = null;
        Object object2 = sc;
        sc = null;
        return ((IFn)const__1.getRawRoot()).invoke((Object)byteBuffer.put(RT.byteCast((long)-127L)).put(RT.byteCast((Object)object)).putShort(RT.shortCast((Object)((Number)const__4))).put(RT.byteCast((long)0L)).put(RT.byteCast((long)0L)).putShort(RT.shortCast((Object)((Number)const__4))).putInt(RT.intCast((long)0L)).putInt(RT.intCast((long)0L)).putLong(0L).flip(), object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return valcache$reply_empty_ok.invokeStatic(object3, object4);
    }
}

