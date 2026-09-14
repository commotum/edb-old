/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.APersistentMap
 *  clojure.lang.Counted
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.IFn$OLO
 *  clojure.lang.IFn$OOL
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
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.MapEntry
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentHashMap
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
import clojure.lang.KeywordLookupSite;
import clojure.lang.MapEntry;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.RecordIterator;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Attribute;
import datomic.Database;
import datomic.Entity;
import datomic.db.Db$fn__13466;
import datomic.db.Db$fn__13468;
import datomic.db.Db$fn__13470;
import datomic.db.Db$fn__13475;
import datomic.db.Db$reify__13425;
import datomic.db.Db$reify__13427;
import datomic.db.Db$reify__13429;
import datomic.db.Db$reify__13431;
import datomic.db.Db$reify__13433;
import datomic.db.Db$reify__13435;
import datomic.db.Db$reify__13437;
import datomic.db.Db$reify__13439;
import datomic.db.Db$reify__13441;
import datomic.db.Db$reify__13443;
import datomic.db.Db$reify__13445;
import datomic.db.Db$reify__13447;
import datomic.db.Db$reify__13449;
import datomic.db.Db$reify__13451;
import datomic.db.Db$reify__13453;
import datomic.db.Db$reify__13455;
import datomic.db.Db$reify__13457;
import datomic.db.Db$reify__13459;
import datomic.db.Db$reify__13461;
import datomic.db.Db$reify__13463;
import datomic.db.Function;
import datomic.db.IDatumImpl;
import datomic.db.IDb;
import datomic.db.IDbImpl;
import datomic.db.IElementImpl;
import datomic.db.IndexSet;
import datomic.impl.Circular;
import datomic.impl.db.IDatum;
import datomic.iter.Iter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public final class Db
implements IDb,
IDbImpl,
Database,
IRecord,
IHashEq,
IObj,
ILookup,
IKeywordLookup,
IPersistentMap,
Map,
Serializable {
    public final Object id;
    public final Object memidx;
    public final Object indexing;
    public final Object mid_index;
    public final Object index;
    public final Object history;
    public final Object memlog;
    public final long basisT;
    public final long nextT;
    public final long indexBasisT;
    public final Object indexingNextT;
    public final Object elements;
    public final Object keys;
    public final Object ids;
    public final Object index_root_id;
    public final Object index_rev;
    public final Object asOfT;
    public final Object sinceT;
    public final Object raw;
    public final Object filt;
    public final Object __meta;
    public final Object __extmap;
    int __hash;
    int __hasheq;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__8 = RT.keyword(null, (String)"indexBasisT");
    public static final Keyword const__9 = RT.keyword(null, (String)"mid-index");
    public static final Keyword const__10 = RT.keyword(null, (String)"nextT");
    public static final Keyword const__11 = RT.keyword(null, (String)"index");
    public static final Keyword const__12 = RT.keyword(null, (String)"raw");
    public static final Keyword const__13 = RT.keyword(null, (String)"basisT");
    public static final Keyword const__14 = RT.keyword(null, (String)"history");
    public static final Keyword const__15 = RT.keyword(null, (String)"ids");
    public static final Keyword const__16 = RT.keyword(null, (String)"indexing");
    public static final Keyword const__17 = RT.keyword(null, (String)"elements");
    public static final Keyword const__18 = RT.keyword(null, (String)"index-rev");
    public static final Keyword const__19 = RT.keyword(null, (String)"memidx");
    public static final Keyword const__20 = RT.keyword(null, (String)"keys");
    public static final Keyword const__21 = RT.keyword(null, (String)"memlog");
    public static final Keyword const__22 = RT.keyword(null, (String)"id");
    public static final Keyword const__23 = RT.keyword(null, (String)"indexingNextT");
    public static final Keyword const__24 = RT.keyword(null, (String)"sinceT");
    public static final Keyword const__25 = RT.keyword(null, (String)"index-root-id");
    public static final Keyword const__26 = RT.keyword(null, (String)"filt");
    public static final Keyword const__27 = RT.keyword(null, (String)"asOfT");
    public static final AFn const__28 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"indexBasisT"), RT.keyword(null, (String)"mid-index"), RT.keyword(null, (String)"nextT"), RT.keyword(null, (String)"index"), RT.keyword(null, (String)"raw"), RT.keyword(null, (String)"basisT"), RT.keyword(null, (String)"history"), RT.keyword(null, (String)"ids"), RT.keyword(null, (String)"indexing"), RT.keyword(null, (String)"elements"), RT.keyword(null, (String)"index-rev"), RT.keyword(null, (String)"memidx"), RT.keyword(null, (String)"keys"), RT.keyword(null, (String)"memlog"), RT.keyword(null, (String)"id"), RT.keyword(null, (String)"indexingNextT"), RT.keyword(null, (String)"sinceT"), RT.keyword(null, (String)"index-root-id"), RT.keyword(null, (String)"filt"), RT.keyword(null, (String)"asOfT")});
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"not-empty");
    public static final Var const__33 = RT.var((String)"clojure.core", (String)"identical?");
    public static final Var const__34 = RT.var((String)"clojure.core", (String)"assoc");
    public static final AFn const__35 = (AFn)RT.vector((Object[])new Object[]{RT.keyword(null, (String)"id"), RT.keyword(null, (String)"memidx"), RT.keyword(null, (String)"indexing"), RT.keyword(null, (String)"mid-index"), RT.keyword(null, (String)"index"), RT.keyword(null, (String)"history"), RT.keyword(null, (String)"memlog"), RT.keyword(null, (String)"basisT"), RT.keyword(null, (String)"nextT"), RT.keyword(null, (String)"indexBasisT"), RT.keyword(null, (String)"indexingNextT"), RT.keyword(null, (String)"elements"), RT.keyword(null, (String)"keys"), RT.keyword(null, (String)"ids"), RT.keyword(null, (String)"index-root-id"), RT.keyword(null, (String)"index-rev"), RT.keyword(null, (String)"asOfT"), RT.keyword(null, (String)"sinceT"), RT.keyword(null, (String)"raw"), RT.keyword(null, (String)"filt")});
    public static final Var const__36 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__37 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__38 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__39 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__40 = RT.var((String)"clojure.core", (String)"imap-cons");
    public static final Var const__41 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__50 = RT.var((String)"datomic.db", (String)"as-of-t");
    public static final Var const__51 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__52 = RT.var((String)"datomic.db", (String)"attr-info");
    public static final Var const__53 = RT.var((String)"datomic.db", (String)"resolve-kw");
    public static final Var const__54 = RT.var((String)"datomic.db", (String)"entid-at");
    public static final Var const__55 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__56 = RT.var((String)"datomic.db", (String)"invoke");
    public static final Var const__57 = RT.var((String)"datomic.db", (String)"with-tx+opts");
    public static final Var const__58 = RT.var((String)"datomic.db", (String)"datoms");
    public static final Var const__59 = RT.var((String)"datomic.db", (String)"seek-datoms");
    public static final Var const__60 = RT.var((String)"datomic.iter", (String)"iterable");
    public static final Var const__63 = RT.var((String)"datomic.db", (String)"to-kw");
    public static final Var const__64 = RT.var((String)"datomic.iter", (String)"merge-iters");
    public static final Var const__65 = RT.var((String)"datomic.db", (String)"eavt-cmp");
    public static final Var const__66 = RT.var((String)"datomic.btset", (String)"seek");
    public static final Var const__68 = RT.var((String)"datomic.db", (String)"avet-cmp");
    public static final Var const__69 = RT.var((String)"datomic.db", (String)"aevt-cmp");
    public static final Var const__70 = RT.var((String)"datomic.db", (String)"raet-cmp");
    public static final Var const__71 = RT.var((String)"datomic.db", (String)"user-proc?");
    public static final Var const__72 = RT.var((String)"datomic.db", (String)"require-id");
    public static final Keyword const__74 = RT.keyword((String)"db", (String)"query");
    public static final Keyword const__75 = RT.keyword((String)"db", (String)"fn");
    public static final Var const__77 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__78 = RT.keyword((String)"db.error", (String)"not-a-data-function");
    public static final Var const__81 = RT.var((String)"datomic.db", (String)"require-kw");
    public static final Var const__82 = RT.var((String)"datomic.db", (String)"reverse-key?");
    public static final Var const__83 = RT.var((String)"clojure.core", (String)"update");
    public static final Keyword const__84 = RT.keyword(null, (String)"_keys");
    public static final Var const__85 = RT.var((String)"clojure.core", (String)"fnil");
    public static final Var const__86 = RT.var((String)"clojure.core", (String)"conj");
    public static final Object const__88 = 0x100000L;
    public static final Var const__89 = RT.var((String)"datomic.db", (String)"growvec");
    public static final Keyword const__90 = RT.keyword((String)"db.error", (String)"schema-count-exceeded");
    public static final Var const__91 = RT.var((String)"clojure.core", (String)"not=");
    public static final Keyword const__92 = RT.keyword((String)"db.error", (String)"invalid-element-change");
    public static final Var const__93 = RT.var((String)"datomic.db", (String)"entity-error-desc");
    public static final Var const__94 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Keyword const__95 = RT.keyword(null, (String)"dedup-tx-ms");
    public static final Keyword const__96 = RT.keyword(null, (String)"dedup-ct");
    public static final Keyword const__97 = RT.keyword(null, (String)"dup-datoms");
    public static final Keyword const__98 = RT.keyword(null, (String)"ucheck-ct");
    public static final Keyword const__99 = RT.keyword(null, (String)"ucheck-tx-ms");
    public static final Var const__101 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__103 = RT.var((String)"datomic.db", (String)"long-add!");
    public static final Var const__105 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__106 = RT.var((String)"datomic.db", (String)"find-aevt");
    public static final Var const__108 = RT.var((String)"datomic.common", (String)"equals-with-strict-scale");
    public static final Object const__109 = 19L;
    public static final Var const__110 = RT.var((String)"datomic.db", (String)"find-avet");
    public static final Var const__111 = RT.var((String)"datomic.error", (String)"state");
    public static final Keyword const__112 = RT.keyword((String)"db.error", (String)"unique-conflict");
    public static final Var const__113 = RT.var((String)"datomic.db", (String)"get-hook");
    public static final Var const__115 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__117 = RT.var((String)"datomic.db", (String)"retracting-datum");
    public static final Var const__118 = RT.var((String)"datomic.db", (String)"add-log");
    public static final Var const__119 = RT.var((String)"clojure.core", (String)"vec");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"memidx"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"avet"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"memidx"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"avet"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"memidx"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"avet"));
    static ILookupThunk __thunk__5__ = __site__5__;

    public Db(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, long l, long l2, long l3, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14, Object object15, Object object16, Object object17, Object object18, Object object19, int n, int n2) {
        this.id = object;
        this.memidx = object2;
        this.indexing = object3;
        this.mid_index = object4;
        this.index = object5;
        this.history = object6;
        this.memlog = object7;
        this.basisT = l;
        this.nextT = l2;
        this.indexBasisT = l3;
        this.indexingNextT = object8;
        this.elements = object9;
        this.keys = object10;
        this.ids = object11;
        this.index_root_id = object12;
        this.index_rev = object13;
        this.asOfT = object14;
        this.sinceT = object15;
        this.raw = object16;
        this.filt = object17;
        this.__meta = object18;
        this.__extmap = object19;
        this.__hash = n;
        this.__hasheq = n2;
    }

    public Db(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, long l, long l2, long l3, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14, Object object15, Object object16, Object object17) {
        this(object, object2, object3, object4, object5, object6, object7, l, l2, l3, object8, object9, object10, object11, object12, object13, object14, object15, object16, object17, null, null, 0, 0);
    }

    public Db(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, long l, long l2, long l3, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14, Object object15, Object object16, Object object17, Object object18, Object object19) {
        this(object, object2, object3, object4, object5, object6, object7, l, l2, l3, object8, object9, object10, object11, object12, object13, object14, object15, object16, object17, object18, object19, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"id"), ((IObj)Symbol.intern(null, (String)"memidx")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IndexSet")})), ((IObj)Symbol.intern(null, (String)"indexing")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IndexSet")})), ((IObj)Symbol.intern(null, (String)"mid-index")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IndexSet")})), ((IObj)Symbol.intern(null, (String)"index")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IndexSet")})), ((IObj)Symbol.intern(null, (String)"history")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IndexSet")})), Symbol.intern(null, (String)"memlog"), ((IObj)Symbol.intern(null, (String)"basisT")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), ((IObj)Symbol.intern(null, (String)"nextT")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), ((IObj)Symbol.intern(null, (String)"indexBasisT")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), Symbol.intern(null, (String)"indexingNextT"), Symbol.intern(null, (String)"elements"), Symbol.intern(null, (String)"keys"), Symbol.intern(null, (String)"ids"), Symbol.intern(null, (String)"index-root-id"), Symbol.intern(null, (String)"index-rev"), Symbol.intern(null, (String)"asOfT"), Symbol.intern(null, (String)"sinceT"), Symbol.intern(null, (String)"raw"), Symbol.intern(null, (String)"filt")});
    }

    public static Db create(IPersistentMap iPersistentMap) {
        Object object = iPersistentMap.valAt((Object)Keyword.intern((String)"id"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"id"));
        Object object2 = iPersistentMap.valAt((Object)Keyword.intern((String)"memidx"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"memidx"));
        Object object3 = iPersistentMap.valAt((Object)Keyword.intern((String)"indexing"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"indexing"));
        Object object4 = iPersistentMap.valAt((Object)Keyword.intern((String)"mid-index"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"mid-index"));
        Object object5 = iPersistentMap.valAt((Object)Keyword.intern((String)"index"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"index"));
        Object object6 = iPersistentMap.valAt((Object)Keyword.intern((String)"history"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"history"));
        Object object7 = iPersistentMap.valAt((Object)Keyword.intern((String)"memlog"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"memlog"));
        Long l = (Long)iPersistentMap.valAt((Object)Keyword.intern((String)"basisT"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"basisT"));
        Long l2 = (Long)iPersistentMap.valAt((Object)Keyword.intern((String)"nextT"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"nextT"));
        Long l3 = (Long)iPersistentMap.valAt((Object)Keyword.intern((String)"indexBasisT"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"indexBasisT"));
        Object object8 = iPersistentMap.valAt((Object)Keyword.intern((String)"indexingNextT"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"indexingNextT"));
        Object object9 = iPersistentMap.valAt((Object)Keyword.intern((String)"elements"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"elements"));
        Object object10 = iPersistentMap.valAt((Object)Keyword.intern((String)"keys"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"keys"));
        Object object11 = iPersistentMap.valAt((Object)Keyword.intern((String)"ids"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"ids"));
        Object object12 = iPersistentMap.valAt((Object)Keyword.intern((String)"index-root-id"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"index-root-id"));
        Object object13 = iPersistentMap.valAt((Object)Keyword.intern((String)"index-rev"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"index-rev"));
        Object object14 = iPersistentMap.valAt((Object)Keyword.intern((String)"asOfT"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"asOfT"));
        Object object15 = iPersistentMap.valAt((Object)Keyword.intern((String)"sinceT"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"sinceT"));
        Object object16 = iPersistentMap.valAt((Object)Keyword.intern((String)"raw"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"raw"));
        Object object17 = iPersistentMap.valAt((Object)Keyword.intern((String)"filt"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"filt"));
        return new Db(object, object2, object3, object4, object5, object6, object7, l, l2, l3, object8, object9, object10, object11, object12, object13, object14, object15, object16, object17, null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    public Object acceptDataCheck(Object indata, Object check) {
        Object object;
        Object and__5236__auto__13481;
        long basis = this_.nextT;
        Object d = ((IFn)const__101.getRawRoot()).invoke(indata);
        Object object2 = and__5236__auto__13481 = check;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object and__5236__auto__13480;
            Object object3 = and__5236__auto__13480 = d;
            if (object3 != null && object3 != Boolean.FALSE) {
                object = ((IFn)const__91.getRawRoot()).invoke((Object)Numbers.num((long)((IDatum)d).getT()), (Object)Numbers.num((long)this_.nextT));
            } else {
                object = and__5236__auto__13480;
                and__5236__auto__13480 = null;
            }
        } else {
            object = and__5236__auto__13481;
            and__5236__auto__13481 = null;
        }
        if (object != null && object != Boolean.FALSE) {
            throw (Throwable)new IllegalStateException((String)((IFn)const__41.getRawRoot()).invoke((Object)"Gap in data - expected: ", (Object)Numbers.num((long)this_.nextT), (Object)" got: ", (Object)Numbers.num((long)((IDatum)d).getT())));
        }
        Object db2 = this_;
        long next_t2 = this_.nextT;
        Object eavt2 = ((IndexSet)this_.memidx).eavt;
        Object avet2 = ((IndexSet)this_.memidx).avet;
        Object aevt2 = ((IndexSet)this_.memidx).aevt;
        Object raet2 = ((IndexSet)this_.memidx).raet;
        Object data2 = ((IFn)const__36.getRawRoot()).invoke(indata);
        while (true) {
            Boolean temp__5455__auto__13485;
            Object object4;
            Object object5;
            Object and__5236__auto__13483;
            Object object6;
            Object object7;
            Object and__5236__auto__13482;
            Object object8 = data2;
            if (object8 == null || object8 == Boolean.FALSE) break;
            Object d2 = ((IFn)const__101.getRawRoot()).invoke(data2);
            long eidx = ((IDatumImpl)d2).eidx();
            long next_t3 = next_t2 <= eidx ? eidx + 1L : next_t2;
            int attrid = ((IDatum)d2).getA();
            Object attr = ((IDbImpl)db2).elementAt(attrid);
            Object object9 = eavt2;
            eavt2 = null;
            Object eavt3 = ((IFn)const__86.getRawRoot()).invoke(object9, d2);
            Object object10 = and__5236__auto__13482 = attr;
            if (object10 != null && object10 != Boolean.FALSE) {
                object7 = ((datomic.db.Attribute)attr).needsAVET;
            } else {
                object7 = and__5236__auto__13482;
                and__5236__auto__13482 = null;
            }
            if (object7 != null && object7 != Boolean.FALSE) {
                Object object11 = avet2;
                avet2 = null;
                object6 = ((IFn)const__86.getRawRoot()).invoke(object11, d2);
            } else {
                object6 = avet2;
                avet2 = null;
            }
            Object avet3 = object6;
            Object object12 = aevt2;
            aevt2 = null;
            Object aevt3 = ((IFn)const__86.getRawRoot()).invoke(object12, d2);
            Object object13 = and__5236__auto__13483 = attr;
            if (object13 != null && object13 != Boolean.FALSE) {
                Object object14 = attr;
                attr = null;
                object5 = Util.equiv((long)20L, (Object)((datomic.db.Attribute)object14).vtypeid) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object5 = and__5236__auto__13483;
                and__5236__auto__13483 = null;
            }
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object15 = raet2;
                raet2 = null;
                object4 = ((IFn)const__86.getRawRoot()).invoke(object15, d2);
            } else {
                object4 = raet2;
                raet2 = null;
            }
            Object raet3 = object4;
            boolean and__5236__auto__13484 = ((IDatum)d2).isAssertion();
            Boolean bl = temp__5455__auto__13485 = and__5236__auto__13484 ? ((IFn)const__113.getRawRoot()).invoke((Object)attrid) : (and__5236__auto__13484 ? Boolean.TRUE : Boolean.FALSE);
            if (bl != null && bl != Boolean.FALSE) {
                Object object16;
                Boolean bl2 = temp__5455__auto__13485;
                temp__5455__auto__13485 = null;
                Boolean hook = bl2;
                Db db3 = db2;
                db2 = null;
                Object object17 = avet3;
                avet3 = null;
                Object dbval = ((IFn)const__34.getRawRoot()).invoke((Object)db3, (Object)const__19, (Object)new IndexSet(eavt3, object17, aevt3, raet3, null), (Object)const__13, (Object)Numbers.num((long)basis), (Object)const__10, (Object)Numbers.num((long)next_t3));
                Boolean bl3 = hook;
                hook = null;
                Object object18 = dbval;
                dbval = null;
                Object object19 = d2;
                d2 = null;
                Object dbval2 = ((IFn)bl3).invoke((Object)this_, object18, object19, check);
                ILookupThunk iLookupThunk = __thunk__5__;
                ILookupThunk iLookupThunk2 = __thunk__4__;
                Object object20 = dbval2;
                Object object21 = iLookupThunk2.get(object20);
                if (iLookupThunk2 == object21) {
                    __thunk__4__ = __site__4__.fault(object20);
                    object21 = __thunk__4__.get(object20);
                }
                if (iLookupThunk == (object16 = iLookupThunk.get(object21))) {
                    __thunk__5__ = __site__5__.fault(object21);
                    object16 = __thunk__5__.get(object21);
                }
                Object avet4 = object16;
                Object object22 = dbval2;
                dbval2 = null;
                Object object23 = eavt3;
                eavt3 = null;
                Object object24 = avet4;
                avet4 = null;
                Object object25 = aevt3;
                aevt3 = null;
                Object object26 = raet3;
                raet3 = null;
                Object object27 = data2;
                data2 = null;
                data2 = ((IFn)const__115.getRawRoot()).invoke(object27);
                raet2 = object26;
                aevt2 = object25;
                avet2 = object24;
                eavt2 = object23;
                next_t2 = next_t3;
                db2 = object22;
                continue;
            }
            Db db4 = db2;
            db2 = null;
            Object object28 = eavt3;
            eavt3 = null;
            Object object29 = avet3;
            avet3 = null;
            Object object30 = aevt3;
            aevt3 = null;
            Object object31 = raet3;
            raet3 = null;
            Object object32 = data2;
            data2 = null;
            data2 = ((IFn)const__115.getRawRoot()).invoke(object32);
            raet2 = object31;
            aevt2 = object30;
            avet2 = object29;
            eavt2 = object28;
            next_t2 = next_t3;
            db2 = db4;
        }
        Db db5 = db2;
        db2 = null;
        Object object33 = eavt2;
        eavt2 = null;
        Object object34 = avet2;
        avet2 = null;
        Object object35 = aevt2;
        aevt2 = null;
        Object object36 = raet2;
        raet2 = null;
        Object object37 = check;
        Db this_ = null;
        return ((IFn)const__34.getRawRoot()).invoke((Object)db5, (Object)const__19, (Object)new IndexSet(object33, object34, object35, object36, ((IndexSet)this_.memidx).fulltext), (Object)const__21, ((IFn)const__118.getRawRoot()).invoke(this_.memlog, (Object)Numbers.num((long)basis), ((IFn)const__119.getRawRoot()).invoke(indata)), (Object)const__13, (Object)(object37 != null && object37 != Boolean.FALSE ? (Number)Numbers.num((long)basis) : (Number)Numbers.num((long)((IDatum)d).getT())), (Object)const__10, (Object)Numbers.num((long)next_t2));
    }

    public Object acceptData(Object indata) {
        Object object = indata;
        indata = null;
        return ((IDbImpl)this).acceptDataCheck(object, Boolean.TRUE);
    }

    public Object addData(Object indata, ArrayList added, Object tx_stat_registers) {
        Object object;
        long basis = this_.nextT;
        Object object2 = tx_stat_registers;
        tx_stat_registers = null;
        Object map__13474 = object2;
        Object object3 = ((IFn)const__94.getRawRoot()).invoke(map__13474);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__13474;
            map__13474 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__36.getRawRoot()).invoke(object4)));
        } else {
            object = map__13474;
            map__13474 = null;
        }
        Object map__134742 = object;
        Object dedup_tx_ms = RT.get((Object)map__134742, (Object)const__95);
        Object dedup_ct = RT.get((Object)map__134742, (Object)const__96);
        Object dup_datoms = RT.get((Object)map__134742, (Object)const__97);
        Object ucheck_ct = RT.get((Object)map__134742, (Object)const__98);
        Object object5 = map__134742;
        map__134742 = null;
        Object ucheck_tx_ms = RT.get((Object)object5, (Object)const__99);
        Object db2 = this_;
        long next_t2 = basis + 1L;
        Object eavt2 = ((IndexSet)this_.memidx).eavt;
        Object avet2 = ((IndexSet)this_.memidx).avet;
        Object aevt2 = ((IndexSet)this_.memidx).aevt;
        Object raet2 = ((IndexSet)this_.memidx).raet;
        Object data2 = ((IFn)const__36.getRawRoot()).invoke(indata);
        while (true) {
            Boolean temp__5455__auto__13504;
            Object object6;
            Object object7;
            Object and__5236__auto__13502;
            Object object8;
            Object object9;
            Object object10;
            Object and__5236__auto__13501;
            Object object11;
            Object tomb;
            Object object12;
            Boolean bl;
            Object object13;
            Object object14;
            Object object15;
            Object and__5236__auto__13493;
            Object object16;
            Object object17;
            Boolean ed;
            Boolean and__5236__auto__13490;
            Object object18;
            boolean and__5236__auto__13489;
            Object object19 = data2;
            if (object19 == null || object19 == Boolean.FALSE) break;
            Object d = ((IFn)const__101.getRawRoot()).invoke(data2);
            long eidx = ((IDatumImpl)d).eidx();
            long next_t3 = next_t2 <= eidx ? eidx + 1L : next_t2;
            int attrid = ((IDatum)d).getA();
            Object v = ((IDatum)d).getV();
            Object attr = ((IDbImpl)db2).elementAt(attrid);
            boolean card_one_QMARK_ = Util.equiv((Object)((datomic.db.Attribute)attr).cardinality, (long)35L);
            boolean or__5238__auto__13486 = Numbers.isZero((long)basis);
            boolean bl2 = and__5236__auto__13489 = or__5238__auto__13486 ? or__5238__auto__13486 : Numbers.lt((long)eidx, (long)basis);
            if (and__5236__auto__13489) {
                ((IFn.OLO)const__103.getRawRoot()).invokePrim(dedup_ct, 1L);
                long start__13414__auto__13487 = System.nanoTime();
                Object ret__13415__auto__13488 = ((IFn)const__105.getRawRoot()).invoke(((IFn)const__106.getRawRoot()).invoke(db2, (Object)((IDatum)d).getA(), (Object)Numbers.num((long)((IDatum)d).getE()), card_one_QMARK_ ? null : ((IDatum)d).getV()));
                ((IFn.OLO)const__103.getRawRoot()).invokePrim(dedup_tx_ms, System.nanoTime() - start__13414__auto__13487);
                object18 = ret__13415__auto__13488;
                ret__13415__auto__13488 = null;
            } else {
                object18 = and__5236__auto__13489 ? Boolean.TRUE : Boolean.FALSE;
            }
            Boolean bl3 = and__5236__auto__13490 = (ed = object18);
            if (bl3 != null && bl3 != Boolean.FALSE) {
                object17 = ((IFn)const__108.getRawRoot()).invoke(v, ((IDatum)((Object)ed)).getV());
            } else {
                object17 = and__5236__auto__13490;
                and__5236__auto__13490 = null;
            }
            Boolean there_QMARK_ = object17;
            if (((IDatum)d).isAssertion()) {
                Boolean and__5236__auto__13491;
                Boolean bl4 = there_QMARK_;
                there_QMARK_ = null;
                Boolean bl5 = and__5236__auto__13491 = bl4;
                if (bl5 != null && bl5 != Boolean.FALSE) {
                    object16 = ((IFn)const__91.getRawRoot()).invoke((Object)((IDatum)d).getA(), const__109);
                } else {
                    object16 = and__5236__auto__13491;
                    and__5236__auto__13491 = null;
                }
            } else {
                Boolean bl6 = there_QMARK_;
                there_QMARK_ = null;
                object16 = ((IFn)const__38.getRawRoot()).invoke((Object)bl6);
            }
            Object redundant_QMARK_ = object16;
            Object object20 = and__5236__auto__13493 = ((IFn)const__38.getRawRoot()).invoke(redundant_QMARK_);
            if (object20 != null && object20 != Boolean.FALSE) {
                Object and__5236__auto__13492;
                Object object21 = and__5236__auto__13492 = ((datomic.db.Attribute)attr).unique;
                if (object21 != null && object21 != Boolean.FALSE) {
                    object15 = ((IDatum)d).isAssertion() ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    object15 = and__5236__auto__13492;
                    and__5236__auto__13492 = null;
                }
            } else {
                object15 = and__5236__auto__13493;
                and__5236__auto__13493 = null;
            }
            if (object15 != null && object15 != Boolean.FALSE) {
                Object temp__5457__auto__13496;
                ((IFn.OLO)const__103.getRawRoot()).invokePrim(ucheck_ct, 1L);
                long start__13414__auto__13494 = System.nanoTime();
                Object ret__13415__auto__13495 = ((IFn)const__105.getRawRoot()).invoke(((IFn)const__110.getRawRoot()).invoke(db2, (Object)attrid, v));
                ((IFn.OLO)const__103.getRawRoot()).invokePrim(ucheck_tx_ms, System.nanoTime() - start__13414__auto__13494);
                Object object22 = ret__13415__auto__13495;
                ret__13415__auto__13495 = null;
                Object object23 = temp__5457__auto__13496 = object22;
                if (object23 != null && object23 != Boolean.FALSE) {
                    Object object24 = temp__5457__auto__13496;
                    temp__5457__auto__13496 = null;
                    Object it = object24;
                    Object object25 = ((IFn)const__4.getRawRoot()).invoke((Object)new Db$fn__13475(it, card_one_QMARK_), indata);
                    if (object25 != null && object25 != Boolean.FALSE) {
                    } else {
                        Object object26 = v;
                        v = null;
                        Object object27 = it;
                        it = null;
                        ((IFn)const__111.getRawRoot()).invoke((Object)const__112, ((IFn)const__41.getRawRoot()).invoke((Object)"Unique conflict: ", ((datomic.db.Attribute)attr).kw(), (Object)", value: ", object26, (Object)" already held by: ", (Object)Numbers.num((long)((IDatum)object27).getE()), (Object)" asserted for: ", (Object)Numbers.num((long)((IDatum)d).getE())));
                    }
                }
            }
            Object object28 = redundant_QMARK_;
            redundant_QMARK_ = null;
            if (object28 != null && object28 != Boolean.FALSE) {
                Boolean temp__5455__auto__13498;
                boolean and__5236__auto__13497 = ((IDatum)d).isAssertion();
                Boolean bl7 = temp__5455__auto__13498 = and__5236__auto__13497 ? ((IFn)const__113.getRawRoot()).invoke((Object)attrid) : (and__5236__auto__13497 ? Boolean.TRUE : Boolean.FALSE);
                if (bl7 != null && bl7 != Boolean.FALSE) {
                    Object object29;
                    Boolean bl8 = temp__5455__auto__13498;
                    temp__5455__auto__13498 = null;
                    Boolean hook = bl8;
                    Db db3 = db2;
                    db2 = null;
                    Object object30 = avet2;
                    avet2 = null;
                    Object dbval = ((IFn)const__34.getRawRoot()).invoke((Object)db3, (Object)const__19, (Object)new IndexSet(eavt2, object30, aevt2, raet2, null), (Object)const__13, (Object)Numbers.num((long)basis), (Object)const__10, (Object)Numbers.num((long)next_t3));
                    ILookupThunk iLookupThunk = __thunk__1__;
                    ILookupThunk iLookupThunk2 = __thunk__0__;
                    Object object31 = dbval;
                    Object object32 = iLookupThunk2.get(object31);
                    if (iLookupThunk2 == object32) {
                        __thunk__0__ = __site__0__.fault(object31);
                        object32 = __thunk__0__.get(object31);
                    }
                    if (iLookupThunk == (object29 = iLookupThunk.get(object32))) {
                        __thunk__1__ = __site__1__.fault(object32);
                        object29 = __thunk__1__.get(object32);
                    }
                    Object avet3 = object29;
                    Boolean bl9 = hook;
                    hook = null;
                    Object object33 = dbval;
                    dbval = null;
                    Object object34 = d;
                    d = null;
                    Object object35 = eavt2;
                    eavt2 = null;
                    Object object36 = avet3;
                    avet3 = null;
                    Object object37 = aevt2;
                    aevt2 = null;
                    Object object38 = raet2;
                    raet2 = null;
                    Object object39 = data2;
                    data2 = null;
                    data2 = ((IFn)const__115.getRawRoot()).invoke(object39);
                    raet2 = object38;
                    aevt2 = object37;
                    avet2 = object36;
                    eavt2 = object35;
                    next_t2 = next_t3;
                    db2 = ((IFn)bl9).invoke((Object)this_, object33, object34, (Object)Boolean.TRUE);
                    continue;
                }
                ((IFn.OLO)const__103.getRawRoot()).invokePrim(dup_datoms, 1L);
                Db db4 = db2;
                db2 = null;
                Object object40 = eavt2;
                eavt2 = null;
                Object object41 = avet2;
                avet2 = null;
                Object object42 = aevt2;
                aevt2 = null;
                Object object43 = raet2;
                raet2 = null;
                Object object44 = data2;
                data2 = null;
                data2 = ((IFn)const__115.getRawRoot()).invoke(object44);
                raet2 = object43;
                aevt2 = object42;
                avet2 = object41;
                eavt2 = object40;
                next_t2 = next_t3;
                db2 = db4;
                continue;
            }
            Object object45 = eavt2;
            eavt2 = null;
            Object eavt3 = ((IFn)const__86.getRawRoot()).invoke(object45, d);
            Object object46 = ((datomic.db.Attribute)attr).needsAVET;
            if (object46 != null && object46 != Boolean.FALSE) {
                Object object47 = avet2;
                avet2 = null;
                object14 = ((IFn)const__86.getRawRoot()).invoke(object47, d);
            } else {
                object14 = avet2;
                avet2 = null;
            }
            Object avet4 = object14;
            Object object48 = aevt2;
            aevt2 = null;
            Object aevt3 = ((IFn)const__86.getRawRoot()).invoke(object48, d);
            if (Util.equiv((long)20L, (Object)((datomic.db.Attribute)attr).vtypeid)) {
                Object object49 = raet2;
                raet2 = null;
                object13 = ((IFn)const__86.getRawRoot()).invoke(object49, d);
            } else {
                object13 = raet2;
                raet2 = null;
            }
            Object raet3 = object13;
            boolean and__5236__auto__13500 = ((IDatum)d).isAssertion();
            if (and__5236__auto__13500) {
                Boolean and__5236__auto__13499;
                Boolean bl10 = and__5236__auto__13499 = ed;
                if (bl10 != null && bl10 != Boolean.FALSE) {
                    bl = card_one_QMARK_ ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    bl = and__5236__auto__13499;
                    and__5236__auto__13499 = null;
                }
            } else {
                bl = and__5236__auto__13500 ? Boolean.TRUE : Boolean.FALSE;
            }
            if (bl != null && bl != Boolean.FALSE) {
                Boolean bl11 = ed;
                ed = null;
                object12 = ((IFn.LLOLO)const__117.getRawRoot()).invokePrim(((IDatum)d).getE(), (long)((IDatum)d).getA(), ((IDatum)((Object)bl11)).getV(), ((IDatum)d).getT());
            } else {
                object12 = null;
            }
            Object object50 = tomb = object12;
            if (object50 != null && object50 != Boolean.FALSE) {
                Object object51 = eavt3;
                eavt3 = null;
                object11 = ((IFn)const__86.getRawRoot()).invoke(object51, tomb);
            } else {
                object11 = eavt3;
                eavt3 = null;
            }
            Object eavt4 = object11;
            Object object52 = and__5236__auto__13501 = tomb;
            if (object52 != null && object52 != Boolean.FALSE) {
                object10 = ((datomic.db.Attribute)attr).needsAVET;
            } else {
                object10 = and__5236__auto__13501;
                and__5236__auto__13501 = null;
            }
            if (object10 != null && object10 != Boolean.FALSE) {
                Object object53 = avet4;
                avet4 = null;
                object9 = ((IFn)const__86.getRawRoot()).invoke(object53, tomb);
            } else {
                object9 = avet4;
                avet4 = null;
            }
            Object avet5 = object9;
            Object object54 = tomb;
            if (object54 != null && object54 != Boolean.FALSE) {
                Object object55 = aevt3;
                aevt3 = null;
                object8 = ((IFn)const__86.getRawRoot()).invoke(object55, tomb);
            } else {
                object8 = aevt3;
                aevt3 = null;
            }
            Object aevt4 = object8;
            Object object56 = and__5236__auto__13502 = tomb;
            if (object56 != null && object56 != Boolean.FALSE) {
                Object object57 = attr;
                attr = null;
                object7 = Util.equiv((long)20L, (Object)((datomic.db.Attribute)object57).vtypeid) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object7 = and__5236__auto__13502;
                and__5236__auto__13502 = null;
            }
            if (object7 != null && object7 != Boolean.FALSE) {
                Object object58 = raet3;
                raet3 = null;
                object6 = ((IFn)const__86.getRawRoot()).invoke(object58, tomb);
            } else {
                object6 = raet3;
                raet3 = null;
            }
            Object raet4 = object6;
            Boolean bl12 = added.add(d) ? Boolean.TRUE : Boolean.FALSE;
            Object object59 = tomb;
            if (object59 != null && object59 != Boolean.FALSE) {
                Object object60 = tomb;
                tomb = null;
                Boolean bl13 = added.add(object60) ? Boolean.TRUE : Boolean.FALSE;
            }
            boolean and__5236__auto__13503 = ((IDatum)d).isAssertion();
            Boolean bl14 = temp__5455__auto__13504 = and__5236__auto__13503 ? ((IFn)const__113.getRawRoot()).invoke((Object)attrid) : (and__5236__auto__13503 ? Boolean.TRUE : Boolean.FALSE);
            if (bl14 != null && bl14 != Boolean.FALSE) {
                Object object61;
                Boolean bl15 = temp__5455__auto__13504;
                temp__5455__auto__13504 = null;
                Boolean hook = bl15;
                Object object62 = db2;
                db2 = null;
                Object object63 = avet5;
                avet5 = null;
                Object dbval = ((IFn)const__34.getRawRoot()).invoke(object62, (Object)const__19, (Object)new IndexSet(eavt4, object63, aevt4, raet4, null), (Object)const__13, (Object)Numbers.num((long)basis), (Object)const__10, (Object)Numbers.num((long)next_t3));
                Boolean bl16 = hook;
                hook = null;
                Object object64 = dbval;
                dbval = null;
                Object object65 = d;
                d = null;
                Object dbval2 = ((IFn)bl16).invoke((Object)this_, object64, object65, (Object)Boolean.TRUE);
                ILookupThunk iLookupThunk = __thunk__3__;
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object66 = dbval2;
                Object object67 = iLookupThunk3.get(object66);
                if (iLookupThunk3 == object67) {
                    __thunk__2__ = __site__2__.fault(object66);
                    object67 = __thunk__2__.get(object66);
                }
                if (iLookupThunk == (object61 = iLookupThunk.get(object67))) {
                    __thunk__3__ = __site__3__.fault(object67);
                    object61 = __thunk__3__.get(object67);
                }
                Object avet6 = object61;
                Object object68 = dbval2;
                dbval2 = null;
                Object object69 = eavt4;
                eavt4 = null;
                Object object70 = avet6;
                avet6 = null;
                Object object71 = aevt4;
                aevt4 = null;
                Object object72 = raet4;
                raet4 = null;
                Object object73 = data2;
                data2 = null;
                data2 = ((IFn)const__115.getRawRoot()).invoke(object73);
                raet2 = object72;
                aevt2 = object71;
                avet2 = object70;
                eavt2 = object69;
                next_t2 = next_t3;
                db2 = object68;
                continue;
            }
            Object object74 = db2;
            db2 = null;
            Object object75 = eavt4;
            eavt4 = null;
            Object object76 = avet5;
            avet5 = null;
            Object object77 = aevt4;
            aevt4 = null;
            Object object78 = raet4;
            raet4 = null;
            Object object79 = data2;
            data2 = null;
            data2 = ((IFn)const__115.getRawRoot()).invoke(object79);
            raet2 = object78;
            aevt2 = object77;
            avet2 = object76;
            eavt2 = object75;
            next_t2 = next_t3;
            db2 = object74;
        }
        Db db5 = db2;
        db2 = null;
        Object object80 = eavt2;
        eavt2 = null;
        Object object81 = avet2;
        avet2 = null;
        Object object82 = aevt2;
        aevt2 = null;
        Object object83 = raet2;
        raet2 = null;
        Db this_ = null;
        return ((IFn)const__34.getRawRoot()).invoke((Object)db5, (Object)const__19, (Object)new IndexSet(object80, object81, object82, object83, ((IndexSet)this_.memidx).fulltext), (Object)const__21, ((IFn)const__118.getRawRoot()).invoke(this_.memlog, (Object)Numbers.num((long)basis), ((IFn)const__119.getRawRoot()).invoke((Object)added)), (Object)const__13, (Object)Numbers.num((long)basis), (Object)const__10, (Object)Numbers.num((long)next_t2));
    }

    public Object getRawId() {
        return this.id;
    }

    public Object addElement(IElementImpl e) {
        Object object;
        Object temp__5457__auto__13505;
        Object id = e.id();
        Object object2 = temp__5457__auto__13505 = RT.get((Object)this_.elements, (Object)id);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5457__auto__13505;
            temp__5457__auto__13505 = null;
            Object current = object3;
            Object object4 = ((IFn)const__91.getRawRoot()).invoke(((IFn)const__39.getRawRoot()).invoke(current), ((IFn)const__39.getRawRoot()).invoke((Object)e));
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = current;
                current = null;
                object = ((IFn)const__77.getRawRoot()).invoke((Object)const__92, ((IFn)const__41.getRawRoot()).invoke(((IFn)const__93.getRawRoot()).invoke((Object)this_, id), (Object)" cannot be both ", (Object)((Class)((IFn)const__39.getRawRoot()).invoke(object5)).getSimpleName(), (Object)" and ", (Object)((Class)((IFn)const__39.getRawRoot()).invoke((Object)e)).getSimpleName()));
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        Object object6 = ((IFn)const__89.getRawRoot()).invoke(this_.elements, id);
        Object object7 = id;
        id = null;
        IElementImpl iElementImpl = e;
        e = null;
        Object new_elements = ((IFn)const__34.getRawRoot()).invoke(object6, object7, (Object)iElementImpl);
        Db db2 = this_;
        Object object8 = new_elements;
        new_elements = null;
        Db this_ = null;
        return ((IFn)const__34.getRawRoot()).invoke((Object)db2, (Object)const__17, object8);
    }

    public Object growElements(Object i) {
        Object object;
        Db this_;
        if (Numbers.lte((Object)i, (long)0x100000L)) {
            Db db2 = this_;
            Object object2 = i;
            i = null;
            this_ = null;
            object = ((IFn)const__34.getRawRoot()).invoke((Object)db2, (Object)const__17, ((IFn)const__89.getRawRoot()).invoke(this_.elements, object2));
        } else {
            this_ = null;
            object = ((IFn)const__77.getRawRoot()).invoke((Object)const__90, ((IFn)const__41.getRawRoot()).invoke((Object)"Schema can contain at most ", const__88, (Object)" elements"));
        }
        return object;
    }

    public Object addKeyword(Object kw, Object id) {
        Object object;
        Object object2 = ((IFn)const__34.getRawRoot()).invoke(this_.keys, id, kw);
        Object object3 = id;
        id = null;
        Object G__13473 = ((IFn)const__34.getRawRoot()).invoke((Object)this_, (Object)const__20, object2, (Object)const__15, ((IFn)const__34.getRawRoot()).invoke(this_.ids, ((IFn)const__81.getRawRoot()).invoke(kw), object3));
        Object object4 = ((IFn)const__82.getRawRoot()).invoke(kw);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = G__13473;
            G__13473 = null;
            Object object6 = kw;
            kw = null;
            Db this_ = null;
            object = ((IFn)const__83.getRawRoot()).invoke(object5, (Object)const__84, ((IFn)const__85.getRawRoot()).invoke(const__86.getRawRoot(), (Object)PersistentHashSet.EMPTY), object6);
        } else {
            object = G__13473;
            Object var3_3 = null;
        }
        return object;
    }

    public Object elementAt(Object i) {
        Object object = i;
        i = null;
        Db this_ = null;
        return RT.nth((Object)this_.elements, (int)RT.uncheckedIntCast((Object)((Number)object)), null);
    }

    /*
     * WARNING - void declaration
     */
    public Object eq(Object other) {
        boolean bl;
        boolean and__5236__auto__13509 = other instanceof Db;
        if (and__5236__auto__13509) {
            boolean and__5236__auto__13508 = Util.equiv((Object)this_.id, (Object)((Db)other).id);
            if (and__5236__auto__13508) {
                boolean and__5236__auto__13507 = Util.equiv((long)this_.nextT, (long)((Database)other).nextT());
                if (and__5236__auto__13507) {
                    boolean and__5236__auto__13506 = Util.equiv((Object)this_.asOfT, (Object)((Database)other).asOfT());
                    if (and__5236__auto__13506) {
                        Object object = other;
                        other = null;
                        bl = Util.equiv((Object)this_.sinceT, (Object)((Database)object).sinceT());
                    } else {
                        bl = and__5236__auto__13506;
                    }
                } else {
                    bl = and__5236__auto__13507;
                }
            } else {
                void var3_3;
                bl = var3_3;
            }
        } else {
            void var2_2;
            bl = var2_2;
        }
        Db this_ = null;
        return RT.booleanCast((boolean)bl) ? Boolean.TRUE : Boolean.FALSE;
    }

    public IFn getFn(Object x) {
        Object object;
        Object object2 = ((IFn)const__71.getRawRoot()).invoke(x);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = x;
            x = null;
        } else {
            Object temp__5455__auto__13512;
            Object object3;
            long fnid = ((IFn.OOL)const__72.getRawRoot()).invokePrim((Object)this_, x);
            Object elem = ((IDbImpl)this_).elementAt(Numbers.num((long)fnid));
            if (elem instanceof Function) {
                object3 = elem;
                elem = null;
            } else {
                object3 = null;
            }
            Object object4 = temp__5455__auto__13512 = object3;
            if (object4 != null && object4 != Boolean.FALSE) {
                Object func;
                Object object5 = temp__5455__auto__13512;
                temp__5455__auto__13512 = null;
                Object object6 = func = object5;
                func = null;
                object = ((Function)object6).f;
            } else {
                Db this_;
                Object temp__5455__auto__13511;
                Object object7 = temp__5455__auto__13511 = RT.get((Object)((Database)this_).entity(Numbers.num((long)fnid)), (Object)const__74);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object q2;
                    Object object8 = temp__5455__auto__13511;
                    temp__5455__auto__13511 = null;
                    Object object9 = q2 = object8;
                    q2 = null;
                    this_ = null;
                    object = Circular.constructFn(object9);
                } else {
                    Object object10;
                    Object f;
                    Object and__5236__auto__13510;
                    Object object11 = and__5236__auto__13510 = (f = RT.get((Object)((Database)this_).entity(Numbers.num((long)fnid)), (Object)const__75));
                    if (object11 != null && object11 != Boolean.FALSE) {
                        object10 = f instanceof IFn ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        object10 = and__5236__auto__13510;
                        and__5236__auto__13510 = null;
                    }
                    if (object10 != null && object10 != Boolean.FALSE) {
                        object = f;
                        f = null;
                    } else {
                        Object object12 = x;
                        x = null;
                        this_ = null;
                        object = ((IFn)const__77.getRawRoot()).invoke((Object)const__78, ((IFn)const__41.getRawRoot()).invoke((Object)"Not a data function: ", object12));
                    }
                }
            }
        }
        return (IFn)object;
    }

    public Iter seekRAET(IDatum d) {
        Object object;
        Object object2;
        Object or__5238__auto__13517;
        Object object3;
        Object and__5236__auto__13515;
        Object object4;
        Object and__5236__auto__13514;
        Object object5;
        Object and__5236__auto__13513;
        IFn iFn = (IFn)const__64.getRawRoot();
        Object object6 = const__70.getRawRoot();
        Object object7 = ((IFn)const__66.getRawRoot()).invoke(((IndexSet)this_.memidx).raet, (Object)d);
        IFn iFn2 = (IFn)const__66.getRawRoot();
        Object object8 = and__5236__auto__13513 = this_.indexing;
        if (object8 != null && object8 != Boolean.FALSE) {
            object5 = ((IndexSet)this_.indexing).raet;
        } else {
            object5 = and__5236__auto__13513;
            and__5236__auto__13513 = null;
        }
        Object object9 = iFn2.invoke(object5, (Object)d);
        IFn iFn3 = (IFn)const__66.getRawRoot();
        Object object10 = and__5236__auto__13514 = this_.mid_index;
        if (object10 != null && object10 != Boolean.FALSE) {
            object4 = ((IndexSet)this_.mid_index).raet;
        } else {
            object4 = and__5236__auto__13514;
            and__5236__auto__13514 = null;
        }
        Object object11 = iFn3.invoke(object4, (Object)d);
        IFn iFn4 = (IFn)const__66.getRawRoot();
        Object object12 = and__5236__auto__13515 = this_.index;
        if (object12 != null && object12 != Boolean.FALSE) {
            object3 = ((IndexSet)this_.index).raet;
        } else {
            object3 = and__5236__auto__13515;
            and__5236__auto__13515 = null;
        }
        Object object13 = iFn4.invoke(object3, (Object)d);
        Object object14 = or__5238__auto__13517 = this_.raw;
        if (object14 != null && object14 != Boolean.FALSE) {
            object2 = or__5238__auto__13517;
            or__5238__auto__13517 = null;
        } else {
            Object and__5236__auto__13516;
            Object object15 = and__5236__auto__13516 = this_.asOfT;
            if (object15 != null && object15 != Boolean.FALSE) {
                object2 = Numbers.lt((Object)this_.asOfT, (long)this_.indexBasisT) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object2 = and__5236__auto__13516;
                Object var3_3 = null;
            }
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object16;
            Object and__5236__auto__13518;
            IFn iFn5 = (IFn)const__66.getRawRoot();
            Object object17 = and__5236__auto__13518 = this_.history;
            if (object17 != null && object17 != Boolean.FALSE) {
                object16 = ((IndexSet)this_.history).raet;
            } else {
                object16 = and__5236__auto__13518;
                Object var2_2 = null;
            }
            IDatum iDatum = d;
            d = null;
            object = iFn5.invoke(object16, (Object)iDatum);
        } else {
            object = null;
        }
        Db this_ = null;
        return (Iter)iFn.invoke(object6, object7, object9, object11, object13, object);
    }

    public Iter seekAEVT(IDatum d) {
        Object object;
        Object object2;
        Object or__5238__auto__13523;
        Object object3;
        Object and__5236__auto__13521;
        Object object4;
        Object and__5236__auto__13520;
        Object object5;
        Object and__5236__auto__13519;
        IFn iFn = (IFn)const__64.getRawRoot();
        Object object6 = const__69.getRawRoot();
        Object object7 = ((IFn)const__66.getRawRoot()).invoke(((IndexSet)this_.memidx).aevt, (Object)d);
        IFn iFn2 = (IFn)const__66.getRawRoot();
        Object object8 = and__5236__auto__13519 = this_.indexing;
        if (object8 != null && object8 != Boolean.FALSE) {
            object5 = ((IndexSet)this_.indexing).aevt;
        } else {
            object5 = and__5236__auto__13519;
            and__5236__auto__13519 = null;
        }
        Object object9 = iFn2.invoke(object5, (Object)d);
        IFn iFn3 = (IFn)const__66.getRawRoot();
        Object object10 = and__5236__auto__13520 = this_.mid_index;
        if (object10 != null && object10 != Boolean.FALSE) {
            object4 = ((IndexSet)this_.mid_index).aevt;
        } else {
            object4 = and__5236__auto__13520;
            and__5236__auto__13520 = null;
        }
        Object object11 = iFn3.invoke(object4, (Object)d);
        IFn iFn4 = (IFn)const__66.getRawRoot();
        Object object12 = and__5236__auto__13521 = this_.index;
        if (object12 != null && object12 != Boolean.FALSE) {
            object3 = ((IndexSet)this_.index).aevt;
        } else {
            object3 = and__5236__auto__13521;
            and__5236__auto__13521 = null;
        }
        Object object13 = iFn4.invoke(object3, (Object)d);
        Object object14 = or__5238__auto__13523 = this_.raw;
        if (object14 != null && object14 != Boolean.FALSE) {
            object2 = or__5238__auto__13523;
            or__5238__auto__13523 = null;
        } else {
            Object and__5236__auto__13522;
            Object object15 = and__5236__auto__13522 = this_.asOfT;
            if (object15 != null && object15 != Boolean.FALSE) {
                object2 = Numbers.lt((Object)this_.asOfT, (long)this_.indexBasisT) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object2 = and__5236__auto__13522;
                Object var3_3 = null;
            }
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object16;
            Object and__5236__auto__13524;
            IFn iFn5 = (IFn)const__66.getRawRoot();
            Object object17 = and__5236__auto__13524 = this_.history;
            if (object17 != null && object17 != Boolean.FALSE) {
                object16 = ((IndexSet)this_.history).aevt;
            } else {
                object16 = and__5236__auto__13524;
                Object var2_2 = null;
            }
            IDatum iDatum = d;
            d = null;
            object = iFn5.invoke(object16, (Object)iDatum);
        } else {
            object = null;
        }
        Db this_ = null;
        return (Iter)iFn.invoke(object6, object7, object9, object11, object13, object);
    }

    public Iter seekAVET(IDatum d) {
        Object object;
        Object object2;
        Object or__5238__auto__13529;
        Object object3;
        Object and__5236__auto__13527;
        Object object4;
        Object and__5236__auto__13526;
        Object object5;
        Object and__5236__auto__13525;
        IFn iFn = (IFn)const__64.getRawRoot();
        Object object6 = const__68.getRawRoot();
        Object object7 = ((IFn)const__66.getRawRoot()).invoke(((IndexSet)this_.memidx).avet, (Object)d);
        IFn iFn2 = (IFn)const__66.getRawRoot();
        Object object8 = and__5236__auto__13525 = this_.indexing;
        if (object8 != null && object8 != Boolean.FALSE) {
            object5 = ((IndexSet)this_.indexing).avet;
        } else {
            object5 = and__5236__auto__13525;
            and__5236__auto__13525 = null;
        }
        Object object9 = iFn2.invoke(object5, (Object)d);
        IFn iFn3 = (IFn)const__66.getRawRoot();
        Object object10 = and__5236__auto__13526 = this_.mid_index;
        if (object10 != null && object10 != Boolean.FALSE) {
            object4 = ((IndexSet)this_.mid_index).avet;
        } else {
            object4 = and__5236__auto__13526;
            and__5236__auto__13526 = null;
        }
        Object object11 = iFn3.invoke(object4, (Object)d);
        IFn iFn4 = (IFn)const__66.getRawRoot();
        Object object12 = and__5236__auto__13527 = this_.index;
        if (object12 != null && object12 != Boolean.FALSE) {
            object3 = ((IndexSet)this_.index).avet;
        } else {
            object3 = and__5236__auto__13527;
            and__5236__auto__13527 = null;
        }
        Object object13 = iFn4.invoke(object3, (Object)d);
        Object object14 = or__5238__auto__13529 = this_.raw;
        if (object14 != null && object14 != Boolean.FALSE) {
            object2 = or__5238__auto__13529;
            or__5238__auto__13529 = null;
        } else {
            Object and__5236__auto__13528;
            Object object15 = and__5236__auto__13528 = this_.asOfT;
            if (object15 != null && object15 != Boolean.FALSE) {
                object2 = Numbers.lt((Object)this_.asOfT, (long)this_.indexBasisT) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object2 = and__5236__auto__13528;
                Object var3_3 = null;
            }
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object16;
            Object and__5236__auto__13530;
            IFn iFn5 = (IFn)const__66.getRawRoot();
            Object object17 = and__5236__auto__13530 = this_.history;
            if (object17 != null && object17 != Boolean.FALSE) {
                object16 = ((IndexSet)this_.history).avet;
            } else {
                object16 = and__5236__auto__13530;
                Object var2_2 = null;
            }
            IDatum iDatum = d;
            d = null;
            object = iFn5.invoke(object16, (Object)iDatum);
        } else {
            object = null;
        }
        Db this_ = null;
        return (Iter)iFn.invoke(object6, object7, object9, object11, object13, object);
    }

    public Iter seekEAVT(IDatum d) {
        Object object;
        Object object2;
        Object or__5238__auto__13535;
        Object object3;
        Object and__5236__auto__13533;
        Object object4;
        Object and__5236__auto__13532;
        Object object5;
        Object and__5236__auto__13531;
        IFn iFn = (IFn)const__64.getRawRoot();
        Object object6 = const__65.getRawRoot();
        Object object7 = ((IFn)const__66.getRawRoot()).invoke(((IndexSet)this_.memidx).eavt, (Object)d);
        IFn iFn2 = (IFn)const__66.getRawRoot();
        Object object8 = and__5236__auto__13531 = this_.indexing;
        if (object8 != null && object8 != Boolean.FALSE) {
            object5 = ((IndexSet)this_.indexing).eavt;
        } else {
            object5 = and__5236__auto__13531;
            and__5236__auto__13531 = null;
        }
        Object object9 = iFn2.invoke(object5, (Object)d);
        IFn iFn3 = (IFn)const__66.getRawRoot();
        Object object10 = and__5236__auto__13532 = this_.mid_index;
        if (object10 != null && object10 != Boolean.FALSE) {
            object4 = ((IndexSet)this_.mid_index).eavt;
        } else {
            object4 = and__5236__auto__13532;
            and__5236__auto__13532 = null;
        }
        Object object11 = iFn3.invoke(object4, (Object)d);
        IFn iFn4 = (IFn)const__66.getRawRoot();
        Object object12 = and__5236__auto__13533 = this_.index;
        if (object12 != null && object12 != Boolean.FALSE) {
            object3 = ((IndexSet)this_.index).eavt;
        } else {
            object3 = and__5236__auto__13533;
            and__5236__auto__13533 = null;
        }
        Object object13 = iFn4.invoke(object3, (Object)d);
        Object object14 = or__5238__auto__13535 = this_.raw;
        if (object14 != null && object14 != Boolean.FALSE) {
            object2 = or__5238__auto__13535;
            or__5238__auto__13535 = null;
        } else {
            Object and__5236__auto__13534;
            Object object15 = and__5236__auto__13534 = this_.asOfT;
            if (object15 != null && object15 != Boolean.FALSE) {
                object2 = Numbers.lt((Object)this_.asOfT, (long)this_.indexBasisT) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object2 = and__5236__auto__13534;
                Object var3_3 = null;
            }
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object16;
            Object and__5236__auto__13536;
            IFn iFn5 = (IFn)const__66.getRawRoot();
            Object object17 = and__5236__auto__13536 = this_.history;
            if (object17 != null && object17 != Boolean.FALSE) {
                object16 = ((IndexSet)this_.history).eavt;
            } else {
                object16 = and__5236__auto__13536;
                Object var2_2 = null;
            }
            IDatum iDatum = d;
            d = null;
            object = iFn5.invoke(object16, (Object)iDatum);
        } else {
            object = null;
        }
        Db this_ = null;
        return (Iter)iFn.invoke(object6, object7, object9, object11, object13, object);
    }

    public Object getFilter() {
        return this.filt;
    }

    public Object getRaw() {
        return this.raw;
    }

    public Object getSinceT() {
        return this.sinceT;
    }

    public Object getAsOfT() {
        return this.asOfT;
    }

    public Object idOf(Object kw) {
        Object object = kw;
        kw = null;
        Db this_ = null;
        return RT.get((Object)this_.ids, (Object)((IFn)const__63.getRawRoot()).invoke(object));
    }

    public Object keywordOf(Object id) {
        Object object = id;
        id = null;
        Db this_ = null;
        return ((IFn)this_.keys).invoke(object);
    }

    public Object getNextT() {
        return Numbers.num((long)this.nextT);
    }

    public Map dbStats() {
        Db db2 = this_;
        Db this_ = null;
        return (Map)Circular.dbStats(db2);
    }

    public Database filter(Database.Predicate pred2) {
        Database.Predicate predicate = pred2;
        pred2 = null;
        return ((Database)this).filter((Object)predicate);
    }

    public Database filter(Object pred2) {
        Object object;
        Object object2;
        if (pred2 instanceof Database.Predicate) {
            pred2 = null;
            object2 = new Db$fn__13468(pred2);
        } else {
            object2 = pred2;
            pred2 = null;
        }
        Object pred3 = object2;
        IFn iFn = (IFn)const__34.getRawRoot();
        Db db2 = this_;
        Object object3 = this_.filt;
        if (object3 != null && object3 != Boolean.FALSE) {
            pred3 = null;
            object = new Db$fn__13470(pred3, this_.filt);
        } else {
            object = pred3;
            pred3 = null;
        }
        Db this_ = null;
        return (Database)iFn.invoke((Object)db2, (Object)const__26, object);
    }

    public Database history() {
        Db db2 = this_;
        Db this_ = null;
        return (Database)((IFn)const__34.getRawRoot()).invoke((Object)db2, (Object)const__12, (Object)Boolean.TRUE);
    }

    public Iterable indexRange(Object attrid, Object start, Object end) {
        Object object = attrid;
        attrid = null;
        Object object2 = end;
        end = null;
        Object object3 = start;
        start = null;
        Db$fn__13466 db$fn__13466 = new Db$fn__13466(object, this_, object2, object3);
        Db this_ = null;
        return (Iterable)((IFn)const__60.getRawRoot()).invoke((Object)db$fn__13466);
    }

    public Iterable seekDatoms(Object index2, Object[] components) {
        Db db2 = this_;
        Object object = index2;
        index2 = null;
        Object[] objectArray = components;
        components = null;
        Db this_ = null;
        return (Iterable)((IFn)const__59.getRawRoot()).invoke((Object)db2, object, (Object)objectArray);
    }

    public Iterable datoms(Object index2, Object[] components) {
        Db db2 = this_;
        Object object = index2;
        index2 = null;
        Object[] objectArray = components;
        components = null;
        Db this_ = null;
        return (Iterable)((IFn)const__58.getRawRoot()).invoke((Object)db2, object, (Object)objectArray);
    }

    public Map with(List txdata, Object opts) {
        Object object = this_.raw;
        if (object != null && object != Boolean.FALSE) {
            throw (Throwable)new IllegalStateException("Can't get history with txdata");
        }
        Db db2 = this_;
        List list = txdata;
        txdata = null;
        Object object2 = opts;
        opts = null;
        Db this_ = null;
        return (Map)((IFn)const__57.getRawRoot()).invoke((Object)db2, (Object)list, object2);
    }

    public Map with(List txdata) {
        List list = txdata;
        txdata = null;
        return ((Database)this).with(list, null);
    }

    public Object invoke(Object keyOrId, Object[] args) {
        Db db2 = this_;
        Object object = keyOrId;
        keyOrId = null;
        Object[] objectArray = args;
        args = null;
        Db this_ = null;
        return ((IFn)const__55.getRawRoot()).invoke(const__56.getRawRoot(), (Object)db2, object, (Object)objectArray);
    }

    public Object entidAt(Object part2, Object t_or_date) {
        Db db2 = this_;
        Object object = part2;
        part2 = null;
        Object object2 = t_or_date;
        t_or_date = null;
        Db this_ = null;
        return ((IFn)const__54.getRawRoot()).invoke((Object)db2, object, object2);
    }

    public Object entid(Object keyOrId) {
        Db db2 = this_;
        Object object = keyOrId;
        keyOrId = null;
        Db this_ = null;
        return ((IFn)const__51.getRawRoot()).invoke((Object)db2, object);
    }

    public Object ident(Object idOrKey) {
        Db db2 = this_;
        Object object = idOrKey;
        idOrKey = null;
        Db this_ = null;
        return ((IFn)const__53.getRawRoot()).invoke((Object)db2, object);
    }

    public Attribute attribute(Object aid) {
        Db db2 = this_;
        Object object = aid;
        aid = null;
        Db this_ = null;
        return (Attribute)((IFn)const__52.getRawRoot()).invoke((Object)db2, object);
    }

    public Object pullMany(Object selector, List eids, Object options) {
        Db db2 = this_;
        Object object = selector;
        selector = null;
        List list = eids;
        eids = null;
        Object object2 = options;
        options = null;
        Db this_ = null;
        return Circular.pullMany(db2, object, list, object2);
    }

    public List pullMany(Object selector, List eids) {
        Db db2 = this_;
        Object object = selector;
        selector = null;
        List list = eids;
        eids = null;
        Db this_ = null;
        return (List)Circular.pullMany(db2, object, list);
    }

    public Stream indexPull(Object options) {
        Db db2 = this_;
        Object object = options;
        options = null;
        Db this_ = null;
        return (Stream)Circular.indexPull(db2, object);
    }

    public Object pull(Object selector, Object eid, Object options) {
        Db db2 = this_;
        Object object = selector;
        selector = null;
        Object object2 = eid;
        eid = null;
        Object object3 = options;
        options = null;
        Db this_ = null;
        return Circular.pull(db2, object, object2, object3);
    }

    public Map pull(Object selector, Object eid) {
        Db db2 = this_;
        Object object = selector;
        selector = null;
        Object object2 = eid;
        eid = null;
        Db this_ = null;
        return (Map)Circular.pull(db2, object, object2, null);
    }

    public Entity entity(Object eid) {
        Object object;
        Object temp__5457__auto__13537;
        Object object2 = this_.raw;
        if (object2 != null && object2 != Boolean.FALSE) {
            throw (Throwable)new IllegalStateException("Can't create entity from history");
        }
        Object object3 = eid;
        eid = null;
        Object object4 = temp__5457__auto__13537 = ((IFn)const__51.getRawRoot()).invoke((Object)this_, object3);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5457__auto__13537;
            temp__5457__auto__13537 = null;
            Object id = object5;
            Db db2 = this_;
            Object object6 = id;
            id = null;
            Db this_ = null;
            object = Circular.emap(db2, object6);
        } else {
            object = null;
        }
        return (Entity)object;
    }

    public Database since(Object t) {
        Db db2 = this_;
        Object object = t;
        t = null;
        Object object2 = ((IFn)const__50.getRawRoot()).invoke((Object)this_, object);
        Db this_ = null;
        return (Database)((IFn)const__34.getRawRoot()).invoke((Object)db2, (Object)const__24, object2);
    }

    public Database asOf(Object t) {
        Db db2 = this_;
        Object object = t;
        t = null;
        Object object2 = ((IFn)const__50.getRawRoot()).invoke((Object)this_, object);
        Db this_ = null;
        return (Database)((IFn)const__34.getRawRoot()).invoke((Object)db2, (Object)const__27, object2);
    }

    public boolean isFiltered() {
        return RT.booleanCast((Object)this.filt);
    }

    public boolean isHistory() {
        return RT.booleanCast((Object)this.raw);
    }

    public Long sinceT() {
        return (Long)this.sinceT;
    }

    public Long asOfT() {
        return (Long)this.asOfT;
    }

    public long nextT() {
        return this.nextT;
    }

    public long basisT() {
        return this.basisT;
    }

    public String id() {
        Db this_ = null;
        return (String)((IFn)const__41.getRawRoot()).invoke(this_.id);
    }

    /*
     * WARNING - void declaration
     */
    public int hasheq() {
        void v0;
        int hq__7465__auto__13539 = this.__hasheq;
        if ((long)hq__7465__auto__13539 == 0L) {
            void var2_2;
            int h__7466__auto__13538;
            this.__hasheq = h__7466__auto__13538 = (int)(0x34790828L ^ (long)APersistentMap.mapHasheq((IPersistentMap)this));
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
        int hash__7468__auto__13541 = this.__hash;
        if ((long)hash__7468__auto__13541 == 0L) {
            void var2_2;
            int h__7469__auto__13540;
            this.__hash = h__7469__auto__13540 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__13420) {
        Object object = G__13420;
        G__13420 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__13420) {
        IPersistentMap iPersistentMap = G__13420;
        G__13420 = null;
        return new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
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
        Object G__13465 = k__7476__auto__;
        switch (Util.hash((Object)G__13465) >> 0 & 0x7F) {
            case 3: {
                if (G__13465 != const__14) break;
                object = this_.history;
                return object;
            }
            case 9: {
                if (G__13465 != const__26) break;
                object = this_.filt;
                return object;
            }
            case 24: {
                if (G__13465 != const__15) break;
                object = this_.ids;
                return object;
            }
            case 36: {
                if (G__13465 != const__12) break;
                object = this_.raw;
                return object;
            }
            case 49: {
                if (G__13465 != const__13) break;
                object = Numbers.num((long)this_.basisT);
                return object;
            }
            case 54: {
                if (G__13465 != const__16) break;
                object = this_.indexing;
                return object;
            }
            case 60: {
                if (G__13465 != const__21) break;
                object = this_.memlog;
                return object;
            }
            case 78: {
                if (G__13465 != const__24) break;
                object = this_.sinceT;
                return object;
            }
            case 81: {
                if (G__13465 != const__25) break;
                object = this_.index_root_id;
                return object;
            }
            case 89: {
                if (G__13465 != const__27) break;
                object = this_.asOfT;
                return object;
            }
            case 92: {
                if (G__13465 != const__19) break;
                object = this_.memidx;
                return object;
            }
            case 93: {
                if (G__13465 != const__22) break;
                object = this_.id;
                return object;
            }
            case 97: {
                if (G__13465 != const__10) break;
                object = Numbers.num((long)this_.nextT);
                return object;
            }
            case 100: {
                if (G__13465 != const__18) break;
                object = this_.index_rev;
                return object;
            }
            case 106: {
                if (G__13465 != const__9) break;
                object = this_.mid_index;
                return object;
            }
            case 117: {
                if (G__13465 != const__23) break;
                object = this_.indexingNextT;
                return object;
            }
            case 120: {
                if (G__13465 != const__11) break;
                object = this_.index;
                return object;
            }
            case 122: {
                if (G__13465 != const__17) break;
                object = this_.elements;
                return object;
            }
            case 123: {
                if (G__13465 != const__20) break;
                object = this_.keys;
                return object;
            }
            case 124: {
                if (G__13465 != const__8) break;
                object = Numbers.num((long)this_.indexBasisT);
                return object;
            }
        }
        Object object2 = k__7476__auto__;
        k__7476__auto__ = null;
        Object object3 = else__7477__auto__;
        else__7477__auto__ = null;
        Db this_ = null;
        object = RT.get((Object)this_.__extmap, (Object)object2, (Object)object3);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public ILookupThunk getLookupThunk(Keyword k__7479__auto__) {
        Object object;
        Object gclass = ((IFn)const__39.getRawRoot()).invoke((Object)this);
        Keyword keyword = k__7479__auto__;
        k__7479__auto__ = null;
        Keyword G__13424 = keyword;
        switch (Util.hash((Object)G__13424) >> 0 & 0x7F) {
            case 3: {
                if (G__13424 != const__14) break;
                gclass = null;
                object = new Db$reify__13425(null, gclass);
                return object;
            }
            case 9: {
                if (G__13424 != const__26) break;
                gclass = null;
                object = new Db$reify__13427(null, gclass);
                return object;
            }
            case 24: {
                if (G__13424 != const__15) break;
                gclass = null;
                object = new Db$reify__13429(null, gclass);
                return object;
            }
            case 36: {
                if (G__13424 != const__12) break;
                gclass = null;
                object = new Db$reify__13431(null, gclass);
                return object;
            }
            case 49: {
                if (G__13424 != const__13) break;
                gclass = null;
                object = new Db$reify__13433(null, gclass);
                return object;
            }
            case 54: {
                if (G__13424 != const__16) break;
                gclass = null;
                object = new Db$reify__13435(null, gclass);
                return object;
            }
            case 60: {
                if (G__13424 != const__21) break;
                gclass = null;
                object = new Db$reify__13437(null, gclass);
                return object;
            }
            case 78: {
                if (G__13424 != const__24) break;
                gclass = null;
                object = new Db$reify__13439(null, gclass);
                return object;
            }
            case 81: {
                if (G__13424 != const__25) break;
                gclass = null;
                object = new Db$reify__13441(null, gclass);
                return object;
            }
            case 89: {
                if (G__13424 != const__27) break;
                gclass = null;
                object = new Db$reify__13443(null, gclass);
                return object;
            }
            case 92: {
                if (G__13424 != const__19) break;
                gclass = null;
                object = new Db$reify__13445(null, gclass);
                return object;
            }
            case 93: {
                if (G__13424 != const__22) break;
                gclass = null;
                object = new Db$reify__13447(null, gclass);
                return object;
            }
            case 97: {
                if (G__13424 != const__10) break;
                gclass = null;
                object = new Db$reify__13449(null, gclass);
                return object;
            }
            case 100: {
                if (G__13424 != const__18) break;
                gclass = null;
                object = new Db$reify__13451(null, gclass);
                return object;
            }
            case 106: {
                if (G__13424 != const__9) break;
                gclass = null;
                object = new Db$reify__13453(null, gclass);
                return object;
            }
            case 117: {
                if (G__13424 != const__23) break;
                gclass = null;
                object = new Db$reify__13455(null, gclass);
                return object;
            }
            case 120: {
                if (G__13424 != const__11) break;
                gclass = null;
                object = new Db$reify__13457(null, gclass);
                return object;
            }
            case 122: {
                if (G__13424 != const__17) break;
                gclass = null;
                object = new Db$reify__13459(null, gclass);
                return object;
            }
            case 123: {
                if (G__13424 != const__20) break;
                gclass = null;
                object = new Db$reify__13461(null, gclass);
                return object;
            }
            case 124: {
                if (G__13424 != const__8) break;
                gclass = null;
                object = new Db$reify__13463(null, gclass);
                return object;
            }
        }
        object = null;
        return object;
    }

    public int count() {
        return RT.intCast((long)(20L + (long)RT.count((Object)this.__extmap)));
    }

    public IPersistentCollection empty() {
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__41.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.db.Db"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        Db db2 = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        Db this_ = null;
        return (IPersistentCollection)((IFn)const__40).invoke((Object)db2, object);
    }

    public boolean equiv(Object G__13420) {
        Boolean bl;
        boolean or__5238__auto__13562 = Util.identical((Object)this, (Object)G__13420);
        if (or__5238__auto__13562) {
            bl = or__5238__auto__13562 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__39.getRawRoot()).invoke((Object)this), (Object)((IFn)const__39.getRawRoot()).invoke(G__13420))) {
            Object object = G__13420;
            G__13420 = null;
            Object G__134202 = object;
            boolean and__5236__auto__13561 = Util.equiv((Object)this.id, (Object)((Db)G__134202).id);
            if (and__5236__auto__13561) {
                boolean and__5236__auto__13560 = Util.equiv((Object)this.memidx, (Object)((Db)G__134202).memidx);
                if (and__5236__auto__13560) {
                    boolean and__5236__auto__13559 = Util.equiv((Object)this.indexing, (Object)((Db)G__134202).indexing);
                    if (and__5236__auto__13559) {
                        boolean and__5236__auto__13558 = Util.equiv((Object)this.mid_index, (Object)((Db)G__134202).mid_index);
                        if (and__5236__auto__13558) {
                            boolean and__5236__auto__13557 = Util.equiv((Object)this.index, (Object)((Db)G__134202).index);
                            if (and__5236__auto__13557) {
                                boolean and__5236__auto__13556 = Util.equiv((Object)this.history, (Object)((Db)G__134202).history);
                                if (and__5236__auto__13556) {
                                    boolean and__5236__auto__13555 = Util.equiv((Object)this.memlog, (Object)((Db)G__134202).memlog);
                                    if (and__5236__auto__13555) {
                                        boolean and__5236__auto__13554 = Util.equiv((long)this.basisT, (long)((Db)G__134202).basisT);
                                        if (and__5236__auto__13554) {
                                            boolean and__5236__auto__13553 = Util.equiv((long)this.nextT, (long)((Db)G__134202).nextT);
                                            if (and__5236__auto__13553) {
                                                boolean and__5236__auto__13552 = Util.equiv((long)this.indexBasisT, (long)((Db)G__134202).indexBasisT);
                                                if (and__5236__auto__13552) {
                                                    boolean and__5236__auto__13551 = Util.equiv((Object)this.indexingNextT, (Object)((Db)G__134202).indexingNextT);
                                                    if (and__5236__auto__13551) {
                                                        boolean and__5236__auto__13550 = Util.equiv((Object)this.elements, (Object)((Db)G__134202).elements);
                                                        if (and__5236__auto__13550) {
                                                            boolean and__5236__auto__13549 = Util.equiv((Object)this.keys, (Object)((Db)G__134202).keys);
                                                            if (and__5236__auto__13549) {
                                                                boolean and__5236__auto__13548 = Util.equiv((Object)this.ids, (Object)((Db)G__134202).ids);
                                                                if (and__5236__auto__13548) {
                                                                    boolean and__5236__auto__13547 = Util.equiv((Object)this.index_root_id, (Object)((Db)G__134202).index_root_id);
                                                                    if (and__5236__auto__13547) {
                                                                        boolean and__5236__auto__13546 = Util.equiv((Object)this.index_rev, (Object)((Db)G__134202).index_rev);
                                                                        if (and__5236__auto__13546) {
                                                                            boolean and__5236__auto__13545 = Util.equiv((Object)this.asOfT, (Object)((Db)G__134202).asOfT);
                                                                            if (and__5236__auto__13545) {
                                                                                boolean and__5236__auto__13544 = Util.equiv((Object)this.sinceT, (Object)((Db)G__134202).sinceT);
                                                                                if (and__5236__auto__13544) {
                                                                                    boolean and__5236__auto__13543 = Util.equiv((Object)this.raw, (Object)((Db)G__134202).raw);
                                                                                    if (and__5236__auto__13543) {
                                                                                        boolean and__5236__auto__13542 = Util.equiv((Object)this.filt, (Object)((Db)G__134202).filt);
                                                                                        if (and__5236__auto__13542) {
                                                                                            Object object2 = G__134202;
                                                                                            G__134202 = null;
                                                                                            bl = Util.equiv((Object)this.__extmap, (Object)((Db)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
                                                                                        } else {
                                                                                            bl = and__5236__auto__13542 ? Boolean.TRUE : Boolean.FALSE;
                                                                                        }
                                                                                    } else {
                                                                                        bl = and__5236__auto__13543 ? Boolean.TRUE : Boolean.FALSE;
                                                                                    }
                                                                                } else {
                                                                                    bl = and__5236__auto__13544 ? Boolean.TRUE : Boolean.FALSE;
                                                                                }
                                                                            } else {
                                                                                bl = and__5236__auto__13545 ? Boolean.TRUE : Boolean.FALSE;
                                                                            }
                                                                        } else {
                                                                            bl = and__5236__auto__13546 ? Boolean.TRUE : Boolean.FALSE;
                                                                        }
                                                                    } else {
                                                                        bl = and__5236__auto__13547 ? Boolean.TRUE : Boolean.FALSE;
                                                                    }
                                                                } else {
                                                                    bl = and__5236__auto__13548 ? Boolean.TRUE : Boolean.FALSE;
                                                                }
                                                            } else {
                                                                bl = and__5236__auto__13549 ? Boolean.TRUE : Boolean.FALSE;
                                                            }
                                                        } else {
                                                            bl = and__5236__auto__13550 ? Boolean.TRUE : Boolean.FALSE;
                                                        }
                                                    } else {
                                                        bl = and__5236__auto__13551 ? Boolean.TRUE : Boolean.FALSE;
                                                    }
                                                } else {
                                                    bl = and__5236__auto__13552 ? Boolean.TRUE : Boolean.FALSE;
                                                }
                                            } else {
                                                bl = and__5236__auto__13553 ? Boolean.TRUE : Boolean.FALSE;
                                            }
                                        } else {
                                            bl = and__5236__auto__13554 ? Boolean.TRUE : Boolean.FALSE;
                                        }
                                    } else {
                                        bl = and__5236__auto__13555 ? Boolean.TRUE : Boolean.FALSE;
                                    }
                                } else {
                                    bl = and__5236__auto__13556 ? Boolean.TRUE : Boolean.FALSE;
                                }
                            } else {
                                bl = and__5236__auto__13557 ? Boolean.TRUE : Boolean.FALSE;
                            }
                        } else {
                            bl = and__5236__auto__13558 ? Boolean.TRUE : Boolean.FALSE;
                        }
                    } else {
                        bl = and__5236__auto__13559 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    bl = and__5236__auto__13560 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__13561 ? Boolean.TRUE : Boolean.FALSE;
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
        Db this_ = null;
        return (Boolean)((IFn)const__38.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__13563 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__13563)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__13563;
            v__7489__auto__13563 = null;
            Db this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        Db this_ = null;
        return (ISeq)((IFn)const__36.getRawRoot()).invoke(((IFn)const__37.getRawRoot()).invoke((Object)RT.vector((Object[])new Object[]{MapEntry.create((Object)const__22, (Object)this_.id), MapEntry.create((Object)const__19, (Object)this_.memidx), MapEntry.create((Object)const__16, (Object)this_.indexing), MapEntry.create((Object)const__9, (Object)this_.mid_index), MapEntry.create((Object)const__11, (Object)this_.index), MapEntry.create((Object)const__14, (Object)this_.history), MapEntry.create((Object)const__21, (Object)this_.memlog), MapEntry.create((Object)const__13, (Object)Numbers.num((long)this_.basisT)), MapEntry.create((Object)const__10, (Object)Numbers.num((long)this_.nextT)), MapEntry.create((Object)const__8, (Object)Numbers.num((long)this_.indexBasisT)), MapEntry.create((Object)const__23, (Object)this_.indexingNextT), MapEntry.create((Object)const__17, (Object)this_.elements), MapEntry.create((Object)const__20, (Object)this_.keys), MapEntry.create((Object)const__15, (Object)this_.ids), MapEntry.create((Object)const__25, (Object)this_.index_root_id), MapEntry.create((Object)const__18, (Object)this_.index_rev), MapEntry.create((Object)const__27, (Object)this_.asOfT), MapEntry.create((Object)const__24, (Object)this_.sinceT), MapEntry.create((Object)const__12, (Object)this_.raw), MapEntry.create((Object)const__26, (Object)this_.filt)}), this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)const__35, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__13420) {
        Db db2;
        Object pred__13422 = const__33.getRawRoot();
        Object expr__13423 = k__7493__auto__;
        Object object = ((IFn)pred__13422).invoke((Object)const__22, expr__13423);
        if (object != null && object != Boolean.FALSE) {
            G__13420 = null;
            db2 = new Db(G__13420, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
        } else {
            Object object2 = ((IFn)pred__13422).invoke((Object)const__19, expr__13423);
            if (object2 != null && object2 != Boolean.FALSE) {
                G__13420 = null;
                db2 = new Db(this.id, G__13420, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
            } else {
                Object object3 = ((IFn)pred__13422).invoke((Object)const__16, expr__13423);
                if (object3 != null && object3 != Boolean.FALSE) {
                    G__13420 = null;
                    db2 = new Db(this.id, this.memidx, G__13420, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                } else {
                    Object object4 = ((IFn)pred__13422).invoke((Object)const__9, expr__13423);
                    if (object4 != null && object4 != Boolean.FALSE) {
                        G__13420 = null;
                        db2 = new Db(this.id, this.memidx, this.indexing, G__13420, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                    } else {
                        Object object5 = ((IFn)pred__13422).invoke((Object)const__11, expr__13423);
                        if (object5 != null && object5 != Boolean.FALSE) {
                            G__13420 = null;
                            db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, G__13420, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                        } else {
                            Object object6 = ((IFn)pred__13422).invoke((Object)const__14, expr__13423);
                            if (object6 != null && object6 != Boolean.FALSE) {
                                G__13420 = null;
                                db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, G__13420, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                            } else {
                                Object object7 = ((IFn)pred__13422).invoke((Object)const__21, expr__13423);
                                if (object7 != null && object7 != Boolean.FALSE) {
                                    G__13420 = null;
                                    db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, G__13420, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                                } else {
                                    Object object8 = ((IFn)pred__13422).invoke((Object)const__13, expr__13423);
                                    if (object8 != null && object8 != Boolean.FALSE) {
                                        G__13420 = null;
                                        db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, RT.uncheckedLongCast((Object)((Number)G__13420)), this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                                    } else {
                                        Object object9 = ((IFn)pred__13422).invoke((Object)const__10, expr__13423);
                                        if (object9 != null && object9 != Boolean.FALSE) {
                                            G__13420 = null;
                                            db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, RT.uncheckedLongCast((Object)((Number)G__13420)), this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                                        } else {
                                            Object object10 = ((IFn)pred__13422).invoke((Object)const__8, expr__13423);
                                            if (object10 != null && object10 != Boolean.FALSE) {
                                                G__13420 = null;
                                                db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, RT.uncheckedLongCast((Object)((Number)G__13420)), this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                                            } else {
                                                Object object11 = ((IFn)pred__13422).invoke((Object)const__23, expr__13423);
                                                if (object11 != null && object11 != Boolean.FALSE) {
                                                    G__13420 = null;
                                                    db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, G__13420, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                                                } else {
                                                    Object object12 = ((IFn)pred__13422).invoke((Object)const__17, expr__13423);
                                                    if (object12 != null && object12 != Boolean.FALSE) {
                                                        G__13420 = null;
                                                        db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, G__13420, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                                                    } else {
                                                        Object object13 = ((IFn)pred__13422).invoke((Object)const__20, expr__13423);
                                                        if (object13 != null && object13 != Boolean.FALSE) {
                                                            G__13420 = null;
                                                            db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, G__13420, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                                                        } else {
                                                            Object object14 = ((IFn)pred__13422).invoke((Object)const__15, expr__13423);
                                                            if (object14 != null && object14 != Boolean.FALSE) {
                                                                G__13420 = null;
                                                                db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, G__13420, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                                                            } else {
                                                                Object object15 = ((IFn)pred__13422).invoke((Object)const__25, expr__13423);
                                                                if (object15 != null && object15 != Boolean.FALSE) {
                                                                    G__13420 = null;
                                                                    db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, G__13420, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                                                                } else {
                                                                    Object object16 = ((IFn)pred__13422).invoke((Object)const__18, expr__13423);
                                                                    if (object16 != null && object16 != Boolean.FALSE) {
                                                                        G__13420 = null;
                                                                        db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, G__13420, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                                                                    } else {
                                                                        Object object17 = ((IFn)pred__13422).invoke((Object)const__27, expr__13423);
                                                                        if (object17 != null && object17 != Boolean.FALSE) {
                                                                            G__13420 = null;
                                                                            db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, G__13420, this.sinceT, this.raw, this.filt, this.__meta, this.__extmap);
                                                                        } else {
                                                                            Object object18 = ((IFn)pred__13422).invoke((Object)const__24, expr__13423);
                                                                            if (object18 != null && object18 != Boolean.FALSE) {
                                                                                G__13420 = null;
                                                                                db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, G__13420, this.raw, this.filt, this.__meta, this.__extmap);
                                                                            } else {
                                                                                Object object19 = ((IFn)pred__13422).invoke((Object)const__12, expr__13423);
                                                                                if (object19 != null && object19 != Boolean.FALSE) {
                                                                                    G__13420 = null;
                                                                                    db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, G__13420, this.filt, this.__meta, this.__extmap);
                                                                                } else {
                                                                                    Object object20 = pred__13422;
                                                                                    pred__13422 = null;
                                                                                    Object object21 = expr__13423;
                                                                                    expr__13423 = null;
                                                                                    Object object22 = ((IFn)object20).invoke((Object)const__26, object21);
                                                                                    if (object22 != null && object22 != Boolean.FALSE) {
                                                                                        G__13420 = null;
                                                                                        db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, G__13420, this.__meta, this.__extmap);
                                                                                    } else {
                                                                                        k__7493__auto__ = null;
                                                                                        G__13420 = null;
                                                                                        db2 = new Db(this.id, this.memidx, this.indexing, this.mid_index, this.index, this.history, this.memlog, this.basisT, this.nextT, this.indexBasisT, this.indexingNextT, this.elements, this.keys, this.ids, this.index_root_id, this.index_rev, this.asOfT, this.sinceT, this.raw, this.filt, this.__meta, ((IFn)const__34.getRawRoot()).invoke(this.__extmap, k__7493__auto__, G__13420));
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
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return db2;
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)const__28, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__30.getRawRoot()).invoke(((IFn)const__31.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            Db this_ = null;
            object = ((IFn)const__29.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new Db(this_.id, this_.memidx, this_.indexing, this_.mid_index, this_.index, this_.history, this_.memlog, this_.basisT, this_.nextT, this_.indexBasisT, this_.indexingNextT, this_.elements, this_.keys, this_.ids, this_.index_root_id, this_.index_rev, this_.asOfT, this_.sinceT, this_.raw, this_.filt, this_.__meta, ((IFn)const__32.getRawRoot()).invoke(((IFn)const__29.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
        }
        return (IPersistentMap)object;
    }

    public int size() {
        Counted counted = (Counted)this_;
        Db this_ = null;
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
        Db this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Collection values() {
        Db db2 = this_;
        Db this_ = null;
        return (Collection)((IFn)const__1.getRawRoot()).invoke((Object)db2);
    }

    public Set entrySet() {
        Db db2 = this_;
        Db this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke((Object)db2);
    }
}

