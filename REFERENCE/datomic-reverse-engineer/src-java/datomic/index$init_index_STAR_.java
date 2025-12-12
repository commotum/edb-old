/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db.Db;
import datomic.db.IndexSet;
import datomic.index$init_index_STAR_$fn__15265;
import datomic.index$init_index_STAR_$fn__15268;
import datomic.index$init_index_STAR_$fn__15273;
import datomic.index$init_index_STAR_$fn__15276;

public final class index$init_index_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"tenant");
    public static final Keyword const__4 = RT.keyword(null, (String)"db");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"bootstrap-db");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"repeatedly");
    public static final Var const__7 = RT.var((String)"datomic.common", (String)"rand-uuid");
    public static final Object const__9 = 0L;
    public static final Object const__11 = 2L;
    public static final Var const__20 = RT.var((String)"datomic.index", (String)"fress");
    public static final Keyword const__21 = RT.keyword(null, (String)"birth-level");
    public static final Keyword const__22 = RT.keyword(null, (String)"nextT");
    public static final Keyword const__23 = RT.keyword(null, (String)"schema-level");
    public static final Keyword const__24 = RT.keyword(null, (String)"eavt-main");
    public static final Keyword const__25 = RT.keyword(null, (String)"avet-mid");
    public static final Keyword const__26 = RT.keyword(null, (String)"aevt-main");
    public static final Keyword const__27 = RT.keyword(null, (String)"rev");
    public static final Keyword const__28 = RT.keyword(null, (String)"raet-main");
    public static final Keyword const__29 = RT.keyword(null, (String)"raet-mid");
    public static final Keyword const__30 = RT.keyword(null, (String)"buildRevision");
    public static final Var const__31 = RT.var((String)"datomic.config", (String)"property");
    public static final Keyword const__32 = RT.keyword(null, (String)"avet-hist");
    public static final Keyword const__33 = RT.keyword(null, (String)"eavt-hist");
    public static final Keyword const__34 = RT.keyword(null, (String)"raet-hist");
    public static final Keyword const__35 = RT.keyword(null, (String)"aevt-mid");
    public static final Keyword const__36 = RT.keyword(null, (String)"version");
    public static final Keyword const__37 = RT.keyword(null, (String)"avet-main");
    public static final Keyword const__38 = RT.keyword(null, (String)"aevt-hist");
    public static final Keyword const__39 = RT.keyword(null, (String)"eavt-mid");
    public static final Var const__40 = RT.var((String)"datomic.index", (String)"common-write-handlers");
    public static final Var const__41 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__42 = RT.var((String)"datomic.index", (String)"root-node");
    public static final Var const__43 = RT.var((String)"datomic.index", (String)"transpose");
    public static final Var const__44 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__45 = RT.var((String)"datomic.index", (String)"dir-node");
    public static final AFn const__47 = (AFn)Tuple.create((Object)0L);
    public static final AFn const__49 = (AFn)Tuple.create((Object)0L);
    public static final Var const__50 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__51 = RT.var((String)"clojure.core", (String)"partition-by");
    public static final Var const__52 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__53 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Var const__54 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__55 = RT.var((String)"datomic.index", (String)"write-vals");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"birth-level"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"schema-level"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object cstore) {
        Object dbname;
        Object object;
        Object map__15255 = cstore;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(map__15255);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = map__15255;
            map__15255 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object3)));
        } else {
            object = map__15255;
            map__15255 = null;
        }
        Object map__152552 = object;
        RT.get((Object)map__152552, (Object)const__3);
        Object object4 = map__152552;
        map__152552 = null;
        Object object5 = dbname = RT.get((Object)object4, (Object)const__4);
        dbname = null;
        Object db2 = ((IFn)const__5.getRawRoot()).invoke(object5);
        Object iset = ((Db)db2).memidx;
        Object vec__15256 = ((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot());
        Object rootid = RT.nth((Object)vec__15256, (int)RT.uncheckedIntCast((long)0L), null);
        Object eavtid = RT.nth((Object)vec__15256, (int)RT.uncheckedIntCast((long)1L), null);
        Object avetid = RT.nth((Object)vec__15256, (int)RT.uncheckedIntCast((long)2L), null);
        Object aevtid = RT.nth((Object)vec__15256, (int)RT.uncheckedIntCast((long)3L), null);
        Object raetid = RT.nth((Object)vec__15256, (int)RT.uncheckedIntCast((long)4L), null);
        Object eavt_dirid = RT.nth((Object)vec__15256, (int)RT.uncheckedIntCast((long)5L), null);
        Object avet_dirid = RT.nth((Object)vec__15256, (int)RT.uncheckedIntCast((long)6L), null);
        Object aevt_dirid = RT.nth((Object)vec__15256, (int)RT.uncheckedIntCast((long)7L), null);
        Object raet_dirid = RT.nth((Object)vec__15256, (int)RT.uncheckedIntCast((long)8L), null);
        Object eavt_segid = RT.nth((Object)vec__15256, (int)RT.uncheckedIntCast((long)9L), null);
        Object object6 = vec__15256;
        vec__15256 = null;
        Object raet_segid = RT.nth((Object)object6, (int)RT.uncheckedIntCast((long)10L), null);
        IFn iFn = (IFn)const__20.getRawRoot();
        Object[] objectArray = new Object[36];
        objectArray[0] = const__21;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = db2;
        Object object8 = iLookupThunk.get(object7);
        if (iLookupThunk == object8) {
            __thunk__0__ = __site__0__.fault(object7);
            object8 = __thunk__0__.get(object7);
        }
        objectArray[1] = object8;
        objectArray[2] = const__22;
        objectArray[3] = Numbers.num((long)((Db)db2).nextT());
        objectArray[4] = const__23;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object9 = db2;
        db2 = null;
        Object object10 = iLookupThunk2.get(object9);
        if (iLookupThunk2 == object10) {
            __thunk__1__ = __site__1__.fault(object9);
            object10 = __thunk__1__.get(object9);
        }
        objectArray[5] = object10;
        objectArray[6] = const__24;
        objectArray[7] = eavtid;
        objectArray[8] = const__25;
        objectArray[9] = null;
        objectArray[10] = const__26;
        objectArray[11] = aevtid;
        objectArray[12] = const__27;
        objectArray[13] = const__9;
        objectArray[14] = const__28;
        objectArray[15] = raetid;
        objectArray[16] = const__29;
        objectArray[17] = null;
        objectArray[18] = const__30;
        objectArray[19] = ((IFn)const__31.getRawRoot()).invoke((Object)"datomic.buildRevision");
        objectArray[20] = const__32;
        objectArray[21] = null;
        objectArray[22] = const__33;
        objectArray[23] = null;
        objectArray[24] = const__34;
        objectArray[25] = null;
        objectArray[26] = const__35;
        objectArray[27] = null;
        objectArray[28] = const__36;
        objectArray[29] = const__11;
        objectArray[30] = const__37;
        objectArray[31] = avetid;
        objectArray[32] = const__38;
        objectArray[33] = null;
        objectArray[34] = const__39;
        objectArray[35] = null;
        Object root = iFn.invoke((Object)RT.mapUniqueKeys((Object[])objectArray), const__40.getRawRoot());
        Object eavt2 = ((IFn)const__41.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IndexSet)iset).eavt));
        Object eavt_root = ((IFn)const__20.getRawRoot()).invoke(((IFn)const__42.getRawRoot()).invoke(((IFn)const__43.getRawRoot()).invoke((Object)Tuple.create((Object)((IFn)eavt2).invoke(const__9))), ((IFn)const__44.getRawRoot()).invoke((Object)Tuple.create((Object)eavt_dirid))), const__40.getRawRoot());
        Object eavt_dir = ((IFn)const__20.getRawRoot()).invoke(((IFn)const__45.getRawRoot()).invoke(((IFn)const__43.getRawRoot()).invoke((Object)Tuple.create((Object)((IFn)eavt2).invoke(const__9))), ((IFn)const__44.getRawRoot()).invoke((Object)Tuple.create((Object)eavt_segid)), (Object)Numbers.int_array((Object)const__47), (Object)Numbers.int_array((Object)Tuple.create((Object)RT.count((Object)eavt2)))), const__40.getRawRoot());
        Object object11 = eavt2;
        eavt2 = null;
        Object eavt_seg = ((IFn)const__20.getRawRoot()).invoke(((IFn)const__43.getRawRoot()).invoke(object11), const__40.getRawRoot());
        Object raet2 = ((IFn)const__41.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IndexSet)iset).raet));
        Object raet_root = ((IFn)const__20.getRawRoot()).invoke(((IFn)const__42.getRawRoot()).invoke(((IFn)const__43.getRawRoot()).invoke((Object)Tuple.create((Object)((IFn)raet2).invoke(const__9))), ((IFn)const__44.getRawRoot()).invoke((Object)Tuple.create((Object)raet_dirid))), const__40.getRawRoot());
        Object raet_dir = ((IFn)const__20.getRawRoot()).invoke(((IFn)const__45.getRawRoot()).invoke(((IFn)const__43.getRawRoot()).invoke((Object)Tuple.create((Object)((IFn)raet2).invoke(const__9))), ((IFn)const__44.getRawRoot()).invoke((Object)Tuple.create((Object)raet_segid)), (Object)Numbers.int_array((Object)const__49), (Object)Numbers.int_array((Object)Tuple.create((Object)RT.count((Object)raet2)))), const__40.getRawRoot());
        Object object12 = raet2;
        raet2 = null;
        Object raet_seg = ((IFn)const__20.getRawRoot()).invoke(((IFn)const__43.getRawRoot()).invoke(object12), const__40.getRawRoot());
        Object avet2 = ((IFn)const__41.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IndexSet)iset).avet));
        Object avets = ((IFn)const__50.getRawRoot()).invoke(const__41.getRawRoot(), ((IFn)const__51.getRawRoot()).invoke((Object)new index$init_index_STAR_$fn__15265(), avet2));
        Object object13 = avet2;
        avet2 = null;
        Object avet_root = ((IFn)const__20.getRawRoot()).invoke(((IFn)const__42.getRawRoot()).invoke(((IFn)const__43.getRawRoot()).invoke((Object)Tuple.create((Object)((IFn)object13).invoke(const__9))), ((IFn)const__44.getRawRoot()).invoke((Object)Tuple.create((Object)avet_dirid))), const__40.getRawRoot());
        Object object14 = avets;
        avets = null;
        Object vec__15259 = ((IFn)const__52.getRawRoot()).invoke((Object)new index$init_index_STAR_$fn__15268(), (Object)Tuple.create((Object)PersistentVector.EMPTY, (Object)PersistentVector.EMPTY, (Object)PersistentVector.EMPTY, (Object)PersistentVector.EMPTY, (Object)PersistentArrayMap.EMPTY), object14);
        Object avet_keys = RT.nth((Object)vec__15259, (int)RT.uncheckedIntCast((long)0L), null);
        Object avet_ids = RT.nth((Object)vec__15259, (int)RT.uncheckedIntCast((long)1L), null);
        Object avet_offsets = RT.nth((Object)vec__15259, (int)RT.uncheckedIntCast((long)2L), null);
        Object avet_counts = RT.nth((Object)vec__15259, (int)RT.uncheckedIntCast((long)3L), null);
        Object object15 = vec__15259;
        vec__15259 = null;
        Object bufmap = RT.nth((Object)object15, (int)RT.uncheckedIntCast((long)4L), null);
        Object object16 = avet_keys;
        avet_keys = null;
        Object object17 = avet_ids;
        avet_ids = null;
        Object object18 = avet_offsets;
        avet_offsets = null;
        Object object19 = avet_counts;
        avet_counts = null;
        Object avet_dir = ((IFn)const__20.getRawRoot()).invoke(((IFn)const__45.getRawRoot()).invoke(((IFn)const__43.getRawRoot()).invoke(object16), ((IFn)const__44.getRawRoot()).invoke(object17), ((IFn)const__53.getRawRoot()).invoke(Integer.TYPE, object18), ((IFn)const__53.getRawRoot()).invoke(Integer.TYPE, object19)), const__40.getRawRoot());
        Object object20 = iset;
        iset = null;
        Object aevt2 = ((IFn)const__41.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IndexSet)object20).aevt));
        Object aevts = ((IFn)const__50.getRawRoot()).invoke(const__41.getRawRoot(), ((IFn)const__51.getRawRoot()).invoke((Object)new index$init_index_STAR_$fn__15273(), aevt2));
        Object object21 = aevt2;
        aevt2 = null;
        Object aevt_root = ((IFn)const__20.getRawRoot()).invoke(((IFn)const__42.getRawRoot()).invoke(((IFn)const__43.getRawRoot()).invoke((Object)Tuple.create((Object)((IFn)object21).invoke(const__9))), ((IFn)const__44.getRawRoot()).invoke((Object)Tuple.create((Object)aevt_dirid))), const__40.getRawRoot());
        Object object22 = bufmap;
        bufmap = null;
        Object object23 = aevts;
        aevts = null;
        Object vec__15262 = ((IFn)const__52.getRawRoot()).invoke((Object)new index$init_index_STAR_$fn__15276(), (Object)Tuple.create((Object)PersistentVector.EMPTY, (Object)PersistentVector.EMPTY, (Object)PersistentVector.EMPTY, (Object)PersistentVector.EMPTY, (Object)object22), object23);
        Object aevt_keys = RT.nth((Object)vec__15262, (int)RT.uncheckedIntCast((long)0L), null);
        Object aevt_ids = RT.nth((Object)vec__15262, (int)RT.uncheckedIntCast((long)1L), null);
        Object aevt_offsets = RT.nth((Object)vec__15262, (int)RT.uncheckedIntCast((long)2L), null);
        Object aevt_counts = RT.nth((Object)vec__15262, (int)RT.uncheckedIntCast((long)3L), null);
        Object object24 = vec__15262;
        vec__15262 = null;
        Object bufmap2 = RT.nth((Object)object24, (int)RT.uncheckedIntCast((long)4L), null);
        Object object25 = aevt_keys;
        aevt_keys = null;
        Object object26 = aevt_ids;
        aevt_ids = null;
        Object object27 = aevt_offsets;
        aevt_offsets = null;
        Object object28 = aevt_counts;
        aevt_counts = null;
        Object aevt_dir = ((IFn)const__20.getRawRoot()).invoke(((IFn)const__45.getRawRoot()).invoke(((IFn)const__43.getRawRoot()).invoke(object25), ((IFn)const__44.getRawRoot()).invoke(object26), ((IFn)const__53.getRawRoot()).invoke(Integer.TYPE, object27), ((IFn)const__53.getRawRoot()).invoke(Integer.TYPE, object28)), const__40.getRawRoot());
        Object object29 = bufmap2;
        bufmap2 = null;
        Object object30 = root;
        root = null;
        Object object31 = eavtid;
        eavtid = null;
        Object object32 = eavt_root;
        eavt_root = null;
        Object object33 = eavt_dirid;
        eavt_dirid = null;
        Object object34 = eavt_dir;
        eavt_dir = null;
        Object object35 = eavt_segid;
        eavt_segid = null;
        Object object36 = eavt_seg;
        eavt_seg = null;
        Object object37 = raetid;
        raetid = null;
        Object object38 = raet_root;
        raet_root = null;
        Object object39 = raet_dirid;
        raet_dirid = null;
        Object object40 = raet_dir;
        raet_dir = null;
        Object object41 = raet_segid;
        raet_segid = null;
        Object object42 = raet_seg;
        raet_seg = null;
        Object object43 = avetid;
        avetid = null;
        Object object44 = avet_root;
        avet_root = null;
        Object object45 = avet_dirid;
        avet_dirid = null;
        Object object46 = avet_dir;
        avet_dir = null;
        Object object47 = aevtid;
        aevtid = null;
        Object[] objectArray2 = new Object[3];
        Object object48 = aevt_root;
        aevt_root = null;
        objectArray2[0] = object48;
        Object object49 = aevt_dirid;
        aevt_dirid = null;
        objectArray2[1] = object49;
        Object object50 = aevt_dir;
        aevt_dir = null;
        objectArray2[2] = object50;
        Object bufmap3 = ((IFn)const__54.getRawRoot()).invoke(object29, rootid, object30, object31, object32, object33, object34, object35, object36, object37, object38, object39, object40, object41, object42, object43, object44, object45, object46, object47, objectArray2);
        Object object51 = cstore;
        cstore = null;
        Object object52 = bufmap3;
        bufmap3 = null;
        ((IFn)const__55.getRawRoot()).invoke(object51, object52);
        Object object53 = rootid;
        rootid = null;
        return object53;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index$init_index_STAR_.invokeStatic(object2);
    }
}

