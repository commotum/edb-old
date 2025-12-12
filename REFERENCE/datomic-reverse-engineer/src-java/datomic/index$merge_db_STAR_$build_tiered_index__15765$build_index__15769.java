/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index$merge_db_STAR_$build_tiered_index__15765$build_index__15769
extends AFunction {
    Object partfn;
    Object db;
    Object as_of_t;
    Object cmp;
    Object root_map;
    Object write_handlers;
    Object cmpi;
    Object xpreds;
    Object idxcmp;
    Object cstore;
    Object dirs_written_ref;
    Object segs_written_ref;
    Object olookup;
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"merge-one-index");

    public index$merge_db_STAR_$build_tiered_index__15765$build_index__15769(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13) {
        this.partfn = object;
        this.db = object2;
        this.as_of_t = object3;
        this.cmp = object4;
        this.root_map = object5;
        this.write_handlers = object6;
        this.cmpi = object7;
        this.xpreds = object8;
        this.idxcmp = object9;
        this.cstore = object10;
        this.dirs_written_ref = object11;
        this.segs_written_ref = object12;
        this.olookup = object13;
    }

    public Object invoke(Object destk, Object datoms2, Object filter_retractions_QMARK_, Object filter_segids, Object garbage2) {
        Object object = destk;
        destk = null;
        Object object2 = datoms2;
        datoms2 = null;
        Object object3 = garbage2;
        garbage2 = null;
        Object object4 = filter_retractions_QMARK_;
        filter_retractions_QMARK_ = null;
        Object object5 = filter_segids;
        filter_segids = null;
        index$merge_db_STAR_$build_tiered_index__15765$build_index__15769 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, this_.cstore, this_.olookup, ((IFn)object).invoke(this_.root_map), object2, object3, this_.partfn, this_.cmp, this_.write_handlers, object4, this_.as_of_t, this_.idxcmp, this_.xpreds, object5, this_.cmpi, this_.segs_written_ref, this_.dirs_written_ref);
    }
}

