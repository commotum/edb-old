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
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.thread$daemon_factory$reify__21010;

public final class thread$daemon_factory
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.thread", (String)"daemon-factory");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"atom");
    public static final Object const__2 = 0L;
    public static final AFn const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 47, RT.keyword(null, (String)"column"), 6});

    public static Object invokeStatic(Object name_prefix, Object group) {
        Object idx = ((IFn)const__1.getRawRoot()).invoke(const__2);
        Object object = name_prefix;
        name_prefix = null;
        Object object2 = idx;
        idx = null;
        Object object3 = group;
        group = null;
        return ((IObj)new thread$daemon_factory$reify__21010(null, object, object2, object3)).withMeta((IPersistentMap)const__7);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return thread$daemon_factory.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object name_prefix) {
        Object object = name_prefix;
        name_prefix = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, null);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return thread$daemon_factory.invokeStatic(object2);
    }
}

