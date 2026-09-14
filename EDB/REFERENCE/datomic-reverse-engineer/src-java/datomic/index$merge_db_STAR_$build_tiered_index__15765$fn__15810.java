/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index.Index;
import datomic.index.TreeIter;

public final class index$merge_db_STAR_$build_tiered_index__15765$fn__15810
extends AFunction {
    long n_segs;
    Object part_size;
    Object mid_idx;
    Object main_idx;
    Object olookup;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"least-pop-slice");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__3 = RT.keyword(null, (String)"key");
    public static final Keyword const__4 = RT.keyword(null, (String)"threw");

    public index$merge_db_STAR_$build_tiered_index__15765$fn__15810(long l, Object object, Object object2, Object object3, Object object4) {
        this.n_segs = l;
        this.part_size = object;
        this.mid_idx = object2;
        this.main_idx = object3;
        this.olookup = object4;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            iPersistentMap = RT.mapUniqueKeys((Object[])new Object[]{const__0, ((IFn)const__1.getRawRoot()).invoke(this.olookup, ((IFn)const__2.getRawRoot()).invoke((Object)const__3, ((TreeIter)((Index)this.mid_idx).seek()).dir_seq()), this.main_idx, this.part_size, (Object)Numbers.num((long)this.n_segs))});
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__4;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

