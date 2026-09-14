/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.LockingTransaction
 *  clojure.lang.MultiFn
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.valcache;

import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.MultiFn;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.valcache.puts_pool$fn__9826;
import datomic.valcache.puts_pool$fn__9832;
import datomic.valcache.puts_pool$fn__9835;
import datomic.valcache.puts_pool$fn__9852;
import datomic.valcache.puts_pool$fn__9865;
import datomic.valcache.puts_pool$fn__9869;
import datomic.valcache.puts_pool$get_from_queued_put;
import datomic.valcache.puts_pool$loading__6434__auto____9824;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class puts_pool__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Object const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Keyword const__7;
    public static final Var const__8;
    public static final ISeq const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final AFn const__15;
    public static final Keyword const__16;
    public static final AFn const__17;
    public static final Keyword const__18;
    public static final Keyword const__19;
    public static final AFn const__22;
    public static final Keyword const__23;
    public static final Var const__24;
    public static final Var const__25;
    public static final Var const__26;
    public static final AFn const__27;
    public static final AFn const__28;
    public static final Keyword const__29;
    public static final AFn const__30;
    public static final AFn const__31;
    public static final Var const__32;
    public static final AFn const__33;
    public static final Var const__34;
    public static final Keyword const__35;
    public static final Var const__36;
    public static final AFn const__41;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new puts_pool$loading__6434__auto____9824()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new puts_pool$fn__9826())));
            v2 = null;
        }
        Object object3 = ((IFn)new puts_pool$fn__9832()).invoke();
        Object object4 = const__3;
        Object object5 = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, const__6.getRawRoot(), (Object)const__7, null);
        Object object6 = ((IFn)const__8).invoke((Object)const__5, (Object)const__9);
        Object object7 = ((IFn)const__10.getRawRoot()).invoke((Object)const__5, const__11.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__15, (Object)const__16, (Object)const__17, (Object)const__18, (Object)const__5, (Object)const__19, (Object)const__22, (Object)const__23, (Object)RT.map((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__27, ((IFn)const__11.getRawRoot()).invoke((Object)const__28, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__5})))), new puts_pool$fn__9835(), ((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__30, ((IFn)const__11.getRawRoot()).invoke((Object)const__31, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__5})))), new puts_pool$fn__9852()})));
        Object object8 = ((IFn)const__32.getRawRoot()).invoke(const__5.getRawRoot());
        AFn aFn = const__33;
        Object object9 = ((IFn)new puts_pool$fn__9865()).invoke();
        MultiFn multiFn = ((MultiFn)const__34.getRawRoot()).addMethod((Object)const__35, (IFn)new puts_pool$fn__9869());
        Var var = const__36;
        var.setMeta((IPersistentMap)const__41);
        Var var2 = var;
        var.bindRoot((Object)new puts_pool$get_from_queued_put());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.valcache.puts-pool");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.classForName((String)"datomic.valcache.puts_pool.PutsPool");
        const__4 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__5 = RT.var((String)"datomic.valcache.puts-pool", (String)"PutsPool");
        const__6 = RT.var((String)"clojure.core", (String)"assoc");
        const__7 = RT.keyword(null, (String)"doc");
        const__8 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__9 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"submit")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"data"), (Object)Symbol.intern(null, (String)"f"))))})), ((IObj)Symbol.intern(null, (String)"get-queued-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"))))}))));
        const__10 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__11 = RT.var((String)"clojure.core", (String)"merge");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.valcache.puts_pool.PutsPool"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.valcache.puts_pool.PutsPool")});
        const__16 = RT.keyword(null, (String)"sigs");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"submit"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"submit")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"data"), (Object)Symbol.intern(null, (String)"f"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"data"), (Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"doc"), "Submits fn f to put key k. Associates data with put of k, see get-queued-put. f should return trueish if put was successful.\nReturns future if submitted, else nil.\n\ndata map should include \n\n:source      keyword tag for source data type, e.g. :bbuf\n:v           source data value.\n\nSee also get-from-queued-put."}), RT.keyword(null, (String)"get-queued-put"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"get-queued-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"doc"), "Returns a map with :data, :fut if put for k is in queue."})});
        const__18 = RT.keyword(null, (String)"var");
        const__19 = RT.keyword(null, (String)"method-map");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"get-queued-put"), RT.keyword(null, (String)"get-queued-put"), RT.keyword(null, (String)"submit"), RT.keyword(null, (String)"submit")});
        const__23 = RT.keyword(null, (String)"method-builders");
        const__24 = RT.var((String)"clojure.core", (String)"intern");
        const__25 = RT.var((String)"clojure.core", (String)"*ns*");
        const__26 = RT.var((String)"clojure.core", (String)"with-meta");
        const__27 = (AFn)((IObj)Symbol.intern(null, (String)"submit")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"data"), (Object)Symbol.intern(null, (String)"f"))))}));
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"submit")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"data"), (Object)Symbol.intern(null, (String)"f"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"data"), (Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"doc"), "Submits fn f to put key k. Associates data with put of k, see get-queued-put. f should return trueish if put was successful.\nReturns future if submitted, else nil.\n\ndata map should include \n\n:source      keyword tag for source data type, e.g. :bbuf\n:v           source data value.\n\nSee also get-from-queued-put."});
        const__29 = RT.keyword(null, (String)"protocol");
        const__30 = (AFn)((IObj)Symbol.intern(null, (String)"get-queued-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"))))}));
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"get-queued-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"doc"), "Returns a map with :data, :fut if put for k is in queue."});
        const__32 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__33 = (AFn)Symbol.intern(null, (String)"PutsPool");
        const__34 = RT.var((String)"datomic.valcache.puts-pool", (String)"get-from-put");
        const__35 = RT.keyword(null, (String)"default");
        const__36 = RT.var((String)"datomic.valcache.puts-pool", (String)"get-from-queued-put");
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"puts-pool"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        puts_pool__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.valcache.puts_pool__init").getClassLoader());
        try {
            puts_pool__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

