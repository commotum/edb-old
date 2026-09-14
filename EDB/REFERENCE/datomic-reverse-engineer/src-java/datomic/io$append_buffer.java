/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OLO
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class io$append_buffer
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.io", (String)"expand-buffer");

    public static Object invokeStatic(Object dest, Object src) {
        Object object = dest;
        dest = null;
        Object result2 = ((IFn.OLO)const__0.getRawRoot()).invokePrim(object, (long)((Buffer)src).remaining());
        Object object2 = src;
        src = null;
        ((ByteBuffer)result2).put(((ByteBuffer)object2).duplicate());
        Object object3 = result2;
        result2 = null;
        return ((ByteBuffer)object3).flip();
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return io$append_buffer.invokeStatic(object3, object4);
    }
}

