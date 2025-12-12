/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.PersistentArrayMap
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
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.garbage$append_leaf;
import datomic.garbage$create_garbage_node;
import datomic.garbage$deleted_db_cluster_conf;
import datomic.garbage$dir_seq;
import datomic.garbage$do_mark_garbage;
import datomic.garbage$ensure_root;
import datomic.garbage$ensure_root_ref;
import datomic.garbage$flush_garbage;
import datomic.garbage$fn__19762;
import datomic.garbage$gc;
import datomic.garbage$gc_delete_vals;
import datomic.garbage$gc_deleted_db;
import datomic.garbage$gc_deleted_dbs;
import datomic.garbage$gc_dir;
import datomic.garbage$gc_get_node;
import datomic.garbage$gc_leaf;
import datomic.garbage$get_db_ids;
import datomic.garbage$install_mark_handler;
import datomic.garbage$leaf_seq;
import datomic.garbage$loading__6434__auto____19655;
import datomic.garbage$mark_garbage;
import datomic.garbage$pace_gc;
import datomic.garbage$pending_garbage_count;
import datomic.garbage$queue_gc;
import datomic.garbage$root_ref_key;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class garbage__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__11;
    public static final Object const__12;
    public static final Var const__13;
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
    public static final AFn const__37;
    public static final Var const__38;
    public static final AFn const__40;
    public static final Var const__41;
    public static final AFn const__43;
    public static final Var const__44;
    public static final AFn const__46;
    public static final Var const__47;
    public static final AFn const__49;
    public static final Var const__50;
    public static final AFn const__51;
    public static final Var const__52;
    public static final Var const__53;
    public static final Var const__54;
    public static final AFn const__56;
    public static final Var const__57;
    public static final AFn const__59;
    public static final Var const__60;
    public static final AFn const__62;
    public static final Var const__63;
    public static final AFn const__65;
    public static final Var const__66;
    public static final AFn const__68;
    public static final Var const__69;
    public static final AFn const__71;
    public static final Var const__72;
    public static final AFn const__73;
    public static final Var const__74;
    public static final AFn const__76;
    public static final Var const__77;
    public static final AFn const__79;
    public static final Var const__80;
    public static final AFn const__82;
    public static final Var const__83;
    public static final AFn const__85;
    public static final Var const__86;
    public static final AFn const__88;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new garbage$loading__6434__auto____19655()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new garbage$fn__19762())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new garbage$root_ref_key());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__11);
        Var var4 = var3;
        var3.bindRoot(const__12);
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__16);
        Var var6 = var5;
        var5.bindRoot((Object)new garbage$create_garbage_node());
        Var var7 = const__17;
        var7.setMeta((IPersistentMap)const__19);
        Var var8 = var7;
        var7.bindRoot((Object)new garbage$dir_seq());
        Var var9 = const__20;
        var9.setMeta((IPersistentMap)const__22);
        Var var10 = var9;
        var9.bindRoot((Object)new garbage$leaf_seq());
        Var var11 = const__23;
        var11.setMeta((IPersistentMap)const__25);
        Var var12 = var11;
        var11.bindRoot((Object)new garbage$ensure_root_ref());
        Var var13 = const__26;
        var13.setMeta((IPersistentMap)const__28);
        Var var14 = var13;
        var13.bindRoot((Object)new garbage$ensure_root());
        Var var15 = const__29;
        var15.setMeta((IPersistentMap)const__31);
        Var var16 = var15;
        var15.bindRoot((Object)new garbage$append_leaf());
        Var var17 = const__32;
        var17.setMeta((IPersistentMap)const__33);
        Var var18 = var17;
        var17.bindRoot(((IFn)const__34.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY));
        Var var19 = const__35;
        var19.setMeta((IPersistentMap)const__37);
        Var var20 = var19;
        var19.bindRoot((Object)new garbage$do_mark_garbage());
        Var var21 = const__38;
        var21.setMeta((IPersistentMap)const__40);
        Var var22 = var21;
        var21.bindRoot((Object)new garbage$mark_garbage());
        Var var23 = const__41;
        var23.setMeta((IPersistentMap)const__43);
        Var var24 = var23;
        var23.bindRoot((Object)new garbage$install_mark_handler());
        Var var25 = const__44;
        var25.setMeta((IPersistentMap)const__46);
        Var var26 = var25;
        var25.bindRoot((Object)new garbage$flush_garbage());
        Var var27 = const__47;
        var27.setMeta((IPersistentMap)const__49);
        Var var28 = var27;
        var27.bindRoot((Object)new garbage$pending_garbage_count());
        Var var29 = const__50;
        var29.setMeta((IPersistentMap)const__51);
        Var var30 = var29;
        var29.bindRoot(((IFn)const__52.getRawRoot()).invoke(const__53.getRawRoot()));
        Var var31 = const__54;
        var31.setMeta((IPersistentMap)const__56);
        Var var32 = var31;
        var31.bindRoot((Object)new garbage$gc_get_node());
        Var var33 = const__57;
        var33.setMeta((IPersistentMap)const__59);
        Var var34 = var33;
        var33.bindRoot((Object)new garbage$pace_gc());
        Var var35 = const__60;
        var35.setMeta((IPersistentMap)const__62);
        Var var36 = var35;
        var35.bindRoot((Object)new garbage$gc_delete_vals());
        Var var37 = const__63;
        var37.setMeta((IPersistentMap)const__65);
        Var var38 = var37;
        var37.bindRoot((Object)new garbage$gc_leaf());
        Var var39 = const__66;
        var39.setMeta((IPersistentMap)const__68);
        Var var40 = var39;
        var39.bindRoot((Object)new garbage$gc_dir());
        Var var41 = const__69;
        var41.setMeta((IPersistentMap)const__71);
        Var var42 = var41;
        var41.bindRoot((Object)new garbage$gc());
        Var var43 = const__72;
        var43.setMeta((IPersistentMap)const__73);
        Var var44 = var43;
        var43.bindRoot(((IFn)const__34.getRawRoot()).invoke(null));
        Var var45 = const__74;
        var45.setMeta((IPersistentMap)const__76);
        Var var46 = var45;
        var45.bindRoot((Object)new garbage$queue_gc());
        Var var47 = const__77;
        var47.setMeta((IPersistentMap)const__79);
        Var var48 = var47;
        var47.bindRoot((Object)new garbage$gc_deleted_db());
        Var var49 = const__80;
        var49.setMeta((IPersistentMap)const__82);
        Var var50 = var49;
        var49.bindRoot((Object)new garbage$get_db_ids());
        Var var51 = const__83;
        var51.setMeta((IPersistentMap)const__85);
        Var var52 = var51;
        var51.bindRoot((Object)new garbage$deleted_db_cluster_conf());
        Var var53 = const__86;
        var53.setMeta((IPersistentMap)const__88);
        Var var54 = var53;
        var53.bindRoot((Object)new garbage$gc_deleted_dbs());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.garbage");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.garbage", (String)"root-ref-key");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.garbage", (String)"leaf-threshold");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__12 = 600L;
        const__13 = RT.var((String)"datomic.garbage", (String)"create-garbage-node");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"uuid"), (Object)Symbol.intern(null, (String)"o")))), RT.keyword(null, (String)"column"), 1});
        const__17 = RT.var((String)"datomic.garbage", (String)"dir-seq");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"root")))), RT.keyword(null, (String)"column"), 1});
        const__20 = RT.var((String)"datomic.garbage", (String)"leaf-seq");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"root")))), RT.keyword(null, (String)"column"), 1});
        const__23 = RT.var((String)"datomic.garbage", (String)"ensure-root-ref");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster")), Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"forget-garbage")))), RT.keyword(null, (String)"column"), 1});
        const__26 = RT.var((String)"datomic.garbage", (String)"ensure-root");
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"lookup")))), RT.keyword(null, (String)"column"), 1});
        const__29 = RT.var((String)"datomic.garbage", (String)"append-leaf");
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"leaf"), (Object)Symbol.intern(null, (String)"max-dir-size")))), RT.keyword(null, (String)"column"), 1});
        const__32 = RT.var((String)"datomic.garbage", (String)"garbage-agent");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__34 = RT.var((String)"clojure.core", (String)"agent");
        const__35 = RT.var((String)"datomic.garbage", (String)"do-mark-garbage");
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"ids"), (Object)Symbol.intern(null, (String)"max-leaf-size"), (Object)Symbol.intern(null, (String)"max-dir-size")))), RT.keyword(null, (String)"column"), 1});
        const__38 = RT.var((String)"datomic.garbage", (String)"mark-garbage");
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"ids")), Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"ids"), (Object)Symbol.intern(null, (String)"max-leaf-size"), (Object)Symbol.intern(null, (String)"max-dir-size")))), RT.keyword(null, (String)"column"), 1});
        const__41 = RT.var((String)"datomic.garbage", (String)"install-mark-handler");
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__44 = RT.var((String)"datomic.garbage", (String)"flush-garbage");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"olookup")))), RT.keyword(null, (String)"column"), 1});
        const__47 = RT.var((String)"datomic.garbage", (String)"pending-garbage-count");
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"gmap"), (Object)Symbol.intern(null, (String)"cluster")))), RT.keyword(null, (String)"column"), 1});
        const__50 = RT.var((String)"datomic.garbage", (String)"gc-val->obj");
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__52 = RT.var((String)"datomic.fressian", (String)"val->obj");
        const__53 = RT.var((String)"datomic.garbage.fressian", (String)"read-handlers");
        const__54 = RT.var((String)"datomic.garbage", (String)"gc-get-node");
        const__56 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"uuid")))), RT.keyword(null, (String)"column"), 1});
        const__57 = RT.var((String)"datomic.garbage", (String)"pace-gc");
        const__59 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__60 = RT.var((String)"datomic.garbage", (String)"gc-delete-vals");
        const__62 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"vs")))), RT.keyword(null, (String)"column"), 1});
        const__63 = RT.var((String)"datomic.garbage", (String)"gc-leaf");
        const__65 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"uuid"), (Object)Symbol.intern(null, (String)"tstamp")))), RT.keyword(null, (String)"column"), 1});
        const__66 = RT.var((String)"datomic.garbage", (String)"gc-dir");
        const__68 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"uuid"), (Object)Symbol.intern(null, (String)"tstamp")))), RT.keyword(null, (String)"column"), 1});
        const__69 = RT.var((String)"datomic.garbage", (String)"gc");
        const__71 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"tstamp")), Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"tstamp"), (Object)Symbol.intern(null, (String)"progress")))), RT.keyword(null, (String)"column"), 1});
        const__72 = RT.var((String)"datomic.garbage", (String)"collection-agent");
        const__73 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__74 = RT.var((String)"datomic.garbage", (String)"queue-gc");
        const__76 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"older-than")))), RT.keyword(null, (String)"column"), 1});
        const__77 = RT.var((String)"datomic.garbage", (String)"gc-deleted-db");
        const__79 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"system-cluster"), (Object)Symbol.intern(null, (String)"db-cluster"), (Object)Symbol.intern(null, (String)"status-callback")))), RT.keyword(null, (String)"column"), 1});
        const__80 = RT.var((String)"datomic.garbage", (String)"get-db-ids");
        const__82 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"uri")))), RT.keyword(null, (String)"column"), 1});
        const__83 = RT.var((String)"datomic.garbage", (String)"deleted-db-cluster-conf");
        const__85 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"uri"), (Object)Symbol.intern(null, (String)"id")))), RT.keyword(null, (String)"column"), 1});
        const__86 = RT.var((String)"datomic.garbage", (String)"gc-deleted-dbs");
        const__88 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"uri"))})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        garbage__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.garbage__init").getClassLoader());
        try {
            garbage__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

