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
import datomic.artemis_client$wrap_as_failure_listener$reify__20801;

public final class artemis_client$wrap_as_failure_listener
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 57, RT.keyword(null, (String)"column"), 3});

    public static Object invokeStatic(Object f) {
        Object object = f;
        f = null;
        return ((IObj)new artemis_client$wrap_as_failure_listener$reify__20801(null, object)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return artemis_client$wrap_as_failure_listener.invokeStatic(object2);
    }
}

