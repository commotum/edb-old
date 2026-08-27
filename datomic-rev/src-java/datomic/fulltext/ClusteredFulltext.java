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
package datomic.fulltext;

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
import datomic.fulltext.ClusteredFulltext$reify__14728;
import datomic.fulltext.ClusteredFulltext$reify__14730;
import datomic.fulltext.Root;
import datomic.fulltext_index.LuceneProvider;
import datomic.lucene.Readerable;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class ClusteredFulltext
implements LuceneProvider,
IRecord,
IHashEq,
IObj,
ILookup,
IKeywordLookup,
IPersistentMap,
Map,
Serializable {
    public final Object olookup;
    public final Object root;
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
    public static final Keyword const__8;
    public static final Keyword const__9;
    public static final AFn const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final AFn const__17;
    public static final Var const__18;
    public static final Var const__19;
    public static final Var const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Var const__32;
    public static final Var const__33;
    public static final Var const__34;

    public ClusteredFulltext(Object object, Object object2, Object object3, Object object4, int n, int n2) {
        this.olookup = object;
        this.root = object2;
        this.__meta = object3;
        this.__extmap = object4;
        this.__hash = n;
        this.__hasheq = n2;
    }

    public ClusteredFulltext(Object object, Object object2) {
        this(object, object2, null, null, 0, 0);
    }

    public ClusteredFulltext(Object object, Object object2, Object object3, Object object4) {
        this(object, object2, object3, object4, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"olookup"), (Object)((IObj)Symbol.intern(null, (String)"root")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Root")})));
    }

    public static ClusteredFulltext create(IPersistentMap iPersistentMap) {
        Object object = iPersistentMap.valAt((Object)Keyword.intern((String)"olookup"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"olookup"));
        Object object2 = iPersistentMap.valAt((Object)Keyword.intern((String)"root"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"root"));
        return new ClusteredFulltext(object, object2, null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object fulltext_attr_reader(Object attrid) {
        Object object;
        Object cfsid;
        Object temp__5457__auto__14734;
        Object object2 = attrid;
        attrid = null;
        Object object3 = temp__5457__auto__14734 = RT.get((Object)((Root)this_.root).attrmap, (Object)object2);
        if (object3 == null) return null;
        if (object3 == Boolean.FALSE) return null;
        Object object4 = temp__5457__auto__14734;
        temp__5457__auto__14734 = null;
        Object object5 = cfsid = object4;
        cfsid = null;
        Object object6 = ((IFn)const__33.getRawRoot()).invoke(((IFn)const__34.getRawRoot()).invoke(this_.olookup, object5), this_.olookup);
        if (Util.classOf((Object)object6) != __cached_class__0) {
            if (object6 instanceof Readerable) {
                object = ((Readerable)object6).index_reader();
                return object;
            }
            object6 = object6;
            __cached_class__0 = Util.classOf((Object)object6);
        }
        ClusteredFulltext this_ = null;
        object = const__32.getRawRoot().invoke(object6);
        return object;
    }

    /*
     * WARNING - void declaration
     */
    public int hasheq() {
        void v0;
        int hq__7465__auto__14736 = this.__hasheq;
        if ((long)hq__7465__auto__14736 == 0L) {
            void var2_2;
            int h__7466__auto__14735;
            this.__hasheq = h__7466__auto__14735 = RT.intCast((long)(0x59C5DFEL ^ (long)APersistentMap.mapHasheq((IPersistentMap)this)));
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
        int hash__7468__auto__14738 = this.__hash;
        if ((long)hash__7468__auto__14738 == 0L) {
            void var2_2;
            int h__7469__auto__14737;
            this.__hash = h__7469__auto__14737 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__14723) {
        Object object = G__14723;
        G__14723 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__14723) {
        IPersistentMap iPersistentMap = G__14723;
        G__14723 = null;
        return new ClusteredFulltext(this.olookup, this.root, iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
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
        Object G__14732 = k__7476__auto__;
        switch (Util.hash((Object)G__14732) >> 0 & 1) {
            case 0: {
                if (G__14732 != const__9) break;
                object = this_.root;
                return object;
            }
            case 1: {
                if (G__14732 != const__8) break;
                object = this_.olookup;
                return object;
            }
        }
        Object object2 = k__7476__auto__;
        k__7476__auto__ = null;
        Object object3 = else__7477__auto__;
        else__7477__auto__ = null;
        ClusteredFulltext this_ = null;
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
        Keyword G__14727 = keyword;
        switch (Util.hash((Object)G__14727) >> 0 & 1) {
            case 0: {
                if (G__14727 != const__9) break;
                gclass = null;
                object = new ClusteredFulltext$reify__14728(null, gclass);
                return object;
            }
            case 1: {
                if (G__14727 != const__8) break;
                gclass = null;
                object = new ClusteredFulltext$reify__14730(null, gclass);
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
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__23.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.fulltext.ClusteredFulltext"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        ClusteredFulltext clusteredFulltext = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        ClusteredFulltext this_ = null;
        return (IPersistentCollection)((IFn)const__22).invoke((Object)clusteredFulltext, object);
    }

    public boolean equiv(Object G__14723) {
        Boolean bl;
        boolean or__5238__auto__14741 = Util.identical((Object)this, (Object)G__14723);
        if (or__5238__auto__14741) {
            bl = or__5238__auto__14741 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__21.getRawRoot()).invoke((Object)this), (Object)((IFn)const__21.getRawRoot()).invoke(G__14723))) {
            Object object = G__14723;
            G__14723 = null;
            Object G__147232 = object;
            boolean and__5236__auto__14740 = Util.equiv((Object)this.olookup, (Object)((ClusteredFulltext)G__147232).olookup);
            if (and__5236__auto__14740) {
                boolean and__5236__auto__14739 = Util.equiv((Object)this.root, (Object)((ClusteredFulltext)G__147232).root);
                if (and__5236__auto__14739) {
                    Object object2 = G__147232;
                    G__147232 = null;
                    bl = Util.equiv((Object)this.__extmap, (Object)((ClusteredFulltext)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    bl = and__5236__auto__14739 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__14740 ? Boolean.TRUE : Boolean.FALSE;
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
        ClusteredFulltext this_ = null;
        return (Boolean)((IFn)const__20.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__14742 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__14742)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__14742;
            v__7489__auto__14742 = null;
            ClusteredFulltext this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        ClusteredFulltext this_ = null;
        return (ISeq)((IFn)const__18.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke((Object)Tuple.create((Object)MapEntry.create((Object)const__8, (Object)this_.olookup), (Object)MapEntry.create((Object)const__9, (Object)this_.root)), this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)const__17, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__14723) {
        ClusteredFulltext clusteredFulltext;
        Object pred__14725 = const__15.getRawRoot();
        Object expr__14726 = k__7493__auto__;
        Object object = ((IFn)pred__14725).invoke((Object)const__8, expr__14726);
        if (object != null && object != Boolean.FALSE) {
            G__14723 = null;
            clusteredFulltext = new ClusteredFulltext(G__14723, this.root, this.__meta, this.__extmap);
        } else {
            Object object2 = pred__14725;
            pred__14725 = null;
            Object object3 = expr__14726;
            expr__14726 = null;
            Object object4 = ((IFn)object2).invoke((Object)const__9, object3);
            if (object4 != null && object4 != Boolean.FALSE) {
                G__14723 = null;
                clusteredFulltext = new ClusteredFulltext(this.olookup, G__14723, this.__meta, this.__extmap);
            } else {
                k__7493__auto__ = null;
                G__14723 = null;
                clusteredFulltext = new ClusteredFulltext(this.olookup, this.root, this.__meta, ((IFn)const__16.getRawRoot()).invoke(this.__extmap, k__7493__auto__, G__14723));
            }
        }
        return clusteredFulltext;
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)const__10, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__12.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            ClusteredFulltext this_ = null;
            object = ((IFn)const__11.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new ClusteredFulltext(this_.olookup, this_.root, this_.__meta, ((IFn)const__14.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
        }
        return (IPersistentMap)object;
    }

    public int size() {
        Counted counted = (Counted)this_;
        ClusteredFulltext this_ = null;
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
        ClusteredFulltext this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Collection values() {
        ClusteredFulltext clusteredFulltext = this_;
        ClusteredFulltext this_ = null;
        return (Collection)((IFn)const__1.getRawRoot()).invoke((Object)clusteredFulltext);
    }

    public Set entrySet() {
        ClusteredFulltext clusteredFulltext = this_;
        ClusteredFulltext this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke((Object)clusteredFulltext);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"set");
        const__1 = RT.var((String)"clojure.core", (String)"vals");
        const__2 = RT.var((String)"clojure.core", (String)"keys");
        const__4 = RT.var((String)"clojure.core", (String)"some");
        const__7 = RT.var((String)"clojure.core", (String)"contains?");
        const__8 = RT.keyword(null, (String)"olookup");
        const__9 = RT.keyword(null, (String)"root");
        const__10 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"olookup"), RT.keyword(null, (String)"root")});
        const__11 = RT.var((String)"clojure.core", (String)"dissoc");
        const__12 = RT.var((String)"clojure.core", (String)"with-meta");
        const__13 = RT.var((String)"clojure.core", (String)"into");
        const__14 = RT.var((String)"clojure.core", (String)"not-empty");
        const__15 = RT.var((String)"clojure.core", (String)"identical?");
        const__16 = RT.var((String)"clojure.core", (String)"assoc");
        const__17 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"olookup"), (Object)RT.keyword(null, (String)"root"));
        const__18 = RT.var((String)"clojure.core", (String)"seq");
        const__19 = RT.var((String)"clojure.core", (String)"concat");
        const__20 = RT.var((String)"clojure.core", (String)"not");
        const__21 = RT.var((String)"clojure.core", (String)"class");
        const__22 = RT.var((String)"clojure.core", (String)"imap-cons");
        const__23 = RT.var((String)"clojure.core", (String)"str");
        const__32 = RT.var((String)"datomic.lucene", (String)"index-reader");
        const__33 = RT.var((String)"datomic.fulltext", (String)"cluster-directory");
        const__34 = RT.var((String)"datomic.common", (String)"getx");
    }
}

