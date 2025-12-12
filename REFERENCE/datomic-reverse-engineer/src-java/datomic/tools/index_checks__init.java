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
package datomic.tools;

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
import datomic.tools.index_checks$_main;
import datomic.tools.index_checks$_main_STAR_;
import datomic.tools.index_checks$boot_tail_collision_QMARK_;
import datomic.tools.index_checks$card_one_collisions;
import datomic.tools.index_checks$fn__21881;
import datomic.tools.index_checks$loading__6434__auto____21879;
import datomic.tools.index_checks$log_only;
import datomic.tools.index_checks$maybe_renamed_QMARK_;
import datomic.tools.index_checks$non_unique_QMARK_;
import datomic.tools.index_checks$progress_dot_fn;
import datomic.tools.index_checks$rename_from;
import datomic.tools.index_checks$rename_to;
import datomic.tools.index_checks$reporter;
import datomic.tools.index_checks$unique_collisions;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class index_checks__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__12;
    public static final Var const__13;
    public static final AFn const__15;
    public static final Var const__16;
    public static final AFn const__18;
    public static final Var const__19;
    public static final AFn const__21;
    public static final Var const__22;
    public static final AFn const__24;
    public static final Var const__25;
    public static final AFn const__27;
    public static final Var const__28;
    public static final AFn const__30;
    public static final Var const__31;
    public static final AFn const__33;
    public static final Var const__34;
    public static final AFn const__36;
    public static final Var const__37;
    public static final AFn const__39;
    public static final Var const__40;
    public static final AFn const__42;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new index_checks$loading__6434__auto____21879()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new index_checks$fn__21881())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new index_checks$card_one_collisions());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new index_checks$boot_tail_collision_QMARK_());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new index_checks$non_unique_QMARK_());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new index_checks$reporter());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__21);
        Var var10 = var9;
        var9.bindRoot((Object)new index_checks$unique_collisions());
        Var var11 = const__22;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot((Object)new index_checks$rename_to());
        Var var13 = const__25;
        var13.setMeta((IPersistentMap)const__27);
        Var var14 = var13;
        var13.bindRoot((Object)new index_checks$rename_from());
        Var var15 = const__28;
        var15.setMeta((IPersistentMap)const__30);
        Var var16 = var15;
        var15.bindRoot((Object)new index_checks$maybe_renamed_QMARK_());
        Var var17 = const__31;
        var17.setMeta((IPersistentMap)const__33);
        Var var18 = var17;
        var17.bindRoot((Object)new index_checks$log_only());
        Var var19 = const__34;
        var19.setMeta((IPersistentMap)const__36);
        Var var20 = var19;
        var19.bindRoot((Object)new index_checks$progress_dot_fn());
        Var var21 = const__37;
        var21.setMeta((IPersistentMap)const__39);
        Var var22 = var21;
        var21.bindRoot((Object)new index_checks$_main_STAR_());
        Var var23 = const__40;
        var23.setMeta((IPersistentMap)const__42);
        Var var24 = var23;
        var23.bindRoot((Object)new index_checks$_main());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.tools.index-checks");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.tools.index-checks", (String)"card-one-collisions");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"sort"), (Object)Symbol.intern(null, (String)"progress"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"components")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.tools.index-checks", (String)"boot-tail-collision?");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Tuple.create((Object)((IObj)Symbol.intern(null, (String)"d1")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Datom")})), (Object)Symbol.intern(null, (String)"_"))))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.tools.index-checks", (String)"non-unique?");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Tuple.create((Object)((IObj)Symbol.intern(null, (String)"d1")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Datom")})), (Object)((IObj)Symbol.intern(null, (String)"d2")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Datom")})))))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.tools.index-checks", (String)"reporter");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.tools.index-checks", (String)"unique-collisions");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"progress")))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.tools.index-checks", (String)"rename-to");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"e")))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.tools.index-checks", (String)"rename-from");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"e")))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.tools.index-checks", (String)"maybe-renamed?");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"e"), (Object)Symbol.intern(null, (String)"a"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"tx"))})))), RT.keyword(null, (String)"column"), 1});
        const__31 = RT.var((String)"datomic.tools.index-checks", (String)"log-only");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cr"), (Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"progress")))), RT.keyword(null, (String)"column"), 1});
        const__34 = RT.var((String)"datomic.tools.index-checks", (String)"progress-dot-fn");
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"n")))), RT.keyword(null, (String)"column"), 1});
        const__37 = RT.var((String)"datomic.tools.index-checks", (String)"-main*");
        const__39 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"uri")))), RT.keyword(null, (String)"column"), 1});
        const__40 = RT.var((String)"datomic.tools.index-checks", (String)"-main");
        const__42 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"uri")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        index_checks__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.tools.index_checks__init").getClassLoader());
        try {
            index_checks__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

