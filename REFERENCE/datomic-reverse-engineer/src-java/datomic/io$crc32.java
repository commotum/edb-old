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
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public final class io$crc32
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.io", (String)"alias-buf-bytes");

    public static Object invokeStatic(Object bbuf) {
        CRC32 G__9369 = new CRC32();
        Object object = bbuf;
        bbuf = null;
        ((Checksum)G__9369).update((byte[])((IFn)const__0.getRawRoot()).invoke(object));
        Object var1_1 = null;
        return Numbers.num((long)G__9369.getValue());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$crc32.invokeStatic(object2);
    }
}

