/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.index$merge_one_index$f__15304__auto____15473$f__15304__auto____15489;
import datomic.index$merge_one_index$f__15304__auto____15473$fn__15495;
import datomic.index$merge_one_index$f__15304__auto____15473$fn__15500;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class index$merge_one_index$f__15304__auto____15473
extends AFunction {
    Object cmp;
    Object drainq;
    Object cstore;
    Object olookup;
    Object partfn;
    Object write_handlers;
    Object segs_written_ref;
    Object db;
    Object lt;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final AFn const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), Symbol.intern((String)"index", (String)"merge-one-index"), RT.keyword(null, (String)"stage"), RT.keyword(null, (String)"nil-data")});
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"into");
    public static final AFn const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), Symbol.intern((String)"index", (String)"merge-one-index"), RT.keyword(null, (String)"stage"), RT.keyword(null, (String)"nil-des")});
    public static final Var const__16 = RT.var((String)"datomic.index", (String)"build-segs");
    public static final Keyword const__17 = RT.keyword(null, (String)"else");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"fnext");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"first");
    public static final AFn const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), Symbol.intern((String)"index", (String)"merge-one-index"), RT.keyword(null, (String)"stage"), RT.keyword(null, (String)"skip")});
    public static final AFn const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), Symbol.intern((String)"index", (String)"merge-one-index"), RT.keyword(null, (String)"stage"), RT.keyword(null, (String)"merge")});
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"promise");
    public static final Var const__29 = RT.var((String)"datomic.index", (String)"fully-take-while-delivering-tail");
    public static final Var const__32 = RT.var((String)"datomic.index", (String)"bounded-count");
    public static final AFn const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), Symbol.intern((String)"index", (String)"merge-one-index"), RT.keyword(null, (String)"stage"), RT.keyword(null, (String)"build")});
    public static final Var const__38 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__39 = RT.var((String)"datomic.index", (String)"index-parallelism");
    public static final Var const__41 = RT.var((String)"clojure.core", (String)"peek");
    public static final Var const__42 = RT.var((String)"clojure.core", (String)"pop");
    public static final Var const__43 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__44 = RT.var((String)"clojure.core", (String)"future-call");
    public static final Var const__45 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__46 = RT.keyword(null, (String)"IndexWriteQueueCount");
    public static final Var const__47 = RT.var((String)"datomic.index", (String)"deref-or-throw");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"segid"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public index$merge_one_index$f__15304__auto____15473(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.cmp = object;
        this.drainq = object2;
        this.cstore = object3;
        this.olookup = object4;
        this.partfn = object5;
        this.write_handlers = object6;
        this.segs_written_ref = object7;
        this.db = object8;
        this.lt = object9;
    }

    public Object invoke(Object es, Object garbage2, Object retractions, Object des, Object data2, Object erq) {
        IPersistentVector iPersistentVector;
        block23: {
            while (true) {
                Object object;
                Object object2;
                Object erq2;
                IPersistentVector iPersistentVector2;
                Object object3;
                Object and__5236__auto__15506;
                Object object4;
                Object es2;
                Object object5;
                Object and__5236__auto__15504;
                Object retractions2;
                Object es3;
                Logger logger;
                if (Util.identical((Object)((IFn)const__1.getRawRoot()).invoke(data2), null)) {
                    logger = LoggerFactory.getLogger((String)"datomic.index");
                    if (logger.isDebugEnabled()) {
                        Logger logger2 = logger;
                        logger = null;
                        logger2.debug((String)((IFn)const__2.getRawRoot()).invoke((Object)const__7));
                    }
                    Object object6 = es;
                    es = null;
                    Object object7 = retractions;
                    retractions = null;
                    Object object8 = erq;
                    erq = null;
                    Object vec__15474 = ((IFn)this.drainq).invoke(object6, object7, object8);
                    es3 = RT.nth((Object)vec__15474, (int)RT.uncheckedIntCast((long)0L), null);
                    retractions2 = RT.nth((Object)vec__15474, (int)RT.uncheckedIntCast((long)1L), null);
                    Object object9 = vec__15474;
                    vec__15474 = null;
                    RT.nth((Object)object9, (int)RT.uncheckedIntCast((long)2L), null);
                    Object object10 = es3;
                    es3 = null;
                    Object object11 = des;
                    des = null;
                    Object object12 = garbage2;
                    garbage2 = null;
                    Object object13 = retractions2;
                    retractions2 = null;
                    iPersistentVector = Tuple.create((Object)((IFn)const__12.getRawRoot()).invoke(object10, object11), (Object)object12, (Object)object13);
                    break block23;
                }
                if (Util.identical((Object)((IFn)const__1.getRawRoot()).invoke(des), null)) {
                    logger = LoggerFactory.getLogger((String)"datomic.index");
                    if (logger.isDebugEnabled()) {
                        Logger logger3 = logger;
                        logger = null;
                        logger3.debug((String)((IFn)const__2.getRawRoot()).invoke((Object)const__15));
                    }
                    Object object14 = es;
                    es = null;
                    Object object15 = retractions;
                    retractions = null;
                    Object object16 = erq;
                    erq = null;
                    Object vec__15477 = ((IFn)this.drainq).invoke(object14, object15, object16);
                    es3 = RT.nth((Object)vec__15477, (int)RT.uncheckedIntCast((long)0L), null);
                    retractions2 = RT.nth((Object)vec__15477, (int)RT.uncheckedIntCast((long)1L), null);
                    Object object17 = vec__15477;
                    vec__15477 = null;
                    RT.nth((Object)object17, (int)RT.uncheckedIntCast((long)2L), null);
                    Object object18 = data2;
                    data2 = null;
                    Object object19 = es3;
                    es3 = null;
                    Object object20 = retractions2;
                    retractions2 = null;
                    Object vec__15480 = ((IFn)const__16.getRawRoot()).invoke(this.db, this.cstore, this.olookup, object18, object19, object20, this.partfn, this.write_handlers, this.segs_written_ref);
                    Object es4 = RT.nth((Object)vec__15480, (int)RT.uncheckedIntCast((long)0L), null);
                    Object object21 = vec__15480;
                    vec__15480 = null;
                    Object retractions3 = RT.nth((Object)object21, (int)RT.uncheckedIntCast((long)1L), null);
                    Object object22 = es4;
                    es4 = null;
                    Object object23 = garbage2;
                    garbage2 = null;
                    Object object24 = retractions3;
                    retractions3 = null;
                    iPersistentVector = Tuple.create((Object)object22, (Object)object23, (Object)object24);
                    break block23;
                }
                Keyword keyword = const__17;
                if (keyword == null || keyword == Boolean.FALSE) break;
                Object object25 = and__5236__auto__15504 = ((IFn)const__18.getRawRoot()).invoke(des);
                if (object25 != null && object25 != Boolean.FALSE) {
                    IFn iFn = (IFn)this.lt;
                    ILookupThunk iLookupThunk = __thunk__0__;
                    Object object26 = ((IFn)const__20.getRawRoot()).invoke(des);
                    Object object27 = iLookupThunk.get(object26);
                    if (iLookupThunk == object27) {
                        __thunk__0__ = __site__0__.fault(object26);
                        object27 = __thunk__0__.get(object26);
                    }
                    object5 = iFn.invoke(object27, ((IFn)const__21.getRawRoot()).invoke(data2));
                } else {
                    object5 = and__5236__auto__15504;
                    and__5236__auto__15504 = null;
                }
                if (object5 != null && object5 != Boolean.FALSE) {
                    index$merge_one_index$f__15304__auto____15473$f__15304__auto____15489 f__15304__auto__15505;
                    logger = LoggerFactory.getLogger((String)"datomic.index");
                    if (logger.isDebugEnabled()) {
                        Logger logger4 = logger;
                        logger = null;
                        logger4.debug((String)((IFn)const__2.getRawRoot()).invoke((Object)const__24));
                    }
                    Object d = ((IFn)const__21.getRawRoot()).invoke(data2);
                    Object object28 = es;
                    es = null;
                    Object object29 = retractions;
                    retractions = null;
                    Object object30 = erq;
                    erq = null;
                    Object vec__15483 = ((IFn)this.drainq).invoke(object28, object29, object30);
                    Object es5 = RT.nth((Object)vec__15483, (int)RT.uncheckedIntCast((long)0L), null);
                    Object retractions4 = RT.nth((Object)vec__15483, (int)RT.uncheckedIntCast((long)1L), null);
                    Object object31 = vec__15483;
                    vec__15483 = null;
                    Object erq3 = RT.nth((Object)object31, (int)RT.uncheckedIntCast((long)2L), null);
                    Object object32 = d;
                    d = null;
                    index$merge_one_index$f__15304__auto____15473$f__15304__auto____15489 index$merge_one_index$f__15304__auto____15473$f__15304__auto____15489 = f__15304__auto__15505 = new index$merge_one_index$f__15304__auto____15473$f__15304__auto____15489(object32, this.lt);
                    f__15304__auto__15505 = null;
                    Object object33 = es5;
                    es5 = null;
                    Object object34 = des;
                    des = null;
                    Object vec__15486 = ((IFn)index$merge_one_index$f__15304__auto____15473$f__15304__auto____15489).invoke(object33, object34);
                    es2 = RT.nth((Object)vec__15486, (int)RT.uncheckedIntCast((long)0L), null);
                    Object object35 = vec__15486;
                    vec__15486 = null;
                    Object des2 = RT.nth((Object)object35, (int)RT.uncheckedIntCast((long)1L), null);
                    Object object36 = es2;
                    es2 = null;
                    Object object37 = garbage2;
                    garbage2 = null;
                    Object object38 = retractions4;
                    retractions4 = null;
                    Object object39 = des2;
                    des2 = null;
                    Object object40 = data2;
                    data2 = null;
                    Object object41 = erq3;
                    erq3 = null;
                    erq = object41;
                    data2 = object40;
                    des = object39;
                    retractions = object38;
                    garbage2 = object37;
                    es = object36;
                    continue;
                }
                logger = LoggerFactory.getLogger((String)"datomic.index");
                if (logger.isDebugEnabled()) {
                    Logger logger5 = logger;
                    logger = null;
                    logger5.debug((String)((IFn)const__2.getRawRoot()).invoke((Object)const__27));
                }
                Object tailp = ((IFn)const__28.getRawRoot()).invoke();
                IFn iFn = (IFn)const__1.getRawRoot();
                Object object42 = ((IFn)const__18.getRawRoot()).invoke(des);
                if (object42 != null && object42 != Boolean.FALSE) {
                    Object object43 = data2;
                    data2 = null;
                    object4 = ((IFn)const__29.getRawRoot()).invoke(tailp, (Object)new index$merge_one_index$f__15304__auto____15473$fn__15495(des, this.lt), object43);
                } else {
                    object4 = data2;
                    data2 = null;
                }
                Object insert_data = iFn.invoke(object4);
                long lookahead = 10000L;
                Object v50 = Util.equiv((Object)((IFn)const__32.getRawRoot()).invoke((Object)Numbers.num((long)lookahead), insert_data), (long)lookahead) ? null : null;
                Object object44 = and__5236__auto__15506 = des;
                if (object44 != null && object44 != Boolean.FALSE) {
                    ILookupThunk iLookupThunk = __thunk__1__;
                    Object object45 = ((IFn)const__21.getRawRoot()).invoke(des);
                    object3 = iLookupThunk.get(object45);
                    if (iLookupThunk == object3) {
                        __thunk__1__ = __site__1__.fault(object45);
                        object3 = __thunk__1__.get(object45);
                    }
                } else {
                    object3 = and__5236__auto__15506;
                    and__5236__auto__15506 = null;
                }
                Object segid = object3;
                Logger logger6 = LoggerFactory.getLogger((String)"datomic.index");
                if (logger6.isDebugEnabled()) {
                    Logger logger7 = logger6;
                    logger6 = null;
                    logger7.debug((String)((IFn)const__2.getRawRoot()).invoke((Object)const__36));
                }
                if (Numbers.lte((Object)((IFn)const__38.getRawRoot()).invoke(const__39.getRawRoot()), (long)RT.count((Object)erq))) {
                    Object vec__15497 = ((IFn)const__38.getRawRoot()).invoke(((IFn)const__41.getRawRoot()).invoke(erq));
                    Object nes = RT.nth((Object)vec__15497, (int)RT.uncheckedIntCast((long)0L), null);
                    Object object46 = vec__15497;
                    vec__15497 = null;
                    Object nrs = RT.nth((Object)object46, (int)RT.uncheckedIntCast((long)1L), null);
                    Object object47 = es;
                    es = null;
                    Object object48 = nes;
                    nes = null;
                    Object object49 = retractions;
                    retractions = null;
                    Object object50 = nrs;
                    nrs = null;
                    Object object51 = erq;
                    erq = null;
                    iPersistentVector2 = Tuple.create((Object)((IFn)const__12.getRawRoot()).invoke(object47, object48), (Object)((IFn)const__12.getRawRoot()).invoke(object49, object50), (Object)((IFn)const__42.getRawRoot()).invoke(object51));
                } else {
                    Object object52 = es;
                    es = null;
                    Object object53 = retractions;
                    retractions = null;
                    Object object54 = erq;
                    erq = null;
                    iPersistentVector2 = Tuple.create((Object)object52, (Object)object53, (Object)object54);
                }
                IPersistentVector vec__15492 = iPersistentVector2;
                es2 = RT.nth((Object)vec__15492, (int)RT.uncheckedIntCast((long)0L), null);
                Object retractions5 = RT.nth((Object)vec__15492, (int)RT.uncheckedIntCast((long)1L), null);
                IPersistentVector iPersistentVector3 = vec__15492;
                vec__15492 = null;
                Object object55 = erq2 = RT.nth((Object)iPersistentVector3, (int)RT.uncheckedIntCast((long)2L), null);
                erq2 = null;
                Object object56 = insert_data;
                insert_data = null;
                Object erq4 = ((IFn)const__43.getRawRoot()).invoke(object55, ((IFn)const__44.getRawRoot()).invoke((Object)new index$merge_one_index$f__15304__auto____15473$fn__15500(this.cmp, this.cstore, retractions5, object56, this.olookup, tailp, this.partfn, this.write_handlers, this.segs_written_ref, this.db, segid)));
                ((IFn)const__45.getRawRoot()).invoke((Object)const__46, (Object)RT.count((Object)erq4));
                Object object57 = es2;
                es2 = null;
                Object object58 = segid;
                if (object58 != null && object58 != Boolean.FALSE) {
                    Object object59 = garbage2;
                    garbage2 = null;
                    Object object60 = segid;
                    segid = null;
                    object2 = ((IFn)const__43.getRawRoot()).invoke(object59, object60);
                } else {
                    object2 = garbage2;
                    garbage2 = null;
                }
                Object object61 = retractions5;
                retractions5 = null;
                Object object62 = ((IFn)const__18.getRawRoot()).invoke(des);
                Object object63 = des;
                des = null;
                Object object64 = ((IFn)const__18.getRawRoot()).invoke(object63);
                if (object64 != null && object64 != Boolean.FALSE) {
                    Object object65 = tailp;
                    tailp = null;
                    object = ((IFn)const__47.getRawRoot()).invoke(object65);
                } else {
                    object = null;
                }
                Object object66 = erq4;
                erq4 = null;
                erq = object66;
                data2 = object;
                des = object62;
                retractions = object61;
                garbage2 = object2;
                es = object57;
            }
            iPersistentVector = null;
        }
        return iPersistentVector;
    }
}

