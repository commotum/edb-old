/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.db$reverse_comparator$reify__12845;

public final class db$reverse_comparator
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 1001, RT.keyword(null, (String)"column"), 3});

    public static Object invokeStatic(Object cmp) {
        Object object = cmp;
        cmp = null;
        return ((IObj)new db$reverse_comparator$reify__12845(null, object)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$reverse_comparator.invokeStatic(object2);
    }
}

