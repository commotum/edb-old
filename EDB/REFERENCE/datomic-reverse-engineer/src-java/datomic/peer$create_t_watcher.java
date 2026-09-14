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
import datomic.peer$create_t_watcher$reify__21478;
import datomic.peer.TWatcherImpl;
import java.util.Comparator;
import java.util.PriorityQueue;

public final class peer$create_t_watcher
extends AFunction {
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 146, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic(Object f, Object db_ref) {
        Object object = db_ref;
        db_ref = null;
        Object object2 = f;
        f = null;
        return new TWatcherImpl(new PriorityQueue(RT.intCast((long)11L), (Comparator)((IObj)new peer$create_t_watcher$reify__21478(null)).withMeta((IPersistentMap)const__5)), object, object2, new Object());
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return peer$create_t_watcher.invokeStatic(object3, object4);
    }
}

