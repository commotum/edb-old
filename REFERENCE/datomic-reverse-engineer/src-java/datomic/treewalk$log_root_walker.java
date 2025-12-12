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
import datomic.treewalk$log_root_walker$reify__19749;

public final class treewalk$log_root_walker
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 141, RT.keyword(null, (String)"column"), 3});

    public static Object invokeStatic(Object root, Object lookup, Object allow_missing_QMARK_) {
        Object object = lookup;
        lookup = null;
        Object object2 = allow_missing_QMARK_;
        allow_missing_QMARK_ = null;
        Object object3 = root;
        root = null;
        return ((IObj)new treewalk$log_root_walker$reify__19749(null, object, object2, object3)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return treewalk$log_root_walker.invokeStatic(object4, object5, object6);
    }
}

