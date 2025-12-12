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
package datomic.garbage;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.garbage.fressian$read_handler$reify__16636;

public final class fressian$read_handler
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 26, RT.keyword(null, (String)"column"), 3});

    public static Object invokeStatic(Object factory) {
        Object object = factory;
        factory = null;
        return ((IObj)new fressian$read_handler$reify__16636(null, object)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fressian$read_handler.invokeStatic(object2);
    }
}

