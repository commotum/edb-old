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
import datomic.clusterfs$all_keys;
import datomic.clusterfs$ceil;
import datomic.clusterfs$chunk_keys;
import datomic.clusterfs$chunk_path;
import datomic.clusterfs$create_file;
import datomic.clusterfs$create_files;
import datomic.clusterfs$create_fs;
import datomic.clusterfs$describe;
import datomic.clusterfs$file_chunk_keys;
import datomic.clusterfs$files;
import datomic.clusterfs$floor;
import datomic.clusterfs$fn__14179;
import datomic.clusterfs$fn__14185;
import datomic.clusterfs$fn__14206;
import datomic.clusterfs$fressian_chunk_from_channel;
import datomic.clusterfs$loading__6434__auto____14177;
import datomic.clusterfs$reify__14234;
import datomic.clusterfs$reify__14236;
import datomic.clusterfs$reify__14238;
import datomic.clusterfs$reify__14240;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class clusterfs__init {
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
    public static final AFn const__29;
    public static final Var const__30;
    public static final Var const__31;
    public static final Object const__32;
    public static final AFn const__36;
    public static final Object const__37;
    public static final AFn const__39;
    public static final Var const__40;
    public static final AFn const__41;
    public static final Var const__42;
    public static final AFn const__45;
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
        Object object2 = ((IFn)new clusterfs$loading__6434__auto____14177()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new clusterfs$fn__14179())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new clusterfs$chunk_path());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new clusterfs$floor());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new clusterfs$ceil());
        Object object4 = ((IFn)new clusterfs$fn__14185()).invoke();
        Object object5 = ((IFn)new clusterfs$fn__14206()).invoke();
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new clusterfs$files());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__21);
        Var var10 = var9;
        var9.bindRoot((Object)new clusterfs$file_chunk_keys());
        Var var11 = const__22;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot((Object)new clusterfs$chunk_keys());
        Var var13 = const__25;
        var13.setMeta((IPersistentMap)const__27);
        Var var14 = var13;
        var13.bindRoot((Object)new clusterfs$all_keys());
        Var var15 = const__28;
        var15.setMeta((IPersistentMap)const__29);
        Var var16 = var15;
        var15.bindRoot(((IFn)const__30.getRawRoot()).invoke(const__31.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__32, RT.mapUniqueKeys((Object[])new Object[]{"clusterfs-chunk", ((IObj)new clusterfs$reify__14234(null)).withMeta((IPersistentMap)const__36)}), const__37, RT.mapUniqueKeys((Object[])new Object[]{"clusterfs-root", ((IObj)new clusterfs$reify__14236(null)).withMeta((IPersistentMap)const__39)})})));
        Var var17 = const__40;
        var17.setMeta((IPersistentMap)const__41);
        Var var18 = var17;
        var17.bindRoot(((IFn)const__30.getRawRoot()).invoke(const__42.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{"clusterfs-chunk", ((IObj)new clusterfs$reify__14238(null)).withMeta((IPersistentMap)const__45), "clusterfs-root", ((IObj)new clusterfs$reify__14240(null)).withMeta((IPersistentMap)const__47)})));
        Var var19 = const__48;
        var19.setMeta((IPersistentMap)const__50);
        Var var20 = var19;
        var19.bindRoot((Object)new clusterfs$fressian_chunk_from_channel());
        Var var21 = const__51;
        var21.setMeta((IPersistentMap)const__53);
        Var var22 = var21;
        var21.bindRoot((Object)new clusterfs$create_file());
        Var var23 = const__54;
        var23.setMeta((IPersistentMap)const__56);
        Var var24 = var23;
        var23.bindRoot((Object)new clusterfs$create_files());
        Var var25 = const__57;
        var25.setMeta((IPersistentMap)const__59);
        Var var26 = var25;
        var25.bindRoot((Object)new clusterfs$describe());
        Var var27 = const__60;
        var27.setMeta((IPersistentMap)const__62);
        Var var28 = var27;
        var27.bindRoot((Object)new clusterfs$create_fs());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.clusterfs");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.clusterfs", (String)"chunk-path");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"path"), (Object)Symbol.intern(null, (String)"chunk")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.clusterfs", (String)"floor");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"num"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.clusterfs", (String)"ceil");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"num"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.clusterfs", (String)"files");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"clusterfs")))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.clusterfs", (String)"file-chunk-keys");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"clusterfs")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClusterFS")})), (Object)Symbol.intern(null, (String)"file")))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.clusterfs", (String)"chunk-keys");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"clusterfs"), (Object)Symbol.intern(null, (String)"files")))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.clusterfs", (String)"all-keys");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"clusterfs")))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.clusterfs", (String)"write-handlers");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__30 = RT.var((String)"clojure.core", (String)"merge");
        const__31 = RT.var((String)"datomic.fressian", (String)"clojure-write-handlers");
        const__32 = RT.classForName((String)"datomic.clusterfs.Chunk");
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 74, RT.keyword(null, (String)"column"), 12});
        const__37 = RT.classForName((String)"datomic.clusterfs.ClusterFS");
        const__39 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 81, RT.keyword(null, (String)"column"), 12});
        const__40 = RT.var((String)"datomic.clusterfs", (String)"read-handlers");
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__42 = RT.var((String)"datomic.fressian", (String)"clojure-read-handlers");
        const__45 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 91, RT.keyword(null, (String)"column"), 11});
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 94, RT.keyword(null, (String)"column"), 11});
        const__48 = RT.var((String)"datomic.clusterfs", (String)"fressian-chunk-from-channel");
        const__50 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"rc"), (Object)Symbol.intern(null, (String)"n")))), RT.keyword(null, (String)"column"), 1});
        const__51 = RT.var((String)"datomic.clusterfs", (String)"create-file");
        const__53 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cs"), (Object)Symbol.intern(null, (String)"local-file"), (Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"chunk-size"))})))), RT.keyword(null, (String)"column"), 1});
        const__54 = RT.var((String)"datomic.clusterfs", (String)"create-files");
        const__56 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cs"), (Object)Symbol.intern(null, (String)"files"), (Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"chunk-size"))})))), RT.keyword(null, (String)"column"), 1});
        const__57 = RT.var((String)"datomic.clusterfs", (String)"describe");
        const__59 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cfs")))), RT.keyword(null, (String)"column"), 1});
        const__60 = RT.var((String)"datomic.clusterfs", (String)"create-fs");
        const__62 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cs"), (Object)Symbol.intern(null, (String)"files"), (Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"chunk-size"), (Object)Symbol.intern(null, (String)"base"))})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        clusterfs__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.clusterfs__init").getClassLoader());
        try {
            clusterfs__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

