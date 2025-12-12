/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.LockingTransaction
 *  clojure.lang.MultiFn
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
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.MultiFn;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.error$add_details_to_msg;
import datomic.error$arg;
import datomic.error$argd;
import datomic.error$cancelled_QMARK_;
import datomic.error$create;
import datomic.error$deserialize_exception;
import datomic.error$eval_exception;
import datomic.error$fn__648;
import datomic.error$fn__650;
import datomic.error$fn__656;
import datomic.error$fn__658;
import datomic.error$fn__660;
import datomic.error$fn__684;
import datomic.error$fn__694;
import datomic.error$has_string_constructor_QMARK_;
import datomic.error$loading__6434__auto____646;
import datomic.error$raise;
import datomic.error$report;
import datomic.error$runonce;
import datomic.error$should_log_exception_QMARK_;
import datomic.error$state;
import datomic.error$with_unwind;
import datomic.error$with_unwind_STAR_;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class error__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Object const__5;
    public static final Object const__6;
    public static final Keyword const__7;
    public static final Var const__8;
    public static final AFn const__13;
    public static final Var const__14;
    public static final AFn const__16;
    public static final Var const__17;
    public static final AFn const__19;
    public static final Var const__20;
    public static final AFn const__22;
    public static final Var const__23;
    public static final AFn const__25;
    public static final Var const__26;
    public static final AFn const__28;
    public static final Var const__29;
    public static final AFn const__31;
    public static final Var const__32;
    public static final AFn const__33;
    public static final Var const__34;
    public static final Var const__35;
    public static final AFn const__38;
    public static final Var const__39;
    public static final AFn const__40;
    public static final Var const__41;
    public static final Var const__42;
    public static final AFn const__44;
    public static final Var const__45;
    public static final AFn const__47;
    public static final Var const__48;
    public static final AFn const__50;
    public static final Var const__51;
    public static final AFn const__53;
    public static final Var const__54;
    public static final AFn const__56;
    public static final Var const__57;
    public static final AFn const__59;
    public static final Var const__60;
    public static final AFn const__62;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new error$loading__6434__auto____646()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new error$fn__648())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)new error$fn__650()).invoke();
        MultiFn multiFn = ((MultiFn)const__4.getRawRoot()).addMethod(const__5, (IFn)new error$fn__656());
        MultiFn multiFn2 = ((MultiFn)const__4.getRawRoot()).addMethod(const__6, (IFn)new error$fn__658());
        MultiFn multiFn3 = ((MultiFn)const__4.getRawRoot()).addMethod((Object)const__7, (IFn)new error$fn__660());
        Var var = const__8;
        var.setMeta((IPersistentMap)const__13);
        Var var2 = var;
        var.bindRoot((Object)new error$create());
        const__8.setMacro();
        Object v10 = null;
        Var var3 = const__8;
        Var var4 = const__14;
        var4.setMeta((IPersistentMap)const__16);
        Var var5 = var4;
        var4.bindRoot((Object)new error$raise());
        Var var6 = const__17;
        var6.setMeta((IPersistentMap)const__19);
        Var var7 = var6;
        var6.bindRoot((Object)new error$arg());
        Var var8 = const__20;
        var8.setMeta((IPersistentMap)const__22);
        Var var9 = var8;
        var8.bindRoot((Object)new error$eval_exception());
        Var var10 = const__23;
        var10.setMeta((IPersistentMap)const__25);
        Var var11 = var10;
        var10.bindRoot((Object)new error$add_details_to_msg());
        Var var12 = const__26;
        var12.setMeta((IPersistentMap)const__28);
        Var var13 = var12;
        var12.bindRoot((Object)new error$argd());
        Var var14 = const__29;
        var14.setMeta((IPersistentMap)const__31);
        Var var15 = var14;
        var14.bindRoot((Object)new error$state());
        Var var16 = const__32;
        var16.setMeta((IPersistentMap)const__33);
        Var var17 = var16;
        var16.bindRoot(((IFn)const__34.getRawRoot()).invoke((Object)new error$fn__684()));
        Var var18 = const__35;
        var18.setMeta((IPersistentMap)const__38);
        Var var19 = var18;
        var18.bindRoot((Object)new error$has_string_constructor_QMARK_());
        Var var20 = const__39;
        var20.setMeta((IPersistentMap)const__40);
        Var var21 = var20;
        var20.bindRoot(((IFn)const__41.getRawRoot()).invoke((Object)new error$fn__694()));
        Var var22 = const__42;
        var22.setMeta((IPersistentMap)const__44);
        Var var23 = var22;
        var22.bindRoot((Object)new error$deserialize_exception());
        Var var24 = const__45;
        var24.setMeta((IPersistentMap)const__47);
        Var var25 = var24;
        var24.bindRoot((Object)new error$report());
        Var var26 = const__48;
        var26.setMeta((IPersistentMap)const__50);
        Var var27 = var26;
        var26.bindRoot((Object)new error$runonce());
        Var var28 = const__51;
        var28.setMeta((IPersistentMap)const__53);
        Var var29 = var28;
        var28.bindRoot((Object)new error$with_unwind_STAR_());
        const__51.setMacro();
        Object v38 = null;
        Var var30 = const__51;
        Var var31 = const__54;
        var31.setMeta((IPersistentMap)const__56);
        Var var32 = var31;
        var31.bindRoot((Object)new error$with_unwind());
        const__54.setMacro();
        Object v42 = null;
        Var var33 = const__54;
        Var var34 = const__57;
        var34.setMeta((IPersistentMap)const__59);
        Var var35 = var34;
        var34.bindRoot((Object)new error$cancelled_QMARK_());
        Var var36 = const__60;
        var36.setMeta((IPersistentMap)const__62);
        Var var37 = var36;
        var36.bindRoot((Object)new error$should_log_exception_QMARK_());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.error");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.error", (String)"anomalize");
        const__5 = RT.classForName((String)"datomic.impl.Exceptions$IllegalArgumentExceptionInfo");
        const__6 = RT.classForName((String)"datomic.impl.Exceptions$IllegalStateExceptionInfo");
        const__7 = RT.keyword(null, (String)"default");
        const__8 = RT.var((String)"datomic.error", (String)"create");
        const__13 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cls"), (Object)Symbol.intern(null, (String)"code")), Tuple.create((Object)Symbol.intern(null, (String)"cls"), (Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"cls"), (Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"details")), Tuple.create((Object)Symbol.intern(null, (String)"cls"), (Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"details"), (Object)Symbol.intern(null, (String)"cause")))), RT.keyword(null, (String)"column"), 1});
        const__14 = RT.var((String)"datomic.error", (String)"raise");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"code")), Tuple.create((Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"details")), Tuple.create((Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"details"), (Object)Symbol.intern(null, (String)"cause")))), RT.keyword(null, (String)"column"), 1});
        const__17 = RT.var((String)"datomic.error", (String)"arg");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"code")), Tuple.create((Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"details")), Tuple.create((Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"details"), (Object)Symbol.intern(null, (String)"cause")))), RT.keyword(null, (String)"column"), 1});
        const__20 = RT.var((String)"datomic.error", (String)"eval-exception");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"context"), (Object)Symbol.intern(null, (String)"expr"), (Object)Symbol.intern(null, (String)"arguments")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"cmap")}), (Object)Symbol.intern(null, (String)"t")))), RT.keyword(null, (String)"column"), 1});
        const__23 = RT.var((String)"datomic.error", (String)"add-details-to-msg");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"details")))), RT.keyword(null, (String)"column"), 1});
        const__26 = RT.var((String)"datomic.error", (String)"argd");
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"code")), Tuple.create((Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"details")), Tuple.create((Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"details"), (Object)Symbol.intern(null, (String)"cause")))), RT.keyword(null, (String)"column"), 1});
        const__29 = RT.var((String)"datomic.error", (String)"state");
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"code")), Tuple.create((Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"details")), Tuple.create((Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"details"), (Object)Symbol.intern(null, (String)"cause")))), RT.keyword(null, (String)"column"), 1});
        const__32 = RT.var((String)"datomic.error", (String)"reporter");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__34 = RT.var((String)"clojure.core", (String)"atom");
        const__35 = RT.var((String)"datomic.error", (String)"has-string-constructor?");
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cls")))), RT.keyword(null, (String)"column"), 1});
        const__39 = RT.var((String)"datomic.error", (String)"exception-deserializer");
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__41 = RT.var((String)"clojure.core", (String)"memoize");
        const__42 = RT.var((String)"datomic.error", (String)"deserialize-exception");
        const__44 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"classname"), (Object)Symbol.intern(null, (String)"error"), (Object)Symbol.intern(null, (String)"error-data"))})))), RT.keyword(null, (String)"column"), 1});
        const__45 = RT.var((String)"datomic.error", (String)"report");
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"t")))), RT.keyword(null, (String)"column"), 1});
        const__48 = RT.var((String)"datomic.error", (String)"runonce");
        const__50 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"column"), 1});
        const__51 = RT.var((String)"datomic.error", (String)"with-unwind*");
        const__53 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"unwind-prev"), (Object)Symbol.intern(null, (String)"unwind-all"), (Object)Symbol.intern(null, (String)"bindings"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"column"), 1});
        const__54 = RT.var((String)"datomic.error", (String)"with-unwind");
        const__56 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"unwind-all"), (Object)Symbol.intern(null, (String)"bindings"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"column"), 1});
        const__57 = RT.var((String)"datomic.error", (String)"cancelled?");
        const__59 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"anom")))), RT.keyword(null, (String)"column"), 1});
        const__60 = RT.var((String)"datomic.error", (String)"should-log-exception?");
        const__62 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"anom")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        error__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.error__init").getClassLoader());
        try {
            error__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

