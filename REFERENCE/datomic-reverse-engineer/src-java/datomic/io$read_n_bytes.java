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

public final class io$read_n_bytes
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.io", (String)"read-into-buffer");

    public static Object invokeStatic(Object n, Object rc) {
        ByteBuffer bb;
        ByteBuffer byteBuffer = bb = ByteBuffer.wrap(Numbers.byte_array((Object)n));
        bb = null;
        Object object = n;
        n = null;
        Object object2 = rc;
        rc = null;
        return ((IFn)const__1.getRawRoot()).invoke((Object)byteBuffer, object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return io$read_n_bytes.invokeStatic(object3, object4);
    }
}

