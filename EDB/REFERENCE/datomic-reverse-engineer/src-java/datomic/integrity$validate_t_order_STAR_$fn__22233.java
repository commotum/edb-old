/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.Arrays;

public final class integrity$validate_t_order_STAR_$fn__22233
extends AFunction {
    Object log;
    Object progress;
    public static final Keyword const__3 = RT.keyword(null, (String)"n");
    public static final Keyword const__4 = RT.keyword(null, (String)"t");
    public static final Var const__5 = RT.var((String)"datomic.api", (String)"t->tx");
    public static final Var const__6 = RT.var((String)"datomic.log", (String)"max-eidx");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"seq");
    public static final Object const__12 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"="), ((IObj)PersistentList.create(Arrays.asList(RT.keyword(null, (String)"tx"), Symbol.intern(null, (String)"d")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 25})), Symbol.intern(null, (String)"tx")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 22}));
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__14 = RT.keyword(null, (String)"bindings");
    public static final AFn const__15 = (AFn)Symbol.intern(null, (String)"max-eidx");
    public static final AFn const__16 = (AFn)Symbol.intern(null, (String)"seq_22237");
    public static final AFn const__17 = (AFn)Symbol.intern(null, (String)"vec__22234");
    public static final AFn const__18 = (AFn)Symbol.intern(null, (String)"progress");
    public static final AFn const__19 = (AFn)Symbol.intern(null, (String)"log");
    public static final AFn const__20 = (AFn)Symbol.intern(null, (String)"ctr");
    public static final AFn const__21 = (AFn)Symbol.intern(null, (String)"tx1");
    public static final AFn const__22 = (AFn)Symbol.intern(null, (String)"tx");
    public static final AFn const__23 = (AFn)Symbol.intern(null, (String)"tx2");
    public static final AFn const__24 = (AFn)Symbol.intern(null, (String)"count_22239");
    public static final AFn const__25 = (AFn)Symbol.intern(null, (String)"p__22232");
    public static final AFn const__26 = (AFn)((IObj)Symbol.intern(null, (String)"chunk_22238")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"clojure.lang.IChunk")}));
    public static final AFn const__27 = (AFn)Symbol.intern(null, (String)"i_22240");
    public static final AFn const__28 = (AFn)Symbol.intern(null, (String)"d");
    public static final Keyword const__29 = RT.keyword(null, (String)"form");
    public static final Var const__30 = RT.var((String)"datomic.assert", (String)"*assert-handler*");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__33 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__34 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__37 = RT.var((String)"clojure.core", (String)"first");
    public static final AFn const__38 = (AFn)Symbol.intern(null, (String)"temp__5457__auto__");
    public static final Var const__39 = RT.var((String)"clojure.core", (String)"next");
    public static final Object const__40 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"<"), ((IObj)PersistentList.create(Arrays.asList(RT.keyword(null, (String)"t"), Symbol.intern(null, (String)"tx1")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 25})), ((IObj)PersistentList.create(Arrays.asList(RT.keyword(null, (String)"t"), Symbol.intern(null, (String)"tx2")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 34}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 22}));
    public static final Object const__43 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"<="), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"inc"), Symbol.intern(null, (String)"max-eidx")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 26})), ((IObj)PersistentList.create(Arrays.asList(RT.keyword(null, (String)"t"), Symbol.intern(null, (String)"tx2")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 41}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 22}));
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"data"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"data"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"tx"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"tx"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__7__ = __site__7__;
    static final KeywordLookupSite __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__8__ = __site__8__;

    public integrity$validate_t_order_STAR_$fn__22233(Object object, Object object2) {
        this.log = object;
        this.progress = object2;
    }

    public Object invoke(Object ctr, Object p__22232) {
        Object vec__22234 = p__22232;
        Object tx1 = RT.nth((Object)vec__22234, (int)RT.intCast((long)0L), null);
        Object tx2 = RT.nth((Object)vec__22234, (int)RT.intCast((long)1L), null);
        Object object = this_.progress;
        if (object != null && object != Boolean.FALSE) {
            IFn iFn = (IFn)this_.progress;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__3;
            objectArray[1] = ctr;
            objectArray[2] = const__4;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object2 = tx1;
            Object object3 = iLookupThunk.get(object2);
            if (iLookupThunk == object3) {
                __thunk__0__ = __site__0__.fault(object2);
                object3 = __thunk__0__.get(object2);
            }
            objectArray[3] = object3;
            iFn.invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        IFn.LO lO = (IFn.LO)const__5.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__1__;
        Object object4 = tx1;
        Object object5 = iLookupThunk.get(object4);
        if (iLookupThunk == object5) {
            __thunk__1__ = __site__1__.fault(object4);
            object5 = __thunk__1__.get(object4);
        }
        Object tx = lO.invokePrim(RT.longCast((Object)((Number)object5)));
        IFn iFn = (IFn)const__6.getRawRoot();
        ILookupThunk iLookupThunk2 = __thunk__2__;
        Object object6 = tx1;
        Object object7 = iLookupThunk2.get(object6);
        if (iLookupThunk2 == object7) {
            __thunk__2__ = __site__2__.fault(object6);
            object7 = __thunk__2__.get(object6);
        }
        Object max_eidx2 = iFn.invoke(object7);
        IFn iFn2 = (IFn)const__8.getRawRoot();
        ILookupThunk iLookupThunk3 = __thunk__3__;
        Object object8 = tx1;
        Object object9 = iLookupThunk3.get(object8);
        if (iLookupThunk3 == object9) {
            __thunk__3__ = __site__3__.fault(object8);
            object9 = __thunk__3__.get(object8);
        }
        Object seq_22237 = iFn2.invoke(object9);
        Object chunk_22238 = null;
        long count_22239 = 0L;
        long i_22240 = 0L;
        while (true) {
            Object temp__5457__auto__22247;
            if (i_22240 < count_22239) {
                Object d = ((Indexed)chunk_22238).nth(RT.intCast((long)i_22240));
                ILookupThunk iLookupThunk4 = __thunk__4__;
                Object object10 = d;
                Object object11 = iLookupThunk4.get(object10);
                if (iLookupThunk4 == object11) {
                    __thunk__4__ = __site__4__.fault(object10);
                    object11 = __thunk__4__.get(object10);
                }
                if (Util.equiv((Object)object11, (Object)tx)) {
                } else {
                    Object form__20659__auto__22242 = const__12;
                    Object[] objectArray = new Object[4];
                    objectArray[0] = const__14;
                    Object[] objectArray2 = new Object[28];
                    objectArray2[0] = const__15;
                    objectArray2[1] = max_eidx2;
                    objectArray2[2] = const__16;
                    objectArray2[3] = seq_22237;
                    objectArray2[4] = const__17;
                    objectArray2[5] = vec__22234;
                    objectArray2[6] = const__18;
                    objectArray2[7] = this_.progress;
                    objectArray2[8] = const__19;
                    objectArray2[9] = this_.log;
                    objectArray2[10] = const__20;
                    objectArray2[11] = ctr;
                    objectArray2[12] = const__21;
                    objectArray2[13] = tx1;
                    objectArray2[14] = const__22;
                    objectArray2[15] = tx;
                    objectArray2[16] = const__23;
                    objectArray2[17] = tx2;
                    objectArray2[18] = const__24;
                    objectArray2[19] = Numbers.num((long)count_22239);
                    objectArray2[20] = const__25;
                    objectArray2[21] = p__22232;
                    objectArray2[22] = const__26;
                    objectArray2[23] = chunk_22238;
                    objectArray2[24] = const__27;
                    objectArray2[25] = Numbers.num((long)i_22240);
                    objectArray2[26] = const__28;
                    Object object12 = d;
                    d = null;
                    objectArray2[27] = object12;
                    objectArray[1] = RT.mapUniqueKeys((Object[])objectArray2);
                    objectArray[2] = const__29;
                    Object object13 = form__20659__auto__22242;
                    form__20659__auto__22242 = null;
                    objectArray[3] = object13;
                    Object error__20660__auto__22243 = ((IFn)const__13.getRawRoot()).invoke((Object)"not all datoms in transaction have same tx", (Object)RT.mapUniqueKeys((Object[])objectArray));
                    Object object14 = const__30.get();
                    if (object14 != null && object14 != Boolean.FALSE) {
                        Object object15 = error__20660__auto__22243;
                        error__20660__auto__22243 = null;
                        ((IFn)const__30.get()).invoke(object15);
                    } else {
                        Object object16 = error__20660__auto__22243;
                        error__20660__auto__22243 = null;
                        throw (Throwable)object16;
                    }
                }
                Object object17 = seq_22237;
                seq_22237 = null;
                Object object18 = chunk_22238;
                chunk_22238 = null;
                ++i_22240;
                chunk_22238 = object18;
                seq_22237 = object17;
                continue;
            }
            Object object19 = seq_22237;
            seq_22237 = null;
            Object object20 = temp__5457__auto__22247 = ((IFn)const__8.getRawRoot()).invoke(object19);
            if (object20 == null || object20 == Boolean.FALSE) break;
            Object seq_222372 = temp__5457__auto__22247;
            Object object21 = ((IFn)const__32.getRawRoot()).invoke(seq_222372);
            if (object21 != null && object21 != Boolean.FALSE) {
                Object c__5719__auto__22244 = ((IFn)const__33.getRawRoot()).invoke(seq_222372);
                Object object22 = seq_222372;
                seq_222372 = null;
                Object object23 = c__5719__auto__22244;
                Object object24 = c__5719__auto__22244;
                c__5719__auto__22244 = null;
                i_22240 = RT.intCast((long)0L);
                count_22239 = RT.intCast((int)RT.count((Object)object24));
                chunk_22238 = object23;
                seq_22237 = ((IFn)const__34.getRawRoot()).invoke(object22);
                continue;
            }
            Object d = ((IFn)const__37.getRawRoot()).invoke(seq_222372);
            ILookupThunk iLookupThunk5 = __thunk__5__;
            Object object25 = d;
            Object object26 = iLookupThunk5.get(object25);
            if (iLookupThunk5 == object26) {
                __thunk__5__ = __site__5__.fault(object25);
                object26 = __thunk__5__.get(object25);
            }
            if (Util.equiv((Object)object26, (Object)tx)) {
            } else {
                Object form__20659__auto__22245 = const__12;
                Object[] objectArray = new Object[4];
                objectArray[0] = const__14;
                Object[] objectArray3 = new Object[30];
                objectArray3[0] = const__15;
                objectArray3[1] = max_eidx2;
                objectArray3[2] = const__16;
                objectArray3[3] = seq_222372;
                objectArray3[4] = const__17;
                objectArray3[5] = vec__22234;
                objectArray3[6] = const__18;
                objectArray3[7] = this_.progress;
                objectArray3[8] = const__19;
                objectArray3[9] = this_.log;
                objectArray3[10] = const__20;
                objectArray3[11] = ctr;
                objectArray3[12] = const__38;
                Object object27 = temp__5457__auto__22247;
                temp__5457__auto__22247 = null;
                objectArray3[13] = object27;
                objectArray3[14] = const__21;
                objectArray3[15] = tx1;
                objectArray3[16] = const__22;
                objectArray3[17] = tx;
                objectArray3[18] = const__23;
                objectArray3[19] = tx2;
                objectArray3[20] = const__24;
                objectArray3[21] = Numbers.num((long)count_22239);
                objectArray3[22] = const__25;
                objectArray3[23] = p__22232;
                objectArray3[24] = const__26;
                Object object28 = chunk_22238;
                chunk_22238 = null;
                objectArray3[25] = object28;
                objectArray3[26] = const__27;
                objectArray3[27] = Numbers.num((long)i_22240);
                objectArray3[28] = const__28;
                Object object29 = d;
                d = null;
                objectArray3[29] = object29;
                objectArray[1] = RT.mapUniqueKeys((Object[])objectArray3);
                objectArray[2] = const__29;
                Object object30 = form__20659__auto__22245;
                form__20659__auto__22245 = null;
                objectArray[3] = object30;
                Object error__20660__auto__22246 = ((IFn)const__13.getRawRoot()).invoke((Object)"not all datoms in transaction have same tx", (Object)RT.mapUniqueKeys((Object[])objectArray));
                Object object31 = const__30.get();
                if (object31 != null && object31 != Boolean.FALSE) {
                    Object object32 = error__20660__auto__22246;
                    error__20660__auto__22246 = null;
                    ((IFn)const__30.get()).invoke(object32);
                } else {
                    Object object33 = error__20660__auto__22246;
                    error__20660__auto__22246 = null;
                    throw (Throwable)object33;
                }
            }
            Object object34 = seq_222372;
            seq_222372 = null;
            i_22240 = 0L;
            count_22239 = 0L;
            chunk_22238 = null;
            seq_22237 = ((IFn)const__39.getRawRoot()).invoke(object34);
        }
        Object object35 = tx2;
        if (object35 != null && object35 != Boolean.FALSE) {
            ILookupThunk iLookupThunk6 = __thunk__6__;
            Object object36 = tx1;
            Object object37 = iLookupThunk6.get(object36);
            if (iLookupThunk6 == object37) {
                __thunk__6__ = __site__6__.fault(object36);
                object37 = __thunk__6__.get(object36);
            }
            ILookupThunk iLookupThunk7 = __thunk__7__;
            Object object38 = tx2;
            Object object39 = iLookupThunk7.get(object38);
            if (iLookupThunk7 == object39) {
                __thunk__7__ = __site__7__.fault(object38);
                object39 = __thunk__7__.get(object38);
            }
            if (Numbers.lt((Object)object37, (Object)object39)) {
            } else {
                Object form__20659__auto__22248 = const__40;
                Object[] objectArray = new Object[4];
                objectArray[0] = const__14;
                objectArray[1] = RT.mapUniqueKeys((Object[])new Object[]{const__19, this_.log, const__18, this_.progress, const__20, ctr, const__25, p__22232, const__17, vec__22234, const__21, tx1, const__23, tx2, const__22, tx, const__15, max_eidx2});
                objectArray[2] = const__29;
                Object object40 = form__20659__auto__22248;
                form__20659__auto__22248 = null;
                objectArray[3] = object40;
                Object error__20660__auto__22249 = ((IFn)const__13.getRawRoot()).invoke((Object)"txes are not ascending", (Object)RT.mapUniqueKeys((Object[])objectArray));
                Object object41 = const__30.get();
                if (object41 != null && object41 != Boolean.FALSE) {
                    Object object42 = error__20660__auto__22249;
                    error__20660__auto__22249 = null;
                    ((IFn)const__30.get()).invoke(object42);
                } else {
                    Object object43 = error__20660__auto__22249;
                    error__20660__auto__22249 = null;
                    throw (Throwable)object43;
                }
            }
            Number number = Numbers.inc((Object)max_eidx2);
            ILookupThunk iLookupThunk8 = __thunk__8__;
            Object object44 = tx2;
            Object object45 = iLookupThunk8.get(object44);
            if (iLookupThunk8 == object45) {
                __thunk__8__ = __site__8__.fault(object44);
                object45 = __thunk__8__.get(object44);
            }
            if (Numbers.lte((Object)number, (Object)object45)) {
            } else {
                Object form__20659__auto__22250 = const__43;
                Object[] objectArray = new Object[4];
                objectArray[0] = const__14;
                Object[] objectArray4 = new Object[18];
                objectArray4[0] = const__19;
                objectArray4[1] = this_.log;
                objectArray4[2] = const__18;
                objectArray4[3] = this_.progress;
                objectArray4[4] = const__20;
                objectArray4[5] = ctr;
                objectArray4[6] = const__25;
                Object object46 = p__22232;
                p__22232 = null;
                objectArray4[7] = object46;
                objectArray4[8] = const__17;
                Object object47 = vec__22234;
                vec__22234 = null;
                objectArray4[9] = object47;
                objectArray4[10] = const__21;
                Object object48 = tx1;
                tx1 = null;
                objectArray4[11] = object48;
                objectArray4[12] = const__23;
                Object object49 = tx2;
                tx2 = null;
                objectArray4[13] = object49;
                objectArray4[14] = const__22;
                Object object50 = tx;
                tx = null;
                objectArray4[15] = object50;
                objectArray4[16] = const__15;
                Object object51 = max_eidx2;
                max_eidx2 = null;
                objectArray4[17] = object51;
                objectArray[1] = RT.mapUniqueKeys((Object[])objectArray4);
                objectArray[2] = const__29;
                Object object52 = form__20659__auto__22250;
                form__20659__auto__22250 = null;
                objectArray[3] = object52;
                Object error__20660__auto__22251 = ((IFn)const__13.getRawRoot()).invoke((Object)"entity t too high for tx", (Object)RT.mapUniqueKeys((Object[])objectArray));
                Object object53 = const__30.get();
                if (object53 != null && object53 != Boolean.FALSE) {
                    Object object54 = error__20660__auto__22251;
                    error__20660__auto__22251 = null;
                    ((IFn)const__30.get()).invoke(object54);
                } else {
                    Object object55 = error__20660__auto__22251;
                    error__20660__auto__22251 = null;
                    throw (Throwable)object55;
                }
            }
        }
        Object object56 = ctr;
        ctr = null;
        integrity$validate_t_order_STAR_$fn__22233 this_ = null;
        return Numbers.inc((Object)object56);
    }
}

