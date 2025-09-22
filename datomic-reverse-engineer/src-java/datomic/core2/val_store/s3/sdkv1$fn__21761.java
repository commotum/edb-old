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
package datomic.core2.val_store.s3;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IPersistentMap;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.core2.val_store.s3.sdkv1$fn__21761$__GT_ValStore__21772;
import java.util.Arrays;

public final class sdkv1$fn__21761
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.val-store.s3.sdkv1", (String)"->ValStore");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"read-pool"), (Object)Symbol.intern(null, (String)"write-pool"), (Object)Symbol.intern(null, (String)"retry-fn"), (Object)Symbol.intern(null, (String)"client"), (Object)Symbol.intern(null, (String)"bucket"), (Object)Symbol.intern(null, (String)"prefix")))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.core2.val_store.s3.sdkv1.ValStore");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.core2.val_store.s3.sdkv1.ValStore"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new sdkv1$fn__21761$__GT_ValStore__21772());
        return const__6;
    }

    public Object invoke() {
        return sdkv1$fn__21761.invokeStatic();
    }
}

