/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.Delay
 *  clojure.lang.IFn
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
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.extension_resolver$allow_QMARK_;
import datomic.extension_resolver$allow_list__GT_pred;
import datomic.extension_resolver$anom_map;
import datomic.extension_resolver$anomaly_BANG_;
import datomic.extension_resolver$ensure_allow_list_BANG_;
import datomic.extension_resolver$ensure_extensions_config_BANG_;
import datomic.extension_resolver$explicit_pred;
import datomic.extension_resolver$fn__14296;
import datomic.extension_resolver$fn__14322;
import datomic.extension_resolver$load_extensions_config;
import datomic.extension_resolver$load_preds;
import datomic.extension_resolver$loading__6434__auto____14294;
import datomic.extension_resolver$preload_BANG_;
import datomic.extension_resolver$resolve_BANG_;
import datomic.extension_resolver$resolve_built_in_xform;
import datomic.extension_resolver$resolve_xform_BANG_;
import datomic.extension_resolver$user_namespaces;
import datomic.extension_resolver$wildcard_name_QMARK_;
import datomic.extension_resolver$wildcard_pred;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class extension_resolver__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__8;
    public static final Var const__9;
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
    public static final AFn const__35;
    public static final Var const__36;
    public static final AFn const__38;
    public static final Var const__39;
    public static final AFn const__40;
    public static final Var const__41;
    public static final AFn const__43;
    public static final Var const__44;
    public static final AFn const__46;
    public static final Var const__47;
    public static final AFn const__49;
    public static final Var const__50;
    public static final AFn const__52;
    public static final Var const__53;
    public static final AFn const__55;
    public static final Var const__56;
    public static final AFn const__58;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new extension_resolver$loading__6434__auto____14294()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new extension_resolver$fn__14296())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__8);
        Var var2 = var;
        var.bindRoot((Object)new extension_resolver$wildcard_name_QMARK_());
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new extension_resolver$anom_map());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new extension_resolver$anomaly_BANG_());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new extension_resolver$explicit_pred());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__21);
        Var var10 = var9;
        var9.bindRoot((Object)new extension_resolver$wildcard_pred());
        Var var11 = const__22;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot((Object)new extension_resolver$ensure_allow_list_BANG_());
        Var var13 = const__25;
        var13.setMeta((IPersistentMap)const__27);
        Var var14 = var13;
        var13.bindRoot((Object)new extension_resolver$ensure_extensions_config_BANG_());
        Var var15 = const__28;
        var15.setMeta((IPersistentMap)const__30);
        Var var16 = var15;
        var15.bindRoot((Object)new extension_resolver$allow_list__GT_pred());
        Var var17 = const__31;
        var17.setMeta((IPersistentMap)const__33);
        Var var18 = var17;
        var17.bindRoot((Object)new extension_resolver$load_extensions_config());
        Var var19 = const__34;
        var19.setMeta((IPersistentMap)const__35);
        Var var20 = var19;
        var19.bindRoot((Object)"datomic/extensions.edn");
        Var var21 = const__36;
        var21.setMeta((IPersistentMap)const__38);
        Var var22 = var21;
        var21.bindRoot((Object)new extension_resolver$load_preds());
        Var var23 = const__39;
        var23.setMeta((IPersistentMap)const__40);
        Var var24 = var23;
        var23.bindRoot((Object)new Delay((IFn)new extension_resolver$fn__14322()));
        Var var25 = const__41;
        var25.setMeta((IPersistentMap)const__43);
        Var var26 = var25;
        var25.bindRoot((Object)new extension_resolver$allow_QMARK_());
        Var var27 = const__44;
        var27.setMeta((IPersistentMap)const__46);
        Var var28 = var27;
        var27.bindRoot((Object)new extension_resolver$user_namespaces());
        Var var29 = const__47;
        var29.setMeta((IPersistentMap)const__49);
        Var var30 = var29;
        var29.bindRoot((Object)new extension_resolver$preload_BANG_());
        Var var31 = const__50;
        var31.setMeta((IPersistentMap)const__52);
        Var var32 = var31;
        var31.bindRoot((Object)new extension_resolver$resolve_BANG_());
        Var var33 = const__53;
        var33.setMeta((IPersistentMap)const__55);
        Var var34 = var33;
        var33.bindRoot((Object)new extension_resolver$resolve_built_in_xform());
        Var var35 = const__56;
        var35.setMeta((IPersistentMap)const__58);
        Var var36 = var35;
        var35.bindRoot((Object)new extension_resolver$resolve_xform_BANG_());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.extension-resolver");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.extension-resolver", (String)"wildcard-name?");
        const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"s")))), RT.keyword(null, (String)"column"), 1});
        const__9 = RT.var((String)"datomic.extension-resolver", (String)"anom-map");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"category"), (Object)Symbol.intern(null, (String)"msg")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.extension-resolver", (String)"anomaly!");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"name"), (Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"name"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"cause")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.extension-resolver", (String)"explicit-pred");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"allow")))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.extension-resolver", (String)"wildcard-pred");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"allow")))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.extension-resolver", (String)"ensure-allow-list!");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"allow-list")))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.extension-resolver", (String)"ensure-extensions-config!");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"xforms"))})))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.extension-resolver", (String)"allow-list->pred");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"allow")))), RT.keyword(null, (String)"column"), 1});
        const__31 = RT.var((String)"datomic.extension-resolver", (String)"load-extensions-config");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"rsrc")))), RT.keyword(null, (String)"column"), 1});
        const__34 = RT.var((String)"datomic.extension-resolver", (String)"config-resource");
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__36 = RT.var((String)"datomic.extension-resolver", (String)"load-preds");
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"rsrc")))), RT.keyword(null, (String)"column"), 1});
        const__39 = RT.var((String)"datomic.extension-resolver", (String)"preds-ref");
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__41 = RT.var((String)"datomic.extension-resolver", (String)"allow?");
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sym"), (Object)Symbol.intern(null, (String)"pred")))), RT.keyword(null, (String)"column"), 1});
        const__44 = RT.var((String)"datomic.extension-resolver", (String)"user-namespaces");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"path")))), RT.keyword(null, (String)"column"), 1});
        const__47 = RT.var((String)"datomic.extension-resolver", (String)"preload!");
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"path")))), RT.keyword(null, (String)"column"), 1});
        const__50 = RT.var((String)"datomic.extension-resolver", (String)"resolve!");
        const__52 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x"), (Object)Symbol.intern(null, (String)"context")))), RT.keyword(null, (String)"column"), 1});
        const__53 = RT.var((String)"datomic.extension-resolver", (String)"resolve-built-in-xform");
        const__55 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sym")))), RT.keyword(null, (String)"column"), 1});
        const__56 = RT.var((String)"datomic.extension-resolver", (String)"resolve-xform!");
        const__58 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sym")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        extension_resolver__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.extension_resolver__init").getClassLoader());
        try {
            extension_resolver__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

