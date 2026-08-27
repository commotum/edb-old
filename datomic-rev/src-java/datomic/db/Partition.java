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
import datomic.db.IElementImpl;
import datomic.db.Partition$reify__12660;
import datomic.db.Partition$reify__12662;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class Partition
implements IElementImpl,
IRecord,
IHashEq,
IObj,
ILookup,
IKeywordLookup,
IPersistentMap,
Map,
Serializable {
    public final Object id;
    public final Object kw;
    public final Object __meta;
    public final Object __extmap;
    int __hash;
    int __hasheq;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__8 = RT.keyword(null, (String)"kw");
    public static final Keyword const__9 = RT.keyword(null, (String)"id");
    public static final AFn const__10 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"kw"), RT.keyword(null, (String)"id")});
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"not-empty");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"identical?");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"assoc");
    public static final AFn const__17 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"id"), (Object)RT.keyword(null, (String)"kw"));
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"imap-cons");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"str");

    public Partition(Object object, Object object2, Object object3, Object object4, int n, int n2) {
        this.id = object;
        this.kw = object2;
        this.__meta = object3;
        this.__extmap = object4;
        this.__hash = n;
        this.__hasheq = n2;
    }

    public Partition(Object object, Object object2) {
        this(object, object2, null, null, 0, 0);
    }

    public Partition(Object object, Object object2, Object object3, Object object4) {
        this(object, object2, object3, object4, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"kw"));
    }

    public static Partition create(IPersistentMap iPersistentMap) {
        Object object = iPersistentMap.valAt((Object)Keyword.intern((String)"id"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"id"));
        Object object2 = iPersistentMap.valAt((Object)Keyword.intern((String)"kw"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"kw"));
        return new Partition(object, object2, null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    public Object id() {
        return this.id;
    }

    public Object kw() {
        return this.kw;
    }

    /*
     * WARNING - void declaration
     */
    public int hasheq() {
        void v0;
        int hq__7465__auto__12667 = this.__hasheq;
        if ((long)hq__7465__auto__12667 == 0L) {
            void var2_2;
            int h__7466__auto__12666;
            this.__hasheq = h__7466__auto__12666 = (int)(0xFFFFFFFFFF92858FL ^ (long)APersistentMap.mapHasheq((IPersistentMap)this));
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
        int hash__7468__auto__12669 = this.__hash;
        if ((long)hash__7468__auto__12669 == 0L) {
            void var2_2;
            int h__7469__auto__12668;
            this.__hash = h__7469__auto__12668 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__12655) {
        Object object = G__12655;
        G__12655 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__12655) {
        IPersistentMap iPersistentMap = G__12655;
        G__12655 = null;
        return new Partition(this.id, this.kw, iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
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
        Object G__12664 = k__7476__auto__;
        switch (Util.hash((Object)G__12664)) {
            case 1014122589: {
                if (G__12664 != const__9) break;
                object = this_.id;
                return object;
            }
            case 1014124337: {
                if (G__12664 != const__8) break;
                object = this_.kw;
                return object;
            }
        }
        Object object2 = k__7476__auto__;
        k__7476__auto__ = null;
        Object object3 = else__7477__auto__;
        else__7477__auto__ = null;
        Partition this_ = null;
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
        Keyword G__12659 = keyword;
        switch (Util.hash((Object)G__12659)) {
            case 1014122589: {
                if (G__12659 != const__9) break;
                gclass = null;
                object = new Partition$reify__12660(null, gclass);
                return object;
            }
            case 1014124337: {
                if (G__12659 != const__8) break;
                gclass = null;
                object = new Partition$reify__12662(null, gclass);
                return object;
            }
        }
        object = null;
        return object;
    }

    public int count() {
        return RT.intCast((long)(2L + (long)RT.count((Object)this.__extmap)));
    }

    public IPersistentCollection empty() {
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__23.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.db.Partition"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        Partition partition = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        Partition this_ = null;
        return (IPersistentCollection)((IFn)const__22).invoke((Object)partition, object);
    }

    public boolean equiv(Object G__12655) {
        Boolean bl;
        boolean or__5238__auto__12672 = Util.identical((Object)this, (Object)G__12655);
        if (or__5238__auto__12672) {
            bl = or__5238__auto__12672 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__21.getRawRoot()).invoke((Object)this), (Object)((IFn)const__21.getRawRoot()).invoke(G__12655))) {
            Object object = G__12655;
            G__12655 = null;
            Object G__126552 = object;
            boolean and__5236__auto__12671 = Util.equiv((Object)this.id, (Object)((Partition)G__126552).id);
            if (and__5236__auto__12671) {
                boolean and__5236__auto__12670 = Util.equiv((Object)this.kw, (Object)((Partition)G__126552).kw);
                if (and__5236__auto__12670) {
                    Object object2 = G__126552;
                    G__126552 = null;
                    bl = Util.equiv((Object)this.__extmap, (Object)((Partition)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    bl = and__5236__auto__12670 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__12671 ? Boolean.TRUE : Boolean.FALSE;
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
        Partition this_ = null;
        return (Boolean)((IFn)const__20.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__12673 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__12673)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__12673;
            v__7489__auto__12673 = null;
            Partition this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        Partition this_ = null;
        return (ISeq)((IFn)const__18.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke((Object)Tuple.create((Object)MapEntry.create((Object)const__9, (Object)this_.id), (Object)MapEntry.create((Object)const__8, (Object)this_.kw)), this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)const__17, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__12655) {
        Partition partition;
        Object pred__12657 = const__15.getRawRoot();
        Object expr__12658 = k__7493__auto__;
        Object object = ((IFn)pred__12657).invoke((Object)const__9, expr__12658);
        if (object != null && object != Boolean.FALSE) {
            G__12655 = null;
            partition = new Partition(G__12655, this.kw, this.__meta, this.__extmap);
        } else {
            Object object2 = pred__12657;
            pred__12657 = null;
            Object object3 = expr__12658;
            expr__12658 = null;
            Object object4 = ((IFn)object2).invoke((Object)const__8, object3);
            if (object4 != null && object4 != Boolean.FALSE) {
                G__12655 = null;
                partition = new Partition(this.id, G__12655, this.__meta, this.__extmap);
            } else {
                k__7493__auto__ = null;
                G__12655 = null;
                partition = new Partition(this.id, this.kw, this.__meta, ((IFn)const__16.getRawRoot()).invoke(this.__extmap, k__7493__auto__, G__12655));
            }
        }
        return partition;
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)const__10, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__12.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            Partition this_ = null;
            object = ((IFn)const__11.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new Partition(this_.id, this_.kw, this_.__meta, ((IFn)const__14.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
        }
        return (IPersistentMap)object;
    }

    public int size() {
        Counted counted = (Counted)this_;
        Partition this_ = null;
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
        Partition this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Collection values() {
        Partition partition = this_;
        Partition this_ = null;
        return (Collection)((IFn)const__1.getRawRoot()).invoke((Object)partition);
    }

    public Set entrySet() {
        Partition partition = this_;
        Partition this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke((Object)partition);
    }
}

