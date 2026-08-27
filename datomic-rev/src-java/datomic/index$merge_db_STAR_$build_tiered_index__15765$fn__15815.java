/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.index$merge_db_STAR_$build_tiered_index__15765$fn__15815$fn__15825;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class index$merge_db_STAR_$build_tiered_index__15765$fn__15815
extends AFunction {
    Object build_index;
    Object dirs_written;
    Object as_of_t;
    Object written;
    Object slice_segids;
    Object root_map;
    int count_mem_idx;
    Object garbage;
    Object idx_name;
    Object midk;
    Object xpreds;
    Object dirs_written_ref;
    Object histk;
    Object maink;
    Object segs_written_ref;
    Object olookup;
    Object mem_idx;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__6 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__7 = RT.keyword(null, (String)"event");
    public static final Keyword const__8 = RT.keyword((String)"index", (String)"step-1");
    public static final Keyword const__9 = RT.keyword(null, (String)"index");
    public static final Keyword const__10 = RT.keyword(null, (String)"written");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"deref");
    public static final Keyword const__13 = RT.keyword(null, (String)"dirs-written");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__15 = RT.keyword((String)"index", (String)"step-2");
    public static final Var const__16 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__17 = RT.var((String)"datomic.btset", (String)"seek");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"set");
    public static final Keyword const__19 = RT.keyword((String)"index", (String)"merged-index");
    public static final Keyword const__20 = RT.keyword(null, (String)"mode");
    public static final Keyword const__21 = RT.keyword(null, (String)"merge-slice");
    public static final Keyword const__22 = RT.keyword(null, (String)"count");
    public static final Keyword const__23 = RT.keyword(null, (String)"as-of-t");
    public static final Keyword const__24 = RT.keyword(null, (String)"threw");

    public index$merge_db_STAR_$build_tiered_index__15765$fn__15815(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, int n, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14, Object object15, Object object16) {
        this.build_index = object;
        this.dirs_written = object2;
        this.as_of_t = object3;
        this.written = object4;
        this.slice_segids = object5;
        this.root_map = object6;
        this.count_mem_idx = n;
        this.garbage = object7;
        this.idx_name = object8;
        this.midk = object9;
        this.xpreds = object10;
        this.dirs_written_ref = object11;
        this.histk = object12;
        this.maink = object13;
        this.segs_written_ref = object14;
        this.olookup = object15;
        this.mem_idx = object16;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object object;
            Object object2;
            Object or__5238__auto__15828;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            Object vec__15816 = ((IFn)this.build_index).invoke(this.maink, ((IFn)const__1.getRawRoot()).invoke((Object)new index$merge_db_STAR_$build_tiered_index__15765$fn__15815$fn__15825(this.olookup), this.slice_segids), (Object)Boolean.TRUE, null, this.garbage);
            Object retid = RT.nth((Object)vec__15816, (int)RT.uncheckedIntCast((long)0L), null);
            Object garbage2 = RT.nth((Object)vec__15816, (int)RT.uncheckedIntCast((long)1L), null);
            Object object3 = vec__15816;
            vec__15816 = null;
            Object retractions = RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)2L), null);
            Logger logger = LoggerFactory.getLogger((String)"datomic.index");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.info((String)((IFn)const__6.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__7, const__8, const__9, this.idx_name, const__10, Numbers.unchecked_minus((Object)((IFn)const__12.getRawRoot()).invoke(this.segs_written_ref), (Object)this.written), const__13, Numbers.unchecked_minus((Object)((IFn)const__12.getRawRoot()).invoke(this.dirs_written_ref), (Object)this.dirs_written)})));
            }
            Object object4 = or__5238__auto__15828 = ((IFn)const__14.getRawRoot()).invoke(retractions);
            if (object4 != null && object4 != Boolean.FALSE) {
                object2 = or__5238__auto__15828;
                or__5238__auto__15828 = null;
            } else {
                object2 = ((IFn)const__14.getRawRoot()).invoke(this.xpreds);
            }
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object5 = retractions;
                retractions = null;
                Object object6 = garbage2;
                garbage2 = null;
                object = ((IFn)this.build_index).invoke(this.histk, object5, (Object)Boolean.FALSE, null, object6);
            } else {
                Object object7 = garbage2;
                garbage2 = null;
                object = Tuple.create((Object)((IFn)this.histk).invoke(this.root_map), (Object)object7);
            }
            IPersistentVector vec__15819 = object;
            Object hist_retid = RT.nth((Object)vec__15819, (int)RT.uncheckedIntCast((long)0L), null);
            IPersistentVector iPersistentVector = vec__15819;
            vec__15819 = null;
            Object garbage3 = RT.nth((Object)iPersistentVector, (int)RT.uncheckedIntCast((long)1L), null);
            Logger logger3 = LoggerFactory.getLogger((String)"datomic.index");
            if (logger3.isInfoEnabled()) {
                Logger logger4 = logger3;
                logger3 = null;
                logger4.info((String)((IFn)const__6.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__7, const__15, const__9, this.idx_name, const__10, Numbers.unchecked_minus((Object)((IFn)const__12.getRawRoot()).invoke(this.segs_written_ref), (Object)this.written), const__13, Numbers.unchecked_minus((Object)((IFn)const__12.getRawRoot()).invoke(this.dirs_written_ref), (Object)this.dirs_written)})));
            }
            Object object8 = garbage3;
            garbage3 = null;
            Object vec__15822 = ((IFn)this.build_index).invoke(this.midk, ((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke(this.mem_idx)), (Object)Boolean.FALSE, ((IFn)const__18.getRawRoot()).invoke(this.slice_segids), object8);
            Object mid_retid = RT.nth((Object)vec__15822, (int)RT.uncheckedIntCast((long)0L), null);
            Object object9 = vec__15822;
            vec__15822 = null;
            Object garbage4 = RT.nth((Object)object9, (int)RT.uncheckedIntCast((long)1L), null);
            Logger logger5 = LoggerFactory.getLogger((String)"datomic.index");
            if (logger5.isInfoEnabled()) {
                Logger logger6 = logger5;
                logger5 = null;
                logger6.info((String)((IFn)const__6.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__7, const__19, const__20, const__21, const__9, this.idx_name, const__22, this.count_mem_idx, const__23, this.as_of_t, const__10, Numbers.unchecked_minus((Object)((IFn)const__12.getRawRoot()).invoke(this.segs_written_ref), (Object)this.written), const__13, Numbers.unchecked_minus((Object)((IFn)const__12.getRawRoot()).invoke(this.dirs_written_ref), (Object)this.dirs_written)})));
            }
            Object object10 = mid_retid;
            mid_retid = null;
            Object object11 = retid;
            retid = null;
            Object object12 = hist_retid;
            hist_retid = null;
            Object object13 = garbage4;
            garbage4 = null;
            objectArray[1] = Tuple.create((Object)object10, (Object)object11, (Object)object12, (Object)object13);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__24;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

