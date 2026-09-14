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
import datomic.db$get_prefetch_dispatcher$reify__14046;
import datomic.db$get_prefetch_dispatcher$reify__14050;
import java.util.concurrent.atomic.AtomicBoolean;

public final class db$get_prefetch_dispatcher
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"default-prefetch-executor");
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 3771, RT.keyword(null, (String)"column"), 7});
    public static final AFn const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 3777, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic() {
        IObj iObj;
        Object temp__5455__auto__14053;
        Object object = temp__5455__auto__14053 = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot());
        if (object != null && object != Boolean.FALSE) {
            AtomicBoolean done_flag;
            Object object2 = temp__5455__auto__14053;
            temp__5455__auto__14053 = null;
            Object exec = object2;
            AtomicBoolean atomicBoolean = done_flag = new AtomicBoolean(Boolean.FALSE);
            done_flag = null;
            Object object3 = exec;
            exec = null;
            iObj = ((IObj)new db$get_prefetch_dispatcher$reify__14046(null, atomicBoolean, object3)).withMeta((IPersistentMap)const__6);
        } else {
            iObj = ((IObj)new db$get_prefetch_dispatcher$reify__14050(null)).withMeta((IPersistentMap)const__9);
        }
        return iObj;
    }

    public Object invoke() {
        return db$get_prefetch_dispatcher.invokeStatic();
    }
}

