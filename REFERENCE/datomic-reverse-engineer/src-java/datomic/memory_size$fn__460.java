/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.BigInt
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.BigInt;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.memory_size.MemorySize;
import java.math.BigInteger;

public final class memory_size$fn__460
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__2;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object bi) {
        Object object;
        Object object2 = bi;
        bi = null;
        BigInteger bigInteger = ((BigInt)object2).bipart;
        if (Util.classOf((Object)bigInteger) != __cached_class__0) {
            if (bigInteger instanceof MemorySize) {
                object = ((MemorySize)((Object)bigInteger)).memory_size();
                return Numbers.add((long)16L, (Object)object);
            }
            bigInteger = bigInteger;
            __cached_class__0 = Util.classOf((Object)bigInteger);
        }
        object = const__2.getRawRoot().invoke((Object)bigInteger);
        return Numbers.add((long)16L, (Object)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory_size$fn__460.invokeStatic(object2);
    }

    static {
        const__2 = RT.var((String)"datomic.memory-size", (String)"memory-size");
    }
}

