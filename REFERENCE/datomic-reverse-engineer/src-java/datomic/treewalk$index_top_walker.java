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
import datomic.treewalk$index_top_walker$reify__19744;

public final class treewalk$index_top_walker
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 118, RT.keyword(null, (String)"column"), 3});

    public static Object invokeStatic(Object top) {
        Object object = top;
        top = null;
        return ((IObj)new treewalk$index_top_walker$reify__19744(null, object)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return treewalk$index_top_walker.invokeStatic(object2);
    }
}

