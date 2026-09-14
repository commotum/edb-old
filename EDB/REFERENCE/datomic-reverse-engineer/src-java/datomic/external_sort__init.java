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
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.external_sort$chunk_seq;
import datomic.external_sort$file_system_sorter;
import datomic.external_sort$fn__14350;
import datomic.external_sort$fn__14358;
import datomic.external_sort$fn__14361;
import datomic.external_sort$fn__14372;
import datomic.external_sort$fn__14385;
import datomic.external_sort$fn__14416;
import datomic.external_sort$fn__14419;
import datomic.external_sort$fn__14430;
import datomic.external_sort$fn__14443;
import datomic.external_sort$fn__14459;
import datomic.external_sort$loading__6434__auto____14348;
import datomic.external_sort$next_chunk;
import datomic.external_sort$temp_file_io;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class external_sort__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Object const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final Var const__9;
    public static final ISeq const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final AFn const__16;
    public static final Keyword const__17;
    public static final AFn const__18;
    public static final Keyword const__19;
    public static final Keyword const__20;
    public static final AFn const__24;
    public static final Keyword const__25;
    public static final Var const__26;
    public static final Var const__27;
    public static final Var const__28;
    public static final AFn const__29;
    public static final AFn const__30;
    public static final Keyword const__31;
    public static final AFn const__32;
    public static final AFn const__33;
    public static final AFn const__34;
    public static final AFn const__35;
    public static final Var const__36;
    public static final AFn const__37;
    public static final Var const__38;
    public static final AFn const__43;
    public static final Var const__44;
    public static final AFn const__47;
    public static final Var const__48;
    public static final AFn const__50;
    public static final Object const__51;
    public static final Var const__52;
    public static final ISeq const__53;
    public static final AFn const__55;
    public static final AFn const__56;
    public static final AFn const__60;
    public static final AFn const__61;
    public static final AFn const__62;
    public static final AFn const__63;
    public static final AFn const__64;
    public static final AFn const__65;
    public static final AFn const__66;
    public static final AFn const__67;
    public static final Var const__68;
    public static final AFn const__70;
    public static final Object const__71;
    public static final Var const__72;
    public static final AFn const__74;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new external_sort$loading__6434__auto____14348()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new external_sort$fn__14350())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)new external_sort$fn__14358()).invoke();
        Object object5 = const__4;
        Object object6 = ((IFn)const__5.getRawRoot()).invoke((Object)const__6, const__7.getRawRoot(), (Object)const__8, null);
        Object object7 = ((IFn)const__9).invoke((Object)const__6, (Object)const__10);
        Object object8 = ((IFn)const__11.getRawRoot()).invoke((Object)const__6, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__16, (Object)const__17, (Object)const__18, (Object)const__19, (Object)const__6, (Object)const__20, (Object)const__24, (Object)const__25, (Object)RT.map((Object[])new Object[]{((IFn)const__26.getRawRoot()).invoke(const__27.get(), ((IFn)const__28.getRawRoot()).invoke((Object)const__29, ((IFn)const__12.getRawRoot()).invoke((Object)const__30, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__31, const__6})))), new external_sort$fn__14361(), ((IFn)const__26.getRawRoot()).invoke(const__27.get(), ((IFn)const__28.getRawRoot()).invoke((Object)const__32, ((IFn)const__12.getRawRoot()).invoke((Object)const__33, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__31, const__6})))), new external_sort$fn__14372(), ((IFn)const__26.getRawRoot()).invoke(const__27.get(), ((IFn)const__28.getRawRoot()).invoke((Object)const__34, ((IFn)const__12.getRawRoot()).invoke((Object)const__35, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__31, const__6})))), new external_sort$fn__14385()})));
        Object object9 = ((IFn)const__36.getRawRoot()).invoke(const__6.getRawRoot());
        AFn aFn = const__37;
        Var var = const__38;
        var.setMeta((IPersistentMap)const__43);
        Var var2 = var;
        var.bindRoot((Object)new external_sort$temp_file_io());
        Var var3 = const__44;
        var3.setMeta((IPersistentMap)const__47);
        Var var4 = var3;
        var3.bindRoot((Object)new external_sort$next_chunk());
        Var var5 = const__48;
        var5.setMeta((IPersistentMap)const__50);
        Var var6 = var5;
        var5.bindRoot((Object)new external_sort$chunk_seq());
        Object object10 = ((IFn)new external_sort$fn__14416()).invoke();
        Object object11 = const__51;
        Object object12 = ((IFn)const__5.getRawRoot()).invoke((Object)const__52, const__7.getRawRoot(), (Object)const__8, null);
        Object object13 = ((IFn)const__9).invoke((Object)const__52, (Object)const__53);
        Object object14 = ((IFn)const__11.getRawRoot()).invoke((Object)const__52, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__55, (Object)const__17, (Object)const__56, (Object)const__19, (Object)const__52, (Object)const__20, (Object)const__60, (Object)const__25, (Object)RT.map((Object[])new Object[]{((IFn)const__26.getRawRoot()).invoke(const__27.get(), ((IFn)const__28.getRawRoot()).invoke((Object)const__61, ((IFn)const__12.getRawRoot()).invoke((Object)const__62, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__31, const__52})))), new external_sort$fn__14419(), ((IFn)const__26.getRawRoot()).invoke(const__27.get(), ((IFn)const__28.getRawRoot()).invoke((Object)const__63, ((IFn)const__12.getRawRoot()).invoke((Object)const__64, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__31, const__52})))), new external_sort$fn__14430(), ((IFn)const__26.getRawRoot()).invoke(const__27.get(), ((IFn)const__28.getRawRoot()).invoke((Object)const__65, ((IFn)const__12.getRawRoot()).invoke((Object)const__66, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__31, const__52})))), new external_sort$fn__14443()})));
        Object object15 = ((IFn)const__36.getRawRoot()).invoke(const__52.getRawRoot());
        AFn aFn2 = const__67;
        Var var7 = const__68;
        var7.setMeta((IPersistentMap)const__70);
        Var var8 = var7;
        var7.bindRoot(const__71);
        Object object16 = ((IFn)new external_sort$fn__14459()).invoke();
        Var var9 = const__72;
        var9.setMeta((IPersistentMap)const__74);
        Var var10 = var9;
        var9.bindRoot((Object)new external_sort$file_system_sorter());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.external-sort");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.classForName((String)"datomic.external_sort.IO");
        const__5 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__6 = RT.var((String)"datomic.external-sort", (String)"IO");
        const__7 = RT.var((String)"clojure.core", (String)"assoc");
        const__8 = RT.keyword(null, (String)"doc");
        const__9 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__10 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"make-temp-file")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), ((IObj)Symbol.intern(null, (String)"temp-file-size")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"f"))))})), ((IObj)Symbol.intern(null, (String)"delete-temp-file")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"fname"))))}))));
        const__11 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__12 = RT.var((String)"clojure.core", (String)"merge");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.external_sort.IO"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.external_sort.IO")});
        const__17 = RT.keyword(null, (String)"sigs");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"make-temp-file"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"make-temp-file")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Returns something that can be opened by io/input-stream"}), RT.keyword(null, (String)"temp-file-size"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"temp-file-size")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"f"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"doc"), "Returns the size of the created file"}), RT.keyword(null, (String)"delete-temp-file"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"delete-temp-file")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"fname"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"fname")))), RT.keyword(null, (String)"doc"), "Deletes thing created by make-temp-file"})});
        const__19 = RT.keyword(null, (String)"var");
        const__20 = RT.keyword(null, (String)"method-map");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"delete-temp-file"), RT.keyword(null, (String)"delete-temp-file"), RT.keyword(null, (String)"make-temp-file"), RT.keyword(null, (String)"make-temp-file"), RT.keyword(null, (String)"temp-file-size"), RT.keyword(null, (String)"temp-file-size")});
        const__25 = RT.keyword(null, (String)"method-builders");
        const__26 = RT.var((String)"clojure.core", (String)"intern");
        const__27 = RT.var((String)"clojure.core", (String)"*ns*");
        const__28 = RT.var((String)"clojure.core", (String)"with-meta");
        const__29 = (AFn)((IObj)Symbol.intern(null, (String)"make-temp-file")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"make-temp-file")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Returns something that can be opened by io/input-stream"});
        const__31 = RT.keyword(null, (String)"protocol");
        const__32 = (AFn)((IObj)Symbol.intern(null, (String)"temp-file-size")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"f"))))}));
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"temp-file-size")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"f"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"doc"), "Returns the size of the created file"});
        const__34 = (AFn)((IObj)Symbol.intern(null, (String)"delete-temp-file")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"fname"))))}));
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"delete-temp-file")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"fname"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"fname")))), RT.keyword(null, (String)"doc"), "Deletes thing created by make-temp-file"});
        const__36 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__37 = (AFn)Symbol.intern(null, (String)"IO");
        const__38 = RT.var((String)"datomic.external-sort", (String)"temp-file-io");
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"dir")))), RT.keyword(null, (String)"column"), 1});
        const__44 = RT.var((String)"datomic.external-sort", (String)"next-chunk");
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"max-size"), (Object)Symbol.intern(null, (String)"sizer"), (Object)((IObj)Symbol.intern(null, (String)"iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iter")}))))), RT.keyword(null, (String)"column"), 1});
        const__48 = RT.var((String)"datomic.external-sort", (String)"chunk-seq");
        const__50 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"max-size"), (Object)Symbol.intern(null, (String)"sizer"), (Object)Symbol.intern(null, (String)"iter")))), RT.keyword(null, (String)"column"), 1});
        const__51 = RT.classForName((String)"datomic.external_sort.ExternalSort");
        const__52 = RT.var((String)"datomic.external-sort", (String)"ExternalSort");
        const__53 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"consume-iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"handler"))))})), ((IObj)Symbol.intern(null, (String)"consume-files-iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"files"), (Object)Symbol.intern(null, (String)"handler"))))})), ((IObj)Symbol.intern(null, (String)"merge-step")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__55 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.external_sort.ExternalSort"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.external_sort.ExternalSort")});
        const__56 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"consume-iter"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"consume-iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"handler"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"handler")))), RT.keyword(null, (String)"doc"), "Call hanlder with final result"}), RT.keyword(null, (String)"consume-files-iter"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"consume-files-iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"files"), (Object)Symbol.intern(null, (String)"handler"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"files"), (Object)Symbol.intern(null, (String)"handler")))), RT.keyword(null, (String)"doc"), "Call handler with iterator over merged files"}), RT.keyword(null, (String)"merge-step"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"merge-step")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Returns coll of new files made in this step"})});
        const__60 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"merge-step"), RT.keyword(null, (String)"merge-step"), RT.keyword(null, (String)"consume-files-iter"), RT.keyword(null, (String)"consume-files-iter"), RT.keyword(null, (String)"consume-iter"), RT.keyword(null, (String)"consume-iter")});
        const__61 = (AFn)((IObj)Symbol.intern(null, (String)"merge-step")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__62 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"merge-step")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Returns coll of new files made in this step"});
        const__63 = (AFn)((IObj)Symbol.intern(null, (String)"consume-iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"handler"))))}));
        const__64 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"consume-iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"handler"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"handler")))), RT.keyword(null, (String)"doc"), "Call hanlder with final result"});
        const__65 = (AFn)((IObj)Symbol.intern(null, (String)"consume-files-iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"files"), (Object)Symbol.intern(null, (String)"handler"))))}));
        const__66 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"consume-files-iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"files"), (Object)Symbol.intern(null, (String)"handler"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"files"), (Object)Symbol.intern(null, (String)"handler")))), RT.keyword(null, (String)"doc"), "Call handler with iterator over merged files"});
        const__67 = (AFn)Symbol.intern(null, (String)"ExternalSort");
        const__68 = RT.var((String)"datomic.external-sort", (String)"MAX_MERGE");
        const__70 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__71 = 4L;
        const__72 = RT.var((String)"datomic.external-sort", (String)"file-system-sorter");
        const__74 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"max-chunk-size"), Symbol.intern(null, (String)"item-sizer"), Symbol.intern(null, (String)"io"), Symbol.intern(null, (String)"cmp"), Symbol.intern(null, (String)"prog-fn"), Symbol.intern(null, (String)"file-iter-fn"), Symbol.intern(null, (String)"create-file-writer-fn"), Symbol.intern(null, (String)"threads")}), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"cmp"), Symbol.intern(null, (String)"compare"), Symbol.intern(null, (String)"prog-fn"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"constantly"), null))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 30})), Symbol.intern(null, (String)"threads"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern((String)"config", (String)"property"), "datomic.externalSortPool"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 55}))})}), (Object)Symbol.intern(null, (String)"iter")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        external_sort__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.external_sort__init").getClassLoader());
        try {
            external_sort__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}

