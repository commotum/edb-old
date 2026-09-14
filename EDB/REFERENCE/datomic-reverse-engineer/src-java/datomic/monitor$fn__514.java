/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class monitor$fn__514
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"AvailableMB");
    public static final Var const__1 = RT.var((String)"datomic.math", (String)"rounded-mb");

    public static Object invokeStatic(Object runtime) {
        Runtime rt = Runtime.getRuntime();
        long max2 = rt.maxMemory();
        long total2 = rt.totalMemory();
        Runtime runtime2 = rt;
        rt = null;
        long free = runtime2.freeMemory();
        return RT.mapUniqueKeys((Object[])new Object[]{const__0, ((IFn)const__1.getRawRoot()).invoke((Object)Numbers.num((long)Numbers.add((long)free, (long)Numbers.minus((long)max2, (long)total2))))});
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return monitor$fn__514.invokeStatic(object2);
    }
}

