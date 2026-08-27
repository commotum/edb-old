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
import datomic.db.Datum;
import datomic.memory_size.MemorySize;

public final class domain$fn__16775
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__2;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object d) {
        Object object;
        Object object2 = d;
        d = null;
        Object object3 = ((Datum)object2).v();
        if (Util.classOf((Object)object3) != __cached_class__0) {
            if (object3 instanceof MemorySize) {
                object = ((MemorySize)object3).memory_size();
                return Numbers.add((long)40L, (Object)object);
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        object = const__2.getRawRoot().invoke(object3);
        return Numbers.add((long)40L, (Object)object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return domain$fn__16775.invokeStatic(object2);
    }

    static {
        const__2 = RT.var((String)"datomic.memory-size", (String)"memory-size");
    }
}

