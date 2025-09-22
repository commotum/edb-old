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
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class simple_kv$pack
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"pr-str");

    public static Object invokeStatic(Object m, Object v) {
        byte[] mbytes;
        Object object = m;
        m = null;
        byte[] byArray = mbytes = ((String)((IFn)const__0.getRawRoot()).invoke(object)).getBytes("UTF-8");
        mbytes = null;
        Object object2 = v;
        v = null;
        return ByteBuffer.allocate(RT.intCast((long)Numbers.add((long)Numbers.add((long)12L, (long)RT.count((Object)mbytes)), (long)((Buffer)v).remaining()))).putLong(568780356367818079L).putInt(RT.count((Object)mbytes)).put(byArray).put((ByteBuffer)object2).flip();
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return simple_kv$pack.invokeStatic(object3, object4);
    }
}

