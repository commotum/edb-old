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
import datomic.core2.val_store.fs$fn__21324$__GT_FS__21338;
import java.util.Arrays;

public final class fs$fn__21324
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.val-store.fs", (String)"->FS");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"get-pool"), (Object)Symbol.intern(null, (String)"put-pool"), (Object)Symbol.intern(null, (String)"path"), (Object)Symbol.intern(null, (String)"delete-pool")))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.core2.val_store.fs.FS");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.core2.val_store.fs.FS"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new fs$fn__21324$__GT_FS__21338());
        return const__6;
    }

    public Object invoke() {
        return fs$fn__21324.invokeStatic();
    }
}

