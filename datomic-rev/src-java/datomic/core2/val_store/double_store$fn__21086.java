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
package datomic.core2.val_store;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IPersistentMap;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.core2.val_store.double_store$fn__21086$__GT_ValStore__21248;
import java.util.Arrays;

public final class double_store$fn__21086
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.val-store.double-store", (String)"->ValStore");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"near-store"), (Object)Symbol.intern(null, (String)"far-store"), (Object)Symbol.intern(null, (String)"repair-metric"), (Object)Symbol.intern(null, (String)"get-fallback-msec")))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.core2.val_store.double_store.ValStore");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.core2.val_store.double_store.ValStore"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new double_store$fn__21086$__GT_ValStore__21248());
        return const__6;
    }

    public Object invoke() {
        return double_store$fn__21086.invokeStatic();
    }
}

