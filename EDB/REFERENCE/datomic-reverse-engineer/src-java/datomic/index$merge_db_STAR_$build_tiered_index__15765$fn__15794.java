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
 *  clojure.lang.PersistentVector
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
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class index$merge_db_STAR_$build_tiered_index__15765$fn__15794
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
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__7 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__8 = RT.keyword(null, (String)"event");
    public static final Keyword const__9 = RT.keyword((String)"index", (String)"merged-index");
    public static final Keyword const__10 = RT.keyword(null, (String)"mode");
    public static final Keyword const__11 = RT.keyword(null, (String)"merge-mid");
    public static final Keyword const__12 = RT.keyword(null, (String)"index");
    public static final Keyword const__13 = RT.keyword(null, (String)"count");
    public static final Keyword const__14 = RT.keyword(null, (String)"as-of-t");
    public static final Keyword const__15 = RT.keyword(null, (String)"written");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"deref");
    public static final Keyword const__18 = RT.keyword(null, (String)"dirs-written");
    public static final Keyword const__19 = RT.keyword(null, (String)"threw");

    public index$merge_db_STAR_$build_tiered_index__15765$fn__15794(Object object, Object object2, Object object3, Object object4, Object object5, int n, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14) {
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
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            Object vec__15795 = ((IFn)this.build_index).invoke(this.midk, ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this.mem_idx)), (Object)Boolean.FALSE, null, this.garbage);
            Object mid_retid = RT.nth((Object)vec__15795, (int)RT.uncheckedIntCast((long)0L), null);
            Object object2 = vec__15795;
            vec__15795 = null;
            Object garbage2 = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
            Object object3 = ((IFn)const__6.getRawRoot()).invoke(this.xpreds);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = garbage2;
                garbage2 = null;
                object = ((IFn)this.build_index).invoke(this.maink, (Object)PersistentVector.EMPTY, (Object)Boolean.FALSE, null, object4);
            } else {
                Object object5 = garbage2;
                garbage2 = null;
                object = Tuple.create((Object)((IFn)this.maink).invoke(this.root_map), (Object)object5);
            }
            IPersistentVector vec__15798 = object;
            Object main_retid = RT.nth((Object)vec__15798, (int)RT.uncheckedIntCast((long)0L), null);
            IPersistentVector iPersistentVector = vec__15798;
            vec__15798 = null;
            Object garbage3 = RT.nth((Object)iPersistentVector, (int)RT.uncheckedIntCast((long)1L), null);
            Logger logger = LoggerFactory.getLogger((String)"datomic.index");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.info((String)((IFn)const__7.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__8, const__9, const__10, const__11, const__12, this.idx_name, const__13, this.count_mem_idx, const__14, this.as_of_t, const__15, Numbers.unchecked_minus((Object)((IFn)const__17.getRawRoot()).invoke(this.segs_written_ref), (Object)this.written), const__18, Numbers.unchecked_minus((Object)((IFn)const__17.getRawRoot()).invoke(this.dirs_written_ref), (Object)this.dirs_written)})));
            }
            Object object6 = mid_retid;
            mid_retid = null;
            Object object7 = main_retid;
            main_retid = null;
            Object object8 = garbage3;
            garbage3 = null;
            objectArray[1] = Tuple.create((Object)object6, (Object)object7, (Object)((IFn)this.histk).invoke(this.root_map), (Object)object8);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__19;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

