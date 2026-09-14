/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.memory_size.MemorySize;

public final class memory_size$fn__470
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__2;
    public static final Var const__3;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object s) {
        Object object;
        Object object2 = s;
        s = null;
        Object object3 = ((IFn)const__3.getRawRoot()).invoke(object2);
        if (Util.classOf((Object)object3) != __cached_class__0) {
            if (object3 instanceof MemorySize) {
                object = ((MemorySize)object3).memory_size();
                return Numbers.add((long)48L, (Object)object);
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        object = const__2.getRawRoot().invoke(object3);
        return Numbers.add((long)48L, (Object)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory_size$fn__470.invokeStatic(object2);
    }

    static {
        const__2 = RT.var((String)"datomic.memory-size", (String)"memory-size");
        const__3 = RT.var((String)"clojure.core", (String)"meta");
    }
}

