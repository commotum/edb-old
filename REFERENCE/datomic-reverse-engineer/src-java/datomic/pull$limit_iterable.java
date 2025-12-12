/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.pull$limit_iterable$reify__18898;

public final class pull$limit_iterable
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 30, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic(Object limit2, Object iterable2) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(limit2);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = iterable2;
            iterable2 = null;
        } else {
            Object object3 = iterable2;
            iterable2 = null;
            Object object4 = limit2;
            limit2 = null;
            object = ((IObj)new pull$limit_iterable$reify__18898(null, object3, object4)).withMeta((IPersistentMap)const__5);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return pull$limit_iterable.invokeStatic(object3, object4);
    }
}

