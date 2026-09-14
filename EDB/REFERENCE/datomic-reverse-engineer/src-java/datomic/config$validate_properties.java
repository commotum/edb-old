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

public final class config$validate_properties
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__3 = RT.keyword((String)"db.error", (String)"invalid-memory-config");
    public static final Keyword const__8 = RT.keyword((String)"db.error", (String)"not-enough-memory");

    public static Object invokeStatic(Object m) {
        if (Numbers.lt((Object)RT.get((Object)m, (Object)"datomic.memoryIndexMax"), (Object)RT.get((Object)m, (Object)"datomic.memoryIndexThreshold"))) {
            ((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)"datomic.memoryIndexMax must be >= datomic.memoryIndexThreshold", m);
        }
        if (Numbers.gt((Object)Numbers.add((Object)RT.get((Object)m, (Object)"datomic.memoryIndexMax"), (Object)RT.get((Object)m, (Object)"datomic.objectCacheMax")), (double)Numbers.multiply((double)0.75, (long)Runtime.getRuntime().maxMemory()))) {
            ((IFn)const__2.getRawRoot()).invoke((Object)const__8, (Object)"(datomic.objectCacheMax + datomic.memoryIndexMax) exceeds 75% of JVM RAM", m);
        }
        Object object = null;
        return m;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$validate_properties.invokeStatic(object2);
    }
}

