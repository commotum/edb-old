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
import datomic.pull$a_iter;
import datomic.pull$attr_spec__GT_attr;
import datomic.pull$attr_spec__GT_fn;
import datomic.pull$attr_with_opts_QMARK_;
import datomic.pull$attr_with_opts__GT_attr_tuple;
import datomic.pull$attr_with_opts__GT_valfn;
import datomic.pull$default_spec;
import datomic.pull$denormalize_kw;
import datomic.pull$dereffed_index_pull;
import datomic.pull$ea__GT_v;
import datomic.pull$fix_specs_for_underscore_prefix_attrs;
import datomic.pull$fn__18896;
import datomic.pull$fn__18987;
import datomic.pull$index_pull;
import datomic.pull$limit_default_from_map;
import datomic.pull$limit_iterable;
import datomic.pull$loading__6434__auto____18894;
import datomic.pull$next_a;
import datomic.pull$nilify_empty;
import datomic.pull$normalize_attr;
import datomic.pull$normalize_pattern;
import datomic.pull$normalize_recur_limit;
import datomic.pull$parse_index_pull_arg_map;
import datomic.pull$pull;
import datomic.pull$pull_1;
import datomic.pull$pull_STAR_;
import datomic.pull$ra__GT_e;
import datomic.pull$resolve_attr;
import datomic.pull$try_xform;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class pull__init {
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
    public static final AFn const__20;
    public static final Var const__21;
    public static final Object const__22;
    public static final Var const__23;
    public static final AFn const__25;
    public static final Var const__26;
    public static final AFn const__28;
    public static final Var const__29;
    public static final AFn const__31;
    public static final Var const__32;
    public static final AFn const__35;
    public static final Var const__36;
    public static final AFn const__38;
    public static final Var const__39;
    public static final AFn const__41;
    public static final Var const__42;
    public static final AFn const__44;
    public static final Var const__45;
    public static final AFn const__47;
    public static final Var const__48;
    public static final AFn const__50;
    public static final Var const__51;
    public static final AFn const__53;
    public static final Var const__54;
    public static final AFn const__55;
    public static final Var const__56;
    public static final Object const__57;
    public static final Var const__58;
    public static final AFn const__60;
    public static final Var const__61;
    public static final AFn const__63;
    public static final Var const__64;
    public static final AFn const__66;
    public static final Var const__67;
    public static final AFn const__69;
    public static final Var const__70;
    public static final AFn const__72;
    public static final Var const__73;
    public static final AFn const__75;
    public static final Var const__76;
    public static final AFn const__78;
    public static final Var const__79;
    public static final AFn const__81;
    public static final Var const__82;
    public static final AFn const__84;
    public static final Var const__85;
    public static final AFn const__87;
    public static final Var const__88;
    public static final AFn const__90;
    public static final Var const__91;
    public static final AFn const__93;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new pull$loading__6434__auto____18894()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new pull$fn__18896())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new pull$limit_iterable());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new pull$nilify_empty());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new pull$ra__GT_e());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new pull$ea__GT_v());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__20);
        Var var10 = var9;
        var9.bindRoot(((IFn)const__21.getRawRoot()).invoke(const__22));
        Var var11 = const__23;
        var11.setMeta((IPersistentMap)const__25);
        Var var12 = var11;
        var11.bindRoot((Object)new pull$attr_spec__GT_fn());
        Var var13 = const__26;
        var13.setMeta((IPersistentMap)const__28);
        Var var14 = var13;
        var13.bindRoot((Object)new pull$attr_spec__GT_attr());
        Var var15 = const__29;
        var15.setMeta((IPersistentMap)const__31);
        Var var16 = var15;
        var15.bindRoot((Object)new pull$attr_with_opts_QMARK_());
        Var var17 = const__32;
        var17.setMeta((IPersistentMap)const__35);
        Var var18 = var17;
        var17.bindRoot((Object)new pull$limit_default_from_map());
        Var var19 = const__36;
        var19.setMeta((IPersistentMap)const__38);
        Var var20 = var19;
        var19.bindRoot((Object)new pull$try_xform());
        Var var21 = const__39;
        var21.setMeta((IPersistentMap)const__41);
        Var var22 = var21;
        var21.bindRoot((Object)new pull$attr_with_opts__GT_valfn());
        Var var23 = const__42;
        var23.setMeta((IPersistentMap)const__44);
        Var var24 = var23;
        var23.bindRoot((Object)new pull$attr_with_opts__GT_attr_tuple());
        Var var25 = const__45;
        var25.setMeta((IPersistentMap)const__47);
        Var var26 = var25;
        var25.bindRoot((Object)new pull$normalize_attr());
        Var var27 = const__48;
        var27.setMeta((IPersistentMap)const__50);
        Var var28 = var27;
        var27.bindRoot((Object)new pull$normalize_recur_limit());
        Var var29 = const__51;
        var29.setMeta((IPersistentMap)const__53);
        Var var30 = var29;
        var29.bindRoot((Object)new pull$normalize_pattern());
        Var var31 = const__54;
        var31.setMeta((IPersistentMap)const__55);
        Var var32 = var31;
        var31.bindRoot(((IFn)const__56.getRawRoot()).invoke(const__51.getRawRoot(), const__57));
        Var var33 = const__58;
        var33.setMeta((IPersistentMap)const__60);
        Var var34 = var33;
        var33.bindRoot((Object)new pull$next_a());
        Object object4 = ((IFn)new pull$fn__18987()).invoke();
        Var var35 = const__61;
        var35.setMeta((IPersistentMap)const__63);
        Var var36 = var35;
        var35.bindRoot((Object)new pull$a_iter());
        Var var37 = const__64;
        var37.setMeta((IPersistentMap)const__66);
        Var var38 = var37;
        var37.bindRoot((Object)new pull$resolve_attr());
        Var var39 = const__67;
        var39.setMeta((IPersistentMap)const__69);
        Var var40 = var39;
        var39.bindRoot((Object)new pull$default_spec());
        Var var41 = const__70;
        var41.setMeta((IPersistentMap)const__72);
        Var var42 = var41;
        var41.bindRoot((Object)new pull$denormalize_kw());
        Var var43 = const__73;
        var43.setMeta((IPersistentMap)const__75);
        Var var44 = var43;
        var43.bindRoot((Object)new pull$fix_specs_for_underscore_prefix_attrs());
        Var var45 = const__76;
        var45.setMeta((IPersistentMap)const__78);
        Var var46 = var45;
        var45.bindRoot((Object)new pull$pull_STAR_());
        Var var47 = const__79;
        var47.setMeta((IPersistentMap)const__81);
        Var var48 = var47;
        var47.bindRoot((Object)new pull$parse_index_pull_arg_map());
        Var var49 = const__82;
        var49.setMeta((IPersistentMap)const__84);
        Var var50 = var49;
        var49.bindRoot((Object)new pull$index_pull());
        Var var51 = const__85;
        var51.setMeta((IPersistentMap)const__87);
        Var var52 = var51;
        var51.bindRoot((Object)new pull$dereffed_index_pull());
        Var var53 = const__88;
        var53.setMeta((IPersistentMap)const__90);
        Var var54 = var53;
        var53.bindRoot((Object)new pull$pull_1());
        Var var55 = const__91;
        var55.setMeta((IPersistentMap)const__93);
        Var var56 = var55;
        var55.bindRoot((Object)new pull$pull());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.pull");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.pull", (String)"limit-iterable");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"limit"), (Object)((IObj)Symbol.intern(null, (String)"iterable")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iterable")}))))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.pull", (String)"nilify-empty");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.pull", (String)"ra->e");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"r"), (Object)((IObj)Symbol.intern(null, (String)"attr")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Attribute")})), (Object)Symbol.intern(null, (String)"xf"), (Object)Symbol.intern(null, (String)"limit"), (Object)Symbol.intern(null, (String)"valfn")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.pull", (String)"ea->v");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"e"), (Object)Symbol.intern(null, (String)"attr"), (Object)Symbol.intern(null, (String)"xf"), (Object)Symbol.intern(null, (String)"limit"), (Object)Symbol.intern(null, (String)"valfn")), RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"db"), Symbol.intern(null, (String)"e"), ((IObj)Symbol.intern(null, (String)"attr")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Attribute")})), Symbol.intern(null, (String)"xf"), Symbol.intern(null, (String)"limit"), Symbol.intern(null, (String)"valfn"), Symbol.intern(null, (String)"use-aevt?")}))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.pull", (String)"default-limit");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"clojure.core", (String)"atom");
        const__22 = 1000L;
        const__23 = RT.var((String)"datomic.pull", (String)"attr-spec->fn");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"attr-spec")))), RT.keyword(null, (String)"column"), 1});
        const__26 = RT.var((String)"datomic.pull", (String)"attr-spec->attr");
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"attr-spec")))), RT.keyword(null, (String)"column"), 1});
        const__29 = RT.var((String)"datomic.pull", (String)"attr-with-opts?");
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"expr")))), RT.keyword(null, (String)"column"), 1});
        const__32 = RT.var((String)"datomic.pull", (String)"limit-default-from-map");
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"limit")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"args")})))), RT.keyword(null, (String)"column"), 1});
        const__36 = RT.var((String)"datomic.pull", (String)"try-xform");
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"xform")))), RT.keyword(null, (String)"column"), 1});
        const__39 = RT.var((String)"datomic.pull", (String)"attr-with-opts->valfn");
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"args"))))), RT.keyword(null, (String)"column"), 1});
        const__42 = RT.var((String)"datomic.pull", (String)"attr-with-opts->attr-tuple");
        const__44 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Tuple.create((Object)Symbol.intern(null, (String)"attr"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"args"))))), RT.keyword(null, (String)"column"), 1});
        const__45 = RT.var((String)"datomic.pull", (String)"normalize-attr");
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"attr-spec")))), RT.keyword(null, (String)"column"), 1});
        const__48 = RT.var((String)"datomic.pull", (String)"normalize-recur-limit");
        const__50 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")))), RT.keyword(null, (String)"column"), 1});
        const__51 = RT.var((String)"datomic.pull", (String)"normalize-pattern");
        const__53 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"pull-spec")))), RT.keyword(null, (String)"column"), 1});
        const__54 = RT.var((String)"datomic.pull", (String)"normalized-pattern-cache");
        const__55 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__56 = RT.var((String)"datomic.cache", (String)"create-computing");
        const__57 = 1000L;
        const__58 = RT.var((String)"datomic.pull", (String)"next-a");
        const__60 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"e"), (Object)Symbol.intern(null, (String)"a")))), RT.keyword(null, (String)"column"), 1});
        const__61 = RT.var((String)"datomic.pull", (String)"a-iter");
        const__63 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"e")))), RT.keyword(null, (String)"column"), 1});
        const__64 = RT.var((String)"datomic.pull", (String)"resolve-attr");
        const__66 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"kw")))), RT.keyword(null, (String)"column"), 1});
        const__67 = RT.var((String)"datomic.pull", (String)"default-spec");
        const__69 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"attr")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Attribute")})), (Object)Symbol.intern(null, (String)"kw"), (Object)Symbol.intern(null, (String)"db")))), RT.keyword(null, (String)"column"), 1});
        const__70 = RT.var((String)"datomic.pull", (String)"denormalize-kw");
        const__72 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"kw"), (Object)Symbol.intern(null, (String)"type")))), RT.keyword(null, (String)"column"), 1});
        const__73 = RT.var((String)"datomic.pull", (String)"fix-specs-for-underscore-prefix-attrs");
        const__75 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"forward"), (Object)Symbol.intern(null, (String)"reverse")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"m")}), (Object)Symbol.intern(null, (String)"db")))), RT.keyword(null, (String)"column"), 1});
        const__76 = RT.var((String)"datomic.pull", (String)"pull*");
        const__78 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Database")})), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"wildcard"), (Object)Symbol.intern(null, (String)"dbid")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"spec")}), (Object)Symbol.intern(null, (String)"recursed"), (Object)Symbol.intern(null, (String)"prefer-aevt?"), (Object)Symbol.intern(null, (String)"e")))), RT.keyword(null, (String)"column"), 1});
        const__79 = RT.var((String)"datomic.pull", (String)"parse-index-pull-arg-map");
        const__81 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"arg-map")))), RT.keyword(null, (String)"column"), 1});
        const__82 = RT.var((String)"datomic.pull", (String)"index-pull");
        const__84 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Database")})), (Object)Symbol.intern(null, (String)"arg-map")))), RT.keyword(null, (String)"column"), 1});
        const__85 = RT.var((String)"datomic.pull", (String)"dereffed-index-pull");
        const__87 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Database")})), (Object)Symbol.intern(null, (String)"arg-map")))), RT.keyword(null, (String)"column"), 1});
        const__88 = RT.var((String)"datomic.pull", (String)"pull-1");
        const__90 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"selector"), (Object)Symbol.intern(null, (String)"e")), Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"selector"), (Object)Symbol.intern(null, (String)"e"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"io-context")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"options")})))), RT.keyword(null, (String)"column"), 1});
        const__91 = RT.var((String)"datomic.pull", (String)"pull");
        const__93 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"selector"), (Object)Symbol.intern(null, (String)"es")), Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"selector"), (Object)Symbol.intern(null, (String)"es"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"io-context")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"options")})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        pull__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.pull__init").getClassLoader());
        try {
            pull__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

