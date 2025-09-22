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
import datomic.common$key_comparator$reify__9142;
import datomic.common$key_comparator$reify__9144;

public final class common$key_comparator
extends AFunction {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 446, RT.keyword(null, (String)"column"), 6});
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 450, RT.keyword(null, (String)"column"), 6});

    public static Object invokeStatic(Object key_fn, Object comp2) {
        Object object = comp2;
        comp2 = null;
        Object object2 = key_fn;
        key_fn = null;
        return ((IObj)new common$key_comparator$reify__9144(null, object, object2)).withMeta((IPersistentMap)const__6);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$key_comparator.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object key_fn) {
        Object object = key_fn;
        key_fn = null;
        return ((IObj)new common$key_comparator$reify__9142(null, object)).withMeta((IPersistentMap)const__4);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$key_comparator.invokeStatic(object2);
    }
}

