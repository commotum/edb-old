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
import datomic.db.IndexSet$reify__12772;
import datomic.db.IndexSet$reify__12774;
import datomic.db.IndexSet$reify__12776;
import datomic.db.IndexSet$reify__12778;
import datomic.db.IndexSet$reify__12780;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class IndexSet
implements IRecord,
IHashEq,
IObj,
ILookup,
IKeywordLookup,
IPersistentMap,
Map,
Serializable {
    public final Object eavt;
    public final Object avet;
    public final Object aevt;
    public final Object raet;
    public final Object fulltext;
    public final Object __meta;
    public final Object __extmap;
    int __hash;
    int __hasheq;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__8 = RT.keyword(null, (String)"aevt");
    public static final Keyword const__9 = RT.keyword(null, (String)"avet");
    public static final Keyword const__10 = RT.keyword(null, (String)"fulltext");
    public static final Keyword const__11 = RT.keyword(null, (String)"eavt");
    public static final Keyword const__12 = RT.keyword(null, (String)"raet");
    public static final AFn const__13 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"aevt"), RT.keyword(null, (String)"avet"), RT.keyword(null, (String)"fulltext"), RT.keyword(null, (String)"eavt"), RT.keyword(null, (String)"raet")});
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"not-empty");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"identical?");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"assoc");
    public static final AFn const__20 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"eavt"), (Object)RT.keyword(null, (String)"avet"), (Object)RT.keyword(null, (String)"aevt"), (Object)RT.keyword(null, (String)"raet"), (Object)RT.keyword(null, (String)"fulltext"));
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"imap-cons");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"str");

    public IndexSet(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, int n, int n2) {
        this.eavt = object;
        this.avet = object2;
        this.aevt = object3;
        this.raet = object4;
        this.fulltext = object5;
        this.__meta = object6;
        this.__extmap = object7;
        this.__hash = n;
        this.__hasheq = n2;
    }

    public IndexSet(Object object, Object object2, Object object3, Object object4, Object object5) {
        this(object, object2, object3, object4, object5, null, null, 0, 0);
    }

    public IndexSet(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this(object, object2, object3, object4, object5, object6, object7, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"eavt")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDataSet")})), (Object)((IObj)Symbol.intern(null, (String)"avet")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDataSet")})), (Object)((IObj)Symbol.intern(null, (String)"aevt")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDataSet")})), (Object)((IObj)Symbol.intern(null, (String)"raet")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDataSet")})), (Object)Symbol.intern(null, (String)"fulltext"));
    }

    public static IndexSet create(IPersistentMap iPersistentMap) {
        Object object = iPersistentMap.valAt((Object)Keyword.intern((String)"eavt"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"eavt"));
        Object object2 = iPersistentMap.valAt((Object)Keyword.intern((String)"avet"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"avet"));
        Object object3 = iPersistentMap.valAt((Object)Keyword.intern((String)"aevt"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"aevt"));
        Object object4 = iPersistentMap.valAt((Object)Keyword.intern((String)"raet"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"raet"));
        Object object5 = iPersistentMap.valAt((Object)Keyword.intern((String)"fulltext"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"fulltext"));
        return new IndexSet(object, object2, object3, object4, object5, null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    /*
     * WARNING - void declaration
     */
    public int hasheq() {
        void v0;
        int hq__7465__auto__12785 = this.__hasheq;
        if ((long)hq__7465__auto__12785 == 0L) {
            void var2_2;
            int h__7466__auto__12784;
            this.__hasheq = h__7466__auto__12784 = (int)(0x78447992L ^ (long)APersistentMap.mapHasheq((IPersistentMap)this));
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
        int hash__7468__auto__12787 = this.__hash;
        if ((long)hash__7468__auto__12787 == 0L) {
            void var2_2;
            int h__7469__auto__12786;
            this.__hash = h__7469__auto__12786 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__12767) {
        Object object = G__12767;
        G__12767 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__12767) {
        IPersistentMap iPersistentMap = G__12767;
        G__12767 = null;
        return new IndexSet(this.eavt, this.avet, this.aevt, this.raet, this.fulltext, iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
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
        Object G__12782 = k__7476__auto__;
        switch (Util.hash((Object)G__12782) >> 4 & 7) {
            case 0: {
                if (G__12782 != const__8) break;
                object = this_.aevt;
                return object;
            }
            case 2: {
                if (G__12782 != const__10) break;
                object = this_.fulltext;
                return object;
            }
            case 3: {
                if (G__12782 != const__9) break;
                object = this_.avet;
                return object;
            }
            case 4: {
                if (G__12782 != const__11) break;
                object = this_.eavt;
                return object;
            }
            case 5: {
                if (G__12782 != const__12) break;
                object = this_.raet;
                return object;
            }
        }
        Object object2 = k__7476__auto__;
        k__7476__auto__ = null;
        Object object3 = else__7477__auto__;
        else__7477__auto__ = null;
        IndexSet this_ = null;
        object = RT.get((Object)this_.__extmap, (Object)object2, (Object)object3);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public ILookupThunk getLookupThunk(Keyword k__7479__auto__) {
        Object object;
        Object gclass = ((IFn)const__24.getRawRoot()).invoke((Object)this);
        Keyword keyword = k__7479__auto__;
        k__7479__auto__ = null;
        Keyword G__12771 = keyword;
        switch (Util.hash((Object)G__12771) >> 4 & 7) {
            case 0: {
                if (G__12771 != const__8) break;
                gclass = null;
                object = new IndexSet$reify__12772(null, gclass);
                return object;
            }
            case 2: {
                if (G__12771 != const__10) break;
                gclass = null;
                object = new IndexSet$reify__12774(null, gclass);
                return object;
            }
            case 3: {
                if (G__12771 != const__9) break;
                gclass = null;
                object = new IndexSet$reify__12776(null, gclass);
                return object;
            }
            case 4: {
                if (G__12771 != const__11) break;
                gclass = null;
                object = new IndexSet$reify__12778(null, gclass);
                return object;
            }
            case 5: {
                if (G__12771 != const__12) break;
                gclass = null;
                object = new IndexSet$reify__12780(null, gclass);
                return object;
            }
        }
        object = null;
        return object;
    }

    public int count() {
        return RT.intCast((long)(5L + (long)RT.count((Object)this.__extmap)));
    }

    public IPersistentCollection empty() {
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__26.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.db.IndexSet"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        IndexSet indexSet = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        IndexSet this_ = null;
        return (IPersistentCollection)((IFn)const__25).invoke((Object)indexSet, object);
    }

    public boolean equiv(Object G__12767) {
        Boolean bl;
        boolean or__5238__auto__12793 = Util.identical((Object)this, (Object)G__12767);
        if (or__5238__auto__12793) {
            bl = or__5238__auto__12793 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__24.getRawRoot()).invoke((Object)this), (Object)((IFn)const__24.getRawRoot()).invoke(G__12767))) {
            Object object = G__12767;
            G__12767 = null;
            Object G__127672 = object;
            boolean and__5236__auto__12792 = Util.equiv((Object)this.eavt, (Object)((IndexSet)G__127672).eavt);
            if (and__5236__auto__12792) {
                boolean and__5236__auto__12791 = Util.equiv((Object)this.avet, (Object)((IndexSet)G__127672).avet);
                if (and__5236__auto__12791) {
                    boolean and__5236__auto__12790 = Util.equiv((Object)this.aevt, (Object)((IndexSet)G__127672).aevt);
                    if (and__5236__auto__12790) {
                        boolean and__5236__auto__12789 = Util.equiv((Object)this.raet, (Object)((IndexSet)G__127672).raet);
                        if (and__5236__auto__12789) {
                            boolean and__5236__auto__12788 = Util.equiv((Object)this.fulltext, (Object)((IndexSet)G__127672).fulltext);
                            if (and__5236__auto__12788) {
                                Object object2 = G__127672;
                                G__127672 = null;
                                bl = Util.equiv((Object)this.__extmap, (Object)((IndexSet)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
                            } else {
                                bl = and__5236__auto__12788 ? Boolean.TRUE : Boolean.FALSE;
                            }
                        } else {
                            bl = and__5236__auto__12789 ? Boolean.TRUE : Boolean.FALSE;
                        }
                    } else {
                        bl = and__5236__auto__12790 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    bl = and__5236__auto__12791 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__12792 ? Boolean.TRUE : Boolean.FALSE;
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
        IndexSet this_ = null;
        return (Boolean)((IFn)const__23.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__12794 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__12794)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__12794;
            v__7489__auto__12794 = null;
            IndexSet this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        IndexSet this_ = null;
        return (ISeq)((IFn)const__21.getRawRoot()).invoke(((IFn)const__22.getRawRoot()).invoke((Object)Tuple.create((Object)MapEntry.create((Object)const__11, (Object)this_.eavt), (Object)MapEntry.create((Object)const__9, (Object)this_.avet), (Object)MapEntry.create((Object)const__8, (Object)this_.aevt), (Object)MapEntry.create((Object)const__12, (Object)this_.raet), (Object)MapEntry.create((Object)const__10, (Object)this_.fulltext)), this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)const__20, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__12767) {
        IndexSet indexSet;
        Object pred__12769 = const__18.getRawRoot();
        Object expr__12770 = k__7493__auto__;
        Object object = ((IFn)pred__12769).invoke((Object)const__11, expr__12770);
        if (object != null && object != Boolean.FALSE) {
            G__12767 = null;
            indexSet = new IndexSet(G__12767, this.avet, this.aevt, this.raet, this.fulltext, this.__meta, this.__extmap);
        } else {
            Object object2 = ((IFn)pred__12769).invoke((Object)const__9, expr__12770);
            if (object2 != null && object2 != Boolean.FALSE) {
                G__12767 = null;
                indexSet = new IndexSet(this.eavt, G__12767, this.aevt, this.raet, this.fulltext, this.__meta, this.__extmap);
            } else {
                Object object3 = ((IFn)pred__12769).invoke((Object)const__8, expr__12770);
                if (object3 != null && object3 != Boolean.FALSE) {
                    G__12767 = null;
                    indexSet = new IndexSet(this.eavt, this.avet, G__12767, this.raet, this.fulltext, this.__meta, this.__extmap);
                } else {
                    Object object4 = ((IFn)pred__12769).invoke((Object)const__12, expr__12770);
                    if (object4 != null && object4 != Boolean.FALSE) {
                        G__12767 = null;
                        indexSet = new IndexSet(this.eavt, this.avet, this.aevt, G__12767, this.fulltext, this.__meta, this.__extmap);
                    } else {
                        Object object5 = pred__12769;
                        pred__12769 = null;
                        Object object6 = expr__12770;
                        expr__12770 = null;
                        Object object7 = ((IFn)object5).invoke((Object)const__10, object6);
                        if (object7 != null && object7 != Boolean.FALSE) {
                            G__12767 = null;
                            indexSet = new IndexSet(this.eavt, this.avet, this.aevt, this.raet, G__12767, this.__meta, this.__extmap);
                        } else {
                            k__7493__auto__ = null;
                            G__12767 = null;
                            indexSet = new IndexSet(this.eavt, this.avet, this.aevt, this.raet, this.fulltext, this.__meta, ((IFn)const__19.getRawRoot()).invoke(this.__extmap, k__7493__auto__, G__12767));
                        }
                    }
                }
            }
        }
        return indexSet;
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)const__13, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            IndexSet this_ = null;
            object = ((IFn)const__14.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new IndexSet(this_.eavt, this_.avet, this_.aevt, this_.raet, this_.fulltext, this_.__meta, ((IFn)const__17.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
        }
        return (IPersistentMap)object;
    }

    public int size() {
        Counted counted = (Counted)this_;
        IndexSet this_ = null;
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
        IndexSet this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Collection values() {
        IndexSet indexSet = this_;
        IndexSet this_ = null;
        return (Collection)((IFn)const__1.getRawRoot()).invoke((Object)indexSet);
    }

    public Set entrySet() {
        IndexSet indexSet = this_;
        IndexSet this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke((Object)indexSet);
    }
}

