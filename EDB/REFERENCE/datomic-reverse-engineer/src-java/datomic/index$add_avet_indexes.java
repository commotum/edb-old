/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.index$add_avet_indexes$sort_and_merge__15566;

public final class index$add_avet_indexes
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"aevt-attrs-datoms");
    public static final Var const__4 = RT.var((String)"datomic.index", (String)"idx-key");
    public static final Keyword const__5 = RT.keyword(null, (String)"avet-main");
    public static final Keyword const__6 = RT.keyword(null, (String)"avet");
    public static final Keyword const__7 = RT.keyword(null, (String)"avet-hist");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"assoc");

    public static Object invokeStatic(Object cstore, Object olookup, Object db2, Object as_of_t2, Object root_map, Object attrids) {
        Object vec__15557 = ((IFn)const__0.getRawRoot()).invoke(db2, attrids);
        Object mid_main_aevt_datoms = RT.nth((Object)vec__15557, (int)RT.uncheckedIntCast((long)0L), null);
        Object object = vec__15557;
        vec__15557 = null;
        Object hist_aevt_datoms = RT.nth((Object)object, (int)RT.uncheckedIntCast((long)1L), null);
        Object object2 = olookup;
        olookup = null;
        Object object3 = as_of_t2;
        as_of_t2 = null;
        Object object4 = cstore;
        cstore = null;
        Object object5 = attrids;
        attrids = null;
        Object object6 = db2;
        db2 = null;
        index$add_avet_indexes$sort_and_merge__15566 sort_and_merge = new index$add_avet_indexes$sort_and_merge__15566(root_map, object2, object3, object4, object5, object6);
        Object object7 = mid_main_aevt_datoms;
        mid_main_aevt_datoms = null;
        Object vec__15560 = ((IFn)sort_and_merge).invoke(((IFn)const__4.getRawRoot()).invoke((Object)const__5, (Object)const__6), object7, (Object)PersistentVector.EMPTY);
        Object avetid = RT.nth((Object)vec__15560, (int)RT.uncheckedIntCast((long)0L), null);
        Object object8 = vec__15560;
        vec__15560 = null;
        Object garbage2 = RT.nth((Object)object8, (int)RT.uncheckedIntCast((long)1L), null);
        index$add_avet_indexes$sort_and_merge__15566 index$add_avet_indexes$sort_and_merge__15566 = sort_and_merge;
        sort_and_merge = null;
        Object object9 = hist_aevt_datoms;
        hist_aevt_datoms = null;
        Object object10 = garbage2;
        garbage2 = null;
        Object vec__15563 = ((IFn)index$add_avet_indexes$sort_and_merge__15566).invoke((Object)const__7, object9, object10);
        Object hist_avetid = RT.nth((Object)vec__15563, (int)RT.uncheckedIntCast((long)0L), null);
        Object object11 = vec__15563;
        vec__15563 = null;
        Object garbage3 = RT.nth((Object)object11, (int)RT.uncheckedIntCast((long)1L), null);
        Object object12 = root_map;
        root_map = null;
        Object object13 = avetid;
        avetid = null;
        Object object14 = hist_avetid;
        hist_avetid = null;
        Object object15 = garbage3;
        garbage3 = null;
        return Tuple.create((Object)((IFn)const__8.getRawRoot()).invoke(object12, (Object)const__5, object13, (Object)const__7, object14), (Object)object15);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return index$add_avet_indexes.invokeStatic(object7, object8, object9, object10, object11, object12);
    }
}

