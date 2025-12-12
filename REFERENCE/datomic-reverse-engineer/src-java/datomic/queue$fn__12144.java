/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IPersistentMap;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.queue$fn__12144$__GT_DelayingQueue__12146;
import java.util.Arrays;

public final class queue$fn__12144
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.queue", (String)"->DelayingQueue");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"delay"), (Object)Symbol.intern(null, (String)"delay-queue"), (Object)Symbol.intern(null, (String)"thread")))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.queue.DelayingQueue");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.queue.DelayingQueue"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new queue$fn__12144$__GT_DelayingQueue__12146());
        return const__6;
    }

    public Object invoke() {
        return queue$fn__12144.invokeStatic();
    }
}

