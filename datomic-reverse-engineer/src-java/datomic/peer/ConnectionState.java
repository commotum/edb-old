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
package datomic.peer;

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
import datomic.common.AsyncShutdown;
import datomic.peer.ConnectionState$fn__21323;
import datomic.peer.ConnectionState$reify__21314;
import datomic.peer.ConnectionState$reify__21316;
import datomic.peer.ConnectionState$reify__21318;
import datomic.peer.ConnectionState$reify__21320;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class ConnectionState
implements AsyncShutdown,
IRecord,
IHashEq,
IObj,
ILookup,
IKeywordLookup,
IPersistentMap,
Map,
Serializable {
    public final Object connector;
    public final Object notifier;
    public final Object updater;
    public final Object cleanup;
    public final Object __meta;
    public final Object __extmap;
    int __hash;
    int __hasheq;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__8 = RT.keyword(null, (String)"connector");
    public static final Keyword const__9 = RT.keyword(null, (String)"notifier");
    public static final Keyword const__10 = RT.keyword(null, (String)"updater");
    public static final Keyword const__11 = RT.keyword(null, (String)"cleanup");
    public static final AFn const__12 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"connector"), RT.keyword(null, (String)"notifier"), RT.keyword(null, (String)"updater"), RT.keyword(null, (String)"cleanup")});
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"not-empty");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"identical?");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"assoc");
    public static final AFn const__19 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"connector"), (Object)RT.keyword(null, (String)"notifier"), (Object)RT.keyword(null, (String)"updater"), (Object)RT.keyword(null, (String)"cleanup"));
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"imap-cons");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__34 = RT.var((String)"clojure.core", (String)"future-call");

    public ConnectionState(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, int n, int n2) {
        this.connector = object;
        this.notifier = object2;
        this.updater = object3;
        this.cleanup = object4;
        this.__meta = object5;
        this.__extmap = object6;
        this.__hash = n;
        this.__hasheq = n2;
    }

    public ConnectionState(Object object, Object object2, Object object3, Object object4) {
        this(object, object2, object3, object4, null, null, 0, 0);
    }

    public ConnectionState(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this(object, object2, object3, object4, object5, object6, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"connector"), (Object)Symbol.intern(null, (String)"notifier"), (Object)Symbol.intern(null, (String)"updater"), (Object)Symbol.intern(null, (String)"cleanup"));
    }

    public static ConnectionState create(IPersistentMap iPersistentMap) {
        Object object = iPersistentMap.valAt((Object)Keyword.intern((String)"connector"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"connector"));
        Object object2 = iPersistentMap.valAt((Object)Keyword.intern((String)"notifier"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"notifier"));
        Object object3 = iPersistentMap.valAt((Object)Keyword.intern((String)"updater"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"updater"));
        Object object4 = iPersistentMap.valAt((Object)Keyword.intern((String)"cleanup"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"cleanup"));
        return new ConnectionState(object, object2, object3, object4, null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    public Object async_shutdown() {
        ConnectionState this_ = null;
        return ((IFn)const__34.getRawRoot()).invoke((Object)new ConnectionState$fn__21323(this_.cleanup));
    }

    /*
     * WARNING - void declaration
     */
    public int hasheq() {
        void v0;
        int hq__7465__auto__21327 = this.__hasheq;
        if ((long)hq__7465__auto__21327 == 0L) {
            void var2_2;
            int h__7466__auto__21326;
            this.__hasheq = h__7466__auto__21326 = RT.intCast((long)(0x160260BDL ^ (long)APersistentMap.mapHasheq((IPersistentMap)this)));
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
        int hash__7468__auto__21329 = this.__hash;
        if ((long)hash__7468__auto__21329 == 0L) {
            void var2_2;
            int h__7469__auto__21328;
            this.__hash = h__7469__auto__21328 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__21309) {
        Object object = G__21309;
        G__21309 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__21309) {
        IPersistentMap iPersistentMap = G__21309;
        G__21309 = null;
        return new ConnectionState(this.connector, this.notifier, this.updater, this.cleanup, iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
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
        Object G__21322 = k__7476__auto__;
        switch (Util.hash((Object)G__21322) >> 8 & 3) {
            case 0: {
                if (G__21322 != const__11) break;
                object = this_.cleanup;
                return object;
            }
            case 1: {
                if (G__21322 != const__9) break;
                object = this_.notifier;
                return object;
            }
            case 2: {
                if (G__21322 != const__10) break;
                object = this_.updater;
                return object;
            }
            case 3: {
                if (G__21322 != const__8) break;
                object = this_.connector;
                return object;
            }
        }
        Object object2 = k__7476__auto__;
        k__7476__auto__ = null;
        Object object3 = else__7477__auto__;
        else__7477__auto__ = null;
        ConnectionState this_ = null;
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
        Keyword G__21313 = keyword;
        switch (Util.hash((Object)G__21313) >> 8 & 3) {
            case 0: {
                if (G__21313 != const__11) break;
                gclass = null;
                object = new ConnectionState$reify__21314(null, gclass);
                return object;
            }
            case 1: {
                if (G__21313 != const__9) break;
                gclass = null;
                object = new ConnectionState$reify__21316(null, gclass);
                return object;
            }
            case 2: {
                if (G__21313 != const__10) break;
                gclass = null;
                object = new ConnectionState$reify__21318(null, gclass);
                return object;
            }
            case 3: {
                if (G__21313 != const__8) break;
                gclass = null;
                object = new ConnectionState$reify__21320(null, gclass);
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
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__25.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.peer.ConnectionState"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        ConnectionState connectionState = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        ConnectionState this_ = null;
        return (IPersistentCollection)((IFn)const__24).invoke((Object)connectionState, object);
    }

    public boolean equiv(Object G__21309) {
        Boolean bl;
        boolean or__5238__auto__21334 = Util.identical((Object)this, (Object)G__21309);
        if (or__5238__auto__21334) {
            bl = or__5238__auto__21334 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__23.getRawRoot()).invoke((Object)this), (Object)((IFn)const__23.getRawRoot()).invoke(G__21309))) {
            Object object = G__21309;
            G__21309 = null;
            Object G__213092 = object;
            boolean and__5236__auto__21333 = Util.equiv((Object)this.connector, (Object)((ConnectionState)G__213092).connector);
            if (and__5236__auto__21333) {
                boolean and__5236__auto__21332 = Util.equiv((Object)this.notifier, (Object)((ConnectionState)G__213092).notifier);
                if (and__5236__auto__21332) {
                    boolean and__5236__auto__21331 = Util.equiv((Object)this.updater, (Object)((ConnectionState)G__213092).updater);
                    if (and__5236__auto__21331) {
                        boolean and__5236__auto__21330 = Util.equiv((Object)this.cleanup, (Object)((ConnectionState)G__213092).cleanup);
                        if (and__5236__auto__21330) {
                            Object object2 = G__213092;
                            G__213092 = null;
                            bl = Util.equiv((Object)this.__extmap, (Object)((ConnectionState)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
                        } else {
                            bl = and__5236__auto__21330 ? Boolean.TRUE : Boolean.FALSE;
                        }
                    } else {
                        bl = and__5236__auto__21331 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    bl = and__5236__auto__21332 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__21333 ? Boolean.TRUE : Boolean.FALSE;
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
        ConnectionState this_ = null;
        return (Boolean)((IFn)const__22.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__21335 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__21335)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__21335;
            v__7489__auto__21335 = null;
            ConnectionState this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        ConnectionState this_ = null;
        return (ISeq)((IFn)const__20.getRawRoot()).invoke(((IFn)const__21.getRawRoot()).invoke((Object)Tuple.create((Object)MapEntry.create((Object)const__8, (Object)this_.connector), (Object)MapEntry.create((Object)const__9, (Object)this_.notifier), (Object)MapEntry.create((Object)const__10, (Object)this_.updater), (Object)MapEntry.create((Object)const__11, (Object)this_.cleanup)), this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)const__19, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__21309) {
        ConnectionState connectionState;
        Object pred__21311 = const__17.getRawRoot();
        Object expr__21312 = k__7493__auto__;
        Object object = ((IFn)pred__21311).invoke((Object)const__8, expr__21312);
        if (object != null && object != Boolean.FALSE) {
            G__21309 = null;
            connectionState = new ConnectionState(G__21309, this.notifier, this.updater, this.cleanup, this.__meta, this.__extmap);
        } else {
            Object object2 = ((IFn)pred__21311).invoke((Object)const__9, expr__21312);
            if (object2 != null && object2 != Boolean.FALSE) {
                G__21309 = null;
                connectionState = new ConnectionState(this.connector, G__21309, this.updater, this.cleanup, this.__meta, this.__extmap);
            } else {
                Object object3 = ((IFn)pred__21311).invoke((Object)const__10, expr__21312);
                if (object3 != null && object3 != Boolean.FALSE) {
                    G__21309 = null;
                    connectionState = new ConnectionState(this.connector, this.notifier, G__21309, this.cleanup, this.__meta, this.__extmap);
                } else {
                    Object object4 = pred__21311;
                    pred__21311 = null;
                    Object object5 = expr__21312;
                    expr__21312 = null;
                    Object object6 = ((IFn)object4).invoke((Object)const__11, object5);
                    if (object6 != null && object6 != Boolean.FALSE) {
                        G__21309 = null;
                        connectionState = new ConnectionState(this.connector, this.notifier, this.updater, G__21309, this.__meta, this.__extmap);
                    } else {
                        k__7493__auto__ = null;
                        G__21309 = null;
                        connectionState = new ConnectionState(this.connector, this.notifier, this.updater, this.cleanup, this.__meta, ((IFn)const__18.getRawRoot()).invoke(this.__extmap, k__7493__auto__, G__21309));
                    }
                }
            }
        }
        return connectionState;
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)const__12, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__14.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            ConnectionState this_ = null;
            object = ((IFn)const__13.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new ConnectionState(this_.connector, this_.notifier, this_.updater, this_.cleanup, this_.__meta, ((IFn)const__16.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
        }
        return (IPersistentMap)object;
    }

    public int size() {
        Counted counted = (Counted)this_;
        ConnectionState this_ = null;
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
        ConnectionState this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Collection values() {
        ConnectionState connectionState = this_;
        ConnectionState this_ = null;
        return (Collection)((IFn)const__1.getRawRoot()).invoke((Object)connectionState);
    }

    public Set entrySet() {
        ConnectionState connectionState = this_;
        ConnectionState this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke((Object)connectionState);
    }
}

