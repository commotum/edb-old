/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.external_sort_datoms$consume_sorted_datoms;
import datomic.external_sort_datoms$create_file_writer;
import datomic.external_sort_datoms$fn__14500;
import datomic.external_sort_datoms$loading__6434__auto____14346;
import datomic.external_sort_datoms$reify__14505;
import datomic.external_sort_datoms$reify__14507;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class external_sort_datoms__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final Object const__14;
    public static final AFn const__18;
    public static final Var const__19;
    public static final AFn const__20;
    public static final Var const__21;
    public static final AFn const__24;
    public static final Var const__25;
    public static final AFn const__27;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new external_sort_datoms$loading__6434__auto____14346()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new external_sort_datoms$fn__14500())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new external_sort_datoms$create_file_writer());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__11);
        Var var4 = var3;
        var3.bindRoot(((IFn)const__12.getRawRoot()).invoke(const__13.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__14, RT.mapUniqueKeys((Object[])new Object[]{"datum", ((IObj)new external_sort_datoms$reify__14505(null)).withMeta((IPersistentMap)const__18)})})));
        Var var5 = const__19;
        var5.setMeta((IPersistentMap)const__20);
        Var var6 = var5;
        var5.bindRoot(((IFn)const__12.getRawRoot()).invoke(const__21.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{"datum", ((IObj)new external_sort_datoms$reify__14507(null)).withMeta((IPersistentMap)const__24)})));
        Var var7 = const__25;
        var7.setMeta((IPersistentMap)const__27);
        Var var8 = var7;
        var7.bindRoot((Object)new external_sort_datoms$consume_sorted_datoms());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.external-sort-datoms");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.external-sort-datoms", (String)"create-file-writer");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"os"), (Object)Symbol.intern(null, (String)"handlers")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.external-sort-datoms", (String)"datom-write-handlers");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"clojure.core", (String)"merge");
        const__13 = RT.var((String)"datomic.fressian", (String)"user-write-handlers");
        const__14 = RT.classForName((String)"datomic.db.Datum");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 27, RT.keyword(null, (String)"column"), 6});
        const__19 = RT.var((String)"datomic.external-sort-datoms", (String)"datom-read-handlers");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"datomic.fressian", (String)"user-read-handlers");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 42, RT.keyword(null, (String)"column"), 5});
        const__25 = RT.var((String)"datomic.external-sort-datoms", (String)"consume-sorted-datoms");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"iter"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"cmp"), (Object)Symbol.intern(null, (String)"dir"), (Object)Symbol.intern(null, (String)"max-chunk-size"), (Object)Symbol.intern(null, (String)"prog-fn"))}), (Object)Symbol.intern(null, (String)"handler")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        external_sort_datoms__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.external_sort_datoms__init").getClassLoader());
        try {
            external_sort_datoms__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

