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
package datomic.clusterfs;

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
import datomic.clusterfs.Chunk;
import datomic.clusterfs.ClusterFS$reify__14210;
import datomic.clusterfs.ClusterFS$reify__14212;
import datomic.impl.clusterfs.IClusterFS;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class ClusterFS
implements IClusterFS,
IRecord,
IHashEq,
IObj,
ILookup,
IKeywordLookup,
IPersistentMap,
Map,
Serializable {
    public final Object dir;
    public final int chunk_size;
    public final Object __meta;
    public final Object __extmap;
    int __hash;
    int __hasheq;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__8 = RT.keyword(null, (String)"dir");
    public static final Keyword const__9 = RT.keyword(null, (String)"chunk-size");
    public static final AFn const__10 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"dir"), RT.keyword(null, (String)"chunk-size")});
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"not-empty");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"identical?");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"assoc");
    public static final AFn const__17 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"dir"), (Object)RT.keyword(null, (String)"chunk-size"));
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"imap-cons");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"get-in");
    public static final Keyword const__33 = RT.keyword(null, (String)"length");
    public static final Keyword const__34 = RT.keyword(null, (String)"base");
    public static final Var const__35 = RT.var((String)"datomic.clusterfs", (String)"chunk-path");

    public ClusterFS(Object object, int n, Object object2, Object object3, int n2, int n3) {
        this.dir = object;
        this.chunk_size = n;
        this.__meta = object2;
        this.__extmap = object3;
        this.__hash = n2;
        this.__hasheq = n3;
    }

    public ClusterFS(Object object, int n) {
        this(object, n, null, null, 0, 0);
    }

    public ClusterFS(Object object, int n, Object object2, Object object3) {
        this(object, n, object2, object3, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"dir"), (Object)((IObj)Symbol.intern(null, (String)"chunk-size")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"int")})));
    }

    public static ClusterFS create(IPersistentMap iPersistentMap) {
        Object object = iPersistentMap.valAt((Object)Keyword.intern((String)"dir"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"dir"));
        Integer n = (Integer)iPersistentMap.valAt((Object)Keyword.intern((String)"chunk-size"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"chunk-size"));
        return new ClusterFS(object, n, null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    public byte[] getChunk(ILookup olookup, String filekey, int chunkno) {
        Object ck;
        Object temp__5455__auto__14216;
        Object prefix = ((IFn)const__32.getRawRoot()).invoke(this.dir, (Object)Tuple.create((Object)filekey, (Object)const__34));
        ILookup iLookup = olookup;
        olookup = null;
        Object object = temp__5455__auto__14216 = RT.get((Object)iLookup, (Object)((IFn)const__35.getRawRoot()).invoke(prefix, (Object)chunkno));
        if (object == null || object == Boolean.FALSE) {
            String string = filekey;
            filekey = null;
            Object object2 = prefix;
            prefix = null;
            throw (Throwable)new Error((String)((IFn)const__23.getRawRoot()).invoke((Object)"Unable to read ", (Object)string, (Object)" from ", ((IFn)const__35.getRawRoot()).invoke(object2, (Object)chunkno)));
        }
        Object object3 = temp__5455__auto__14216;
        temp__5455__auto__14216 = null;
        Object object4 = ck = object3;
        ck = null;
        return (byte[])((Chunk)object4).chunk;
    }

    public int chunkSize() {
        return this.chunk_size;
    }

    public long fileLength(String f) {
        String string = f;
        f = null;
        ClusterFS this_ = null;
        return ((Number)((IFn)const__32.getRawRoot()).invoke(this_.dir, (Object)Tuple.create((Object)string, (Object)const__33))).longValue();
    }

    public Collection getFiles() {
        ClusterFS this_ = null;
        return (Collection)((IFn)const__2.getRawRoot()).invoke(this_.dir);
    }

    /*
     * WARNING - void declaration
     */
    public int hasheq() {
        void v0;
        int hq__7465__auto__14218 = this.__hasheq;
        if ((long)hq__7465__auto__14218 == 0L) {
            void var2_2;
            int h__7466__auto__14217;
            this.__hasheq = h__7466__auto__14217 = RT.intCast((long)(0x374463FBL ^ (long)APersistentMap.mapHasheq((IPersistentMap)this)));
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
        int hash__7468__auto__14220 = this.__hash;
        if ((long)hash__7468__auto__14220 == 0L) {
            void var2_2;
            int h__7469__auto__14219;
            this.__hash = h__7469__auto__14219 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__14205) {
        Object object = G__14205;
        G__14205 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__14205) {
        IPersistentMap iPersistentMap = G__14205;
        G__14205 = null;
        return new ClusterFS(this.dir, this.chunk_size, iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
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
        Object G__14214 = k__7476__auto__;
        switch (Util.hash((Object)G__14214) >> 0 & 1) {
            case 0: {
                if (G__14214 != const__8) break;
                object = this_.dir;
                return object;
            }
            case 1: {
                if (G__14214 != const__9) break;
                object = this_.chunk_size;
                return object;
            }
        }
        Object object2 = k__7476__auto__;
        k__7476__auto__ = null;
        Object object3 = else__7477__auto__;
        else__7477__auto__ = null;
        ClusterFS this_ = null;
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
        Keyword G__14209 = keyword;
        switch (Util.hash((Object)G__14209) >> 0 & 1) {
            case 0: {
                if (G__14209 != const__8) break;
                gclass = null;
                object = new ClusterFS$reify__14210(null, gclass);
                return object;
            }
            case 1: {
                if (G__14209 != const__9) break;
                gclass = null;
                object = new ClusterFS$reify__14212(null, gclass);
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
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__23.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.clusterfs.ClusterFS"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        ClusterFS clusterFS = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        ClusterFS this_ = null;
        return (IPersistentCollection)((IFn)const__22).invoke((Object)clusterFS, object);
    }

    public boolean equiv(Object G__14205) {
        Boolean bl;
        boolean or__5238__auto__14223 = Util.identical((Object)this, (Object)G__14205);
        if (or__5238__auto__14223) {
            bl = or__5238__auto__14223 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__21.getRawRoot()).invoke((Object)this), (Object)((IFn)const__21.getRawRoot()).invoke(G__14205))) {
            Object object = G__14205;
            G__14205 = null;
            Object G__142052 = object;
            boolean and__5236__auto__14222 = Util.equiv((Object)this.dir, (Object)((ClusterFS)G__142052).dir);
            if (and__5236__auto__14222) {
                boolean and__5236__auto__14221 = Util.equiv((long)this.chunk_size, (long)((ClusterFS)G__142052).chunk_size);
                if (and__5236__auto__14221) {
                    Object object2 = G__142052;
                    G__142052 = null;
                    bl = Util.equiv((Object)this.__extmap, (Object)((ClusterFS)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    bl = and__5236__auto__14221 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__14222 ? Boolean.TRUE : Boolean.FALSE;
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
        ClusterFS this_ = null;
        return (Boolean)((IFn)const__20.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__14224 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__14224)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__14224;
            v__7489__auto__14224 = null;
            ClusterFS this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        ClusterFS this_ = null;
        return (ISeq)((IFn)const__18.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke((Object)Tuple.create((Object)MapEntry.create((Object)const__8, (Object)this_.dir), (Object)MapEntry.create((Object)const__9, (Object)this_.chunk_size)), this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)const__17, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__14205) {
        ClusterFS clusterFS;
        Object pred__14207 = const__15.getRawRoot();
        Object expr__14208 = k__7493__auto__;
        Object object = ((IFn)pred__14207).invoke((Object)const__8, expr__14208);
        if (object != null && object != Boolean.FALSE) {
            G__14205 = null;
            clusterFS = new ClusterFS(G__14205, this.chunk_size, this.__meta, this.__extmap);
        } else {
            Object object2 = pred__14207;
            pred__14207 = null;
            Object object3 = expr__14208;
            expr__14208 = null;
            Object object4 = ((IFn)object2).invoke((Object)const__9, object3);
            if (object4 != null && object4 != Boolean.FALSE) {
                G__14205 = null;
                clusterFS = new ClusterFS(this.dir, RT.intCast((Object)((Number)G__14205)), this.__meta, this.__extmap);
            } else {
                k__7493__auto__ = null;
                G__14205 = null;
                clusterFS = new ClusterFS(this.dir, this.chunk_size, this.__meta, ((IFn)const__16.getRawRoot()).invoke(this.__extmap, k__7493__auto__, G__14205));
            }
        }
        return clusterFS;
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)const__10, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__12.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            ClusterFS this_ = null;
            object = ((IFn)const__11.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new ClusterFS(this_.dir, this_.chunk_size, this_.__meta, ((IFn)const__14.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
        }
        return (IPersistentMap)object;
    }

    public int size() {
        Counted counted = (Counted)this_;
        ClusterFS this_ = null;
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
        ClusterFS this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Collection values() {
        ClusterFS clusterFS = this_;
        ClusterFS this_ = null;
        return (Collection)((IFn)const__1.getRawRoot()).invoke((Object)clusterFS);
    }

    public Set entrySet() {
        ClusterFS clusterFS = this_;
        ClusterFS this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke((Object)clusterFS);
    }
}

