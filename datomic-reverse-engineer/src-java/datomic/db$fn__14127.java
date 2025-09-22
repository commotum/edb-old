/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;

public final class db$fn__14127
extends AFunction {
    public static final Keyword const__3 = RT.keyword((String)"db", (String)"add");
    public static final Keyword const__4 = RT.keyword((String)"db", (String)"doc");

    public static Object invokeStatic(Object p__14126) {
        Object object = p__14126;
        p__14126 = null;
        Object vec__14128 = object;
        Object k = RT.nth((Object)vec__14128, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__14128;
        vec__14128 = null;
        Object v = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
        Object object3 = k;
        k = null;
        Object object4 = v;
        v = null;
        return Tuple.create((Object)const__3, (Object)object3, (Object)const__4, (Object)object4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$fn__14127.invokeStatic(object2);
    }
}

