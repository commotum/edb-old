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
import datomic.extensions$_GT_;
import datomic.extensions$_GT__EQ_;
import datomic.extensions$_LT_;
import datomic.extensions$_LT__EQ_;
import datomic.extensions$_SLASH_;
import datomic.extensions$_gather;
import datomic.extensions$ensure_sv_attrid;
import datomic.extensions$fn__17993;
import datomic.extensions$fulltext;
import datomic.extensions$get_else;
import datomic.extensions$get_some;
import datomic.extensions$ground;
import datomic.extensions$loading__6434__auto____17827;
import datomic.extensions$missing_QMARK_;
import datomic.extensions$project;
import datomic.extensions$q;
import datomic.extensions$tx_data;
import datomic.extensions$tx_ids;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class extensions__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__10;
    public static final Var const__11;
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
    public static final AFn const__34;
    public static final Var const__35;
    public static final AFn const__37;
    public static final Var const__38;
    public static final AFn const__40;
    public static final Var const__41;
    public static final AFn const__42;
    public static final Var const__43;
    public static final Var const__44;
    public static final AFn const__46;
    public static final Var const__47;
    public static final AFn const__49;
    public static final Var const__50;
    public static final AFn const__52;
    public static final Var const__53;
    public static final AFn const__55;
    public static final Var const__56;
    public static final AFn const__57;
    public static final Var const__58;
    public static final Var const__59;
    public static final AFn const__60;
    public static final Var const__61;
    public static final Var const__62;
    public static final AFn const__64;
    public static final Var const__65;
    public static final AFn const__66;
    public static final Var const__67;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new extensions$loading__6434__auto____17827()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new extensions$fn__17993())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__10);
        Var var2 = var;
        var.bindRoot((Object)new extensions$project());
        Var var3 = const__11;
        var3.setMeta((IPersistentMap)const__13);
        Var var4 = var3;
        var3.bindRoot((Object)new extensions$fulltext());
        Var var5 = const__14;
        var5.setMeta((IPersistentMap)const__16);
        Var var6 = var5;
        var5.bindRoot((Object)new extensions$tx_ids());
        Var var7 = const__17;
        var7.setMeta((IPersistentMap)const__19);
        Var var8 = var7;
        var7.bindRoot((Object)new extensions$tx_data());
        Var var9 = const__20;
        var9.setMeta((IPersistentMap)const__22);
        Var var10 = var9;
        var9.bindRoot((Object)new extensions$missing_QMARK_());
        Var var11 = const__23;
        var11.setMeta((IPersistentMap)const__25);
        Var var12 = var11;
        var11.bindRoot((Object)new extensions$ensure_sv_attrid());
        Var var13 = const__26;
        var13.setMeta((IPersistentMap)const__28);
        Var var14 = var13;
        var13.bindRoot((Object)new extensions$get_else());
        Var var15 = const__29;
        var15.setMeta((IPersistentMap)const__31);
        Var var16 = var15;
        var15.bindRoot((Object)new extensions$get_some());
        Var var17 = const__32;
        var17.setMeta((IPersistentMap)const__34);
        Var var18 = var17;
        var17.bindRoot((Object)new extensions$ground());
        Var var19 = const__35;
        var19.setMeta((IPersistentMap)const__37);
        Var var20 = var19;
        var19.bindRoot((Object)new extensions$_gather());
        Var var21 = const__38;
        var21.setMeta((IPersistentMap)const__40);
        Var var22 = var21;
        var21.bindRoot((Object)new extensions$_SLASH_());
        Var var23 = const__41;
        var23.setMeta((IPersistentMap)const__42);
        Var var24 = var23;
        var23.bindRoot(const__43.getRawRoot());
        Var var25 = const__44;
        var25.setMeta((IPersistentMap)const__46);
        Var var26 = var25;
        var25.bindRoot((Object)new extensions$_LT_());
        Var var27 = const__47;
        var27.setMeta((IPersistentMap)const__49);
        Var var28 = var27;
        var27.bindRoot((Object)new extensions$_GT_());
        Var var29 = const__50;
        var29.setMeta((IPersistentMap)const__52);
        Var var30 = var29;
        var29.bindRoot((Object)new extensions$_LT__EQ_());
        Var var31 = const__53;
        var31.setMeta((IPersistentMap)const__55);
        Var var32 = var31;
        var31.bindRoot((Object)new extensions$_GT__EQ_());
        Var var33 = const__56;
        var33.setMeta((IPersistentMap)const__57);
        Var var34 = var33;
        var33.bindRoot(const__58.getRawRoot());
        Var var35 = const__59;
        var35.setMeta((IPersistentMap)const__60);
        Var var36 = var35;
        var35.bindRoot(const__61.getRawRoot());
        Var var37 = const__62;
        var37.setMeta((IPersistentMap)const__64);
        Var var38 = var37;
        var37.bindRoot((Object)new extensions$q());
        Var var39 = const__65;
        var39.setMeta((IPersistentMap)const__66);
        Var var40 = var39;
        var39.bindRoot(const__67.getRawRoot());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.extensions");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.extensions", (String)"project");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"xs"), (Object)Symbol.intern(null, (String)"binds")))), RT.keyword(null, (String)"column"), 1});
        const__11 = RT.var((String)"datomic.extensions", (String)"fulltext");
        const__13 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"attr"), (Object)Symbol.intern(null, (String)"qmap")))), RT.keyword(null, (String)"column"), 1});
        const__14 = RT.var((String)"datomic.extensions", (String)"tx-ids");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"log")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Log")})), (Object)Symbol.intern(null, (String)"start"), (Object)Symbol.intern(null, (String)"end")))), RT.keyword(null, (String)"column"), 1});
        const__17 = RT.var((String)"datomic.extensions", (String)"tx-data");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"log")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Log")})), (Object)Symbol.intern(null, (String)"t")))), RT.keyword(null, (String)"column"), 1});
        const__20 = RT.var((String)"datomic.extensions", (String)"missing?");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Database")})), (Object)Symbol.intern(null, (String)"e"), (Object)Symbol.intern(null, (String)"attr")))), RT.keyword(null, (String)"column"), 1});
        const__23 = RT.var((String)"datomic.extensions", (String)"ensure-sv-attrid");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Database")})), (Object)Symbol.intern(null, (String)"a")))), RT.keyword(null, (String)"column"), 1});
        const__26 = RT.var((String)"datomic.extensions", (String)"get-else");
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"e"), (Object)Symbol.intern(null, (String)"attr"), (Object)Symbol.intern(null, (String)"v")))), RT.keyword(null, (String)"column"), 1});
        const__29 = RT.var((String)"datomic.extensions", (String)"get-some");
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"e"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"attrs")))), RT.keyword(null, (String)"column"), 1});
        const__32 = RT.var((String)"datomic.extensions", (String)"ground");
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")))), RT.keyword(null, (String)"column"), 1});
        const__35 = RT.var((String)"datomic.extensions", (String)"-gather");
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"e"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"attrs")))), RT.keyword(null, (String)"column"), 1});
        const__38 = RT.var((String)"datomic.extensions", (String)"/");
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"a"), (Object)Symbol.intern(null, (String)"b")))), RT.keyword(null, (String)"column"), 1});
        const__41 = RT.var((String)"datomic.extensions", (String)"!=");
        const__42 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__43 = RT.var((String)"clojure.core", (String)"not=");
        const__44 = RT.var((String)"datomic.extensions", (String)"<");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"a"), (Object)Symbol.intern(null, (String)"b")))), RT.keyword(null, (String)"column"), 1});
        const__47 = RT.var((String)"datomic.extensions", (String)">");
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"a"), (Object)Symbol.intern(null, (String)"b")))), RT.keyword(null, (String)"column"), 1});
        const__50 = RT.var((String)"datomic.extensions", (String)"<=");
        const__52 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"a"), (Object)Symbol.intern(null, (String)"b")))), RT.keyword(null, (String)"column"), 1});
        const__53 = RT.var((String)"datomic.extensions", (String)">=");
        const__55 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"a"), (Object)Symbol.intern(null, (String)"b")))), RT.keyword(null, (String)"column"), 1});
        const__56 = RT.var((String)"datomic.extensions", (String)"tuple");
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__58 = RT.var((String)"clojure.core", (String)"vector");
        const__59 = RT.var((String)"datomic.extensions", (String)"untuple");
        const__60 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__61 = RT.var((String)"clojure.core", (String)"identity");
        const__62 = RT.var((String)"datomic.extensions", (String)"q");
        const__64 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"query"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"srcs")))), RT.keyword(null, (String)"column"), 1});
        const__65 = RT.var((String)"datomic.extensions", (String)"db-attr-splits");
        const__66 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__67 = RT.var((String)"datomic.stats", (String)"db-attr-splits");
    }

    static {
        extensions__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.extensions__init").getClassLoader());
        try {
            extensions__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

