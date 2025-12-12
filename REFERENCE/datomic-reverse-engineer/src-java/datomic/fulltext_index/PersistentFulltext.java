/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.RecordIterator
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.fulltext_index;

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
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.RecordIterator;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.fulltext_index.LuceneProvider;
import datomic.lucene.Readerable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class PersistentFulltext
implements LuceneProvider,
IRecord,
IHashEq,
IObj,
ILookup,
IKeywordLookup,
IPersistentMap,
Map,
Serializable {
    public final Object __meta;
    public final Object __extmap;
    int __hash;
    int __hasheq;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__4;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final Var const__19;
    public static final Var const__28;
    public static final Var const__29;

    public PersistentFulltext(Object object, Object object2, int n, int n2) {
        this.__meta = object;
        this.__extmap = object2;
        this.__hash = n;
        this.__hasheq = n2;
    }

    public PersistentFulltext() {
        this(null, null, 0, 0);
    }

    public PersistentFulltext(Object object, Object object2) {
        this(object, object2, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return Tuple.create();
    }

    public static PersistentFulltext create(IPersistentMap iPersistentMap) {
        return new PersistentFulltext(null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object fulltext_attr_reader(Object attr) {
        Object object;
        Object m;
        Object temp__5457__auto__12351;
        Object object2 = attr;
        attr = null;
        Object object3 = temp__5457__auto__12351 = RT.get((Object)this_, (Object)object2);
        if (object3 == null) return null;
        if (object3 == Boolean.FALSE) return null;
        Object object4 = temp__5457__auto__12351;
        temp__5457__auto__12351 = null;
        Object object5 = m = object4;
        m = null;
        Object object6 = ((IFn)const__29.getRawRoot()).invoke(object5);
        if (Util.classOf((Object)object6) != __cached_class__0) {
            if (object6 instanceof Readerable) {
                object = ((Readerable)object6).index_reader();
                return object;
            }
            object6 = object6;
            __cached_class__0 = Util.classOf((Object)object6);
        }
        PersistentFulltext this_ = null;
        object = const__28.getRawRoot().invoke(object6);
        return object;
    }

    /*
     * WARNING - void declaration
     */
    public int hasheq() {
        void v0;
        int hq__7465__auto__12353 = this.__hasheq;
        if ((long)hq__7465__auto__12353 == 0L) {
            void var2_2;
            int h__7466__auto__12352;
            this.__hasheq = h__7466__auto__12352 = RT.intCast((long)(0x3499F4EBL ^ (long)APersistentMap.mapHasheq((IPersistentMap)this)));
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
        int hash__7468__auto__12355 = this.__hash;
        if ((long)hash__7468__auto__12355 == 0L) {
            void var2_2;
            int h__7469__auto__12354;
            this.__hash = h__7469__auto__12354 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__12344) {
        Object object = G__12344;
        G__12344 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__12344) {
        IPersistentMap iPersistentMap = G__12344;
        G__12344 = null;
        return new PersistentFulltext(iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
    }

    public Object valAt(Object k__7474__auto__) {
        Object object = k__7474__auto__;
        k__7474__auto__ = null;
        return ((ILookup)this).valAt(object, null);
    }

    public Object valAt(Object k__7476__auto__, Object else__7477__auto__) {
        Object object = k__7476__auto__;
        k__7476__auto__ = null;
        Object object2 = else__7477__auto__;
        else__7477__auto__ = null;
        PersistentFulltext this_ = null;
        return RT.get((Object)this_.__extmap, (Object)object, (Object)object2);
    }

    public ILookupThunk getLookupThunk(Keyword k__7479__auto__) {
        ((IFn)const__17.getRawRoot()).invoke((Object)this);
        k__7479__auto__ = null;
        return null;
    }

    public int count() {
        return RT.intCast((long)Numbers.add((long)0L, (long)RT.count((Object)this.__extmap)));
    }

    public IPersistentCollection empty() {
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__19.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.fulltext_index.PersistentFulltext"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        PersistentFulltext persistentFulltext = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        PersistentFulltext this_ = null;
        return (IPersistentCollection)((IFn)const__18).invoke((Object)persistentFulltext, object);
    }

    public boolean equiv(Object G__12344) {
        Boolean bl;
        boolean or__5238__auto__12356 = Util.identical((Object)this, (Object)G__12344);
        if (or__5238__auto__12356) {
            bl = or__5238__auto__12356 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__17.getRawRoot()).invoke((Object)this), (Object)((IFn)const__17.getRawRoot()).invoke(G__12344))) {
            Object G__123442;
            Object object = G__12344;
            G__12344 = null;
            Object object2 = G__123442 = object;
            G__123442 = null;
            bl = Util.equiv((Object)this.__extmap, (Object)((PersistentFulltext)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = null;
        }
        return RT.booleanCast((Object)bl);
    }

    public boolean containsKey(Object k__7486__auto__) {
        Object object = k__7486__auto__;
        k__7486__auto__ = null;
        Boolean bl = Util.identical((Object)this_, (Object)((ILookup)this_).valAt(object, (Object)this_)) ? Boolean.TRUE : Boolean.FALSE;
        PersistentFulltext this_ = null;
        return (Boolean)((IFn)const__16.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__12357 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__12357)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__12357;
            v__7489__auto__12357 = null;
            PersistentFulltext this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        PersistentFulltext this_ = null;
        return (ISeq)((IFn)const__14.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke((Object)PersistentVector.EMPTY, this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)PersistentVector.EMPTY, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__12344) {
        const__12.getRawRoot();
        Object object = k__7493__auto__;
        k__7493__auto__ = null;
        Object object2 = G__12344;
        G__12344 = null;
        return new PersistentFulltext(this.__meta, ((IFn)const__13.getRawRoot()).invoke(this.__extmap, object, object2));
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            PersistentFulltext this_ = null;
            object = ((IFn)const__8.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new PersistentFulltext(this_.__meta, ((IFn)const__11.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
        }
        return (IPersistentMap)object;
    }

    public int size() {
        Counted counted = (Counted)this_;
        PersistentFulltext this_ = null;
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
        PersistentFulltext this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Collection values() {
        PersistentFulltext persistentFulltext = this_;
        PersistentFulltext this_ = null;
        return (Collection)((IFn)const__1.getRawRoot()).invoke((Object)persistentFulltext);
    }

    public Set entrySet() {
        PersistentFulltext persistentFulltext = this_;
        PersistentFulltext this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke((Object)persistentFulltext);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"set");
        const__1 = RT.var((String)"clojure.core", (String)"vals");
        const__2 = RT.var((String)"clojure.core", (String)"keys");
        const__4 = RT.var((String)"clojure.core", (String)"some");
        const__7 = RT.var((String)"clojure.core", (String)"contains?");
        const__8 = RT.var((String)"clojure.core", (String)"dissoc");
        const__9 = RT.var((String)"clojure.core", (String)"with-meta");
        const__10 = RT.var((String)"clojure.core", (String)"into");
        const__11 = RT.var((String)"clojure.core", (String)"not-empty");
        const__12 = RT.var((String)"clojure.core", (String)"identical?");
        const__13 = RT.var((String)"clojure.core", (String)"assoc");
        const__14 = RT.var((String)"clojure.core", (String)"seq");
        const__15 = RT.var((String)"clojure.core", (String)"concat");
        const__16 = RT.var((String)"clojure.core", (String)"not");
        const__17 = RT.var((String)"clojure.core", (String)"class");
        const__18 = RT.var((String)"clojure.core", (String)"imap-cons");
        const__19 = RT.var((String)"clojure.core", (String)"str");
        const__28 = RT.var((String)"datomic.lucene", (String)"index-reader");
        const__29 = RT.var((String)"datomic.lucene", (String)"persistent-directory");
    }
}

