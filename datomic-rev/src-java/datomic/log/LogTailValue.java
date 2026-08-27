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
import datomic.log.LogSeek;
import datomic.log.LogTailValue$reify__16460;
import datomic.log.TailTxIter;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class LogTailValue
implements LogSeek,
IRecord,
IHashEq,
IObj,
ILookup,
IKeywordLookup,
IPersistentMap,
Map,
Serializable {
    public final Object txes;
    public final Object __meta;
    public final Object __extmap;
    int __hash;
    int __hasheq;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__8 = RT.keyword(null, (String)"txes");
    public static final AFn const__9 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"txes")});
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"not-empty");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"identical?");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"assoc");
    public static final AFn const__16 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"txes"));
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"imap-cons");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__31 = RT.var((String)"datomic.common", (String)"key-comparator");
    public static final Var const__32 = RT.var((String)"datomic.log", (String)"log-key");
    public static final Var const__33 = RT.var((String)"datomic.log", (String)"binary-search");

    public LogTailValue(Object object, Object object2, Object object3, int n, int n2) {
        this.txes = object;
        this.__meta = object2;
        this.__extmap = object3;
        this.__hash = n;
        this.__hasheq = n2;
    }

    public LogTailValue(Object object) {
        this(object, null, null, 0, 0);
    }

    public LogTailValue(Object object, Object object2, Object object3) {
        this(object, object2, object3, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"txes"));
    }

    public static LogTailValue create(IPersistentMap iPersistentMap) {
        Object object = iPersistentMap.valAt((Object)Keyword.intern((String)"txes"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"txes"));
        return new LogTailValue(object, null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    public Object seek_tx_impl(Object k) {
        TailTxIter tailTxIter;
        Object idx;
        Object comp2 = ((IFn)const__31.getRawRoot()).invoke(const__32.getRawRoot());
        Object object = k;
        k = null;
        Object object2 = comp2;
        comp2 = null;
        Object object3 = idx = ((IFn)const__33.getRawRoot()).invoke(this.txes, object, object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            idx = null;
            tailTxIter = new TailTxIter(this.txes, RT.longCast((Object)((Number)idx)));
        } else {
            tailTxIter = null;
        }
        return tailTxIter;
    }

    /*
     * WARNING - void declaration
     */
    public int hasheq() {
        void v0;
        int hq__7465__auto__16465 = this.__hasheq;
        if ((long)hq__7465__auto__16465 == 0L) {
            void var2_2;
            int h__7466__auto__16464;
            this.__hasheq = h__7466__auto__16464 = RT.intCast((long)(0xFFFFFFFF88FA567FL ^ (long)APersistentMap.mapHasheq((IPersistentMap)this)));
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
        int hash__7468__auto__16467 = this.__hash;
        if ((long)hash__7468__auto__16467 == 0L) {
            void var2_2;
            int h__7469__auto__16466;
            this.__hash = h__7469__auto__16466 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__16455) {
        Object object = G__16455;
        G__16455 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__16455) {
        IPersistentMap iPersistentMap = G__16455;
        G__16455 = null;
        return new LogTailValue(this.txes, iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
    }

    public Object valAt(Object k__7474__auto__) {
        Object object = k__7474__auto__;
        k__7474__auto__ = null;
        return ((ILookup)this).valAt(object, null);
    }

    public Object valAt(Object k__7476__auto__, Object else__7477__auto__) {
        Object object;
        Object G__16462 = k__7476__auto__;
        switch (Util.hash((Object)G__16462)) {
            case 1242078384: {
                if (G__16462 == const__8) {
                    object = this_.txes;
                    break;
                }
            }
            default: {
                Object object2 = k__7476__auto__;
                k__7476__auto__ = null;
                Object object3 = else__7477__auto__;
                else__7477__auto__ = null;
                LogTailValue this_ = null;
                object = RT.get((Object)this_.__extmap, (Object)object2, (Object)object3);
            }
        }
        return object;
    }

    public ILookupThunk getLookupThunk(Keyword k__7479__auto__) {
        LogTailValue$reify__16460 logTailValue$reify__16460;
        Object gclass = ((IFn)const__20.getRawRoot()).invoke((Object)this);
        Keyword keyword = k__7479__auto__;
        k__7479__auto__ = null;
        Keyword G__16459 = keyword;
        switch (Util.hash((Object)G__16459)) {
            case 1242078384: {
                if (G__16459 == const__8) {
                    gclass = null;
                    logTailValue$reify__16460 = new LogTailValue$reify__16460(null, gclass);
                    break;
                }
            }
            default: {
                logTailValue$reify__16460 = null;
            }
        }
        return logTailValue$reify__16460;
    }

    public int count() {
        return RT.intCast((long)Numbers.add((long)1L, (long)RT.count((Object)this.__extmap)));
    }

    public IPersistentCollection empty() {
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__22.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.log.LogTailValue"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        LogTailValue logTailValue = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        LogTailValue this_ = null;
        return (IPersistentCollection)((IFn)const__21).invoke((Object)logTailValue, object);
    }

    public boolean equiv(Object G__16455) {
        Boolean bl;
        boolean or__5238__auto__16469 = Util.identical((Object)this, (Object)G__16455);
        if (or__5238__auto__16469) {
            bl = or__5238__auto__16469 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__20.getRawRoot()).invoke((Object)this), (Object)((IFn)const__20.getRawRoot()).invoke(G__16455))) {
            Object object = G__16455;
            G__16455 = null;
            Object G__164552 = object;
            boolean and__5236__auto__16468 = Util.equiv((Object)this.txes, (Object)((LogTailValue)G__164552).txes);
            if (and__5236__auto__16468) {
                Object object2 = G__164552;
                G__164552 = null;
                bl = Util.equiv((Object)this.__extmap, (Object)((LogTailValue)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                bl = and__5236__auto__16468 ? Boolean.TRUE : Boolean.FALSE;
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
        LogTailValue this_ = null;
        return (Boolean)((IFn)const__19.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__16470 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__16470)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__16470;
            v__7489__auto__16470 = null;
            LogTailValue this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        LogTailValue this_ = null;
        return (ISeq)((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke((Object)Tuple.create((Object)MapEntry.create((Object)const__8, (Object)this_.txes)), this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)const__16, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__16455) {
        LogTailValue logTailValue;
        Object pred__16457 = const__14.getRawRoot();
        Object expr__16458 = k__7493__auto__;
        Object object = pred__16457;
        pred__16457 = null;
        Object object2 = expr__16458;
        expr__16458 = null;
        Object object3 = ((IFn)object).invoke((Object)const__8, object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            G__16455 = null;
            logTailValue = new LogTailValue(G__16455, this.__meta, this.__extmap);
        } else {
            k__7493__auto__ = null;
            G__16455 = null;
            logTailValue = new LogTailValue(this.txes, this.__meta, ((IFn)const__15.getRawRoot()).invoke(this.__extmap, k__7493__auto__, G__16455));
        }
        return logTailValue;
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)const__9, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__11.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            LogTailValue this_ = null;
            object = ((IFn)const__10.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new LogTailValue(this_.txes, this_.__meta, ((IFn)const__13.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
        }
        return (IPersistentMap)object;
    }

    public int size() {
        Counted counted = (Counted)this_;
        LogTailValue this_ = null;
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
        LogTailValue this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Collection values() {
        LogTailValue logTailValue = this_;
        LogTailValue this_ = null;
        return (Collection)((IFn)const__1.getRawRoot()).invoke((Object)logTailValue);
    }

    public Set entrySet() {
        LogTailValue logTailValue = this_;
        LogTailValue this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke((Object)logTailValue);
    }
}

