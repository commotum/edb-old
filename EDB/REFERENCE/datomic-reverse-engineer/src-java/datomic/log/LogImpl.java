/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.APersistentMap
 *  clojure.lang.Counted
 *  clojure.lang.IFn
 *  clojure.lang.IHashEq
 *  clojure.lang.IKeywordLookup
 *  clojure.lang.ILookup
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IMapEntry
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentCollection
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IRecord
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.MapEntry
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.RecordIterator
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic.log;

import clojure.lang.AFn;
import clojure.lang.APersistentMap;
import clojure.lang.Counted;
import clojure.lang.IFn;
import clojure.lang.IHashEq;
import clojure.lang.IKeywordLookup;
import clojure.lang.ILookup;
import clojure.lang.ILookupThunk;
import clojure.lang.IMapEntry;
import clojure.lang.IObj;
import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.IRecord;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.MapEntry;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.RecordIterator;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.iter.Iter;
import datomic.log.Log;
import datomic.log.LogDir;
import datomic.log.LogImpl$fn__16307;
import datomic.log.LogImpl$fn__16309;
import datomic.log.LogImpl$fn__16316;
import datomic.log.LogImpl$reify__16300;
import datomic.log.LogImpl$reify__16302;
import datomic.log.LogImpl$reify__16304;
import datomic.log.LogSeek;
import datomic.log.LogTxIter;
import datomic.log.TailTxes;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LogImpl
implements Log,
LogSeek,
TailTxes,
IRecord,
IHashEq,
IObj,
ILookup,
IKeywordLookup,
IPersistentMap,
Map,
Serializable {
    public final Object olookup;
    public final Object desc;
    public final Object tail;
    public final Object __meta;
    public final Object __extmap;
    int __hash;
    int __hasheq;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    private static Class __cached_class__4;
    private static Class __cached_class__5;
    private static Class __cached_class__6;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__4;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final Keyword const__9;
    public static final Keyword const__10;
    public static final AFn const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final AFn const__18;
    public static final Var const__19;
    public static final Var const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Var const__24;
    public static final Var const__33;
    public static final Var const__34;
    public static final Var const__35;
    public static final Var const__36;
    public static final Var const__37;
    public static final Var const__38;
    public static final Var const__40;
    public static final Var const__41;
    public static final Var const__43;
    public static final Var const__45;
    public static final Keyword const__46;
    public static final Var const__47;
    public static final Var const__48;
    public static final Var const__49;
    public static final Var const__50;
    public static final Var const__51;
    public static final Keyword const__52;
    public static final Keyword const__53;
    public static final Keyword const__54;
    public static final Var const__55;
    public static final Keyword const__56;
    public static final Keyword const__57;
    public static final Keyword const__58;
    public static final Keyword const__59;
    public static final Var const__60;
    public static final Var const__61;
    public static final Keyword const__62;
    public static final Keyword const__63;
    public static final Var const__65;
    public static final Var const__66;
    public static final Keyword const__67;
    public static final Var const__68;
    public static final Keyword const__69;
    public static final Keyword const__70;
    public static final Keyword const__71;
    public static final Keyword const__72;
    public static final Var const__73;
    public static final Var const__74;
    public static final Var const__75;
    public static final Keyword const__76;
    public static final Var const__77;
    public static final Var const__78;
    public static final Var const__79;
    public static final Var const__81;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;
    static final KeywordLookupSite __site__3__;
    static ILookupThunk __thunk__3__;
    static final KeywordLookupSite __site__4__;
    static ILookupThunk __thunk__4__;
    static final KeywordLookupSite __site__5__;
    static ILookupThunk __thunk__5__;
    static final KeywordLookupSite __site__6__;
    static ILookupThunk __thunk__6__;
    static final KeywordLookupSite __site__7__;
    static ILookupThunk __thunk__7__;
    static final KeywordLookupSite __site__8__;
    static ILookupThunk __thunk__8__;

    public LogImpl(Object object, Object object2, Object object3, Object object4, Object object5, int n, int n2) {
        this.olookup = object;
        this.desc = object2;
        this.tail = object3;
        this.__meta = object4;
        this.__extmap = object5;
        this.__hash = n;
        this.__hasheq = n2;
    }

    public LogImpl(Object object, Object object2, Object object3) {
        this(object, object2, object3, null, null, 0, 0);
    }

    public LogImpl(Object object, Object object2, Object object3, Object object4, Object object5) {
        this(object, object2, object3, object4, object5, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"olookup"), (Object)Symbol.intern(null, (String)"desc"), (Object)Symbol.intern(null, (String)"tail"));
    }

    public static LogImpl create(IPersistentMap iPersistentMap) {
        Object object = iPersistentMap.valAt((Object)Keyword.intern((String)"olookup"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"olookup"));
        Object object2 = iPersistentMap.valAt((Object)Keyword.intern((String)"desc"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"desc"));
        Object object3 = iPersistentMap.valAt((Object)Keyword.intern((String)"tail"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"tail"));
        return new LogImpl(object, object2, object3, null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    public Object adopt_root(Object cs, Object new_root_id, Object basis_t2) {
        Object temp__5455__auto__16320;
        Object object = basis_t2;
        basis_t2 = null;
        Object tail = ((IFn)const__74.getRawRoot()).invoke(this_.tail, object);
        Object object2 = new_root_id;
        new_root_id = null;
        Object proposed_desc = ((IFn)const__17.getRawRoot()).invoke(((IFn)const__75.getRawRoot()).invoke(this_.desc), (Object)const__76, null, (Object)const__46, ((IFn)const__50.getRawRoot()).invoke(object2));
        IFn iFn = (IFn)const__77.getRawRoot();
        IFn iFn2 = (IFn)const__78.getRawRoot();
        Object object3 = const__79.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__8__;
        Object object4 = tail;
        Object object5 = iLookupThunk.get(object4);
        if (iLookupThunk == object5) {
            __thunk__8__ = __site__8__.fault(object4);
            object5 = __thunk__8__.get(object4);
        }
        Object pod_buf = iFn.invoke(iFn2.invoke(object3, object5));
        Object object6 = cs;
        cs = null;
        Object object7 = proposed_desc;
        proposed_desc = null;
        Object object8 = pod_buf;
        pod_buf = null;
        Object object9 = temp__5455__auto__16320 = ((IFn)const__81.getRawRoot()).invoke(object6, object7, object8);
        if (object9 == null || object9 == Boolean.FALSE) {
            throw (Throwable)new Error("Conflict adopting log root.");
        }
        Object object10 = temp__5455__auto__16320;
        temp__5455__auto__16320 = null;
        Object new_desc = object10;
        LogImpl logImpl = this_;
        Object object11 = new_desc;
        new_desc = null;
        Object object12 = tail;
        tail = null;
        LogImpl this_ = null;
        return ((IFn)const__17.getRawRoot()).invoke((Object)logImpl, (Object)const__8, object11, (Object)const__10, object12);
    }

    public Object append(Object cs, Object msgs) {
        Object object;
        IPersistentMap iPersistentMap;
        Object bufs = ((IFn)const__51.getRawRoot()).invoke((Object)const__52, msgs);
        Object ids = ((IFn)const__51.getRawRoot()).invoke((Object)const__53, msgs);
        Object object2 = msgs;
        msgs = null;
        Object txes = ((IFn)const__51.getRawRoot()).invoke((Object)const__54, object2);
        Object new_tail = ((IFn)const__55.getRawRoot()).invoke(this.tail, txes, bufs);
        Object[] objectArray = new Object[6];
        objectArray[0] = const__56;
        objectArray[1] = const__57;
        objectArray[2] = const__58;
        objectArray[3] = ids;
        objectArray[4] = const__59;
        ILookupThunk iLookupThunk = __thunk__3__;
        Object object3 = txes;
        txes = null;
        Object object4 = ((IFn)const__60.getRawRoot()).invoke(object3);
        Object object5 = iLookupThunk.get(object4);
        if (iLookupThunk == object5) {
            __thunk__3__ = __site__3__.fault(object4);
            object5 = __thunk__3__.get(object4);
        }
        objectArray[5] = object5;
        IPersistentMap m_16313 = RT.mapUniqueKeys((Object[])objectArray);
        Logger logger = LoggerFactory.getLogger((String)"datomic.log");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__61.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)m_16313, (Object)const__62, (Object)const__63)));
        }
        long start__8981__auto__16323 = System.nanoTime();
        Object object6 = bufs;
        bufs = null;
        Object object7 = new_tail;
        new_tail = null;
        Object object8 = cs;
        cs = null;
        Object result__8982__auto__16324 = ((IFn)new LogImpl$fn__16316(object6, this.desc, object7, this, object8)).invoke();
        long elapsed_16314 = Numbers.minus((long)System.nanoTime(), (long)start__8981__auto__16323);
        Object msec_16315 = ((IFn)const__65.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_16314));
        ((IFn)const__66.getRawRoot()).invoke((Object)const__67, msec_16315);
        IFn iFn = (IFn)const__68.getRawRoot();
        IPersistentMap iPersistentMap2 = m_16313;
        m_16313 = null;
        Object object9 = msec_16315;
        msec_16315 = null;
        Object object10 = ((IFn)const__17.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__69, object9, (Object)const__62, (Object)const__70);
        ILookupThunk iLookupThunk2 = __thunk__4__;
        Object object11 = result__8982__auto__16324;
        Object object12 = iLookupThunk2.get(object11);
        if (iLookupThunk2 == object12) {
            __thunk__4__ = __site__4__.fault(object11);
            object12 = __thunk__4__.get(object11);
        }
        if (object12 != null && object12 != Boolean.FALSE) {
            Object[] objectArray2 = new Object[2];
            objectArray2[0] = const__71;
            IFn iFn2 = (IFn)const__22.getRawRoot();
            ILookupThunk iLookupThunk3 = __thunk__5__;
            Object object13 = result__8982__auto__16324;
            Object object14 = iLookupThunk3.get(object13);
            if (iLookupThunk3 == object14) {
                __thunk__5__ = __site__5__.fault(object13);
                object14 = __thunk__5__.get(object13);
            }
            objectArray2[1] = iFn2.invoke(object14);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray2);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__16321 = iFn.invoke(object10, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.log");
        if (logger3.isDebugEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object15 = endmsg__8984__auto__16321;
            endmsg__8984__auto__16321 = null;
            logger4.debug((String)((IFn)const__61.getRawRoot()).invoke(object15));
        }
        Object object16 = ((IFn)const__7.getRawRoot()).invoke(result__8982__auto__16324, (Object)const__72);
        if (object16 != null && object16 != Boolean.FALSE) {
            ILookupThunk iLookupThunk4 = __thunk__6__;
            Object object17 = result__8982__auto__16324;
            result__8982__auto__16324 = null;
            object = iLookupThunk4.get(object17);
            if (iLookupThunk4 == object) {
                __thunk__6__ = __site__6__.fault(object17);
                object = __thunk__6__.get(object17);
            }
        } else {
            ILookupThunk iLookupThunk5 = __thunk__7__;
            Object object18 = result__8982__auto__16324;
            result__8982__auto__16324 = null;
            Object object19 = iLookupThunk5.get(object18);
            if (iLookupThunk5 == object19) {
                __thunk__7__ = __site__7__.fault(object18);
                object19 = __thunk__7__.get(object18);
            }
            throw (Throwable)object19;
        }
        Object log2 = object;
        Object object20 = ids;
        ids = null;
        ((IFn)const__73.getRawRoot()).invoke(object20);
        Object object21 = log2;
        log2 = null;
        return object21;
    }

    /*
     * Unable to fully structure code
     */
    public Object val_keys() {
        v0 = (IFn)LogImpl.const__49.getRawRoot();
        v1 = new LogImpl$fn__16307(this.olookup);
        v2 = new LogImpl$fn__16309(this.olookup);
        v3 = (IFn)LogImpl.const__50.getRawRoot();
        v4 = this;
        if (Util.classOf((Object)v4) == LogImpl.__cached_class__6) ** GOTO lbl10
        if (!(v4 instanceof Log)) {
            v4 = v4;
            LogImpl.__cached_class__6 = Util.classOf((Object)v4);
lbl10:
            // 2 sources

            v5 = LogImpl.const__48.getRawRoot().invoke((Object)v4);
        } else {
            v5 = ((Log)v4).get_root_id();
        }
        this = null;
        return v0.invoke((Object)v1, (Object)v2, v3.invoke(v5));
    }

    /*
     * Unable to fully structure code
     */
    public Object get_root_val() {
        v0 = (IFn)LogImpl.const__47.getRawRoot();
        v1 = (IFn)LogImpl.const__38.getRawRoot();
        v2 = this;
        if (Util.classOf((Object)v2) == LogImpl.__cached_class__5) ** GOTO lbl8
        if (!(v2 instanceof Log)) {
            v2 = v2;
            LogImpl.__cached_class__5 = Util.classOf((Object)v2);
lbl8:
            // 2 sources

            v3 = LogImpl.const__48.getRawRoot().invoke((Object)v2);
        } else {
            v3 = ((Log)v2).get_root_id();
        }
        this = null;
        return v0.invoke(v1.invoke(this.olookup, v3));
    }

    public Object get_root_id() {
        ILookupThunk iLookupThunk = __thunk__2__;
        Object object = this.desc;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__2__ = __site__2__.fault(object);
            object2 = __thunk__2__.get(object);
        }
        return object2;
    }

    public Object claim(Object cs) {
        Object object;
        Object temp__5457__auto__16325;
        Object object2 = cs;
        cs = null;
        Object object3 = temp__5457__auto__16325 = ((IFn)const__45.getRawRoot()).invoke(object2, this_.desc);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5457__auto__16325;
            temp__5457__auto__16325 = null;
            Object new_desc = object4;
            LogImpl logImpl = this_;
            Object object5 = new_desc;
            new_desc = null;
            LogImpl this_ = null;
            object = ((IFn)const__17.getRawRoot()).invoke((Object)logImpl, (Object)const__8, object5);
        } else {
            object = null;
        }
        return object;
    }

    /*
     * Unable to fully structure code
     */
    public Object seek_seg_path(Object t) {
        comp = ((IFn)LogImpl.const__34.getRawRoot()).invoke(LogImpl.const__35.getRawRoot());
        v0 = this;
        if (Util.classOf((Object)v0) == LogImpl.__cached_class__4) ** GOTO lbl7
        if (!(v0 instanceof Log)) {
            v0 = v0;
            LogImpl.__cached_class__4 = Util.classOf((Object)v0);
lbl7:
            // 2 sources

            v1 = LogImpl.const__36.getRawRoot().invoke((Object)v0);
        } else {
            v1 = ((Log)v0).get_root_val();
        }
        root_val = v1;
        ridx = ((IFn)LogImpl.const__37.getRawRoot()).invoke(root_val, t, comp);
        v2 = root_val;
        root_val = null;
        v3 = ridx;
        ridx = null;
        dirid = ((LogDir)RT.nth((Object)v2, (int)RT.intCast((Object)((Number)v3)))).uuid;
        dir = ((IFn)LogImpl.const__38.getRawRoot()).invoke(this.olookup, dirid);
        if ((long)RT.count((Object)dir) == 0L) {
            v4 = null;
        } else {
            v5 = t;
            t = null;
            v6 = comp;
            comp = null;
            didx = ((IFn)LogImpl.const__37.getRawRoot()).invoke(dir, v5, v6);
            v7 = dir;
            dir = null;
            v8 = didx;
            didx = null;
            segid = ((LogDir)RT.nth((Object)v7, (int)RT.intCast((Object)((Number)v8)))).uuid;
            v9 = dirid;
            dirid = null;
            v10 = segid;
            segid = null;
            v4 = Tuple.create((Object)v9, (Object)v10);
        }
        return v4;
    }

    /*
     * Unable to fully structure code
     */
    public Object seek_tx_impl(Object t) {
        block13: {
            block14: {
                block12: {
                    comp = ((IFn)LogImpl.const__34.getRawRoot()).invoke(LogImpl.const__35.getRawRoot());
                    v0 = this;
                    if (Util.classOf((Object)v0) == LogImpl.__cached_class__1) ** GOTO lbl7
                    if (!(v0 instanceof Log)) {
                        v0 = v0;
                        LogImpl.__cached_class__1 = Util.classOf((Object)v0);
lbl7:
                        // 2 sources

                        v1 = LogImpl.const__36.getRawRoot().invoke((Object)v0);
                    } else {
                        v1 = ((Log)v0).get_root_val();
                    }
                    root_val = v1;
                    ridx = ((IFn)LogImpl.const__37.getRawRoot()).invoke(root_val, t, comp);
                    dir = ((IFn)LogImpl.const__38.getRawRoot()).invoke(this.olookup, ((LogDir)RT.nth((Object)root_val, (int)RT.intCast((Object)((Number)ridx)))).uuid);
                    if ((long)RT.count((Object)dir) != 0L) break block12;
                    v2 = this.tail;
                    if (Util.classOf((Object)v2) == LogImpl.__cached_class__2) ** GOTO lbl19
                    if (!(v2 instanceof LogSeek)) {
                        v2 = v2;
                        LogImpl.__cached_class__2 = Util.classOf((Object)v2);
lbl19:
                        // 2 sources

                        v3 = t;
                        t = null;
                        this = null;
                        v4 = LogImpl.const__40.getRawRoot().invoke(v2, v3);
                    } else {
                        v5 = t;
                        t = null;
                        v4 = ((LogSeek)v2).seek_tx_impl(v5);
                    }
                    break block13;
                }
                didx = ((IFn)LogImpl.const__37.getRawRoot()).invoke(dir, t, comp);
                seg = ((IFn)LogImpl.const__38.getRawRoot()).invoke(this.olookup, ((LogDir)RT.nth((Object)dir, (int)RT.intCast((Object)((Number)didx)))).uuid);
                v6 = comp;
                comp = null;
                v7 = sidx = ((IFn)LogImpl.const__41.getRawRoot()).invoke(seg, t, v6);
                if (v7 == null || v7 == Boolean.FALSE) break block14;
                root_val = null;
                ridx = null;
                dir = null;
                didx = null;
                seg = null;
                sidx = null;
                v4 = new LogTxIter(this.olookup, root_val, this.tail, RT.longCast((Object)((Number)ridx)), dir, RT.longCast((Object)((Number)didx)), seg, RT.longCast((Object)((Number)sidx)));
                break block13;
            }
            v8 = this;
            if (Util.classOf((Object)v8) == LogImpl.__cached_class__3) ** GOTO lbl49
            if (!(v8 instanceof LogSeek)) {
                v8 = v8;
                LogImpl.__cached_class__3 = Util.classOf((Object)v8);
lbl49:
                // 2 sources

                v9 = LogImpl.__thunk__0__;
                v10 = seg;
                seg = null;
                v11 = ((IFn)LogImpl.const__43.getRawRoot()).invoke(v10);
                v12 = v9.get(v11);
                if (v9 == v12) {
                    LogImpl.__thunk__0__ = LogImpl.__site__0__.fault(v11);
                    v12 = LogImpl.__thunk__0__.get(v11);
                }
                v13 = LogImpl.const__40.getRawRoot().invoke((Object)v8, v12);
            } else {
                v14 = v8;
                v15 = LogImpl.__thunk__0__;
                v16 = seg;
                seg = null;
                v17 = ((IFn)LogImpl.const__43.getRawRoot()).invoke(v16);
                v18 = v15.get(v17);
                if (v15 == v18) {
                    LogImpl.__thunk__0__ = LogImpl.__site__0__.fault(v17);
                    v18 = LogImpl.__thunk__0__.get(v17);
                }
                v13 = v14.seek_tx_impl(v18);
            }
            iter = v13;
            while (true) {
                v19 = and__5236__auto__16326 = iter;
                if (v19 != null && v19 != Boolean.FALSE) {
                    v20 = LogImpl.__thunk__1__;
                    v21 = ((Iter)iter).get();
                    v22 = v20.get(v21);
                    if (v20 == v22) {
                        LogImpl.__thunk__1__ = LogImpl.__site__1__.fault(v21);
                        v22 = LogImpl.__thunk__1__.get(v21);
                    }
                    v23 = Numbers.lt((Object)v22, (Object)t) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    v23 = and__5236__auto__16326;
                    and__5236__auto__16326 = null;
                }
                if (v23 == null || v23 == Boolean.FALSE) break;
                v24 = iter;
                iter = null;
                iter = ((Iter)v24).next();
            }
            v4 = iter;
            iter = null;
        }
        return v4;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object tail_txes() {
        Object object;
        Object object2 = this_.tail;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof TailTxes) {
                object = ((TailTxes)object2).tail_txes();
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        LogImpl this_ = null;
        object = const__33.getRawRoot().invoke(object2);
        return object;
    }

    /*
     * WARNING - void declaration
     */
    public int hasheq() {
        void v0;
        int hq__7465__auto__16328 = this.__hasheq;
        if ((long)hq__7465__auto__16328 == 0L) {
            void var2_2;
            int h__7466__auto__16327;
            this.__hasheq = h__7466__auto__16327 = RT.intCast((long)(0xFFFFFFFF9DD3F051L ^ (long)APersistentMap.mapHasheq((IPersistentMap)this)));
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    /*
     * WARNING - void declaration
     */
    public int hashCode() {
        void v0;
        int hash__7468__auto__16330 = this.__hash;
        if ((long)hash__7468__auto__16330 == 0L) {
            void var2_2;
            int h__7469__auto__16329;
            this.__hash = h__7469__auto__16329 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__16295) {
        Object object = G__16295;
        G__16295 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__16295) {
        IPersistentMap iPersistentMap = G__16295;
        G__16295 = null;
        return new LogImpl(this.olookup, this.desc, this.tail, iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
    }

    public Object valAt(Object k__7474__auto__) {
        Object object = k__7474__auto__;
        k__7474__auto__ = null;
        return ((ILookup)this).valAt(object, null);
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object valAt(Object k__7476__auto__, Object else__7477__auto__) {
        Object object;
        Object G__16306 = k__7476__auto__;
        switch (Util.hash((Object)G__16306) >> 0 & 3) {
            case 1: {
                if (G__16306 != const__8) break;
                object = this_.desc;
                return object;
            }
            case 2: {
                if (G__16306 != const__10) break;
                object = this_.tail;
                return object;
            }
            case 3: {
                if (G__16306 != const__9) break;
                object = this_.olookup;
                return object;
            }
        }
        Object object2 = k__7476__auto__;
        k__7476__auto__ = null;
        Object object3 = else__7477__auto__;
        else__7477__auto__ = null;
        LogImpl this_ = null;
        object = RT.get((Object)this_.__extmap, (Object)object2, (Object)object3);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public ILookupThunk getLookupThunk(Keyword k__7479__auto__) {
        Object object;
        Object gclass = ((IFn)const__22.getRawRoot()).invoke((Object)this);
        Keyword keyword = k__7479__auto__;
        k__7479__auto__ = null;
        Keyword G__16299 = keyword;
        switch (Util.hash((Object)G__16299) >> 0 & 3) {
            case 1: {
                if (G__16299 != const__8) break;
                gclass = null;
                object = new LogImpl$reify__16300(null, gclass);
                return object;
            }
            case 2: {
                if (G__16299 != const__10) break;
                gclass = null;
                object = new LogImpl$reify__16302(null, gclass);
                return object;
            }
            case 3: {
                if (G__16299 != const__9) break;
                gclass = null;
                object = new LogImpl$reify__16304(null, gclass);
                return object;
            }
        }
        object = null;
        return object;
    }

    public int count() {
        return RT.intCast((long)Numbers.add((long)3L, (long)RT.count((Object)this.__extmap)));
    }

    public IPersistentCollection empty() {
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__24.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.log.LogImpl"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        LogImpl logImpl = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        LogImpl this_ = null;
        return (IPersistentCollection)((IFn)const__23).invoke((Object)logImpl, object);
    }

    public boolean equiv(Object G__16295) {
        Boolean bl;
        boolean or__5238__auto__16334 = Util.identical((Object)this, (Object)G__16295);
        if (or__5238__auto__16334) {
            bl = or__5238__auto__16334 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__22.getRawRoot()).invoke((Object)this), (Object)((IFn)const__22.getRawRoot()).invoke(G__16295))) {
            Object object = G__16295;
            G__16295 = null;
            Object G__162952 = object;
            boolean and__5236__auto__16333 = Util.equiv((Object)this.olookup, (Object)((LogImpl)G__162952).olookup);
            if (and__5236__auto__16333) {
                boolean and__5236__auto__16332 = Util.equiv((Object)this.desc, (Object)((LogImpl)G__162952).desc);
                if (and__5236__auto__16332) {
                    boolean and__5236__auto__16331 = Util.equiv((Object)this.tail, (Object)((LogImpl)G__162952).tail);
                    if (and__5236__auto__16331) {
                        Object object2 = G__162952;
                        G__162952 = null;
                        bl = Util.equiv((Object)this.__extmap, (Object)((LogImpl)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        bl = and__5236__auto__16331 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    bl = and__5236__auto__16332 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__16333 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = null;
        }
        return RT.booleanCast((Object)bl);
    }

    public boolean containsKey(Object k__7486__auto__) {
        Object object = k__7486__auto__;
        k__7486__auto__ = null;
        Boolean bl = Util.identical((Object)this_, (Object)((ILookup)this_).valAt(object, (Object)this_)) ? Boolean.TRUE : Boolean.FALSE;
        LogImpl this_ = null;
        return (Boolean)((IFn)const__21.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__16335 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__16335)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__16335;
            v__7489__auto__16335 = null;
            LogImpl this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        LogImpl this_ = null;
        return (ISeq)((IFn)const__19.getRawRoot()).invoke(((IFn)const__20.getRawRoot()).invoke((Object)Tuple.create((Object)MapEntry.create((Object)const__9, (Object)this_.olookup), (Object)MapEntry.create((Object)const__8, (Object)this_.desc), (Object)MapEntry.create((Object)const__10, (Object)this_.tail)), this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)const__18, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__16295) {
        LogImpl logImpl;
        Object pred__16297 = const__16.getRawRoot();
        Object expr__16298 = k__7493__auto__;
        Object object = ((IFn)pred__16297).invoke((Object)const__9, expr__16298);
        if (object != null && object != Boolean.FALSE) {
            G__16295 = null;
            logImpl = new LogImpl(G__16295, this.desc, this.tail, this.__meta, this.__extmap);
        } else {
            Object object2 = ((IFn)pred__16297).invoke((Object)const__8, expr__16298);
            if (object2 != null && object2 != Boolean.FALSE) {
                G__16295 = null;
                logImpl = new LogImpl(this.olookup, G__16295, this.tail, this.__meta, this.__extmap);
            } else {
                Object object3 = pred__16297;
                pred__16297 = null;
                Object object4 = expr__16298;
                expr__16298 = null;
                Object object5 = ((IFn)object3).invoke((Object)const__10, object4);
                if (object5 != null && object5 != Boolean.FALSE) {
                    G__16295 = null;
                    logImpl = new LogImpl(this.olookup, this.desc, G__16295, this.__meta, this.__extmap);
                } else {
                    k__7493__auto__ = null;
                    G__16295 = null;
                    logImpl = new LogImpl(this.olookup, this.desc, this.tail, this.__meta, ((IFn)const__17.getRawRoot()).invoke(this.__extmap, k__7493__auto__, G__16295));
                }
            }
        }
        return logImpl;
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)const__11, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__13.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            LogImpl this_ = null;
            object = ((IFn)const__12.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new LogImpl(this_.olookup, this_.desc, this_.tail, this_.__meta, ((IFn)const__15.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
        }
        return (IPersistentMap)object;
    }

    public int size() {
        Counted counted = (Counted)this_;
        LogImpl this_ = null;
        return counted.count();
    }

    public boolean isEmpty() {
        return Util.equiv((long)0L, (long)((Counted)this).count());
    }

    public boolean containsValue(Object v__7499__auto__) {
        Object[] objectArray = new Object[1];
        Object object = v__7499__auto__;
        v__7499__auto__ = null;
        objectArray[0] = object;
        return RT.booleanCast((Object)((IFn)const__4.getRawRoot()).invoke((Object)RT.set((Object[])objectArray), ((IFn)const__1.getRawRoot()).invoke((Object)this)));
    }

    public Object get(Object k__7501__auto__) {
        Object object = k__7501__auto__;
        k__7501__auto__ = null;
        return ((ILookup)this).valAt(object);
    }

    public Object put(Object k__7503__auto__, Object v__7504__auto__) {
        throw (Throwable)new UnsupportedOperationException();
    }

    public Object remove(Object k__7506__auto__) {
        throw (Throwable)new UnsupportedOperationException();
    }

    public void putAll(Map m__7508__auto__) {
        throw (Throwable)new UnsupportedOperationException();
    }

    public void clear() {
        throw (Throwable)new UnsupportedOperationException();
    }

    public Set keySet() {
        Object object = ((IFn)const__2.getRawRoot()).invoke((Object)this_);
        LogImpl this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Collection values() {
        LogImpl logImpl = this_;
        LogImpl this_ = null;
        return (Collection)((IFn)const__1.getRawRoot()).invoke((Object)logImpl);
    }

    public Set entrySet() {
        LogImpl logImpl = this_;
        LogImpl this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke((Object)logImpl);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"set");
        const__1 = RT.var((String)"clojure.core", (String)"vals");
        const__2 = RT.var((String)"clojure.core", (String)"keys");
        const__4 = RT.var((String)"clojure.core", (String)"some");
        const__7 = RT.var((String)"clojure.core", (String)"contains?");
        const__8 = RT.keyword(null, (String)"desc");
        const__9 = RT.keyword(null, (String)"olookup");
        const__10 = RT.keyword(null, (String)"tail");
        const__11 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"desc"), RT.keyword(null, (String)"olookup"), RT.keyword(null, (String)"tail")});
        const__12 = RT.var((String)"clojure.core", (String)"dissoc");
        const__13 = RT.var((String)"clojure.core", (String)"with-meta");
        const__14 = RT.var((String)"clojure.core", (String)"into");
        const__15 = RT.var((String)"clojure.core", (String)"not-empty");
        const__16 = RT.var((String)"clojure.core", (String)"identical?");
        const__17 = RT.var((String)"clojure.core", (String)"assoc");
        const__18 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"olookup"), (Object)RT.keyword(null, (String)"desc"), (Object)RT.keyword(null, (String)"tail"));
        const__19 = RT.var((String)"clojure.core", (String)"seq");
        const__20 = RT.var((String)"clojure.core", (String)"concat");
        const__21 = RT.var((String)"clojure.core", (String)"not");
        const__22 = RT.var((String)"clojure.core", (String)"class");
        const__23 = RT.var((String)"clojure.core", (String)"imap-cons");
        const__24 = RT.var((String)"clojure.core", (String)"str");
        const__33 = RT.var((String)"datomic.log", (String)"tail-txes");
        const__34 = RT.var((String)"datomic.common", (String)"key-comparator");
        const__35 = RT.var((String)"datomic.log", (String)"log-key");
        const__36 = RT.var((String)"datomic.log", (String)"get-root-val");
        const__37 = RT.var((String)"datomic.log", (String)"btree-search");
        const__38 = RT.var((String)"datomic.common", (String)"getx");
        const__40 = RT.var((String)"datomic.log", (String)"seek-tx-impl");
        const__41 = RT.var((String)"datomic.log", (String)"binary-search");
        const__43 = RT.var((String)"clojure.core", (String)"last");
        const__45 = RT.var((String)"datomic.log", (String)"claim-log");
        const__46 = RT.keyword((String)"d", (String)"r");
        const__47 = RT.var((String)"clojure.core", (String)"vec");
        const__48 = RT.var((String)"datomic.log", (String)"get-root-id");
        const__49 = RT.var((String)"clojure.core", (String)"tree-seq");
        const__50 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
        const__51 = RT.var((String)"clojure.core", (String)"map");
        const__52 = RT.keyword(null, (String)"fressianed-tx");
        const__53 = RT.keyword(null, (String)"id");
        const__54 = RT.keyword(null, (String)"tx");
        const__55 = RT.var((String)"datomic.log", (String)"extend-tail");
        const__56 = RT.keyword(null, (String)"event");
        const__57 = RT.keyword((String)"log", (String)"add-next");
        const__58 = RT.keyword(null, (String)"txids");
        const__59 = RT.keyword(null, (String)"firstT");
        const__60 = RT.var((String)"clojure.core", (String)"first");
        const__61 = RT.var((String)"datomic.slf4j", (String)"process");
        const__62 = RT.keyword(null, (String)"phase");
        const__63 = RT.keyword(null, (String)"begin");
        const__65 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
        const__66 = RT.var((String)"datomic.monitor", (String)"add-stat");
        const__67 = RT.keyword(null, (String)"LogWriteMsec");
        const__68 = RT.var((String)"clojure.core", (String)"merge");
        const__69 = RT.keyword(null, (String)"msec");
        const__70 = RT.keyword(null, (String)"end");
        const__71 = RT.keyword(null, (String)"threw");
        const__72 = RT.keyword(null, (String)"returned");
        const__73 = RT.var((String)"datomic.transaction", (String)"log-completion!");
        const__74 = RT.var((String)"datomic.log", (String)"since");
        const__75 = RT.var((String)"datomic.log", (String)"inc-rev");
        const__76 = RT.keyword(null, (String)"etag");
        const__77 = RT.var((String)"datomic.io", (String)"unchunk");
        const__78 = RT.var((String)"clojure.core", (String)"cons");
        const__79 = RT.var((String)"datomic.log", (String)"BEGIN_OPEN_LIST");
        const__81 = RT.var((String)"datomic.log", (String)"write-tail-descriptor");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword((String)"d", (String)"r"));
        __thunk__2__ = __site__2__;
        __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
        __thunk__3__ = __site__3__;
        __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__4__ = __site__4__;
        __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__5__ = __site__5__;
        __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
        __thunk__6__ = __site__6__;
        __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
        __thunk__7__ = __site__7__;
        __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"bufs"));
        __thunk__8__ = __site__8__;
    }
}

