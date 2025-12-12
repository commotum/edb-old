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

public final class io$read_n_direct_bytes
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.io", (String)"read-into-buffer");

    public static Object invokeStatic(Object n, Object rc) {
        ByteBuffer bb;
        ByteBuffer byteBuffer = bb = ByteBuffer.allocateDirect(RT.intCast((Object)((Number)n)));
        bb = null;
        Object object = n;
        n = null;
        Object object2 = rc;
        rc = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)byteBuffer, object, object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return io$read_n_direct_bytes.invokeStatic(object3, object4);
    }
}

