/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.external_sort.ExternalSort;
import datomic.external_sort_datoms$consume_sorted_datoms$fn__14513;
import datomic.external_sort_datoms$consume_sorted_datoms$fn__14515;

public final class external_sort_datoms$consume_sorted_datoms
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__3;
    public static final Keyword const__4;
    public static final Keyword const__5;
    public static final Keyword const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Keyword const__9;
    public static final Var const__10;
    public static final Keyword const__11;
    public static final Var const__12;
    public static final Keyword const__13;
    public static final Keyword const__14;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object iter2, Object p__14511, Object handler) {
        Object object;
        Object object2;
        Object object3 = p__14511;
        p__14511 = null;
        Object map__14512 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__14512);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__14512;
            map__14512 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__14512;
            map__14512 = null;
        }
        Object map__145122 = object2;
        Object cmp = RT.get((Object)map__145122, (Object)const__3);
        Object dir = RT.get((Object)map__145122, (Object)const__4);
        Object max_chunk_size = RT.get((Object)map__145122, (Object)const__5);
        Object object6 = map__145122;
        map__145122 = null;
        Object prog_fn = RT.get((Object)object6, (Object)const__6);
        Object[] objectArray = new Object[14];
        objectArray[0] = const__5;
        Object object7 = max_chunk_size;
        max_chunk_size = null;
        objectArray[1] = object7;
        objectArray[2] = const__9;
        objectArray[3] = const__10.getRawRoot();
        objectArray[4] = const__11;
        Object object8 = dir;
        dir = null;
        objectArray[5] = ((IFn)const__12.getRawRoot()).invoke(object8);
        objectArray[6] = const__6;
        Object object9 = prog_fn;
        prog_fn = null;
        objectArray[7] = object9;
        objectArray[8] = const__3;
        Object object10 = cmp;
        cmp = null;
        objectArray[9] = object10;
        objectArray[10] = const__13;
        objectArray[11] = new external_sort_datoms$consume_sorted_datoms$fn__14513();
        objectArray[12] = const__14;
        objectArray[13] = new external_sort_datoms$consume_sorted_datoms$fn__14515();
        Object object11 = iter2;
        iter2 = null;
        Object object12 = ((IFn)const__8.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), object11);
        if (Util.classOf((Object)object12) != __cached_class__0) {
            if (object12 instanceof ExternalSort) {
                Object object13 = handler;
                handler = null;
                object = ((ExternalSort)object12).consume_iter(object13);
                return object;
            }
            object12 = object12;
            __cached_class__0 = Util.classOf((Object)object12);
        }
        Object object14 = handler;
        handler = null;
        object = const__7.getRawRoot().invoke(object12, object14);
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return external_sort_datoms$consume_sorted_datoms.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"seq?");
        const__1 = RT.var((String)"clojure.core", (String)"seq");
        const__3 = RT.keyword(null, (String)"cmp");
        const__4 = RT.keyword(null, (String)"dir");
        const__5 = RT.keyword(null, (String)"max-chunk-size");
        const__6 = RT.keyword(null, (String)"prog-fn");
        const__7 = RT.var((String)"datomic.external-sort", (String)"consume-iter");
        const__8 = RT.var((String)"datomic.external-sort", (String)"file-system-sorter");
        const__9 = RT.keyword(null, (String)"item-sizer");
        const__10 = RT.var((String)"datomic.memory-size", (String)"memory-size");
        const__11 = RT.keyword(null, (String)"io");
        const__12 = RT.var((String)"datomic.external-sort", (String)"temp-file-io");
        const__13 = RT.keyword(null, (String)"file-iter-fn");
        const__14 = RT.keyword(null, (String)"create-file-writer-fn");
    }
}

