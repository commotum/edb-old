/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import datomic.db.Db;

public final class db$fn__13421$__GT_Db__13564
extends AFunction {
    public Object invoke(Object id, Object memidx, Object indexing, Object mid_index, Object index2, Object history2, Object memlog2, Object basisT, Object nextT, Object indexBasisT, Object indexingNextT, Object elements, Object keys, Object ids, Object index_root_id, Object index_rev, Object asOfT, Object sinceT, Object raw, Object filt) {
        Object object = id;
        id = null;
        Object object2 = memidx;
        memidx = null;
        Object object3 = indexing;
        indexing = null;
        Object object4 = mid_index;
        mid_index = null;
        Object object5 = index2;
        index2 = null;
        Object object6 = history2;
        history2 = null;
        Object object7 = memlog2;
        memlog2 = null;
        Object object8 = basisT;
        basisT = null;
        Object object9 = nextT;
        nextT = null;
        Object object10 = indexBasisT;
        indexBasisT = null;
        Object object11 = indexingNextT;
        indexingNextT = null;
        Object object12 = elements;
        elements = null;
        Object object13 = keys;
        keys = null;
        Object object14 = ids;
        ids = null;
        Object object15 = index_root_id;
        index_root_id = null;
        Object object16 = index_rev;
        index_rev = null;
        Object object17 = asOfT;
        asOfT = null;
        Object object18 = sinceT;
        sinceT = null;
        Object object19 = raw;
        raw = null;
        Object object20 = filt;
        filt = null;
        return new Db(object, object2, object3, object4, object5, object6, object7, RT.uncheckedLongCast((Object)((Number)object8)), RT.uncheckedLongCast((Object)((Number)object9)), RT.uncheckedLongCast((Object)((Number)object10)), object11, object12, object13, object14, object15, object16, object17, object18, object19, object20);
    }
}

