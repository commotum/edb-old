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
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  com.datomic.lucene.util.Constants
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.datomic.lucene.util.Constants;
import datomic.lucene$add_document;
import datomic.lucene$add_documents;
import datomic.lucene$binary_field;
import datomic.lucene$boolean_query;
import datomic.lucene$create_config;
import datomic.lucene$document;
import datomic.lucene$escape_query;
import datomic.lucene$field_stream;
import datomic.lucene$fn__12228;
import datomic.lucene$fn__12230;
import datomic.lucene$fn__12272;
import datomic.lucene$fn__12275;
import datomic.lucene$fn__12286;
import datomic.lucene$fn__12288;
import datomic.lucene$fs_directory;
import datomic.lucene$get_field;
import datomic.lucene$index_searcher;
import datomic.lucene$index_writer;
import datomic.lucene$loading__6434__auto____12226;
import datomic.lucene$long_field;
import datomic.lucene$long_query;
import datomic.lucene$long_value;
import datomic.lucene$multi_reader;
import datomic.lucene$parse_query;
import datomic.lucene$persistent_directory;
import datomic.lucene$ram_directory;
import datomic.lucene$read_only_clone;
import datomic.lucene$search_seq;
import datomic.lucene$string_field;
import datomic.lucene$string_value;
import datomic.lucene$term_enum_seq;
import datomic.lucene$term_from_tokenizer;
import datomic.lucene$term_query;
import datomic.lucene$tokenize_terms;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class lucene__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__12;
    public static final Var const__13;
    public static final Object const__14;
    public static final AFn const__16;
    public static final Var const__17;
    public static final AFn const__19;
    public static final Var const__20;
    public static final Object const__21;
    public static final AFn const__23;
    public static final Var const__24;
    public static final AFn const__27;
    public static final Var const__28;
    public static final AFn const__31;
    public static final Var const__32;
    public static final AFn const__34;
    public static final Var const__35;
    public static final AFn const__38;
    public static final Var const__39;
    public static final AFn const__41;
    public static final Var const__42;
    public static final AFn const__45;
    public static final Var const__46;
    public static final AFn const__48;
    public static final Var const__49;
    public static final AFn const__52;
    public static final Var const__53;
    public static final AFn const__55;
    public static final Var const__56;
    public static final AFn const__58;
    public static final Object const__59;
    public static final Var const__60;
    public static final Var const__61;
    public static final Var const__62;
    public static final Keyword const__63;
    public static final Var const__64;
    public static final ISeq const__65;
    public static final Var const__66;
    public static final Var const__67;
    public static final AFn const__71;
    public static final Keyword const__72;
    public static final AFn const__73;
    public static final Keyword const__74;
    public static final Keyword const__75;
    public static final Keyword const__76;
    public static final AFn const__77;
    public static final Keyword const__78;
    public static final Var const__79;
    public static final Var const__80;
    public static final Var const__81;
    public static final AFn const__82;
    public static final AFn const__83;
    public static final Keyword const__84;
    public static final Var const__85;
    public static final AFn const__86;
    public static final Var const__87;
    public static final Var const__88;
    public static final AFn const__90;
    public static final Var const__91;
    public static final AFn const__94;
    public static final Var const__95;
    public static final AFn const__97;
    public static final Var const__98;
    public static final AFn const__100;
    public static final Var const__101;
    public static final AFn const__104;
    public static final Var const__105;
    public static final AFn const__107;
    public static final Var const__108;
    public static final AFn const__110;
    public static final Var const__111;
    public static final AFn const__113;
    public static final Var const__114;
    public static final AFn const__116;
    public static final Var const__117;
    public static final AFn const__119;
    public static final Var const__120;
    public static final AFn const__122;
    public static final Var const__123;
    public static final AFn const__125;
    public static final Var const__126;
    public static final AFn const__128;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new lucene$loading__6434__auto____12226()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new lucene$fn__12228())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = Constants.LUCENE_MAIN_VERSION.startsWith("3") ? null : ((IFn)new lucene$fn__12230()).invoke();
        Var var = const__4;
        var.setMeta((IPersistentMap)const__12);
        Var var2 = var;
        var.bindRoot((Object)new lucene$create_config());
        Var var3 = const__13;
        var3.setMeta((IPersistentMap)const__16);
        Var var4 = var3;
        var3.bindRoot((Object)new lucene$fs_directory());
        Var var5 = const__17;
        var5.setMeta((IPersistentMap)const__19);
        Var var6 = var5;
        var5.bindRoot((Object)new lucene$ram_directory());
        Var var7 = const__20;
        var7.setMeta((IPersistentMap)const__23);
        Var var8 = var7;
        var7.bindRoot((Object)new lucene$index_writer());
        Var var9 = const__24;
        var9.setMeta((IPersistentMap)const__27);
        Var var10 = var9;
        var9.bindRoot((Object)new lucene$string_field());
        Var var11 = const__28;
        var11.setMeta((IPersistentMap)const__31);
        Var var12 = var11;
        var11.bindRoot((Object)new lucene$long_field());
        Var var13 = const__32;
        var13.setMeta((IPersistentMap)const__34);
        Var var14 = var13;
        var13.bindRoot((Object)new lucene$binary_field());
        Var var15 = const__35;
        var15.setMeta((IPersistentMap)const__38);
        Var var16 = var15;
        var15.bindRoot((Object)new lucene$string_value());
        Var var17 = const__39;
        var17.setMeta((IPersistentMap)const__41);
        Var var18 = var17;
        var17.bindRoot((Object)new lucene$long_value());
        Var var19 = const__42;
        var19.setMeta((IPersistentMap)const__45);
        Var var20 = var19;
        var19.bindRoot((Object)new lucene$long_query());
        Var var21 = const__46;
        var21.setMeta((IPersistentMap)const__48);
        Var var22 = var21;
        var21.bindRoot((Object)new lucene$boolean_query());
        Class clazz = ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"com.datomic.lucene.search.BooleanClause$Occur"));
        Var var23 = const__49;
        var23.setMeta((IPersistentMap)const__52);
        Var var24 = var23;
        var23.bindRoot((Object)new lucene$field_stream());
        Var var25 = const__53;
        var25.setMeta((IPersistentMap)const__55);
        Var var26 = var25;
        var25.bindRoot((Object)new lucene$read_only_clone());
        Var var27 = const__56;
        var27.setMeta((IPersistentMap)const__58);
        Var var28 = var27;
        var27.bindRoot((Object)new lucene$get_field());
        Object object5 = ((IFn)new lucene$fn__12272()).invoke();
        Object object6 = const__59;
        Object object7 = ((IFn)const__60.getRawRoot()).invoke((Object)const__61, const__62.getRawRoot(), (Object)const__63, null);
        Object object8 = ((IFn)const__64).invoke((Object)const__61, (Object)const__65);
        Object object9 = ((IFn)const__66.getRawRoot()).invoke((Object)const__61, const__67.getRawRoot(), ((IFn)const__62.getRawRoot()).invoke((Object)const__71, (Object)const__72, (Object)const__73, (Object)const__74, (Object)const__61, (Object)const__75, (Object)const__77, (Object)const__78, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__79.getRawRoot()).invoke(const__80.get(), ((IFn)const__81.getRawRoot()).invoke((Object)const__82, ((IFn)const__67.getRawRoot()).invoke((Object)const__83, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__84, const__61})))), new lucene$fn__12275()})));
        Object object10 = ((IFn)const__85.getRawRoot()).invoke(const__61.getRawRoot());
        AFn aFn = const__86;
        Object object11 = ((IFn)const__87.getRawRoot()).invoke(const__14, const__61.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__76, new lucene$fn__12286()}));
        Object object12 = ((IFn)const__87.getRawRoot()).invoke(const__21, const__61.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__76, new lucene$fn__12288()}));
        Var var29 = const__88;
        var29.setMeta((IPersistentMap)const__90);
        Var var30 = var29;
        var29.bindRoot((Object)new lucene$multi_reader());
        Var var31 = const__91;
        var31.setMeta((IPersistentMap)const__94);
        Var var32 = var31;
        var31.bindRoot((Object)new lucene$index_searcher());
        Var var33 = const__95;
        var33.setMeta((IPersistentMap)const__97);
        Var var34 = var33;
        var33.bindRoot((Object)new lucene$persistent_directory());
        Var var35 = const__98;
        var35.setMeta((IPersistentMap)const__100);
        Var var36 = var35;
        var35.bindRoot((Object)new lucene$search_seq());
        Var var37 = const__101;
        var37.setMeta((IPersistentMap)const__104);
        Var var38 = var37;
        var37.bindRoot((Object)new lucene$document());
        Var var39 = const__105;
        var39.setMeta((IPersistentMap)const__107);
        Var var40 = var39;
        var39.bindRoot((Object)new lucene$add_document());
        Var var41 = const__108;
        var41.setMeta((IPersistentMap)const__110);
        Var var42 = var41;
        var41.bindRoot((Object)new lucene$add_documents());
        Var var43 = const__111;
        var43.setMeta((IPersistentMap)const__113);
        Var var44 = var43;
        var43.bindRoot((Object)new lucene$term_query());
        Var var45 = const__114;
        var45.setMeta((IPersistentMap)const__116);
        Var var46 = var45;
        var45.bindRoot((Object)new lucene$term_enum_seq());
        Var var47 = const__117;
        var47.setMeta((IPersistentMap)const__119);
        Var var48 = var47;
        var47.bindRoot((Object)new lucene$term_from_tokenizer());
        Var var49 = const__120;
        var49.setMeta((IPersistentMap)const__122);
        Var var50 = var49;
        var49.bindRoot((Object)new lucene$tokenize_terms());
        Var var51 = const__123;
        var51.setMeta((IPersistentMap)const__125);
        Var var52 = var51;
        var51.bindRoot((Object)new lucene$escape_query());
        Var var53 = const__126;
        var53.setMeta((IPersistentMap)const__128);
        Var var54 = var53;
        var53.bindRoot((Object)new lucene$parse_query());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.lucene");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.lucene", (String)"create-config");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"com.datomic.lucene.index.IndexWriterConfig"), RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"version"), (Object)Symbol.intern(null, (String)"analyzer"))})))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.lucene", (String)"fs-directory");
        const__14 = RT.classForName((String)"com.datomic.lucene.store.Directory");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"com.datomic.lucene.store.Directory"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"column"), 1});
        const__17 = RT.var((String)"datomic.lucene", (String)"ram-directory");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"com.datomic.lucene.store.Directory"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__20 = RT.var((String)"datomic.lucene", (String)"index-writer");
        const__21 = RT.classForName((String)"com.datomic.lucene.index.IndexWriter");
        const__23 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"com.datomic.lucene.index.IndexWriter"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"directory")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Directory")})), (Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"version"), (Object)Symbol.intern(null, (String)"analyzer-fn"))})))), RT.keyword(null, (String)"column"), 1});
        const__24 = RT.var((String)"datomic.lucene", (String)"string-field");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"com.datomic.lucene.document.Field"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"name")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})), (Object)((IObj)Symbol.intern(null, (String)"value")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})), (Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"index"), (Object)Symbol.intern(null, (String)"store"), (Object)Symbol.intern(null, (String)"analyze"), (Object)Symbol.intern(null, (String)"omit-norms")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"opts")})))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.lucene", (String)"long-field");
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"com.datomic.lucene.document.NumericField"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"name")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})), (Object)((IObj)Symbol.intern(null, (String)"value")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")}))))), RT.keyword(null, (String)"column"), 1});
        const__32 = RT.var((String)"datomic.lucene", (String)"binary-field");
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"com.datomic.lucene.document.Field"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"name")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})), (Object)((IObj)Symbol.intern(null, (String)"value")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"bytes")})), (Object)((IObj)Symbol.intern(null, (String)"offset")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), (Object)((IObj)Symbol.intern(null, (String)"length")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")}))))), RT.keyword(null, (String)"column"), 1});
        const__35 = RT.var((String)"datomic.lucene", (String)"string-value");
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"java.lang.String"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"f")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Field")}))))), RT.keyword(null, (String)"column"), 1});
        const__39 = RT.var((String)"datomic.lucene", (String)"long-value");
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"f")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"NumericField")}))))), RT.keyword(null, (String)"column"), 1});
        const__42 = RT.var((String)"datomic.lucene", (String)"long-query");
        const__45 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"com.datomic.lucene.search.Query"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"l")))), RT.keyword(null, (String)"column"), 1});
        const__46 = RT.var((String)"datomic.lucene", (String)"boolean-query");
        const__48 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"com.datomic.lucene.search.Query"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"queryoccurs")))), RT.keyword(null, (String)"column"), 1});
        const__49 = RT.var((String)"datomic.lucene", (String)"field-stream");
        const__52 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"java.io.InputStream"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"field")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Field")}))))), RT.keyword(null, (String)"column"), 1});
        const__53 = RT.var((String)"datomic.lucene", (String)"read-only-clone");
        const__55 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"rdr")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IndexReader")}))))), RT.keyword(null, (String)"column"), 1});
        const__56 = RT.var((String)"datomic.lucene", (String)"get-field");
        const__58 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"doc")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Document")})), (Object)Symbol.intern(null, (String)"s")))), RT.keyword(null, (String)"column"), 1});
        const__59 = RT.classForName((String)"datomic.lucene.Readerable");
        const__60 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__61 = RT.var((String)"datomic.lucene", (String)"Readerable");
        const__62 = RT.var((String)"clojure.core", (String)"assoc");
        const__63 = RT.keyword(null, (String)"doc");
        const__64 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__65 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"index-reader")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__66 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__67 = RT.var((String)"clojure.core", (String)"merge");
        const__71 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.lucene.Readerable"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.lucene.Readerable")});
        const__72 = RT.keyword(null, (String)"sigs");
        const__73 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"index-reader"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"index-reader")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Returns a reader which must be closed."})});
        const__74 = RT.keyword(null, (String)"var");
        const__75 = RT.keyword(null, (String)"method-map");
        const__76 = RT.keyword(null, (String)"index-reader");
        const__77 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"index-reader"), RT.keyword(null, (String)"index-reader")});
        const__78 = RT.keyword(null, (String)"method-builders");
        const__79 = RT.var((String)"clojure.core", (String)"intern");
        const__80 = RT.var((String)"clojure.core", (String)"*ns*");
        const__81 = RT.var((String)"clojure.core", (String)"with-meta");
        const__82 = (AFn)((IObj)Symbol.intern(null, (String)"index-reader")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__83 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"index-reader")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Returns a reader which must be closed."});
        const__84 = RT.keyword(null, (String)"protocol");
        const__85 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__86 = (AFn)Symbol.intern(null, (String)"Readerable");
        const__87 = RT.var((String)"clojure.core", (String)"extend");
        const__88 = RT.var((String)"datomic.lucene", (String)"multi-reader");
        const__90 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"rdrs"), (Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"close-subreaders"))}))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.datomic.lucene.index.IndexReader")})))), RT.keyword(null, (String)"column"), 1});
        const__91 = RT.var((String)"datomic.lucene", (String)"index-searcher");
        const__94 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"com.datomic.lucene.search.IndexSearcher"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"rdr")))), RT.keyword(null, (String)"column"), 1});
        const__95 = RT.var((String)"datomic.lucene", (String)"persistent-directory");
        const__97 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"pdmap")))), RT.keyword(null, (String)"column"), 1});
        const__98 = RT.var((String)"datomic.lucene", (String)"search-seq");
    }

    public static void __init1() {
        const__100 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"searcher")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IndexSearcher")})), (Object)((IObj)Symbol.intern(null, (String)"query")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Query")})), (Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"max")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"max"), 10000L})})))), RT.keyword(null, (String)"column"), 1});
        const__101 = RT.var((String)"datomic.lucene", (String)"document");
        const__104 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"com.datomic.lucene.document.Document"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"fields")))), RT.keyword(null, (String)"column"), 1});
        const__105 = RT.var((String)"datomic.lucene", (String)"add-document");
        const__107 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"writer")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IndexWriter")})), (Object)((IObj)Symbol.intern(null, (String)"doc")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Document")}))))), RT.keyword(null, (String)"column"), 1});
        const__108 = RT.var((String)"datomic.lucene", (String)"add-documents");
        const__110 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"writer")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IndexWriter")})), (Object)Symbol.intern(null, (String)"docs")))), RT.keyword(null, (String)"column"), 1});
        const__111 = RT.var((String)"datomic.lucene", (String)"term-query");
        const__113 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"com.datomic.lucene.search.Query"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"field"), (Object)Symbol.intern(null, (String)"term")))), RT.keyword(null, (String)"column"), 1});
        const__114 = RT.var((String)"datomic.lucene", (String)"term-enum-seq");
        const__116 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"te")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"TermEnum")}))))), RT.keyword(null, (String)"column"), 1});
        const__117 = RT.var((String)"datomic.lucene", (String)"term-from-tokenizer");
        const__119 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"t")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"TokenStream")}))))), RT.keyword(null, (String)"column"), 1});
        const__120 = RT.var((String)"datomic.lucene", (String)"tokenize-terms");
        const__122 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"s")))), RT.keyword(null, (String)"column"), 1});
        const__123 = RT.var((String)"datomic.lucene", (String)"escape-query");
        const__125 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"s")))), RT.keyword(null, (String)"column"), 1});
        const__126 = RT.var((String)"datomic.lucene", (String)"parse-query");
        const__128 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"com.datomic.lucene.search.Query"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"s"), (Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"analyzer"))})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        lucene__init.__init0();
        lucene__init.__init1();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.lucene__init").getClassLoader());
        try {
            lucene__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

