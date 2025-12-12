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

public final class memory_size$fn__424
extends AFunction {
    private static Class __cached_class__0;
    public static final Object const__2;
    public static final Var const__9;
    public static final Var const__12;
    public static final Object const__14;
    public static final Var const__15;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object o) {
        block5: {
            block4: {
                cls = o.getClass();
                if (!cls.isArray()) break block4;
                v0 = o;
                o = null;
                v1 = a__6200__auto__426 = (a = v0);
                a__6200__auto__426 = null;
                l__6201__auto__427 = ((Object[])v1).length;
                idx = 0L;
                ret = 16L;
                while (idx < (long)l__6201__auto__427) {
                    v2 = RT.intCast((long)idx) + 1;
                    v3 = RT.aget((Object[])((Object[])a), (int)RT.intCast((long)idx));
                    if (Util.classOf((Object)v3) == memory_size$fn__424.__cached_class__0) ** GOTO lbl17
                    if (!(v3 instanceof MemorySize)) {
                        v3 = v3;
                        memory_size$fn__424.__cached_class__0 = Util.classOf((Object)v3);
lbl17:
                        // 2 sources

                        v4 = memory_size$fn__424.const__9.getRawRoot().invoke(v3);
                    } else {
                        v4 = ((MemorySize)v3).memory_size();
                    }
                    ret = Numbers.add((long)ret, (long)Numbers.max((long)8L, (long)RT.longCast((Object)v4)));
                    idx = v2;
                }
                v5 = Numbers.num((long)ret);
                break block5;
            }
            v6 = ((IFn)memory_size$fn__424.const__12.getRawRoot()).invoke((Object)(Util.identical(cls, (Object)memory_size$fn__424.const__14) != false ? Boolean.TRUE : Boolean.FALSE));
            if (v6 != null && v6 != Boolean.FALSE) {
                v7 = cls;
                cls = null;
                throw (Throwable)new Error((String)((IFn)memory_size$fn__424.const__15.getRawRoot()).invoke((Object)"No size estimator for ", v7));
            }
            v5 = memory_size$fn__424.const__2;
        }
        return v5;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memory_size$fn__424.invokeStatic(object2);
    }

    static {
        const__2 = 16L;
        const__9 = RT.var((String)"datomic.memory-size", (String)"memory-size");
        const__12 = RT.var((String)"clojure.core", (String)"not");
        const__14 = RT.classForName((String)"java.lang.Object");
        const__15 = RT.var((String)"clojure.core", (String)"str");
    }
}

