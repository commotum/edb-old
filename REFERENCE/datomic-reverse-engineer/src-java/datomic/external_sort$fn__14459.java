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
import clojure.lang.Var;
import datomic.external_sort$fn__14459$__GT_FileSystemSorter__14486;
import java.util.Arrays;

public final class external_sort$fn__14459
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.external-sort", (String)"->FileSystemSorter");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"pool"), Symbol.intern(null, (String)"cmp"), Symbol.intern(null, (String)"io"), Symbol.intern(null, (String)"file-iter-fn"), Symbol.intern(null, (String)"create-file-writer-fn"), Symbol.intern(null, (String)"prog-fn"), Symbol.intern(null, (String)"files")}))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.external_sort.FileSystemSorter");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.external_sort.FileSystemSorter"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new external_sort$fn__14459$__GT_FileSystemSorter__14486());
        return const__6;
    }

    public Object invoke() {
        return external_sort$fn__14459.invokeStatic();
    }
}

