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
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute$reify__12552;
import datomic.db.Attribute$reify__12554;
import datomic.db.Attribute$reify__12556;
import datomic.db.Attribute$reify__12558;
import datomic.db.Attribute$reify__12560;
import datomic.db.Attribute$reify__12562;
import datomic.db.Attribute$reify__12564;
import datomic.db.Attribute$reify__12566;
import datomic.db.Attribute$reify__12568;
import datomic.db.Attribute$reify__12570;
import datomic.db.Attribute$reify__12572;
import datomic.db.IAttributeImpl;
import datomic.db.IElementImpl;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class Attribute
implements IAttributeImpl,
IElementImpl,
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
    public final Object vtypeid;
    public final Object cardinality;
    public final Object isComponent;
    public final Object unique;
    public final Object index;
    public final Object storageHasAVET;
    public final Object needsAVET;
    public final Object noHistory;
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
    public static final Keyword const__8 = RT.keyword(null, (String)"unique");
    public static final Keyword const__9 = RT.keyword(null, (String)"vtypeid");
    public static final Keyword const__10 = RT.keyword(null, (String)"storageHasAVET");
    public static final Keyword const__11 = RT.keyword(null, (String)"index");
    public static final Keyword const__12 = RT.keyword(null, (String)"fulltext");
    public static final Keyword const__13 = RT.keyword(null, (String)"noHistory");
    public static final Keyword const__14 = RT.keyword(null, (String)"isComponent");
    public static final Keyword const__15 = RT.keyword(null, (String)"kw");
    public static final Keyword const__16 = RT.keyword(null, (String)"needsAVET");
    public static final Keyword const__17 = RT.keyword(null, (String)"id");
    public static final Keyword const__18 = RT.keyword(null, (String)"cardinality");
    public static final AFn const__19 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"unique"), RT.keyword(null, (String)"vtypeid"), RT.keyword(null, (String)"storageHasAVET"), RT.keyword(null, (String)"index"), RT.keyword(null, (String)"fulltext"), RT.keyword(null, (String)"noHistory"), RT.keyword(null, (String)"isComponent"), RT.keyword(null, (String)"kw"), RT.keyword(null, (String)"needsAVET"), RT.keyword(null, (String)"id"), RT.keyword(null, (String)"cardinality")});
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"not-empty");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"identical?");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"assoc");
    public static final AFn const__26 = (AFn)RT.vector((Object[])new Object[]{RT.keyword(null, (String)"id"), RT.keyword(null, (String)"kw"), RT.keyword(null, (String)"vtypeid"), RT.keyword(null, (String)"cardinality"), RT.keyword(null, (String)"isComponent"), RT.keyword(null, (String)"unique"), RT.keyword(null, (String)"index"), RT.keyword(null, (String)"storageHasAVET"), RT.keyword(null, (String)"needsAVET"), RT.keyword(null, (String)"noHistory"), RT.keyword(null, (String)"fulltext")});
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"imap-cons");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"str");

    public Attribute(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, int n, int n2) {
        this.id = object;
        this.kw = object2;
        this.vtypeid = object3;
        this.cardinality = object4;
        this.isComponent = object5;
        this.unique = object6;
        this.index = object7;
        this.storageHasAVET = object8;
        this.needsAVET = object9;
        this.noHistory = object10;
        this.fulltext = object11;
        this.__meta = object12;
        this.__extmap = object13;
        this.__hash = n;
        this.__hasheq = n2;
    }

    public Attribute(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11) {
        this(object, object2, object3, object4, object5, object6, object7, object8, object9, object10, object11, null, null, 0, 0);
    }

    public Attribute(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13) {
        this(object, object2, object3, object4, object5, object6, object7, object8, object9, object10, object11, object12, object13, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"id"), Symbol.intern(null, (String)"kw"), Symbol.intern(null, (String)"vtypeid"), Symbol.intern(null, (String)"cardinality"), Symbol.intern(null, (String)"isComponent"), Symbol.intern(null, (String)"unique"), Symbol.intern(null, (String)"index"), Symbol.intern(null, (String)"storageHasAVET"), Symbol.intern(null, (String)"needsAVET"), Symbol.intern(null, (String)"noHistory"), Symbol.intern(null, (String)"fulltext")});
    }

    public static Attribute create(IPersistentMap iPersistentMap) {
        Object object = iPersistentMap.valAt((Object)Keyword.intern((String)"id"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"id"));
        Object object2 = iPersistentMap.valAt((Object)Keyword.intern((String)"kw"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"kw"));
        Object object3 = iPersistentMap.valAt((Object)Keyword.intern((String)"vtypeid"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"vtypeid"));
        Object object4 = iPersistentMap.valAt((Object)Keyword.intern((String)"cardinality"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"cardinality"));
        Object object5 = iPersistentMap.valAt((Object)Keyword.intern((String)"isComponent"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"isComponent"));
        Object object6 = iPersistentMap.valAt((Object)Keyword.intern((String)"unique"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"unique"));
        Object object7 = iPersistentMap.valAt((Object)Keyword.intern((String)"index"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"index"));
        Object object8 = iPersistentMap.valAt((Object)Keyword.intern((String)"storageHasAVET"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"storageHasAVET"));
        Object object9 = iPersistentMap.valAt((Object)Keyword.intern((String)"needsAVET"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"needsAVET"));
        Object object10 = iPersistentMap.valAt((Object)Keyword.intern((String)"noHistory"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"noHistory"));
        Object object11 = iPersistentMap.valAt((Object)Keyword.intern((String)"fulltext"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"fulltext"));
        return new Attribute(object, object2, object3, object4, object5, object6, object7, object8, object9, object10, object11, null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    public Object hasAVET() {
        Object object;
        Object and__5236__auto__12576;
        Object object2 = and__5236__auto__12576 = this.storageHasAVET;
        if (object2 != null && object2 != Boolean.FALSE) {
            object = this.needsAVET;
        } else {
            object = and__5236__auto__12576;
            Object var1_1 = null;
        }
        return object;
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
        int hq__7465__auto__12578 = this.__hasheq;
        if ((long)hq__7465__auto__12578 == 0L) {
            void var2_2;
            int h__7466__auto__12577;
            this.__hasheq = h__7466__auto__12577 = (int)(0x3149D926L ^ (long)APersistentMap.mapHasheq((IPersistentMap)this));
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
        int hash__7468__auto__12580 = this.__hash;
        if ((long)hash__7468__auto__12580 == 0L) {
            void var2_2;
            int h__7469__auto__12579;
            this.__hash = h__7469__auto__12579 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__12547) {
        Object object = G__12547;
        G__12547 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__12547) {
        IPersistentMap iPersistentMap = G__12547;
        G__12547 = null;
        return new Attribute(this.id, this.kw, this.vtypeid, this.cardinality, this.isComponent, this.unique, this.index, this.storageHasAVET, this.needsAVET, this.noHistory, this.fulltext, iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
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
        Object G__12574 = k__7476__auto__;
        switch (Util.hash((Object)G__12574) >> 2 & 0xF) {
            case 0: {
                if (G__12574 != const__10) break;
                object = this_.storageHasAVET;
                return object;
            }
            case 1: {
                if (G__12574 != const__13) break;
                object = this_.noHistory;
                return object;
            }
            case 2: {
                if (G__12574 != const__18) break;
                object = this_.cardinality;
                return object;
            }
            case 6: {
                if (G__12574 != const__9) break;
                object = this_.vtypeid;
                return object;
            }
            case 7: {
                if (G__12574 != const__17) break;
                object = this_.id;
                return object;
            }
            case 8: {
                if (G__12574 != const__16) break;
                object = this_.needsAVET;
                return object;
            }
            case 11: {
                if (G__12574 != const__12) break;
                object = this_.fulltext;
                return object;
            }
            case 12: {
                if (G__12574 != const__15) break;
                object = this_.kw;
                return object;
            }
            case 13: {
                if (G__12574 != const__8) break;
                object = this_.unique;
                return object;
            }
            case 14: {
                if (G__12574 != const__11) break;
                object = this_.index;
                return object;
            }
            case 15: {
                if (G__12574 != const__14) break;
                object = this_.isComponent;
                return object;
            }
        }
        Object object2 = k__7476__auto__;
        k__7476__auto__ = null;
        Object object3 = else__7477__auto__;
        else__7477__auto__ = null;
        Attribute this_ = null;
        object = RT.get((Object)this_.__extmap, (Object)object2, (Object)object3);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public ILookupThunk getLookupThunk(Keyword k__7479__auto__) {
        Object object;
        Object gclass = ((IFn)const__30.getRawRoot()).invoke((Object)this);
        Keyword keyword = k__7479__auto__;
        k__7479__auto__ = null;
        Keyword G__12551 = keyword;
        switch (Util.hash((Object)G__12551) >> 2 & 0xF) {
            case 0: {
                if (G__12551 != const__10) break;
                gclass = null;
                object = new Attribute$reify__12552(null, gclass);
                return object;
            }
            case 1: {
                if (G__12551 != const__13) break;
                gclass = null;
                object = new Attribute$reify__12554(null, gclass);
                return object;
            }
            case 2: {
                if (G__12551 != const__18) break;
                gclass = null;
                object = new Attribute$reify__12556(null, gclass);
                return object;
            }
            case 6: {
                if (G__12551 != const__9) break;
                gclass = null;
                object = new Attribute$reify__12558(null, gclass);
                return object;
            }
            case 7: {
                if (G__12551 != const__17) break;
                gclass = null;
                object = new Attribute$reify__12560(null, gclass);
                return object;
            }
            case 8: {
                if (G__12551 != const__16) break;
                gclass = null;
                object = new Attribute$reify__12562(null, gclass);
                return object;
            }
            case 11: {
                if (G__12551 != const__12) break;
                gclass = null;
                object = new Attribute$reify__12564(null, gclass);
                return object;
            }
            case 12: {
                if (G__12551 != const__15) break;
                gclass = null;
                object = new Attribute$reify__12566(null, gclass);
                return object;
            }
            case 13: {
                if (G__12551 != const__8) break;
                gclass = null;
                object = new Attribute$reify__12568(null, gclass);
                return object;
            }
            case 14: {
                if (G__12551 != const__11) break;
                gclass = null;
                object = new Attribute$reify__12570(null, gclass);
                return object;
            }
            case 15: {
                if (G__12551 != const__14) break;
                gclass = null;
                object = new Attribute$reify__12572(null, gclass);
                return object;
            }
        }
        object = null;
        return object;
    }

    public int count() {
        return RT.intCast((long)(11L + (long)RT.count((Object)this.__extmap)));
    }

    public IPersistentCollection empty() {
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__32.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.db.Attribute"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        Attribute attribute2 = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        Attribute this_ = null;
        return (IPersistentCollection)((IFn)const__31).invoke((Object)attribute2, object);
    }

    public boolean equiv(Object G__12547) {
        Boolean bl;
        boolean or__5238__auto__12592 = Util.identical((Object)this, (Object)G__12547);
        if (or__5238__auto__12592) {
            bl = or__5238__auto__12592 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__30.getRawRoot()).invoke((Object)this), (Object)((IFn)const__30.getRawRoot()).invoke(G__12547))) {
            Object object = G__12547;
            G__12547 = null;
            Object G__125472 = object;
            boolean and__5236__auto__12591 = Util.equiv((Object)this.id, (Object)((Attribute)G__125472).id);
            if (and__5236__auto__12591) {
                boolean and__5236__auto__12590 = Util.equiv((Object)this.kw, (Object)((Attribute)G__125472).kw);
                if (and__5236__auto__12590) {
                    boolean and__5236__auto__12589 = Util.equiv((Object)this.vtypeid, (Object)((Attribute)G__125472).vtypeid);
                    if (and__5236__auto__12589) {
                        boolean and__5236__auto__12588 = Util.equiv((Object)this.cardinality, (Object)((Attribute)G__125472).cardinality);
                        if (and__5236__auto__12588) {
                            boolean and__5236__auto__12587 = Util.equiv((Object)this.isComponent, (Object)((Attribute)G__125472).isComponent);
                            if (and__5236__auto__12587) {
                                boolean and__5236__auto__12586 = Util.equiv((Object)this.unique, (Object)((Attribute)G__125472).unique);
                                if (and__5236__auto__12586) {
                                    boolean and__5236__auto__12585 = Util.equiv((Object)this.index, (Object)((Attribute)G__125472).index);
                                    if (and__5236__auto__12585) {
                                        boolean and__5236__auto__12584 = Util.equiv((Object)this.storageHasAVET, (Object)((Attribute)G__125472).storageHasAVET);
                                        if (and__5236__auto__12584) {
                                            boolean and__5236__auto__12583 = Util.equiv((Object)this.needsAVET, (Object)((Attribute)G__125472).needsAVET);
                                            if (and__5236__auto__12583) {
                                                boolean and__5236__auto__12582 = Util.equiv((Object)this.noHistory, (Object)((Attribute)G__125472).noHistory);
                                                if (and__5236__auto__12582) {
                                                    boolean and__5236__auto__12581 = Util.equiv((Object)this.fulltext, (Object)((Attribute)G__125472).fulltext);
                                                    if (and__5236__auto__12581) {
                                                        Object object2 = G__125472;
                                                        G__125472 = null;
                                                        bl = Util.equiv((Object)this.__extmap, (Object)((Attribute)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
                                                    } else {
                                                        bl = and__5236__auto__12581 ? Boolean.TRUE : Boolean.FALSE;
                                                    }
                                                } else {
                                                    bl = and__5236__auto__12582 ? Boolean.TRUE : Boolean.FALSE;
                                                }
                                            } else {
                                                bl = and__5236__auto__12583 ? Boolean.TRUE : Boolean.FALSE;
                                            }
                                        } else {
                                            bl = and__5236__auto__12584 ? Boolean.TRUE : Boolean.FALSE;
                                        }
                                    } else {
                                        bl = and__5236__auto__12585 ? Boolean.TRUE : Boolean.FALSE;
                                    }
                                } else {
                                    bl = and__5236__auto__12586 ? Boolean.TRUE : Boolean.FALSE;
                                }
                            } else {
                                bl = and__5236__auto__12587 ? Boolean.TRUE : Boolean.FALSE;
                            }
                        } else {
                            bl = and__5236__auto__12588 ? Boolean.TRUE : Boolean.FALSE;
                        }
                    } else {
                        bl = and__5236__auto__12589 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    bl = and__5236__auto__12590 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__12591 ? Boolean.TRUE : Boolean.FALSE;
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
        Attribute this_ = null;
        return (Boolean)((IFn)const__29.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__12593 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__12593)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__12593;
            v__7489__auto__12593 = null;
            Attribute this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        Attribute this_ = null;
        return (ISeq)((IFn)const__27.getRawRoot()).invoke(((IFn)const__28.getRawRoot()).invoke((Object)RT.vector((Object[])new Object[]{MapEntry.create((Object)const__17, (Object)this_.id), MapEntry.create((Object)const__15, (Object)this_.kw), MapEntry.create((Object)const__9, (Object)this_.vtypeid), MapEntry.create((Object)const__18, (Object)this_.cardinality), MapEntry.create((Object)const__14, (Object)this_.isComponent), MapEntry.create((Object)const__8, (Object)this_.unique), MapEntry.create((Object)const__11, (Object)this_.index), MapEntry.create((Object)const__10, (Object)this_.storageHasAVET), MapEntry.create((Object)const__16, (Object)this_.needsAVET), MapEntry.create((Object)const__13, (Object)this_.noHistory), MapEntry.create((Object)const__12, (Object)this_.fulltext)}), this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)const__26, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__12547) {
        Attribute attribute2;
        Object pred__12549 = const__24.getRawRoot();
        Object expr__12550 = k__7493__auto__;
        Object object = ((IFn)pred__12549).invoke((Object)const__17, expr__12550);
        if (object != null && object != Boolean.FALSE) {
            G__12547 = null;
            attribute2 = new Attribute(G__12547, this.kw, this.vtypeid, this.cardinality, this.isComponent, this.unique, this.index, this.storageHasAVET, this.needsAVET, this.noHistory, this.fulltext, this.__meta, this.__extmap);
        } else {
            Object object2 = ((IFn)pred__12549).invoke((Object)const__15, expr__12550);
            if (object2 != null && object2 != Boolean.FALSE) {
                G__12547 = null;
                attribute2 = new Attribute(this.id, G__12547, this.vtypeid, this.cardinality, this.isComponent, this.unique, this.index, this.storageHasAVET, this.needsAVET, this.noHistory, this.fulltext, this.__meta, this.__extmap);
            } else {
                Object object3 = ((IFn)pred__12549).invoke((Object)const__9, expr__12550);
                if (object3 != null && object3 != Boolean.FALSE) {
                    G__12547 = null;
                    attribute2 = new Attribute(this.id, this.kw, G__12547, this.cardinality, this.isComponent, this.unique, this.index, this.storageHasAVET, this.needsAVET, this.noHistory, this.fulltext, this.__meta, this.__extmap);
                } else {
                    Object object4 = ((IFn)pred__12549).invoke((Object)const__18, expr__12550);
                    if (object4 != null && object4 != Boolean.FALSE) {
                        G__12547 = null;
                        attribute2 = new Attribute(this.id, this.kw, this.vtypeid, G__12547, this.isComponent, this.unique, this.index, this.storageHasAVET, this.needsAVET, this.noHistory, this.fulltext, this.__meta, this.__extmap);
                    } else {
                        Object object5 = ((IFn)pred__12549).invoke((Object)const__14, expr__12550);
                        if (object5 != null && object5 != Boolean.FALSE) {
                            G__12547 = null;
                            attribute2 = new Attribute(this.id, this.kw, this.vtypeid, this.cardinality, G__12547, this.unique, this.index, this.storageHasAVET, this.needsAVET, this.noHistory, this.fulltext, this.__meta, this.__extmap);
                        } else {
                            Object object6 = ((IFn)pred__12549).invoke((Object)const__8, expr__12550);
                            if (object6 != null && object6 != Boolean.FALSE) {
                                G__12547 = null;
                                attribute2 = new Attribute(this.id, this.kw, this.vtypeid, this.cardinality, this.isComponent, G__12547, this.index, this.storageHasAVET, this.needsAVET, this.noHistory, this.fulltext, this.__meta, this.__extmap);
                            } else {
                                Object object7 = ((IFn)pred__12549).invoke((Object)const__11, expr__12550);
                                if (object7 != null && object7 != Boolean.FALSE) {
                                    G__12547 = null;
                                    attribute2 = new Attribute(this.id, this.kw, this.vtypeid, this.cardinality, this.isComponent, this.unique, G__12547, this.storageHasAVET, this.needsAVET, this.noHistory, this.fulltext, this.__meta, this.__extmap);
                                } else {
                                    Object object8 = ((IFn)pred__12549).invoke((Object)const__10, expr__12550);
                                    if (object8 != null && object8 != Boolean.FALSE) {
                                        G__12547 = null;
                                        attribute2 = new Attribute(this.id, this.kw, this.vtypeid, this.cardinality, this.isComponent, this.unique, this.index, G__12547, this.needsAVET, this.noHistory, this.fulltext, this.__meta, this.__extmap);
                                    } else {
                                        Object object9 = ((IFn)pred__12549).invoke((Object)const__16, expr__12550);
                                        if (object9 != null && object9 != Boolean.FALSE) {
                                            G__12547 = null;
                                            attribute2 = new Attribute(this.id, this.kw, this.vtypeid, this.cardinality, this.isComponent, this.unique, this.index, this.storageHasAVET, G__12547, this.noHistory, this.fulltext, this.__meta, this.__extmap);
                                        } else {
                                            Object object10 = ((IFn)pred__12549).invoke((Object)const__13, expr__12550);
                                            if (object10 != null && object10 != Boolean.FALSE) {
                                                G__12547 = null;
                                                attribute2 = new Attribute(this.id, this.kw, this.vtypeid, this.cardinality, this.isComponent, this.unique, this.index, this.storageHasAVET, this.needsAVET, G__12547, this.fulltext, this.__meta, this.__extmap);
                                            } else {
                                                Object object11 = pred__12549;
                                                pred__12549 = null;
                                                Object object12 = expr__12550;
                                                expr__12550 = null;
                                                Object object13 = ((IFn)object11).invoke((Object)const__12, object12);
                                                if (object13 != null && object13 != Boolean.FALSE) {
                                                    G__12547 = null;
                                                    attribute2 = new Attribute(this.id, this.kw, this.vtypeid, this.cardinality, this.isComponent, this.unique, this.index, this.storageHasAVET, this.needsAVET, this.noHistory, G__12547, this.__meta, this.__extmap);
                                                } else {
                                                    k__7493__auto__ = null;
                                                    G__12547 = null;
                                                    attribute2 = new Attribute(this.id, this.kw, this.vtypeid, this.cardinality, this.isComponent, this.unique, this.index, this.storageHasAVET, this.needsAVET, this.noHistory, this.fulltext, this.__meta, ((IFn)const__25.getRawRoot()).invoke(this.__extmap, k__7493__auto__, G__12547));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return attribute2;
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)const__19, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__21.getRawRoot()).invoke(((IFn)const__22.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            Attribute this_ = null;
            object = ((IFn)const__20.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new Attribute(this_.id, this_.kw, this_.vtypeid, this_.cardinality, this_.isComponent, this_.unique, this_.index, this_.storageHasAVET, this_.needsAVET, this_.noHistory, this_.fulltext, this_.__meta, ((IFn)const__23.getRawRoot()).invoke(((IFn)const__20.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
        }
        return (IPersistentMap)object;
    }

    public int size() {
        Counted counted = (Counted)this_;
        Attribute this_ = null;
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
        Attribute this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Collection values() {
        Attribute attribute2 = this_;
        Attribute this_ = null;
        return (Collection)((IFn)const__1.getRawRoot()).invoke((Object)attribute2);
    }

    public Set entrySet() {
        Attribute attribute2 = this_;
        Attribute this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke((Object)attribute2);
    }
}

