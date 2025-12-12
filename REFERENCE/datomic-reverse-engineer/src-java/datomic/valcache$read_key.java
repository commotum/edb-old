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

public final class valcache$read_key
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.io", (String)"read-into-buffer");

    public static Object invokeStatic(Object key_length, Object sc) {
        ByteBuffer kb = ByteBuffer.allocate(RT.intCast((Object)((Number)key_length)));
        Object object = sc;
        sc = null;
        ((IFn)const__0.getRawRoot()).invoke((Object)kb, key_length, object);
        Object object2 = key_length;
        key_length = null;
        byte[] kbytes = Numbers.byte_array((Object)object2);
        ByteBuffer byteBuffer = kb;
        kb = null;
        byteBuffer.get(kbytes);
        byte[] byArray = kbytes;
        kbytes = null;
        return new String(byArray, "UTF-8");
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return valcache$read_key.invokeStatic(object3, object4);
    }
}

