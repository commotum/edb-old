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
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.RecordIterator
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.db;

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
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.RecordIterator;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.MemLog$reify__13371;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class MemLog
implements IRecord,
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

    public MemLog(Object object, Object object2, Object object3, int n, int n2) {
        this.txes = object;
        this.__meta = object2;
        this.__extmap = object3;
        this.__hash = n;
        this.__hasheq = n2;
    }

    public MemLog(Object object) {
        this(object, null, null, 0, 0);
    }

    public MemLog(Object object, Object object2, Object object3) {
        this(object, object2, object3, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"txes"));
    }

    public static MemLog create(IPersistentMap iPersistentMap) {
        Object object = iPersistentMap.valAt((Object)Keyword.intern((String)"txes"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"txes"));
        return new MemLog(object, null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    /*
     * WARNING - void declaration
     */
    public int hasheq() {
        void v0;
        int hq__7465__auto__13376 = this.__hasheq;
        if ((long)hq__7465__auto__13376 == 0L) {
            void var2_2;
            int h__7466__auto__13375;
            this.__hasheq = h__7466__auto__13375 = (int)(0x2D6D24CL ^ (long)APersistentMap.mapHasheq((IPersistentMap)this));
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
        int hash__7468__auto__13378 = this.__hash;
        if ((long)hash__7468__auto__13378 == 0L) {
            void var2_2;
            int h__7469__auto__13377;
            this.__hash = h__7469__auto__13377 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__13366) {
        Object object = G__13366;
        G__13366 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__13366) {
        IPersistentMap iPersistentMap = G__13366;
        G__13366 = null;
        return new MemLog(this.txes, iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
    }

    public Object valAt(Object k__7474__auto__) {
        Object object = k__7474__auto__;
        k__7474__auto__ = null;
        return ((ILookup)this).valAt(object, null);
    }

    public Object valAt(Object k__7476__auto__, Object else__7477__auto__) {
        Object object;
        Object G__13373 = k__7476__auto__;
        switch (Util.hash((Object)G__13373)) {
            case 1242078384: {
                if (G__13373 == const__8) {
                    object = this_.txes;
                    break;
                }
            }
            default: {
                Object object2 = k__7476__auto__;
                k__7476__auto__ = null;
                Object object3 = else__7477__auto__;
                else__7477__auto__ = null;
                MemLog this_ = null;
                object = RT.get((Object)this_.__extmap, (Object)object2, (Object)object3);
            }
        }
        return object;
    }

    public ILookupThunk getLookupThunk(Keyword k__7479__auto__) {
        MemLog$reify__13371 memLog$reify__13371;
        Object gclass = ((IFn)const__20.getRawRoot()).invoke((Object)this);
        Keyword keyword = k__7479__auto__;
        k__7479__auto__ = null;
        Keyword G__13370 = keyword;
        switch (Util.hash((Object)G__13370)) {
            case 1242078384: {
                if (G__13370 == const__8) {
                    gclass = null;
                    memLog$reify__13371 = new MemLog$reify__13371(null, gclass);
                    break;
                }
            }
            default: {
                memLog$reify__13371 = null;
            }
        }
        return memLog$reify__13371;
    }

    public int count() {
        return RT.intCast((long)(1L + (long)RT.count((Object)this.__extmap)));
    }

    public IPersistentCollection empty() {
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__22.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.db.MemLog"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        MemLog memLog = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        MemLog this_ = null;
        return (IPersistentCollection)((IFn)const__21).invoke((Object)memLog, object);
    }

    public boolean equiv(Object G__13366) {
        Boolean bl;
        boolean or__5238__auto__13380 = Util.identical((Object)this, (Object)G__13366);
        if (or__5238__auto__13380) {
            bl = or__5238__auto__13380 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__20.getRawRoot()).invoke((Object)this), (Object)((IFn)const__20.getRawRoot()).invoke(G__13366))) {
            Object object = G__13366;
            G__13366 = null;
            Object G__133662 = object;
            boolean and__5236__auto__13379 = Util.equiv((Object)this.txes, (Object)((MemLog)G__133662).txes);
            if (and__5236__auto__13379) {
                Object object2 = G__133662;
                G__133662 = null;
                bl = Util.equiv((Object)this.__extmap, (Object)((MemLog)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                bl = and__5236__auto__13379 ? Boolean.TRUE : Boolean.FALSE;
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
        MemLog this_ = null;
        return (Boolean)((IFn)const__19.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__13381 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__13381)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__13381;
            v__7489__auto__13381 = null;
            MemLog this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        MemLog this_ = null;
        return (ISeq)((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke((Object)Tuple.create((Object)MapEntry.create((Object)const__8, (Object)this_.txes)), this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)const__16, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__13366) {
        MemLog memLog;
        Object pred__13368 = const__14.getRawRoot();
        Object expr__13369 = k__7493__auto__;
        Object object = pred__13368;
        pred__13368 = null;
        Object object2 = expr__13369;
        expr__13369 = null;
        Object object3 = ((IFn)object).invoke((Object)const__8, object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            G__13366 = null;
            memLog = new MemLog(G__13366, this.__meta, this.__extmap);
        } else {
            k__7493__auto__ = null;
            G__13366 = null;
            memLog = new MemLog(this.txes, this.__meta, ((IFn)const__15.getRawRoot()).invoke(this.__extmap, k__7493__auto__, G__13366));
        }
        return memLog;
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)const__9, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__11.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            MemLog this_ = null;
            object = ((IFn)const__10.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new MemLog(this_.txes, this_.__meta, ((IFn)const__13.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
        }
        return (IPersistentMap)object;
    }

    public int size() {
        Counted counted = (Counted)this_;
        MemLog this_ = null;
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
        MemLog this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Collection values() {
        MemLog memLog = this_;
        MemLog this_ = null;
        return (Collection)((IFn)const__1.getRawRoot()).invoke((Object)memLog);
    }

    public Set entrySet() {
        MemLog memLog = this_;
        MemLog this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke((Object)memLog);
    }
}

