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
import datomic.Log;
import datomic.iter.Iter;
import datomic.log.LogDir;
import datomic.log.LogSeek;
import datomic.log.LogTxIter;
import datomic.log.LogValue$reify__16489;
import datomic.log.LogValue$reify__16491;
import datomic.log.LogValue$reify__16493;
import datomic.log.LogValue$reify__16495;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class LogValue
implements LogSeek,
Log,
IRecord,
IHashEq,
IObj,
ILookup,
IKeywordLookup,
IPersistentMap,
Map,
Serializable {
    public final Object db;
    public final Object olookup;
    public final Object root_id;
    public final Object tail;
    public final Object __meta;
    public final Object __extmap;
    int __hash;
    int __hasheq;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__4;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final Keyword const__9;
    public static final Keyword const__10;
    public static final Keyword const__11;
    public static final AFn const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final AFn const__19;
    public static final Var const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Var const__24;
    public static final Var const__25;
    public static final Var const__34;
    public static final Var const__35;
    public static final Var const__36;
    public static final Var const__37;
    public static final Var const__38;
    public static final Var const__40;
    public static final Var const__41;
    public static final Var const__43;
    public static final Var const__45;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;

    public LogValue(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, int n, int n2) {
        this.db = object;
        this.olookup = object2;
        this.root_id = object3;
        this.tail = object4;
        this.__meta = object5;
        this.__extmap = object6;
        this.__hash = n;
        this.__hasheq = n2;
    }

    public LogValue(Object object, Object object2, Object object3, Object object4) {
        this(object, object2, object3, object4, null, null, 0, 0);
    }

    public LogValue(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this(object, object2, object3, object4, object5, object6, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"olookup"), (Object)Symbol.intern(null, (String)"root-id"), (Object)Symbol.intern(null, (String)"tail"));
    }

    public static LogValue create(IPersistentMap iPersistentMap) {
        Object object = iPersistentMap.valAt((Object)Keyword.intern((String)"db"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"db"));
        Object object2 = iPersistentMap.valAt((Object)Keyword.intern((String)"olookup"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"olookup"));
        Object object3 = iPersistentMap.valAt((Object)Keyword.intern((String)"root-id"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"root-id"));
        Object object4 = iPersistentMap.valAt((Object)Keyword.intern((String)"tail"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"tail"));
        return new LogValue(object, object2, object3, object4, null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    public Iterable txRange(Object start, Object end) {
        LogValue logValue = this_;
        Object object = start;
        start = null;
        Object object2 = end;
        end = null;
        LogValue this_ = null;
        return (Iterable)((IFn)const__45.getRawRoot()).invoke((Object)logValue, this_.db, object, object2);
    }

    public Object seek_seg_path(Object t) {
        IPersistentVector iPersistentVector;
        Object comp2 = ((IFn)const__34.getRawRoot()).invoke(const__35.getRawRoot());
        Object root_val = ((IFn)const__36.getRawRoot()).invoke(((IFn)const__37.getRawRoot()).invoke(this.olookup, this.root_id));
        Object ridx = ((IFn)const__38.getRawRoot()).invoke(root_val, t, comp2);
        Object object = root_val;
        root_val = null;
        Object object2 = ridx;
        ridx = null;
        Object dirid = ((LogDir)RT.nth((Object)object, (int)RT.intCast((Object)((Number)object2)))).uuid;
        Object dir = ((IFn)const__37.getRawRoot()).invoke(this.olookup, dirid);
        if ((long)RT.count((Object)dir) == 0L) {
            iPersistentVector = null;
        } else {
            Object object3 = t;
            t = null;
            Object object4 = comp2;
            comp2 = null;
            Object didx = ((IFn)const__38.getRawRoot()).invoke(dir, object3, object4);
            Object object5 = dir;
            dir = null;
            Object object6 = didx;
            didx = null;
            Object segid = ((LogDir)RT.nth((Object)object5, (int)RT.intCast((Object)((Number)object6)))).uuid;
            Object object7 = dirid;
            dirid = null;
            Object object8 = segid;
            segid = null;
            iPersistentVector = Tuple.create((Object)object7, (Object)object8);
        }
        return iPersistentVector;
    }

    /*
     * Unable to fully structure code
     */
    public Object seek_tx_impl(Object t) {
        block11: {
            block12: {
                block10: {
                    comp = ((IFn)LogValue.const__34.getRawRoot()).invoke(LogValue.const__35.getRawRoot());
                    root_val = ((IFn)LogValue.const__36.getRawRoot()).invoke(((IFn)LogValue.const__37.getRawRoot()).invoke(this.olookup, this.root_id));
                    ridx = ((IFn)LogValue.const__38.getRawRoot()).invoke(root_val, t, comp);
                    dir = ((IFn)LogValue.const__37.getRawRoot()).invoke(this.olookup, ((LogDir)RT.nth((Object)root_val, (int)RT.intCast((Object)((Number)ridx)))).uuid);
                    if ((long)RT.count((Object)dir) != 0L) break block10;
                    v0 = this.tail;
                    if (Util.classOf((Object)v0) == LogValue.__cached_class__0) ** GOTO lbl11
                    if (!(v0 instanceof LogSeek)) {
                        v0 = v0;
                        LogValue.__cached_class__0 = Util.classOf((Object)v0);
lbl11:
                        // 2 sources

                        v1 = t;
                        t = null;
                        this = null;
                        v2 = LogValue.const__40.getRawRoot().invoke(v0, v1);
                    } else {
                        v3 = t;
                        t = null;
                        v2 = ((LogSeek)v0).seek_tx_impl(v3);
                    }
                    break block11;
                }
                didx = ((IFn)LogValue.const__38.getRawRoot()).invoke(dir, t, comp);
                seg = ((IFn)LogValue.const__37.getRawRoot()).invoke(this.olookup, ((LogDir)RT.nth((Object)dir, (int)RT.intCast((Object)((Number)didx)))).uuid);
                v4 = comp;
                comp = null;
                v5 = sidx = ((IFn)LogValue.const__41.getRawRoot()).invoke(seg, t, v4);
                if (v5 == null || v5 == Boolean.FALSE) break block12;
                root_val = null;
                ridx = null;
                dir = null;
                didx = null;
                seg = null;
                sidx = null;
                v2 = new LogTxIter(this.olookup, root_val, this.tail, RT.longCast((Object)((Number)ridx)), dir, RT.longCast((Object)((Number)didx)), seg, RT.longCast((Object)((Number)sidx)));
                break block11;
            }
            v6 = this;
            if (Util.classOf((Object)v6) == LogValue.__cached_class__1) ** GOTO lbl41
            if (!(v6 instanceof LogSeek)) {
                v6 = v6;
                LogValue.__cached_class__1 = Util.classOf((Object)v6);
lbl41:
                // 2 sources

                v7 = LogValue.__thunk__0__;
                v8 = seg;
                seg = null;
                v9 = ((IFn)LogValue.const__43.getRawRoot()).invoke(v8);
                v10 = v7.get(v9);
                if (v7 == v10) {
                    LogValue.__thunk__0__ = LogValue.__site__0__.fault(v9);
                    v10 = LogValue.__thunk__0__.get(v9);
                }
                v11 = LogValue.const__40.getRawRoot().invoke((Object)v6, v10);
            } else {
                v12 = v6;
                v13 = LogValue.__thunk__0__;
                v14 = seg;
                seg = null;
                v15 = ((IFn)LogValue.const__43.getRawRoot()).invoke(v14);
                v16 = v13.get(v15);
                if (v13 == v16) {
                    LogValue.__thunk__0__ = LogValue.__site__0__.fault(v15);
                    v16 = LogValue.__thunk__0__.get(v15);
                }
                v11 = v12.seek_tx_impl(v16);
            }
            iter = v11;
            while (true) {
                v17 = and__5236__auto__16499 = iter;
                if (v17 != null && v17 != Boolean.FALSE) {
                    v18 = LogValue.__thunk__1__;
                    v19 = ((Iter)iter).get();
                    v20 = v18.get(v19);
                    if (v18 == v20) {
                        LogValue.__thunk__1__ = LogValue.__site__1__.fault(v19);
                        v20 = LogValue.__thunk__1__.get(v19);
                    }
                    v21 = Numbers.lt((Object)v20, (Object)t) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    v21 = and__5236__auto__16499;
                    and__5236__auto__16499 = null;
                }
                if (v21 == null || v21 == Boolean.FALSE) break;
                v22 = iter;
                iter = null;
                iter = ((Iter)v22).next();
            }
            v2 = iter;
            iter = null;
        }
        return v2;
    }

    /*
     * WARNING - void declaration
     */
    public int hasheq() {
        void v0;
        int hq__7465__auto__16501 = this.__hasheq;
        if ((long)hq__7465__auto__16501 == 0L) {
            void var2_2;
            int h__7466__auto__16500;
            this.__hasheq = h__7466__auto__16500 = RT.intCast((long)(0x7FD0DE08L ^ (long)APersistentMap.mapHasheq((IPersistentMap)this)));
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
        int hash__7468__auto__16503 = this.__hash;
        if ((long)hash__7468__auto__16503 == 0L) {
            void var2_2;
            int h__7469__auto__16502;
            this.__hash = h__7469__auto__16502 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__16484) {
        Object object = G__16484;
        G__16484 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__16484) {
        IPersistentMap iPersistentMap = G__16484;
        G__16484 = null;
        return new LogValue(this.db, this.olookup, this.root_id, this.tail, iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
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
        Object G__16497 = k__7476__auto__;
        switch (Util.hash((Object)G__16497) >> 3 & 3) {
            case 0: {
                if (G__16497 != const__10) break;
                object = this_.olookup;
                return object;
            }
            case 1: {
                if (G__16497 != const__8) break;
                object = this_.root_id;
                return object;
            }
            case 2: {
                if (G__16497 != const__11) break;
                object = this_.tail;
                return object;
            }
            case 3: {
                if (G__16497 != const__9) break;
                object = this_.db;
                return object;
            }
        }
        Object object2 = k__7476__auto__;
        k__7476__auto__ = null;
        Object object3 = else__7477__auto__;
        else__7477__auto__ = null;
        LogValue this_ = null;
        object = RT.get((Object)this_.__extmap, (Object)object2, (Object)object3);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public ILookupThunk getLookupThunk(Keyword k__7479__auto__) {
        Object object;
        Object gclass = ((IFn)const__23.getRawRoot()).invoke((Object)this);
        Keyword keyword = k__7479__auto__;
        k__7479__auto__ = null;
        Keyword G__16488 = keyword;
        switch (Util.hash((Object)G__16488) >> 3 & 3) {
            case 0: {
                if (G__16488 != const__10) break;
                gclass = null;
                object = new LogValue$reify__16489(null, gclass);
                return object;
            }
            case 1: {
                if (G__16488 != const__8) break;
                gclass = null;
                object = new LogValue$reify__16491(null, gclass);
                return object;
            }
            case 2: {
                if (G__16488 != const__11) break;
                gclass = null;
                object = new LogValue$reify__16493(null, gclass);
                return object;
            }
            case 3: {
                if (G__16488 != const__9) break;
                gclass = null;
                object = new LogValue$reify__16495(null, gclass);
                return object;
            }
        }
        object = null;
        return object;
    }

    public int count() {
        return RT.intCast((long)Numbers.add((long)4L, (long)RT.count((Object)this.__extmap)));
    }

    public IPersistentCollection empty() {
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__25.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.log.LogValue"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        LogValue logValue = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        LogValue this_ = null;
        return (IPersistentCollection)((IFn)const__24).invoke((Object)logValue, object);
    }

    public boolean equiv(Object G__16484) {
        Boolean bl;
        boolean or__5238__auto__16508 = Util.identical((Object)this, (Object)G__16484);
        if (or__5238__auto__16508) {
            bl = or__5238__auto__16508 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__23.getRawRoot()).invoke((Object)this), (Object)((IFn)const__23.getRawRoot()).invoke(G__16484))) {
            Object object = G__16484;
            G__16484 = null;
            Object G__164842 = object;
            boolean and__5236__auto__16507 = Util.equiv((Object)this.db, (Object)((LogValue)G__164842).db);
            if (and__5236__auto__16507) {
                boolean and__5236__auto__16506 = Util.equiv((Object)this.olookup, (Object)((LogValue)G__164842).olookup);
                if (and__5236__auto__16506) {
                    boolean and__5236__auto__16505 = Util.equiv((Object)this.root_id, (Object)((LogValue)G__164842).root_id);
                    if (and__5236__auto__16505) {
                        boolean and__5236__auto__16504 = Util.equiv((Object)this.tail, (Object)((LogValue)G__164842).tail);
                        if (and__5236__auto__16504) {
                            Object object2 = G__164842;
                            G__164842 = null;
                            bl = Util.equiv((Object)this.__extmap, (Object)((LogValue)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
                        } else {
                            bl = and__5236__auto__16504 ? Boolean.TRUE : Boolean.FALSE;
                        }
                    } else {
                        bl = and__5236__auto__16505 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    bl = and__5236__auto__16506 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__16507 ? Boolean.TRUE : Boolean.FALSE;
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
        LogValue this_ = null;
        return (Boolean)((IFn)const__22.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__16509 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__16509)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__16509;
            v__7489__auto__16509 = null;
            LogValue this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        LogValue this_ = null;
        return (ISeq)((IFn)const__20.getRawRoot()).invoke(((IFn)const__21.getRawRoot()).invoke((Object)Tuple.create((Object)MapEntry.create((Object)const__9, (Object)this_.db), (Object)MapEntry.create((Object)const__10, (Object)this_.olookup), (Object)MapEntry.create((Object)const__8, (Object)this_.root_id), (Object)MapEntry.create((Object)const__11, (Object)this_.tail)), this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)const__19, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__16484) {
        LogValue logValue;
        Object pred__16486 = const__17.getRawRoot();
        Object expr__16487 = k__7493__auto__;
        Object object = ((IFn)pred__16486).invoke((Object)const__9, expr__16487);
        if (object != null && object != Boolean.FALSE) {
            G__16484 = null;
            logValue = new LogValue(G__16484, this.olookup, this.root_id, this.tail, this.__meta, this.__extmap);
        } else {
            Object object2 = ((IFn)pred__16486).invoke((Object)const__10, expr__16487);
            if (object2 != null && object2 != Boolean.FALSE) {
                G__16484 = null;
                logValue = new LogValue(this.db, G__16484, this.root_id, this.tail, this.__meta, this.__extmap);
            } else {
                Object object3 = ((IFn)pred__16486).invoke((Object)const__8, expr__16487);
                if (object3 != null && object3 != Boolean.FALSE) {
                    G__16484 = null;
                    logValue = new LogValue(this.db, this.olookup, G__16484, this.tail, this.__meta, this.__extmap);
                } else {
                    Object object4 = pred__16486;
                    pred__16486 = null;
                    Object object5 = expr__16487;
                    expr__16487 = null;
                    Object object6 = ((IFn)object4).invoke((Object)const__11, object5);
                    if (object6 != null && object6 != Boolean.FALSE) {
                        G__16484 = null;
                        logValue = new LogValue(this.db, this.olookup, this.root_id, G__16484, this.__meta, this.__extmap);
                    } else {
                        k__7493__auto__ = null;
                        G__16484 = null;
                        logValue = new LogValue(this.db, this.olookup, this.root_id, this.tail, this.__meta, ((IFn)const__18.getRawRoot()).invoke(this.__extmap, k__7493__auto__, G__16484));
                    }
                }
            }
        }
        return logValue;
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)const__12, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__14.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            LogValue this_ = null;
            object = ((IFn)const__13.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new LogValue(this_.db, this_.olookup, this_.root_id, this_.tail, this_.__meta, ((IFn)const__16.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
        }
        return (IPersistentMap)object;
    }

    public int size() {
        Counted counted = (Counted)this_;
        LogValue this_ = null;
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
        LogValue this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Collection values() {
        LogValue logValue = this_;
        LogValue this_ = null;
        return (Collection)((IFn)const__1.getRawRoot()).invoke((Object)logValue);
    }

    public Set entrySet() {
        LogValue logValue = this_;
        LogValue this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke((Object)logValue);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"set");
        const__1 = RT.var((String)"clojure.core", (String)"vals");
        const__2 = RT.var((String)"clojure.core", (String)"keys");
        const__4 = RT.var((String)"clojure.core", (String)"some");
        const__7 = RT.var((String)"clojure.core", (String)"contains?");
        const__8 = RT.keyword(null, (String)"root-id");
        const__9 = RT.keyword(null, (String)"db");
        const__10 = RT.keyword(null, (String)"olookup");
        const__11 = RT.keyword(null, (String)"tail");
        const__12 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"root-id"), RT.keyword(null, (String)"db"), RT.keyword(null, (String)"olookup"), RT.keyword(null, (String)"tail")});
        const__13 = RT.var((String)"clojure.core", (String)"dissoc");
        const__14 = RT.var((String)"clojure.core", (String)"with-meta");
        const__15 = RT.var((String)"clojure.core", (String)"into");
        const__16 = RT.var((String)"clojure.core", (String)"not-empty");
        const__17 = RT.var((String)"clojure.core", (String)"identical?");
        const__18 = RT.var((String)"clojure.core", (String)"assoc");
        const__19 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"db"), (Object)RT.keyword(null, (String)"olookup"), (Object)RT.keyword(null, (String)"root-id"), (Object)RT.keyword(null, (String)"tail"));
        const__20 = RT.var((String)"clojure.core", (String)"seq");
        const__21 = RT.var((String)"clojure.core", (String)"concat");
        const__22 = RT.var((String)"clojure.core", (String)"not");
        const__23 = RT.var((String)"clojure.core", (String)"class");
        const__24 = RT.var((String)"clojure.core", (String)"imap-cons");
        const__25 = RT.var((String)"clojure.core", (String)"str");
        const__34 = RT.var((String)"datomic.common", (String)"key-comparator");
        const__35 = RT.var((String)"datomic.log", (String)"log-key");
        const__36 = RT.var((String)"clojure.core", (String)"vec");
        const__37 = RT.var((String)"datomic.common", (String)"getx");
        const__38 = RT.var((String)"datomic.log", (String)"btree-search");
        const__40 = RT.var((String)"datomic.log", (String)"seek-tx-impl");
        const__41 = RT.var((String)"datomic.log", (String)"binary-search");
        const__43 = RT.var((String)"clojure.core", (String)"last");
        const__45 = RT.var((String)"datomic.log", (String)"tx-range");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
        __thunk__1__ = __site__1__;
    }
}

