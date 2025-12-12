/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.external_sort$file_system_sorter$fn__14493;
import datomic.external_sort.FileSystemSorter;

public final class external_sort$file_system_sorter
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"max-chunk-size");
    public static final Keyword const__4 = RT.keyword(null, (String)"item-sizer");
    public static final Keyword const__5 = RT.keyword(null, (String)"io");
    public static final Keyword const__6 = RT.keyword(null, (String)"cmp");
    public static final Var const__7 = RT.var((String)"datomic.common", (String)"compare");
    public static final Keyword const__8 = RT.keyword(null, (String)"prog-fn");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"constantly");
    public static final Keyword const__10 = RT.keyword(null, (String)"file-iter-fn");
    public static final Keyword const__11 = RT.keyword(null, (String)"create-file-writer-fn");
    public static final Keyword const__12 = RT.keyword(null, (String)"threads");
    public static final Var const__13 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__16 = RT.var((String)"datomic.external-sort", (String)"chunk-seq");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"atom");
    public static final Object const__18 = 0L;
    public static final Var const__19 = RT.var((String)"datomic.common", (String)"thread-pool");
    public static final Keyword const__20 = RT.keyword(null, (String)"nthreads");
    public static final Keyword const__21 = RT.keyword(null, (String)"name");

    public static Object invokeStatic(Object p__14491, Object iter2) {
        Object pool;
        Object object;
        Object object2 = p__14491;
        p__14491 = null;
        Object map__14492 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__14492);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__14492;
            map__14492 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__14492;
            map__14492 = null;
        }
        Object map__144922 = object;
        Object max_chunk_size = RT.get((Object)map__144922, (Object)const__3);
        Object item_sizer = RT.get((Object)map__144922, (Object)const__4);
        Object io2 = RT.get((Object)map__144922, (Object)const__5);
        Object cmp = RT.get((Object)map__144922, (Object)const__6, (Object)const__7.getRawRoot());
        Object prog_fn = RT.get((Object)map__144922, (Object)const__8, (Object)((IFn)const__9.getRawRoot()).invoke(null));
        Object file_iter_fn = RT.get((Object)map__144922, (Object)const__10);
        Object create_file_writer_fn = RT.get((Object)map__144922, (Object)const__11);
        Object object5 = map__144922;
        map__144922 = null;
        Object threads = RT.get((Object)object5, (Object)const__12, (Object)((IFn)const__13.getRawRoot()).invoke((Object)"datomic.externalSortPool"));
        Object object6 = max_chunk_size;
        max_chunk_size = null;
        Object object7 = item_sizer;
        item_sizer = null;
        Object object8 = iter2;
        iter2 = null;
        Object files2 = ((IFn)const__14.getRawRoot()).invoke((Object)PersistentVector.EMPTY, ((IFn)const__15.getRawRoot()).invoke((Object)new external_sort$file_system_sorter$fn__14493(io2, create_file_writer_fn, cmp, prog_fn), ((IFn)const__16.getRawRoot()).invoke(object6, object7, object8)));
        ((IFn)const__17.getRawRoot()).invoke(const__18);
        Object[] objectArray = new Object[4];
        objectArray[0] = const__20;
        Object object9 = threads;
        threads = null;
        objectArray[1] = object9;
        objectArray[2] = const__21;
        objectArray[3] = "external-sort";
        Object object10 = pool = ((IFn)const__19.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
        pool = null;
        Object object11 = cmp;
        cmp = null;
        Object object12 = io2;
        io2 = null;
        Object object13 = file_iter_fn;
        file_iter_fn = null;
        Object object14 = create_file_writer_fn;
        create_file_writer_fn = null;
        Object object15 = prog_fn;
        prog_fn = null;
        Object object16 = files2;
        files2 = null;
        return new FileSystemSorter(object10, object11, object12, object13, object14, object15, object16);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return external_sort$file_system_sorter.invokeStatic(object3, object4);
    }
}

