/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.index$merge_db_STAR_$build_tiered_index__15765$build_index__15769;
import datomic.index$merge_db_STAR_$build_tiered_index__15765$fn__15774;
import datomic.index$merge_db_STAR_$build_tiered_index__15765$fn__15779;
import datomic.index$merge_db_STAR_$build_tiered_index__15765$fn__15794;
import datomic.index$merge_db_STAR_$build_tiered_index__15765$fn__15810;
import datomic.index$merge_db_STAR_$build_tiered_index__15765$fn__15815;
import datomic.index$merge_db_STAR_$build_tiered_index__15765$ratio__15805;
import datomic.index.Index;
import datomic.index.TreeIter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class index$merge_db_STAR_$build_tiered_index__15765
extends AFunction {
    Object db;
    Object as_of_t;
    Object root_map;
    Object garbage;
    Object xpreds;
    Object cstore;
    Object dirs_written_ref;
    Object segs_written_ref;
    Object olookup;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.index", (String)"make-sparse-lt");
    public static final Keyword const__3 = RT.keyword(null, (String)"event");
    public static final Keyword const__4 = RT.keyword((String)"index", (String)"mem-index-bytes");
    public static final Keyword const__5 = RT.keyword(null, (String)"index");
    public static final Var const__6 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__8 = RT.keyword(null, (String)"phase");
    public static final Keyword const__9 = RT.keyword(null, (String)"begin");
    public static final Var const__11 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__13 = RT.keyword(null, (String)"msec");
    public static final Keyword const__14 = RT.keyword(null, (String)"end");
    public static final Keyword const__15 = RT.keyword(null, (String)"threw");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__18 = RT.keyword(null, (String)"returned");
    public static final Var const__19 = RT.var((String)"datomic.index", (String)"stg-index-size");
    public static final Object const__21 = 0L;
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__28 = RT.var((String)"datomic.index", (String)"MIN_MAIN_INDEX_THRESHOLD");
    public static final Var const__29 = RT.var((String)"datomic.index", (String)"MID_INDEX_THRESHOLD_FACTOR");
    public static final Keyword const__30 = RT.keyword((String)"index", (String)"merge-main");
    public static final Keyword const__31 = RT.keyword(null, (String)"count");
    public static final Keyword const__32 = RT.keyword(null, (String)"main-segs");
    public static final Keyword const__33 = RT.keyword(null, (String)"as-of-t");
    public static final Keyword const__34 = RT.keyword(null, (String)"N");
    public static final Keyword const__35 = RT.keyword(null, (String)"M");
    public static final Keyword const__42 = RT.keyword(null, (String)"I");
    public static final Keyword const__43 = RT.keyword(null, (String)"mid-segs");
    public static final Keyword const__44 = RT.keyword(null, (String)"TI");
    public static final Keyword const__45 = RT.keyword((String)"index", (String)"merge-mid");
    public static final Keyword const__46 = RT.keyword(null, (String)"S");
    public static final Var const__48 = RT.var((String)"datomic.index", (String)"BYTES_PER_SEG");
    public static final Keyword const__49 = RT.keyword((String)"event", (String)"least-pop-slice");
    public static final Keyword const__50 = RT.keyword(null, (String)"n-segs");
    public static final Var const__52 = RT.var((String)"clojure.core", (String)"take");
    public static final Var const__53 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__54 = RT.keyword(null, (String)"seg");
    public static final Keyword const__55 = RT.keyword(null, (String)"main-offset");
    public static final Keyword const__56 = RT.keyword(null, (String)"main-ratio");
    public static final Keyword const__57 = RT.keyword(null, (String)"main-pop");
    public static final Keyword const__58 = RT.keyword(null, (String)"mid-ratio");
    public static final Keyword const__59 = RT.keyword((String)"index", (String)"merge-slice");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__7__ = __site__7__;
    static final KeywordLookupSite __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__8__ = __site__8__;
    static final KeywordLookupSite __site__9__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__9__ = __site__9__;
    static final KeywordLookupSite __site__10__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__10__ = __site__10__;
    static final KeywordLookupSite __site__11__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__11__ = __site__11__;
    static final KeywordLookupSite __site__12__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__12__ = __site__12__;
    static final KeywordLookupSite __site__13__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__13__ = __site__13__;
    static final KeywordLookupSite __site__14__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__14__ = __site__14__;
    static final KeywordLookupSite __site__15__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__15__ = __site__15__;
    static final KeywordLookupSite __site__16__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__16__ = __site__16__;
    static final KeywordLookupSite __site__17__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__17__ = __site__17__;
    static final KeywordLookupSite __site__18__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__18__ = __site__18__;
    static final KeywordLookupSite __site__19__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__19__ = __site__19__;

    public index$merge_db_STAR_$build_tiered_index__15765(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.db = object;
        this.as_of_t = object2;
        this.root_map = object3;
        this.garbage = object4;
        this.xpreds = object5;
        this.cstore = object6;
        this.dirs_written_ref = object7;
        this.segs_written_ref = object8;
        this.olookup = object9;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public Object invoke(Object idx_name, Object midk, Object maink, Object histk, Object partfn, Object part_size, Object mem_idx, Object mid_idx, Object main_idx, Object cmp, Object cmpi, Object write_handlers2, Object idxcmp) {
        IPersistentMap iPersistentMap;
        Object object;
        IPersistentMap iPersistentMap2;
        Object object2;
        boolean or__5238__auto__15834;
        Object object3;
        IPersistentMap iPersistentMap3;
        Object object4 = partfn;
        partfn = null;
        Object object5 = write_handlers2;
        write_handlers2 = null;
        Object object6 = cmpi;
        cmpi = null;
        Object object7 = idxcmp;
        idxcmp = null;
        index$merge_db_STAR_$build_tiered_index__15765$build_index__15769 build_index2 = new index$merge_db_STAR_$build_tiered_index__15765$build_index__15769(object4, this.db, this.as_of_t, cmp, this.root_map, object5, object6, this.xpreds, object7, this.cstore, this.dirs_written_ref, this.segs_written_ref, this.olookup);
        Object written = ((IFn)const__0.getRawRoot()).invoke(this.segs_written_ref);
        Object dirs_written = ((IFn)const__0.getRawRoot()).invoke(this.dirs_written_ref);
        int count_mem_idx = RT.count((Object)mem_idx);
        Object object8 = cmp;
        cmp = null;
        ((IFn)const__2.getRawRoot()).invoke(object8);
        IPersistentMap m_15771 = RT.mapUniqueKeys((Object[])new Object[]{const__3, const__4, const__5, idx_name});
        Logger logger = LoggerFactory.getLogger((String)"datomic.index");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)m_15771, (Object)const__8, (Object)const__9)));
        }
        long start__8981__auto__15832 = System.nanoTime();
        Object result__8982__auto__15833 = ((IFn)new index$merge_db_STAR_$build_tiered_index__15765$fn__15774(mem_idx)).invoke();
        long elapsed_15772 = System.nanoTime() - start__8981__auto__15832;
        Object msec_15773 = ((IFn)const__11.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_15772));
        IFn iFn = (IFn)const__12.getRawRoot();
        IPersistentMap iPersistentMap4 = m_15771;
        m_15771 = null;
        Object object9 = msec_15773;
        msec_15773 = null;
        Object object10 = ((IFn)const__7.getRawRoot()).invoke((Object)iPersistentMap4, (Object)const__13, object9, (Object)const__8, (Object)const__14);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object11 = result__8982__auto__15833;
        Object object12 = iLookupThunk.get(object11);
        if (iLookupThunk == object12) {
            __thunk__0__ = __site__0__.fault(object11);
            object12 = __thunk__0__.get(object11);
        }
        if (object12 != null && object12 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__15;
            IFn iFn2 = (IFn)const__16.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object13 = result__8982__auto__15833;
            Object object14 = iLookupThunk2.get(object13);
            if (iLookupThunk2 == object14) {
                __thunk__1__ = __site__1__.fault(object13);
                object14 = __thunk__1__.get(object13);
            }
            objectArray[1] = iFn2.invoke(object14);
            iPersistentMap3 = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap3 = null;
        }
        Object endmsg__8984__auto__15830 = iFn.invoke(object10, iPersistentMap3);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.index");
        if (logger3.isInfoEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object15 = endmsg__8984__auto__15830;
            endmsg__8984__auto__15830 = null;
            logger4.info((String)((IFn)const__6.getRawRoot()).invoke(object15));
        }
        Object object16 = ((IFn)const__17.getRawRoot()).invoke(result__8982__auto__15833, (Object)const__18);
        if (object16 != null && object16 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object17 = result__8982__auto__15833;
            result__8982__auto__15833 = null;
            object3 = iLookupThunk3.get(object17);
            if (iLookupThunk3 == object3) {
                __thunk__2__ = __site__2__.fault(object17);
                object3 = __thunk__2__.get(object17);
            }
        } else {
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object18 = result__8982__auto__15833;
            result__8982__auto__15833 = null;
            Object object19 = iLookupThunk4.get(object18);
            if (iLookupThunk4 != object19) throw (Throwable)object19;
            __thunk__3__ = __site__3__.fault(object18);
            object19 = __thunk__3__.get(object18);
            throw (Throwable)object19;
        }
        Object N = object3;
        Object vec__15766 = ((IFn)const__19.getRawRoot()).invoke(this.olookup, main_idx, part_size);
        Object M = RT.nth((Object)vec__15766, (int)RT.uncheckedIntCast((long)0L), null);
        Object object20 = vec__15766;
        vec__15766 = null;
        Object main_segs = RT.nth((Object)object20, (int)RT.uncheckedIntCast((long)1L), null);
        Object object21 = ((IFn)const__23.getRawRoot()).invoke((Object)(Numbers.isZero((Object)N) ? Boolean.TRUE : Boolean.FALSE));
        Object M_to_N = object21 != null && object21 != Boolean.FALSE ? Numbers.divide((Object)M, (Object)N) : const__21;
        boolean and__5236__auto__15835 = Util.identical((Object)mid_idx, null);
        boolean bl = and__5236__auto__15835 ? ((or__5238__auto__15834 = Numbers.lt((Object)M, (Object)const__28.getRawRoot())) ? or__5238__auto__15834 : Numbers.lt((Object)M_to_N, (Object)const__29.getRawRoot())) : and__5236__auto__15835;
        if (bl) {
            IPersistentMap iPersistentMap5;
            Object[] objectArray = new Object[14];
            objectArray[0] = const__3;
            objectArray[1] = const__30;
            objectArray[2] = const__5;
            objectArray[3] = idx_name;
            objectArray[4] = const__31;
            objectArray[5] = count_mem_idx;
            objectArray[6] = const__32;
            Object object22 = main_segs;
            main_segs = null;
            objectArray[7] = object22;
            objectArray[8] = const__33;
            objectArray[9] = this.as_of_t;
            objectArray[10] = const__34;
            Object object23 = N;
            N = null;
            objectArray[11] = object23;
            objectArray[12] = const__35;
            Object object24 = M;
            M = null;
            objectArray[13] = object24;
            IPersistentMap m_15776 = RT.mapUniqueKeys((Object[])objectArray);
            Logger logger5 = LoggerFactory.getLogger((String)"datomic.index");
            if (logger5.isInfoEnabled()) {
                Logger logger6 = logger5;
                logger5 = null;
                logger6.info((String)((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)m_15776, (Object)const__8, (Object)const__9)));
            }
            long start__8981__auto__15838 = System.nanoTime();
            index$merge_db_STAR_$build_tiered_index__15765$build_index__15769 index$merge_db_STAR_$build_tiered_index__15765$build_index__15769 = build_index2;
            build_index2 = null;
            Object object25 = dirs_written;
            dirs_written = null;
            Object object26 = written;
            written = null;
            Object object27 = idx_name;
            idx_name = null;
            Object object28 = midk;
            midk = null;
            Object object29 = histk;
            histk = null;
            Object object30 = maink;
            maink = null;
            Object object31 = mem_idx;
            mem_idx = null;
            Object result__8982__auto__15839 = ((IFn)new index$merge_db_STAR_$build_tiered_index__15765$fn__15779((Object)index$merge_db_STAR_$build_tiered_index__15765$build_index__15769, object25, this.as_of_t, object26, this.root_map, count_mem_idx, this.garbage, object27, object28, this.xpreds, this.dirs_written_ref, object29, object30, this.segs_written_ref, object31)).invoke();
            long elapsed_15777 = System.nanoTime() - start__8981__auto__15838;
            Object msec_15778 = ((IFn)const__11.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_15777));
            IFn iFn3 = (IFn)const__12.getRawRoot();
            IPersistentMap iPersistentMap6 = m_15776;
            m_15776 = null;
            Object object32 = msec_15778;
            msec_15778 = null;
            Object object33 = ((IFn)const__7.getRawRoot()).invoke((Object)iPersistentMap6, (Object)const__13, object32, (Object)const__8, (Object)const__14);
            ILookupThunk iLookupThunk5 = __thunk__4__;
            Object object34 = result__8982__auto__15839;
            Object object35 = iLookupThunk5.get(object34);
            if (iLookupThunk5 == object35) {
                __thunk__4__ = __site__4__.fault(object34);
                object35 = __thunk__4__.get(object34);
            }
            if (object35 != null && object35 != Boolean.FALSE) {
                Object[] objectArray2 = new Object[2];
                objectArray2[0] = const__15;
                IFn iFn4 = (IFn)const__16.getRawRoot();
                ILookupThunk iLookupThunk6 = __thunk__5__;
                Object object36 = result__8982__auto__15839;
                Object object37 = iLookupThunk6.get(object36);
                if (iLookupThunk6 == object37) {
                    __thunk__5__ = __site__5__.fault(object36);
                    object37 = __thunk__5__.get(object36);
                }
                objectArray2[1] = iFn4.invoke(object37);
                iPersistentMap5 = RT.mapUniqueKeys((Object[])objectArray2);
            } else {
                iPersistentMap5 = null;
            }
            Object endmsg__8984__auto__15836 = iFn3.invoke(object33, iPersistentMap5);
            Logger logger7 = LoggerFactory.getLogger((String)"datomic.index");
            if (logger7.isInfoEnabled()) {
                Logger logger8 = logger7;
                logger7 = null;
                Object object38 = endmsg__8984__auto__15836;
                endmsg__8984__auto__15836 = null;
                logger8.info((String)((IFn)const__6.getRawRoot()).invoke(object38));
            }
            Object object39 = ((IFn)const__17.getRawRoot()).invoke(result__8982__auto__15839, (Object)const__18);
            if (object39 != null && object39 != Boolean.FALSE) {
                ILookupThunk iLookupThunk7 = __thunk__6__;
                Object object40 = result__8982__auto__15839;
                result__8982__auto__15839 = null;
                object2 = iLookupThunk7.get(object40);
                if (iLookupThunk7 != object2) return object2;
                __thunk__6__ = __site__6__.fault(object40);
                object2 = __thunk__6__.get(object40);
                return object2;
            }
            ILookupThunk iLookupThunk8 = __thunk__7__;
            Object object41 = result__8982__auto__15839;
            result__8982__auto__15839 = null;
            Object object42 = iLookupThunk8.get(object41);
            if (iLookupThunk8 != object42) throw (Throwable)object42;
            __thunk__7__ = __site__7__.fault(object41);
            object42 = __thunk__7__.get(object41);
            throw (Throwable)object42;
        }
        Object object43 = M_to_N;
        M_to_N = null;
        double TI = Numbers.unchecked_multiply((double)Math.sqrt(RT.uncheckedDoubleCast((Object)((Number)object43))), (Object)N);
        Object vec__15788 = ((IFn)const__19.getRawRoot()).invoke(this.olookup, mid_idx, part_size);
        Object I = RT.nth((Object)vec__15788, (int)RT.uncheckedIntCast((long)0L), null);
        Object object44 = vec__15788;
        vec__15788 = null;
        Object mid_segs = RT.nth((Object)object44, (int)RT.uncheckedIntCast((long)1L), null);
        double TI2 = Numbers.lt((Object)I, (Object)M) ? Numbers.max((double)TI, (double)Numbers.unchecked_multiply((Object)I, (double)Numbers.unchecked_minus((double)1.0, (Object)Numbers.divide((Object)I, (Object)M)))) : TI;
        Object TI3 = Numbers.max((double)TI2, (long)2000000L);
        Number S = Numbers.unchecked_minus((Object)Numbers.unchecked_add((Object)I, (Object)N), (Object)TI3);
        if (Numbers.isNeg((Object)S)) {
            IPersistentMap iPersistentMap7;
            Object[] objectArray = new Object[20];
            objectArray[0] = const__35;
            Object object45 = M;
            M = null;
            objectArray[1] = object45;
            objectArray[2] = const__42;
            Object object46 = I;
            I = null;
            objectArray[3] = object46;
            objectArray[4] = const__5;
            objectArray[5] = idx_name;
            objectArray[6] = const__43;
            Object object47 = mid_segs;
            mid_segs = null;
            objectArray[7] = object47;
            objectArray[8] = const__44;
            Object object48 = TI3;
            TI3 = null;
            objectArray[9] = object48;
            objectArray[10] = const__3;
            objectArray[11] = const__45;
            objectArray[12] = const__31;
            objectArray[13] = count_mem_idx;
            objectArray[14] = const__46;
            Number number = S;
            S = null;
            objectArray[15] = number;
            objectArray[16] = const__34;
            Object object49 = N;
            N = null;
            objectArray[17] = object49;
            objectArray[18] = const__33;
            objectArray[19] = this.as_of_t;
            IPersistentMap m_15791 = RT.mapUniqueKeys((Object[])objectArray);
            Logger logger9 = LoggerFactory.getLogger((String)"datomic.index");
            if (logger9.isInfoEnabled()) {
                Logger logger10 = logger9;
                logger9 = null;
                logger10.info((String)((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)m_15791, (Object)const__8, (Object)const__9)));
            }
            long start__8981__auto__15842 = System.nanoTime();
            index$merge_db_STAR_$build_tiered_index__15765$build_index__15769 index$merge_db_STAR_$build_tiered_index__15765$build_index__15769 = build_index2;
            build_index2 = null;
            Object object50 = dirs_written;
            dirs_written = null;
            Object object51 = written;
            written = null;
            Object object52 = idx_name;
            idx_name = null;
            Object object53 = midk;
            midk = null;
            Object object54 = histk;
            histk = null;
            Object object55 = maink;
            maink = null;
            Object object56 = mem_idx;
            mem_idx = null;
            Object result__8982__auto__15843 = ((IFn)new index$merge_db_STAR_$build_tiered_index__15765$fn__15794((Object)index$merge_db_STAR_$build_tiered_index__15765$build_index__15769, object50, this.as_of_t, object51, this.root_map, count_mem_idx, this.garbage, object52, object53, this.xpreds, this.dirs_written_ref, object54, object55, this.segs_written_ref, object56)).invoke();
            long elapsed_15792 = System.nanoTime() - start__8981__auto__15842;
            Object msec_15793 = ((IFn)const__11.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_15792));
            IFn iFn5 = (IFn)const__12.getRawRoot();
            IPersistentMap iPersistentMap8 = m_15791;
            m_15791 = null;
            Object object57 = msec_15793;
            msec_15793 = null;
            Object object58 = ((IFn)const__7.getRawRoot()).invoke((Object)iPersistentMap8, (Object)const__13, object57, (Object)const__8, (Object)const__14);
            ILookupThunk iLookupThunk9 = __thunk__8__;
            Object object59 = result__8982__auto__15843;
            Object object60 = iLookupThunk9.get(object59);
            if (iLookupThunk9 == object60) {
                __thunk__8__ = __site__8__.fault(object59);
                object60 = __thunk__8__.get(object59);
            }
            if (object60 != null && object60 != Boolean.FALSE) {
                Object[] objectArray3 = new Object[2];
                objectArray3[0] = const__15;
                IFn iFn6 = (IFn)const__16.getRawRoot();
                ILookupThunk iLookupThunk10 = __thunk__9__;
                Object object61 = result__8982__auto__15843;
                Object object62 = iLookupThunk10.get(object61);
                if (iLookupThunk10 == object62) {
                    __thunk__9__ = __site__9__.fault(object61);
                    object62 = __thunk__9__.get(object61);
                }
                objectArray3[1] = iFn6.invoke(object62);
                iPersistentMap7 = RT.mapUniqueKeys((Object[])objectArray3);
            } else {
                iPersistentMap7 = null;
            }
            Object endmsg__8984__auto__15840 = iFn5.invoke(object58, iPersistentMap7);
            Logger logger11 = LoggerFactory.getLogger((String)"datomic.index");
            if (logger11.isInfoEnabled()) {
                Logger logger12 = logger11;
                logger11 = null;
                Object object63 = endmsg__8984__auto__15840;
                endmsg__8984__auto__15840 = null;
                logger12.info((String)((IFn)const__6.getRawRoot()).invoke(object63));
            }
            Object object64 = ((IFn)const__17.getRawRoot()).invoke(result__8982__auto__15843, (Object)const__18);
            if (object64 != null && object64 != Boolean.FALSE) {
                ILookupThunk iLookupThunk11 = __thunk__10__;
                Object object65 = result__8982__auto__15843;
                result__8982__auto__15843 = null;
                object2 = iLookupThunk11.get(object65);
                if (iLookupThunk11 != object2) return object2;
                __thunk__10__ = __site__10__.fault(object65);
                object2 = __thunk__10__.get(object65);
                return object2;
            }
            ILookupThunk iLookupThunk12 = __thunk__11__;
            Object object66 = result__8982__auto__15843;
            result__8982__auto__15843 = null;
            Object object67 = iLookupThunk12.get(object66);
            if (iLookupThunk12 != object67) throw (Throwable)object67;
            __thunk__11__ = __site__11__.fault(object66);
            object67 = __thunk__11__.get(object66);
            throw (Throwable)object67;
        }
        long size = RT.longCast((Object)Numbers.divide((Object)S, (Object)const__48.getRawRoot()));
        long n_segs = size < 1L ? 1L : size;
        index$merge_db_STAR_$build_tiered_index__15765$ratio__15805 ratio = new index$merge_db_STAR_$build_tiered_index__15765$ratio__15805();
        IPersistentMap m_15807 = RT.mapUniqueKeys((Object[])new Object[]{const__3, const__49, const__5, idx_name, const__50, Numbers.num((long)n_segs)});
        Logger logger13 = LoggerFactory.getLogger((String)"datomic.index");
        if (logger13.isInfoEnabled()) {
            Logger logger14 = logger13;
            logger13 = null;
            logger14.info((String)((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)m_15807, (Object)const__8, (Object)const__9)));
        }
        long start__8981__auto__15846 = System.nanoTime();
        Object object68 = part_size;
        part_size = null;
        Object object69 = main_idx;
        main_idx = null;
        Object result__8982__auto__15847 = ((IFn)new index$merge_db_STAR_$build_tiered_index__15765$fn__15810(n_segs, object68, mid_idx, object69, this.olookup)).invoke();
        long elapsed_15808 = System.nanoTime() - start__8981__auto__15846;
        Object msec_15809 = ((IFn)const__11.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_15808));
        IFn iFn7 = (IFn)const__12.getRawRoot();
        IPersistentMap iPersistentMap9 = m_15807;
        m_15807 = null;
        Object object70 = msec_15809;
        msec_15809 = null;
        Object object71 = ((IFn)const__7.getRawRoot()).invoke((Object)iPersistentMap9, (Object)const__13, object70, (Object)const__8, (Object)const__14);
        ILookupThunk iLookupThunk13 = __thunk__12__;
        Object object72 = result__8982__auto__15847;
        Object object73 = iLookupThunk13.get(object72);
        if (iLookupThunk13 == object73) {
            __thunk__12__ = __site__12__.fault(object72);
            object73 = __thunk__12__.get(object72);
        }
        if (object73 != null && object73 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__15;
            IFn iFn8 = (IFn)const__16.getRawRoot();
            ILookupThunk iLookupThunk14 = __thunk__13__;
            Object object74 = result__8982__auto__15847;
            Object object75 = iLookupThunk14.get(object74);
            if (iLookupThunk14 == object75) {
                __thunk__13__ = __site__13__.fault(object74);
                object75 = __thunk__13__.get(object74);
            }
            objectArray[1] = iFn8.invoke(object75);
            iPersistentMap2 = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap2 = null;
        }
        Object endmsg__8984__auto__15844 = iFn7.invoke(object71, iPersistentMap2);
        Logger logger15 = LoggerFactory.getLogger((String)"datomic.index");
        if (logger15.isInfoEnabled()) {
            Logger logger16 = logger15;
            logger15 = null;
            Object object76 = endmsg__8984__auto__15844;
            endmsg__8984__auto__15844 = null;
            logger16.info((String)((IFn)const__6.getRawRoot()).invoke(object76));
        }
        Object object77 = ((IFn)const__17.getRawRoot()).invoke(result__8982__auto__15847, (Object)const__18);
        if (object77 != null && object77 != Boolean.FALSE) {
            ILookupThunk iLookupThunk15 = __thunk__14__;
            Object object78 = result__8982__auto__15847;
            result__8982__auto__15847 = null;
            object = iLookupThunk15.get(object78);
            if (iLookupThunk15 == object) {
                __thunk__14__ = __site__14__.fault(object78);
                object = __thunk__14__.get(object78);
            }
        } else {
            ILookupThunk iLookupThunk16 = __thunk__15__;
            Object object79 = result__8982__auto__15847;
            result__8982__auto__15847 = null;
            Object object80 = iLookupThunk16.get(object79);
            if (iLookupThunk16 != object80) throw (Throwable)object80;
            __thunk__15__ = __site__15__.fault(object79);
            object80 = __thunk__15__.get(object79);
            throw (Throwable)object80;
        }
        Object vec__15802 = object;
        Object start_key = RT.nth((Object)vec__15802, (int)RT.uncheckedIntCast((long)0L), null);
        Object main_pop = RT.nth((Object)vec__15802, (int)RT.uncheckedIntCast((long)1L), null);
        Object object81 = vec__15802;
        vec__15802 = null;
        Object main_offset = RT.nth((Object)object81, (int)RT.uncheckedIntCast((long)2L), null);
        Object object82 = mid_idx;
        mid_idx = null;
        Object object83 = start_key;
        start_key = null;
        Object slice_segids = ((IFn)const__52.getRawRoot()).invoke((Object)Numbers.num((long)n_segs), ((IFn)const__53.getRawRoot()).invoke((Object)const__54, ((TreeIter)((Index)object82).seek(object83)).dir_seq()));
        Object[] objectArray = new Object[32];
        objectArray[0] = const__35;
        Object object84 = M;
        M = null;
        objectArray[1] = object84;
        objectArray[2] = const__42;
        Object object85 = I;
        I = null;
        objectArray[3] = object85;
        objectArray[4] = const__32;
        objectArray[5] = main_segs;
        objectArray[6] = const__5;
        objectArray[7] = idx_name;
        objectArray[8] = const__55;
        Object object86 = main_offset;
        main_offset = null;
        objectArray[9] = object86;
        objectArray[10] = const__56;
        Object object87 = main_segs;
        main_segs = null;
        objectArray[11] = ((IFn)ratio).invoke(main_pop, object87);
        objectArray[12] = const__43;
        objectArray[13] = mid_segs;
        objectArray[14] = const__57;
        Object object88 = main_pop;
        main_pop = null;
        objectArray[15] = object88;
        objectArray[16] = const__50;
        objectArray[17] = Numbers.num((long)n_segs);
        objectArray[18] = const__58;
        index$merge_db_STAR_$build_tiered_index__15765$ratio__15805 index$merge_db_STAR_$build_tiered_index__15765$ratio__15805 = ratio;
        ratio = null;
        Object object89 = mid_segs;
        mid_segs = null;
        objectArray[19] = ((IFn)index$merge_db_STAR_$build_tiered_index__15765$ratio__15805).invoke((Object)Numbers.num((long)n_segs), object89);
        objectArray[20] = const__44;
        Object object90 = TI3;
        TI3 = null;
        objectArray[21] = object90;
        objectArray[22] = const__3;
        objectArray[23] = const__59;
        objectArray[24] = const__31;
        objectArray[25] = count_mem_idx;
        objectArray[26] = const__46;
        Number number = S;
        S = null;
        objectArray[27] = number;
        objectArray[28] = const__34;
        Object object91 = N;
        N = null;
        objectArray[29] = object91;
        objectArray[30] = const__33;
        objectArray[31] = this.as_of_t;
        IPersistentMap m_15812 = RT.mapUniqueKeys((Object[])objectArray);
        Logger logger17 = LoggerFactory.getLogger((String)"datomic.index");
        if (logger17.isInfoEnabled()) {
            Logger logger18 = logger17;
            logger17 = null;
            logger18.info((String)((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)m_15812, (Object)const__8, (Object)const__9)));
        }
        long start__8981__auto__15850 = System.nanoTime();
        index$merge_db_STAR_$build_tiered_index__15765$build_index__15769 index$merge_db_STAR_$build_tiered_index__15765$build_index__15769 = build_index2;
        build_index2 = null;
        Object object92 = dirs_written;
        dirs_written = null;
        Object object93 = written;
        written = null;
        Object object94 = slice_segids;
        slice_segids = null;
        Object object95 = idx_name;
        idx_name = null;
        Object object96 = midk;
        midk = null;
        Object object97 = histk;
        histk = null;
        Object object98 = maink;
        maink = null;
        Object object99 = mem_idx;
        mem_idx = null;
        Object result__8982__auto__15851 = ((IFn)new index$merge_db_STAR_$build_tiered_index__15765$fn__15815((Object)index$merge_db_STAR_$build_tiered_index__15765$build_index__15769, object92, this.as_of_t, object93, object94, this.root_map, count_mem_idx, this.garbage, object95, object96, this.xpreds, this.dirs_written_ref, object97, object98, this.segs_written_ref, this.olookup, object99)).invoke();
        long elapsed_15813 = System.nanoTime() - start__8981__auto__15850;
        Object msec_15814 = ((IFn)const__11.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_15813));
        IFn iFn9 = (IFn)const__12.getRawRoot();
        IPersistentMap iPersistentMap10 = m_15812;
        m_15812 = null;
        Object object100 = msec_15814;
        msec_15814 = null;
        Object object101 = ((IFn)const__7.getRawRoot()).invoke((Object)iPersistentMap10, (Object)const__13, object100, (Object)const__8, (Object)const__14);
        ILookupThunk iLookupThunk17 = __thunk__16__;
        Object object102 = result__8982__auto__15851;
        Object object103 = iLookupThunk17.get(object102);
        if (iLookupThunk17 == object103) {
            __thunk__16__ = __site__16__.fault(object102);
            object103 = __thunk__16__.get(object102);
        }
        if (object103 != null && object103 != Boolean.FALSE) {
            Object[] objectArray4 = new Object[2];
            objectArray4[0] = const__15;
            IFn iFn10 = (IFn)const__16.getRawRoot();
            ILookupThunk iLookupThunk18 = __thunk__17__;
            Object object104 = result__8982__auto__15851;
            Object object105 = iLookupThunk18.get(object104);
            if (iLookupThunk18 == object105) {
                __thunk__17__ = __site__17__.fault(object104);
                object105 = __thunk__17__.get(object104);
            }
            objectArray4[1] = iFn10.invoke(object105);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray4);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__15848 = iFn9.invoke(object101, iPersistentMap);
        Logger logger19 = LoggerFactory.getLogger((String)"datomic.index");
        if (logger19.isInfoEnabled()) {
            Logger logger20 = logger19;
            logger19 = null;
            Object object106 = endmsg__8984__auto__15848;
            endmsg__8984__auto__15848 = null;
            logger20.info((String)((IFn)const__6.getRawRoot()).invoke(object106));
        }
        Object object107 = ((IFn)const__17.getRawRoot()).invoke(result__8982__auto__15851, (Object)const__18);
        if (object107 != null && object107 != Boolean.FALSE) {
            ILookupThunk iLookupThunk19 = __thunk__18__;
            Object object108 = result__8982__auto__15851;
            result__8982__auto__15851 = null;
            object2 = iLookupThunk19.get(object108);
            if (iLookupThunk19 != object2) return object2;
            __thunk__18__ = __site__18__.fault(object108);
            object2 = __thunk__18__.get(object108);
            return object2;
        } else {
            ILookupThunk iLookupThunk20 = __thunk__19__;
            Object object109 = result__8982__auto__15851;
            result__8982__auto__15851 = null;
            Object object110 = iLookupThunk20.get(object109);
            if (iLookupThunk20 != object110) throw (Throwable)object110;
            __thunk__19__ = __site__19__.fault(object109);
            object110 = __thunk__19__.get(object109);
            throw (Throwable)object110;
        }
    }
}

