/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AReference
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AReference;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.cli$apply_defaults;
import datomic.cli$cli__GT_map;
import datomic.cli$coerce_vals;
import datomic.cli$development_version;
import datomic.cli$expand_short_names;
import datomic.cli$fail;
import datomic.cli$fn__20664;
import datomic.cli$loading__6434__auto____20662;
import datomic.cli$missing_values;
import datomic.cli$parse_or_exit_BANG_;
import datomic.cli$print_help;
import datomic.cli$shell;
import datomic.cli$unique_index;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class cli__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__3;
    public static final AFn const__4;
    public static final Var const__5;
    public static final AFn const__11;
    public static final Var const__12;
    public static final AFn const__14;
    public static final Var const__15;
    public static final AFn const__17;
    public static final Var const__18;
    public static final AFn const__20;
    public static final Var const__21;
    public static final AFn const__23;
    public static final Var const__24;
    public static final AFn const__26;
    public static final Var const__27;
    public static final AFn const__29;
    public static final Var const__30;
    public static final AFn const__32;
    public static final Var const__33;
    public static final AFn const__34;
    public static final Var const__35;
    public static final Var const__36;
    public static final AFn const__38;
    public static final Var const__39;
    public static final AFn const__40;
    public static final Var const__41;
    public static final AFn const__43;
    public static final Var const__44;
    public static final AFn const__46;

    public static void load() {
        Object v3;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        IPersistentMap iPersistentMap = ((AReference)Namespace.find((Symbol)((Symbol)const__1))).resetMeta((IPersistentMap)const__3);
        Object object2 = ((IFn)new cli$loading__6434__auto____20662()).invoke();
        if (((Symbol)const__1).equals((Object)const__4)) {
            v3 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new cli$fn__20664())));
            v3 = null;
        }
        Var var = const__5;
        var.setMeta((IPersistentMap)const__11);
        Var var2 = var;
        var.bindRoot((Object)new cli$unique_index());
        Var var3 = const__12;
        var3.setMeta((IPersistentMap)const__14);
        Var var4 = var3;
        var3.bindRoot((Object)new cli$print_help());
        Var var5 = const__15;
        var5.setMeta((IPersistentMap)const__17);
        Var var6 = var5;
        var5.bindRoot((Object)new cli$cli__GT_map());
        Var var7 = const__18;
        var7.setMeta((IPersistentMap)const__20);
        Var var8 = var7;
        var7.bindRoot((Object)new cli$expand_short_names());
        Var var9 = const__21;
        var9.setMeta((IPersistentMap)const__23);
        Var var10 = var9;
        var9.bindRoot((Object)new cli$coerce_vals());
        Var var11 = const__24;
        var11.setMeta((IPersistentMap)const__26);
        Var var12 = var11;
        var11.bindRoot((Object)new cli$apply_defaults());
        Var var13 = const__27;
        var13.setMeta((IPersistentMap)const__29);
        Var var14 = var13;
        var13.bindRoot((Object)new cli$missing_values());
        Var var15 = const__30;
        var15.setMeta((IPersistentMap)const__32);
        Var var16 = var15;
        var15.bindRoot((Object)new cli$parse_or_exit_BANG_());
        Var var17 = const__33;
        var17.setMeta((IPersistentMap)const__34);
        Var var18 = var17;
        var17.bindRoot(((IFn)const__35.getRawRoot()).invoke((Object)Boolean.FALSE));
        Var var19 = const__36;
        var19.setMeta((IPersistentMap)const__38);
        Var var20 = var19;
        var19.bindRoot((Object)new cli$fail());
        Var var21 = const__39;
        var21.setMeta((IPersistentMap)const__40);
        Var var22 = var21;
        var21.bindRoot(((IFn)const__35.getRawRoot()).invoke((Object)Boolean.TRUE));
        Var var23 = const__41;
        var23.setMeta((IPersistentMap)const__43);
        Var var24 = var23;
        var23.bindRoot((Object)new cli$shell());
        Var var25 = const__44;
        var25.setMeta((IPersistentMap)const__46);
        Var var26 = var25;
        var25.bindRoot((Object)new cli$development_version());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)((IObj)Symbol.intern(null, (String)"datomic.cli")).withMeta(RT.map((Object[])new Object[0]));
        const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"doc"), "Command line parsing.\n\nFunctions in this namespace deal with the following data structures:\n\nCommand line args\nA sequence of strings, e.g. from *command-line-args*\n\n(Processed) args\nargs converted into map of keys to typed values\n\nSpecs             \nA set of descriptions of arguments. Specs are maps that contain\nsome or all of the following keys:\n\n* short-name   a single chararcter name\n* long-name    a human-friendly Clojure name\n* coerce       a function to coerce a string to desired type\n* required     true if the arg must be present\n* default      a default value\n* doc          a docstring for the argument\n\nPositions\nA collection of keywords specifying the names to bind to positional\narguments. The names must correspond to the :long-name field in the\nspec map.\n\nA program that had a required, positional argument dir and an optional,\nnamed argument port might start like this:\n\n(def args (cli/parse-or-exit!\n           *file*\n           *command-line-args*\n           #{{:long-name :dir :required true}\n             {:long-name :port :short-name :p :coerce #(Integer. %)}}\n           [:dir]))\n"});
        const__4 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__5 = RT.var((String)"datomic.cli", (String)"unique-index");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"xrel"), (Object)Symbol.intern(null, (String)"k")), Tuple.create((Object)Symbol.intern(null, (String)"xrel"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v")))), RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.cli", (String)"print-help");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cmd"), (Object)Symbol.intern(null, (String)"spec"), (Object)Symbol.intern(null, (String)"positions")))), RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.cli", (String)"cli->map");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"strings"), (Object)Symbol.intern(null, (String)"positions"), (Object)Symbol.intern(null, (String)"vararg")))), RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"datomic.cli", (String)"expand-short-names");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"spec")))), RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"datomic.cli", (String)"coerce-vals");
        const__23 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"spec")))), RT.keyword(null, (String)"column"), 1});
        const__24 = RT.var((String)"datomic.cli", (String)"apply-defaults");
        const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"spec")))), RT.keyword(null, (String)"column"), 1});
        const__27 = RT.var((String)"datomic.cli", (String)"missing-values");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"spec")))), RT.keyword(null, (String)"column"), 1});
        const__30 = RT.var((String)"datomic.cli", (String)"parse-or-exit!");
        const__32 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cmd"), (Object)Symbol.intern(null, (String)"arg"), (Object)Symbol.intern(null, (String)"spec"), (Object)Symbol.intern(null, (String)"positions")), Tuple.create((Object)Symbol.intern(null, (String)"cmd"), (Object)Symbol.intern(null, (String)"args"), (Object)Symbol.intern(null, (String)"spec"), (Object)Symbol.intern(null, (String)"positions"), (Object)Symbol.intern(null, (String)"vararg")))), RT.keyword(null, (String)"column"), 1});
        const__33 = RT.var((String)"datomic.cli", (String)"failed");
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__35 = RT.var((String)"clojure.core", (String)"atom");
        const__36 = RT.var((String)"datomic.cli", (String)"fail");
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"msg")))), RT.keyword(null, (String)"column"), 1});
        const__39 = RT.var((String)"datomic.cli", (String)"exit-after-command");
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__41 = RT.var((String)"datomic.cli", (String)"shell");
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"args")))), RT.keyword(null, (String)"column"), 1});
        const__44 = RT.var((String)"datomic.cli", (String)"development-version");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        cli__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.cli__init").getClassLoader());
        try {
            cli__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

