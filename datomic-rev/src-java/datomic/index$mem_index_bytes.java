/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.memory_size.MemorySize;

public final class index$mem_index_bytes
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__2;
    public static final Var const__3;
    public static final Object const__4;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object index) {
        block3: {
            block2: {
                v0 = index;
                if (v0 == null || v0 == Boolean.FALSE) break block2;
                v1 = index;
                index = null;
                v2 = v1;
                if (Util.classOf((Object)v1) == index$mem_index_bytes.__cached_class__0) ** GOTO lbl10
                if (!(v2 instanceof MemorySize)) {
                    v2 = v2;
                    index$mem_index_bytes.__cached_class__0 = Util.classOf((Object)v2);
lbl10:
                    // 2 sources

                    v3 = index$mem_index_bytes.const__2.getRawRoot().invoke(v2);
                } else {
                    v3 = ((MemorySize)v2).memory_size();
                }
                v4 = Float.valueOf(RT.uncheckedFloatCast((Object)Numbers.unchecked_multiply((Object)v3, (Object)index$mem_index_bytes.const__3.getRawRoot())));
                break block3;
            }
            v4 = index$mem_index_bytes.const__4;
        }
        return v4;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index$mem_index_bytes.invokeStatic(object2);
    }

    static {
        const__2 = RT.var((String)"datomic.memory-size", (String)"memory-size");
        const__3 = RT.var((String)"datomic.index", (String)"MEM_TO_STG_RATIO");
        const__4 = 0.0;
    }
}

