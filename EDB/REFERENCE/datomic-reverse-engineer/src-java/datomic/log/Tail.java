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
import datomic.log.Tail$reify__16204;
import datomic.log.Tail$reify__16206;
import datomic.log.TailTxIter;
import datomic.log.TailTxes;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class Tail
implements LogSeek,
TailTxes,
IRecord,
IHashEq,
IObj,
ILookup,
IKeywordLookup,
IPersistentMap,
Map,
Serializable {
    public final Object txes;
    public final Object bufs;
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
    public static final Keyword const__9 = RT.keyword(null, (String)"bufs");
    public static final AFn const__10 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"txes"), RT.keyword(null, (String)"bufs")});
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"not-empty");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"identical?");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"assoc");
    public static final AFn const__17 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"txes"), (Object)RT.keyword(null, (String)"bufs"));
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"imap-cons");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__32 = RT.var((String)"datomic.common", (String)"key-comparator");
    public static final Var const__33 = RT.var((String)"datomic.log", (String)"log-key");
    public static final Var const__34 = RT.var((String)"datomic.log", (String)"binary-search");

    public Tail(Object object, Object object2, Object object3, Object object4, int n, int n2) {
        this.txes = object;
        this.bufs = object2;
        this.__meta = object3;
        this.__extmap = object4;
        this.__hash = n;
        this.__hasheq = n2;
    }

    public Tail(Object object, Object object2) {
        this(object, object2, null, null, 0, 0);
    }

    public Tail(Object object, Object object2, Object object3, Object object4) {
        this(object, object2, object3, object4, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"txes"), (Object)Symbol.intern(null, (String)"bufs"));
    }

    public static Tail create(IPersistentMap iPersistentMap) {
        Object object = iPersistentMap.valAt((Object)Keyword.intern((String)"txes"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"txes"));
        Object object2 = iPersistentMap.valAt((Object)Keyword.intern((String)"bufs"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"bufs"));
        return new Tail(object, object2, null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    public Object tail_txes() {
        return this.txes;
    }

    public Object seek_tx_impl(Object k) {
        TailTxIter tailTxIter;
        Object idx;
        Object comp2 = ((IFn)const__32.getRawRoot()).invoke(const__33.getRawRoot());
        Object object = k;
        k = null;
        Object object2 = comp2;
        comp2 = null;
        Object object3 = idx = ((IFn)const__34.getRawRoot()).invoke(this.txes, object, object2);
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
        int hq__7465__auto__16211 = this.__hasheq;
        if ((long)hq__7465__auto__16211 == 0L) {
            void var2_2;
            int h__7466__auto__16210;
            this.__hasheq = h__7466__auto__16210 = RT.intCast((long)(0xFFFFFFFF97FDFF13L ^ (long)APersistentMap.mapHasheq((IPersistentMap)this)));
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
        int hash__7468__auto__16213 = this.__hash;
        if ((long)hash__7468__auto__16213 == 0L) {
            void var2_2;
            int h__7469__auto__16212;
            this.__hash = h__7469__auto__16212 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__16199) {
        Object object = G__16199;
        G__16199 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__16199) {
        IPersistentMap iPersistentMap = G__16199;
        G__16199 = null;
        return new Tail(this.txes, this.bufs, iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
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
        Object G__16208 = k__7476__auto__;
        switch (Util.hash((Object)G__16208) >> 1 & 1) {
            case 0: {
                if (G__16208 != const__8) break;
                object = this_.txes;
                return object;
            }
            case 1: {
                if (G__16208 != const__9) break;
                object = this_.bufs;
                return object;
            }
        }
        Object object2 = k__7476__auto__;
        k__7476__auto__ = null;
        Object object3 = else__7477__auto__;
        else__7477__auto__ = null;
        Tail this_ = null;
        object = RT.get((Object)this_.__extmap, (Object)object2, (Object)object3);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public ILookupThunk getLookupThunk(Keyword k__7479__auto__) {
        Object object;
        Object gclass = ((IFn)const__21.getRawRoot()).invoke((Object)this);
        Keyword keyword = k__7479__auto__;
        k__7479__auto__ = null;
        Keyword G__16203 = keyword;
        switch (Util.hash((Object)G__16203) >> 1 & 1) {
            case 0: {
                if (G__16203 != const__8) break;
                gclass = null;
                object = new Tail$reify__16204(null, gclass);
                return object;
            }
            case 1: {
                if (G__16203 != const__9) break;
                gclass = null;
                object = new Tail$reify__16206(null, gclass);
                return object;
            }
        }
        object = null;
        return object;
    }

    public int count() {
        return RT.intCast((long)Numbers.add((long)2L, (long)RT.count((Object)this.__extmap)));
    }

    public IPersistentCollection empty() {
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__23.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.log.Tail"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        Tail tail = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        Tail this_ = null;
        return (IPersistentCollection)((IFn)const__22).invoke((Object)tail, object);
    }

    public boolean equiv(Object G__16199) {
        Boolean bl;
        boolean or__5238__auto__16216 = Util.identical((Object)this, (Object)G__16199);
        if (or__5238__auto__16216) {
            bl = or__5238__auto__16216 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__21.getRawRoot()).invoke((Object)this), (Object)((IFn)const__21.getRawRoot()).invoke(G__16199))) {
            Object object = G__16199;
            G__16199 = null;
            Object G__161992 = object;
            boolean and__5236__auto__16215 = Util.equiv((Object)this.txes, (Object)((Tail)G__161992).txes);
            if (and__5236__auto__16215) {
                boolean and__5236__auto__16214 = Util.equiv((Object)this.bufs, (Object)((Tail)G__161992).bufs);
                if (and__5236__auto__16214) {
                    Object object2 = G__161992;
                    G__161992 = null;
                    bl = Util.equiv((Object)this.__extmap, (Object)((Tail)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    bl = and__5236__auto__16214 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__16215 ? Boolean.TRUE : Boolean.FALSE;
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
        Tail this_ = null;
        return (Boolean)((IFn)const__20.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__16217 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__16217)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__16217;
            v__7489__auto__16217 = null;
            Tail this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        Tail this_ = null;
        return (ISeq)((IFn)const__18.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke((Object)Tuple.create((Object)MapEntry.create((Object)const__8, (Object)this_.txes), (Object)MapEntry.create((Object)const__9, (Object)this_.bufs)), this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)const__17, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__16199) {
        Tail tail;
        Object pred__16201 = const__15.getRawRoot();
        Object expr__16202 = k__7493__auto__;
        Object object = ((IFn)pred__16201).invoke((Object)const__8, expr__16202);
        if (object != null && object != Boolean.FALSE) {
            G__16199 = null;
            tail = new Tail(G__16199, this.bufs, this.__meta, this.__extmap);
        } else {
            Object object2 = pred__16201;
            pred__16201 = null;
            Object object3 = expr__16202;
            expr__16202 = null;
            Object object4 = ((IFn)object2).invoke((Object)const__9, object3);
            if (object4 != null && object4 != Boolean.FALSE) {
                G__16199 = null;
                tail = new Tail(this.txes, G__16199, this.__meta, this.__extmap);
            } else {
                k__7493__auto__ = null;
                G__16199 = null;
                tail = new Tail(this.txes, this.bufs, this.__meta, ((IFn)const__16.getRawRoot()).invoke(this.__extmap, k__7493__auto__, G__16199));
            }
        }
        return tail;
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)const__10, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__12.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            Tail this_ = null;
            object = ((IFn)const__11.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new Tail(this_.txes, this_.bufs, this_.__meta, ((IFn)const__14.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
        }
        return (IPersistentMap)object;
    }

    public int size() {
        Counted counted = (Counted)this_;
        Tail this_ = null;
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
        Tail this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Collection values() {
        Tail tail = this_;
        Tail this_ = null;
        return (Collection)((IFn)const__1.getRawRoot()).invoke((Object)tail);
    }

    public Set entrySet() {
        Tail tail = this_;
        Tail this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke((Object)tail);
    }
}

