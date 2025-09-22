/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
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
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.fressian$as_lookup;
import datomic.fressian$begin_closed_list;
import datomic.fressian$begin_open_list;
import datomic.fressian$byte_buf;
import datomic.fressian$create_reader;
import datomic.fressian$create_writer;
import datomic.fressian$defressian;
import datomic.fressian$end_list;
import datomic.fressian$fn__12162;
import datomic.fressian$fn__12181;
import datomic.fressian$fn__12215;
import datomic.fressian$fressian;
import datomic.fressian$fressian_val;
import datomic.fressian$fressianable_QMARK_;
import datomic.fressian$loading__6434__auto____11922;
import datomic.fressian$read_batch;
import datomic.fressian$read_seq;
import datomic.fressian$reader_iter;
import datomic.fressian$record_latencies;
import datomic.fressian$reify__12191;
import datomic.fressian$reify__12193;
import datomic.fressian$reify__12195;
import datomic.fressian$reify__12197;
import datomic.fressian$reify__12199;
import datomic.fressian$reify__12201;
import datomic.fressian$reify__12203;
import datomic.fressian$reify__12205;
import datomic.fressian$val__GT_obj;
import datomic.fressian$write_handler_lookup;
import datomic.fressian$write_named;
import datomic.impl.Config;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class fressian__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__12;
    public static final Var const__13;
    public static final AFn const__17;
    public static final Var const__18;
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
    public static final AFn const__40;
    public static final Var const__41;
    public static final AFn const__43;
    public static final Var const__44;
    public static final Object const__45;
    public static final Var const__46;
    public static final Keyword const__47;
    public static final Var const__48;
    public static final AFn const__50;
    public static final Var const__51;
    public static final AFn const__53;
    public static final Var const__54;
    public static final AFn const__56;
    public static final Var const__57;
    public static final AFn const__58;
    public static final Object const__59;
    public static final AFn const__63;
    public static final Object const__64;
    public static final AFn const__67;
    public static final Object const__68;
    public static final AFn const__70;
    public static final Var const__71;
    public static final AFn const__72;
    public static final AFn const__74;
    public static final AFn const__76;
    public static final AFn const__78;
    public static final Var const__79;
    public static final AFn const__80;
    public static final Var const__81;
    public static final Object const__82;
    public static final AFn const__85;
    public static final Var const__86;
    public static final AFn const__87;
    public static final AFn const__89;
    public static final Var const__90;
    public static final AFn const__93;
    public static final Var const__94;
    public static final AFn const__96;
    public static final Var const__97;
    public static final AFn const__99;
    public static final Var const__100;
    public static final AFn const__102;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new fressian$loading__6434__auto____11922()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new fressian$fn__12162())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new fressian$as_lookup());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new fressian$write_handler_lookup());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__17);
        Var var6 = var5;
        var5.bindRoot((Object)new fressian$create_writer());
        Var var7 = const__18;
        var7.setMeta((IPersistentMap)const__21);
        Var var8 = var7;
        var7.bindRoot((Object)new fressian$create_reader());
        Var var9 = const__22;
        var9.setMeta((IPersistentMap)const__24);
        Var var10 = var9;
        var9.bindRoot((Object)new fressian$begin_open_list());
        Var var11 = const__25;
        var11.setMeta((IPersistentMap)const__27);
        Var var12 = var11;
        var11.bindRoot((Object)new fressian$begin_closed_list());
        Var var13 = const__28;
        var13.setMeta((IPersistentMap)const__30);
        Var var14 = var13;
        var13.bindRoot((Object)new fressian$end_list());
        Var var15 = const__31;
        var15.setMeta((IPersistentMap)const__33);
        Var var16 = var15;
        var15.bindRoot((Object)new fressian$fressian());
        Var var17 = const__34;
        var17.setMeta((IPersistentMap)const__36);
        Var var18 = var17;
        var17.bindRoot((Object)new fressian$defressian());
        Var var19 = const__37;
        var19.setMeta((IPersistentMap)const__40);
        Var var20 = var19;
        var19.bindRoot((Object)new fressian$byte_buf());
        Var var21 = const__41;
        var21.setMeta((IPersistentMap)const__43);
        Var var22 = var21;
        var21.bindRoot((Object)new fressian$fressian_val());
        Object object4 = ((IFn)const__44.getRawRoot()).invoke(const__45, const__46.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__47, new fressian$fn__12181()}));
        Var var23 = const__48;
        var23.setMeta((IPersistentMap)const__50);
        Var var24 = var23;
        var23.bindRoot((Object)new fressian$read_batch());
        Var var25 = const__51;
        var25.setMeta((IPersistentMap)const__53);
        Var var26 = var25;
        var25.bindRoot((Object)new fressian$read_seq());
        Var var27 = const__54;
        var27.setMeta((IPersistentMap)const__56);
        Var var28 = var27;
        var27.bindRoot((Object)new fressian$write_named());
        Var var29 = const__57;
        var29.setMeta((IPersistentMap)const__58);
        Var var30 = var29;
        var29.bindRoot((Object)RT.mapUniqueKeys((Object[])new Object[]{const__59, RT.mapUniqueKeys((Object[])new Object[]{"key", ((IObj)new fressian$reify__12191(null)).withMeta((IPersistentMap)const__63)}), const__64, RT.mapUniqueKeys((Object[])new Object[]{"bigint", ((IObj)new fressian$reify__12193(null)).withMeta((IPersistentMap)const__67)}), const__68, RT.mapUniqueKeys((Object[])new Object[]{"sym", ((IObj)new fressian$reify__12195(null)).withMeta((IPersistentMap)const__70)})}));
        Var var31 = const__71;
        var31.setMeta((IPersistentMap)const__72);
        Var var32 = var31;
        var31.bindRoot((Object)RT.mapUniqueKeys((Object[])new Object[]{"key", ((IObj)new fressian$reify__12197(null)).withMeta((IPersistentMap)const__74), "sym", ((IObj)new fressian$reify__12199(null)).withMeta((IPersistentMap)const__76), "map", ((IObj)new fressian$reify__12201(null)).withMeta((IPersistentMap)const__78)}));
        Var var33 = const__79;
        var33.setMeta((IPersistentMap)const__80);
        Var var34 = var33;
        var33.bindRoot(((IFn)const__81.getRawRoot()).invoke(const__57.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__82, RT.mapUniqueKeys((Object[])new Object[]{"datomic/fn", ((IObj)new fressian$reify__12203(null)).withMeta((IPersistentMap)const__85)})}), Config.getWriteHandlers()));
        Var var35 = const__86;
        var35.setMeta((IPersistentMap)const__87);
        Var var36 = var35;
        var35.bindRoot(((IFn)const__81.getRawRoot()).invoke(const__71.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{"datomic/fn", ((IObj)new fressian$reify__12205(null)).withMeta((IPersistentMap)const__89)}), Config.getReadHandlers()));
        Var var37 = const__90;
        var37.setMeta((IPersistentMap)const__93);
        Var var38 = var37;
        var37.bindRoot((Object)new fressian$record_latencies());
        Var var39 = const__94;
        var39.setMeta((IPersistentMap)const__96);
        Var var40 = var39;
        var39.bindRoot((Object)new fressian$val__GT_obj());
        Object object5 = ((IFn)new fressian$fn__12215()).invoke();
        Var var41 = const__97;
        var41.setMeta((IPersistentMap)const__99);
        Var var42 = var41;
        var41.bindRoot((Object)new fressian$reader_iter());
        Var var43 = const__100;
        var43.setMeta((IPersistentMap)const__102);
        Var var44 = var43;
        var43.bindRoot((Object)new fressian$fressianable_QMARK_());
        Object v50 = null;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.fressian");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.fressian", (String)"as-lookup");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"o")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.fressian", (String)"write-handler-lookup");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"custom-lookup")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.fressian", (String)"create-writer");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"org.fressian.Writer"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"out")), Tuple.create((Object)Symbol.intern(null, (String)"out"), (Object)Symbol.intern(null, (String)"lookup")))), RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"datomic.fressian", (String)"create-reader");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"org.fressian.Reader"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"in")), Tuple.create((Object)Symbol.intern(null, (String)"in"), (Object)Symbol.intern(null, (String)"lookup")), Tuple.create((Object)Symbol.intern(null, (String)"in"), (Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"validate-checksum")))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.fressian", (String)"begin-open-list");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"writer")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"StreamingWriter")}))))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.fressian", (String)"begin-closed-list");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"writer")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"StreamingWriter")}))))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.fressian", (String)"end-list");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"writer")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"StreamingWriter")}))))), RT.keyword(null, (String)"column"), 1});
        const__31 = RT.var((String)"datomic.fressian", (String)"fressian");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"out"), (Object)Symbol.intern(null, (String)"obj"), (Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"handlers"), (Object)Symbol.intern(null, (String)"footer"))})))), RT.keyword(null, (String)"column"), 1});
        const__34 = RT.var((String)"datomic.fressian", (String)"defressian");
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"in"), (Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"handlers"), (Object)Symbol.intern(null, (String)"footer"))})))), RT.keyword(null, (String)"column"), 1});
        const__37 = RT.var((String)"datomic.fressian", (String)"byte-buf");
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"java.nio.ByteBuffer"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"obj"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"options")))), RT.keyword(null, (String)"column"), 1});
        const__41 = RT.var((String)"datomic.fressian", (String)"fressian-val");
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"val"), (Object)Symbol.intern(null, (String)"handlers")))), RT.keyword(null, (String)"column"), 1});
        const__44 = RT.var((String)"clojure.core", (String)"extend");
        const__45 = RT.classForName((String)"org.fressian.FressianReader");
        const__46 = RT.var((String)"datomic.queue", (String)"BlockingConsumer");
        const__47 = RT.keyword(null, (String)"take");
        const__48 = RT.var((String)"datomic.fressian", (String)"read-batch");
        const__50 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"fin")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Reader")}))))), RT.keyword(null, (String)"column"), 1});
        const__51 = RT.var((String)"datomic.fressian", (String)"read-seq");
        const__53 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"readable")), Tuple.create((Object)Symbol.intern(null, (String)"readable"), (Object)Symbol.intern(null, (String)"handler-lookup")))), RT.keyword(null, (String)"column"), 1});
        const__54 = RT.var((String)"datomic.fressian", (String)"write-named");
        const__56 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"tag"), (Object)((IObj)Symbol.intern(null, (String)"w")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Writer")})), (Object)Symbol.intern(null, (String)"s")))), RT.keyword(null, (String)"column"), 1});
        const__57 = RT.var((String)"datomic.fressian", (String)"clojure-write-handlers");
        const__58 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__59 = RT.classForName((String)"clojure.lang.Keyword");
        const__63 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 160, RT.keyword(null, (String)"column"), 5});
        const__64 = RT.classForName((String)"clojure.lang.BigInt");
        const__67 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 164, RT.keyword(null, (String)"column"), 4});
        const__68 = RT.classForName((String)"clojure.lang.Symbol");
        const__70 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 173, RT.keyword(null, (String)"column"), 4});
        const__71 = RT.var((String)"datomic.fressian", (String)"clojure-read-handlers");
        const__72 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__74 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 178, RT.keyword(null, (String)"column"), 4});
        const__76 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 181, RT.keyword(null, (String)"column"), 4});
        const__78 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 184, RT.keyword(null, (String)"column"), 4});
        const__79 = RT.var((String)"datomic.fressian", (String)"user-write-handlers");
        const__80 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__81 = RT.var((String)"clojure.core", (String)"merge");
        const__82 = RT.classForName((String)"datomic.function.Function");
        const__85 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 195, RT.keyword(null, (String)"column"), 9});
        const__86 = RT.var((String)"datomic.fressian", (String)"user-read-handlers");
        const__87 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__89 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 205, RT.keyword(null, (String)"column"), 4});
        const__90 = RT.var((String)"datomic.fressian", (String)"record-latencies");
        const__93 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"nanos"), (Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"column"), 1});
        const__94 = RT.var((String)"datomic.fressian", (String)"val->obj");
        const__96 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"read-lookup")))), RT.keyword(null, (String)"column"), 1});
        const__97 = RT.var((String)"datomic.fressian", (String)"reader-iter");
        const__99 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"is"), (Object)Symbol.intern(null, (String)"handlers")))), RT.keyword(null, (String)"column"), 1});
    }

    public static void __init1() {
        const__100 = RT.var((String)"datomic.fressian", (String)"fressianable?");
        const__102 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"val")), Tuple.create((Object)Symbol.intern(null, (String)"val"), (Object)Symbol.intern(null, (String)"handlers")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        fressian__init.__init0();
        fressian__init.__init1();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.fressian__init").getClassLoader());
        try {
            fressian__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

