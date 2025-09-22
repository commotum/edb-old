/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
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
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.index.ITreeIter;
import datomic.index.Index;
import datomic.index.RootNode;

public final class index$estimate_seg_offset
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Keyword const__6;
    public static final Keyword const__7;
    public static final Keyword const__9;
    public static final Object const__10;
    public static final Keyword const__11;
    public static final Keyword const__12;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object olookup, Object index, Object dir_partition_size, Object k) {
        block5: {
            block4: {
                v0 = (IFn)index$estimate_seg_offset.const__0.getRawRoot();
                G__15644 = ((Index)index).seek(k);
                if (!Util.identical((Object)G__15644, null)) break block4;
                v1 = null;
                break block5;
            }
            v2 = G__15644;
            G__15644 = null;
            v3 = v2;
            if (Util.classOf((Object)v2) == index$estimate_seg_offset.__cached_class__0) ** GOTO lbl14
            if (!(v3 instanceof ITreeIter)) {
                v3 = v3;
                index$estimate_seg_offset.__cached_class__0 = Util.classOf((Object)v3);
lbl14:
                // 2 sources

                v1 = index$estimate_seg_offset.const__2.getRawRoot().invoke((Object)v3);
            } else {
                v1 = ((ITreeIter)v3).dir_seq();
            }
        }
        map__15643 = v0.invoke(v1);
        v4 = ((IFn)index$estimate_seg_offset.const__3.getRawRoot()).invoke(map__15643);
        if (v4 != null && v4 != Boolean.FALSE) {
            v5 = map__15643;
            map__15643 = null;
            v6 = PersistentHashMap.create((ISeq)((ISeq)((IFn)index$estimate_seg_offset.const__4.getRawRoot()).invoke(v5)));
        } else {
            v6 = map__15643;
            map__15643 = null;
        }
        map__15643 = v6;
        key = RT.get((Object)map__15643, (Object)index$estimate_seg_offset.const__6);
        v7 = index;
        index = null;
        v8 = root = ((Index)v7).root;
        root = null;
        ri = RT.get((Object)map__15643, (Object)index$estimate_seg_offset.const__7, (Object)RT.count((Object)((RootNode)v8).dirids));
        v9 = map__15643;
        map__15643 = null;
        di = RT.get((Object)v9, (Object)index$estimate_seg_offset.const__9, (Object)index$estimate_seg_offset.const__10);
        v10 = new Object[6];
        v10[0] = index$estimate_seg_offset.const__6;
        v11 = k;
        k = null;
        v10[1] = v11;
        v10[2] = index$estimate_seg_offset.const__11;
        v12 = key;
        key = null;
        v10[3] = v12;
        v10[4] = index$estimate_seg_offset.const__12;
        v13 = ri;
        ri = null;
        v14 = dir_partition_size;
        dir_partition_size = null;
        v15 = di;
        di = null;
        v10[5] = Numbers.unchecked_add((Object)Numbers.unchecked_multiply((Object)v13, (Object)v14), (Object)v15);
        return RT.mapUniqueKeys((Object[])v10);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return index$estimate_seg_offset.invokeStatic(object5, object6, object7, object8);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"first");
        const__2 = RT.var((String)"datomic.index", (String)"dir-seq");
        const__3 = RT.var((String)"clojure.core", (String)"seq?");
        const__4 = RT.var((String)"clojure.core", (String)"seq");
        const__6 = RT.keyword(null, (String)"key");
        const__7 = RT.keyword(null, (String)"ri");
        const__9 = RT.keyword(null, (String)"di");
        const__10 = 0L;
        const__11 = RT.keyword(null, (String)"index-key");
        const__12 = RT.keyword(null, (String)"offset");
    }
}

