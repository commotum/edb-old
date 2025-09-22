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

public final class io$write_bytes
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.io", (String)"write-buffer");

    public static Object invokeStatic(Object bytes, Object wc) {
        Object object = bytes;
        bytes = null;
        Object object2 = wc;
        wc = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)ByteBuffer.wrap((byte[])object), object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return io$write_bytes.invokeStatic(object3, object4);
    }
}

