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
package datomic.core2.algo;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.core2.algo.search$fn_comparator$reify__19433;
import datomic.core2.algo.search$fn_comparator$reify__19435;

public final class search$fn_comparator
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 10, RT.keyword(null, (String)"column"), 4});
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 14, RT.keyword(null, (String)"column"), 4});

    public static Object invokeStatic(Object f, Object comp2) {
        Object object = f;
        f = null;
        Object object2 = comp2;
        comp2 = null;
        return ((IObj)new search$fn_comparator$reify__19435(null, object, object2)).withMeta((IPersistentMap)const__6);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return search$fn_comparator.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object f) {
        Object object = f;
        f = null;
        return ((IObj)new search$fn_comparator$reify__19433(null, object)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return search$fn_comparator.invokeStatic(object2);
    }
}

