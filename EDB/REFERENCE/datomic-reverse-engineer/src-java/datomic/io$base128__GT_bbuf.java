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

public final class io$base128__GT_bbuf
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.io", (String)"decode-base128");

    public static Object invokeStatic(Object s) {
        Object bytes;
        Object object = s;
        s = null;
        Object object2 = bytes = ((IFn)const__0.getRawRoot()).invoke((Object)((String)object).getBytes("UTF-8"));
        bytes = null;
        return ByteBuffer.wrap((byte[])object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$base128__GT_bbuf.invokeStatic(object2);
    }
}

