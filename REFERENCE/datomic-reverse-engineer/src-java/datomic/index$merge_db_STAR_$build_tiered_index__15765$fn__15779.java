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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class index$merge_db_STAR_$build_tiered_index__15765$fn__15779
extends AFunction {
    Object build_index;
    Object dirs_written;
    Object as_of_t;
    Object written;
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
    Object mem_idx;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__2 = RT.var((String)"datomic.btset", (String)"seek");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__8 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__9 = RT.keyword(null, (String)"event");
    public static final Keyword const__10 = RT.keyword((String)"index", (String)"merged-index");
    public static final Keyword const__11 = RT.keyword(null, (String)"mode");
    public static final Keyword const__12 = RT.keyword(null, (String)"merge-main");
    public static final Keyword const__13 = RT.keyword(null, (String)"index");
    public static final Keyword const__14 = RT.keyword(null, (String)"count");
    public static final Keyword const__15 = RT.keyword(null, (String)"as-of-t");
    public static final Keyword const__16 = RT.keyword(null, (String)"written");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"deref");
    public static final Keyword const__19 = RT.keyword(null, (String)"dirs-written");
    public static final Keyword const__20 = RT.keyword(null, (String)"threw");

    public index$merge_db_STAR_$build_tiered_index__15765$fn__15779(Object object, Object object2, Object object3, Object object4, Object object5, int n, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14) {
        this.build_index = object;
        this.dirs_written = object2;
        this.as_of_t = object3;
        this.written = object4;
        this.root_map = object5;
        this.count_mem_idx = n;
        this.garbage = object6;
        this.idx_name = object7;
        this.midk = object8;
        this.xpreds = object9;
        this.dirs_written_ref = object10;
        this.histk = object11;
        this.maink = object12;
        this.segs_written_ref = object13;
        this.mem_idx = object14;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object object;
            Object object2;
            Object or__5238__auto__15787;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            Object vec__15780 = ((IFn)this.build_index).invoke(this.maink, ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this.mem_idx)), (Object)Boolean.TRUE, null, this.garbage);
            Object retid = RT.nth((Object)vec__15780, (int)RT.uncheckedIntCast((long)0L), null);
            Object garbage2 = RT.nth((Object)vec__15780, (int)RT.uncheckedIntCast((long)1L), null);
            Object object3 = vec__15780;
            vec__15780 = null;
            Object retractions = RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)2L), null);
            Object object4 = or__5238__auto__15787 = ((IFn)const__7.getRawRoot()).invoke(retractions);
            if (object4 != null && object4 != Boolean.FALSE) {
                object2 = or__5238__auto__15787;
                or__5238__auto__15787 = null;
            } else {
                object2 = ((IFn)const__7.getRawRoot()).invoke(this.xpreds);
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
            IPersistentVector vec__15783 = object;
            Object hist_retid = RT.nth((Object)vec__15783, (int)RT.uncheckedIntCast((long)0L), null);
            IPersistentVector iPersistentVector = vec__15783;
            vec__15783 = null;
            Object garbage3 = RT.nth((Object)iPersistentVector, (int)RT.uncheckedIntCast((long)1L), null);
            Logger logger = LoggerFactory.getLogger((String)"datomic.index");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.info((String)((IFn)const__8.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__9, const__10, const__11, const__12, const__13, this.idx_name, const__14, this.count_mem_idx, const__15, this.as_of_t, const__16, Numbers.unchecked_minus((Object)((IFn)const__18.getRawRoot()).invoke(this.segs_written_ref), (Object)this.written), const__19, Numbers.unchecked_minus((Object)((IFn)const__18.getRawRoot()).invoke(this.dirs_written_ref), (Object)this.dirs_written)})));
            }
            Object object8 = retid;
            retid = null;
            Object object9 = hist_retid;
            hist_retid = null;
            Object object10 = garbage3;
            garbage3 = null;
            objectArray[1] = Tuple.create((Object)((IFn)this.midk).invoke(this.root_map), (Object)object8, (Object)object9, (Object)object10);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__20;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

