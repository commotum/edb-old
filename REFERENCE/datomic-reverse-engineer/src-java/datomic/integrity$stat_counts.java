/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class integrity$stat_counts
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.stats", (String)"aevt");
    public static final Var const__4 = RT.var((String)"datomic.stats", (String)"avet");
    public static final Var const__5 = RT.var((String)"datomic.api", (String)"attribute");
    public static final Keyword const__8 = RT.keyword(null, (String)"aevt-total");
    public static final Object const__10 = 0L;
    public static final Keyword const__11 = RT.keyword(null, (String)"aevt");
    public static final Keyword const__12 = RT.keyword(null, (String)"avet-total");
    public static final Keyword const__13 = RT.keyword(null, (String)"avet");
    public static final Keyword const__14 = RT.keyword(null, (String)"avet-mid");
    public static final Keyword const__15 = RT.keyword(null, (String)"kw");
    public static final Keyword const__16 = RT.keyword(null, (String)"avet-hist");
    public static final Keyword const__17 = RT.keyword(null, (String)"aevt-mid");
    public static final Keyword const__18 = RT.keyword(null, (String)"aevt-hist");
    public static final Keyword const__19 = RT.keyword(null, (String)"attr");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"index"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"mid-index"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"history"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"index"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"mid-index"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"history"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"ident"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"data-count"));
    static ILookupThunk __thunk__7__ = __site__7__;
    static final KeywordLookupSite __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"data-count"));
    static ILookupThunk __thunk__8__ = __site__8__;
    static final KeywordLookupSite __site__9__ = new KeywordLookupSite(RT.keyword(null, (String)"data-count"));
    static ILookupThunk __thunk__9__ = __site__9__;
    static final KeywordLookupSite __site__10__ = new KeywordLookupSite(RT.keyword(null, (String)"data-count"));
    static ILookupThunk __thunk__10__ = __site__10__;
    static final KeywordLookupSite __site__11__ = new KeywordLookupSite(RT.keyword(null, (String)"data-count"));
    static ILookupThunk __thunk__11__ = __site__11__;
    static final KeywordLookupSite __site__12__ = new KeywordLookupSite(RT.keyword(null, (String)"data-count"));
    static ILookupThunk __thunk__12__ = __site__12__;

    public static Object invokeStatic(Object db2, Object a) {
        Object object;
        Object or__5238__auto__22477;
        Object object2;
        Object or__5238__auto__22476;
        Object object3;
        Object or__5238__auto__22475;
        Object object4;
        Object or__5238__auto__22474;
        Object object5;
        Object or__5238__auto__22473;
        Object object6;
        Object or__5238__auto__22472;
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = db2;
        Object object8 = iLookupThunk.get(object7);
        if (iLookupThunk == object8) {
            __thunk__0__ = __site__0__.fault(object7);
            object8 = __thunk__0__.get(object7);
        }
        Object aevt2 = iFn.invoke(db2, object8);
        IFn iFn2 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object9 = db2;
        Object object10 = iLookupThunk2.get(object9);
        if (iLookupThunk2 == object10) {
            __thunk__1__ = __site__1__.fault(object9);
            object10 = __thunk__1__.get(object9);
        }
        Object aevt_mid = iFn2.invoke(db2, object10);
        IFn iFn3 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object11 = db2;
        Object object12 = iLookupThunk3.get(object11);
        if (iLookupThunk3 == object12) {
            __thunk__2__ = __site__2__.fault(object11);
            object12 = __thunk__2__.get(object11);
        }
        Object aevt_hist = iFn3.invoke(db2, object12);
        IFn iFn4 = (IFn)const__4.getRawRoot();
        ILookupThunk iLookupThunk4 = __thunk__3__;
        Object object13 = db2;
        Object object14 = iLookupThunk4.get(object13);
        if (iLookupThunk4 == object14) {
            __thunk__3__ = __site__3__.fault(object13);
            object14 = __thunk__3__.get(object13);
        }
        Object avet2 = iFn4.invoke(db2, object14);
        IFn iFn5 = (IFn)const__4.getRawRoot();
        ILookupThunk iLookupThunk5 = __thunk__4__;
        Object object15 = db2;
        Object object16 = iLookupThunk5.get(object15);
        if (iLookupThunk5 == object16) {
            __thunk__4__ = __site__4__.fault(object15);
            object16 = __thunk__4__.get(object15);
        }
        Object avet_mid = iFn5.invoke(db2, object16);
        IFn iFn6 = (IFn)const__4.getRawRoot();
        ILookupThunk iLookupThunk6 = __thunk__5__;
        Object object17 = db2;
        Object object18 = iLookupThunk6.get(object17);
        if (iLookupThunk6 == object18) {
            __thunk__5__ = __site__5__.fault(object17);
            object18 = __thunk__5__.get(object17);
        }
        Object avet_hist = iFn6.invoke(db2, object18);
        Object object19 = db2;
        db2 = null;
        Object attr = ((IFn)const__5.getRawRoot()).invoke(object19, a);
        ILookupThunk iLookupThunk7 = __thunk__6__;
        Object object20 = attr;
        attr = null;
        Object object21 = iLookupThunk7.get(object20);
        if (iLookupThunk7 == object21) {
            __thunk__6__ = __site__6__.fault(object20);
            object21 = __thunk__6__.get(object20);
        }
        Object kw = object21;
        ILookupThunk iLookupThunk8 = __thunk__7__;
        Object object22 = aevt2;
        aevt2 = null;
        Object object23 = ((IFn)kw).invoke(object22);
        Object object24 = iLookupThunk8.get(object23);
        if (iLookupThunk8 == object24) {
            __thunk__7__ = __site__7__.fault(object23);
            object24 = __thunk__7__.get(object23);
        }
        Object aevtc = object24;
        ILookupThunk iLookupThunk9 = __thunk__8__;
        Object object25 = aevt_mid;
        aevt_mid = null;
        Object object26 = ((IFn)kw).invoke(object25);
        Object object27 = iLookupThunk9.get(object26);
        if (iLookupThunk9 == object27) {
            __thunk__8__ = __site__8__.fault(object26);
            object27 = __thunk__8__.get(object26);
        }
        Object aevt_midc = object27;
        ILookupThunk iLookupThunk10 = __thunk__9__;
        Object object28 = aevt_hist;
        aevt_hist = null;
        Object object29 = ((IFn)kw).invoke(object28);
        Object object30 = iLookupThunk10.get(object29);
        if (iLookupThunk10 == object30) {
            __thunk__9__ = __site__9__.fault(object29);
            object30 = __thunk__9__.get(object29);
        }
        Object aevt_histc = object30;
        ILookupThunk iLookupThunk11 = __thunk__10__;
        Object object31 = avet2;
        avet2 = null;
        Object object32 = ((IFn)kw).invoke(object31);
        Object object33 = iLookupThunk11.get(object32);
        if (iLookupThunk11 == object33) {
            __thunk__10__ = __site__10__.fault(object32);
            object33 = __thunk__10__.get(object32);
        }
        Object avetc = object33;
        ILookupThunk iLookupThunk12 = __thunk__11__;
        Object object34 = avet_mid;
        avet_mid = null;
        Object object35 = ((IFn)kw).invoke(object34);
        Object object36 = iLookupThunk12.get(object35);
        if (iLookupThunk12 == object36) {
            __thunk__11__ = __site__11__.fault(object35);
            object36 = __thunk__11__.get(object35);
        }
        Object avet_midc = object36;
        ILookupThunk iLookupThunk13 = __thunk__12__;
        Object object37 = avet_hist;
        avet_hist = null;
        Object object38 = ((IFn)kw).invoke(object37);
        Object object39 = iLookupThunk13.get(object38);
        if (iLookupThunk13 == object39) {
            __thunk__12__ = __site__12__.fault(object38);
            object39 = __thunk__12__.get(object38);
        }
        Object avet_histc = object39;
        Object[] objectArray = new Object[20];
        objectArray[0] = const__8;
        Object object40 = or__5238__auto__22472 = aevtc;
        if (object40 != null && object40 != Boolean.FALSE) {
            object6 = or__5238__auto__22472;
            or__5238__auto__22472 = null;
        } else {
            object6 = const__10;
        }
        Object object41 = or__5238__auto__22473 = aevt_midc;
        if (object41 != null && object41 != Boolean.FALSE) {
            object5 = or__5238__auto__22473;
            or__5238__auto__22473 = null;
        } else {
            object5 = const__10;
        }
        Number number = Numbers.add((Object)object6, (Object)object5);
        Object object42 = or__5238__auto__22474 = aevt_histc;
        if (object42 != null && object42 != Boolean.FALSE) {
            object4 = or__5238__auto__22474;
            or__5238__auto__22474 = null;
        } else {
            object4 = const__10;
        }
        objectArray[1] = Numbers.add((Object)number, (Object)object4);
        objectArray[2] = const__11;
        Object object43 = aevtc;
        aevtc = null;
        objectArray[3] = object43;
        objectArray[4] = const__12;
        Object object44 = or__5238__auto__22475 = avetc;
        if (object44 != null && object44 != Boolean.FALSE) {
            object3 = or__5238__auto__22475;
            or__5238__auto__22475 = null;
        } else {
            object3 = const__10;
        }
        Object object45 = or__5238__auto__22476 = avet_midc;
        if (object45 != null && object45 != Boolean.FALSE) {
            object2 = or__5238__auto__22476;
            or__5238__auto__22476 = null;
        } else {
            object2 = const__10;
        }
        Number number2 = Numbers.add((Object)object3, (Object)object2);
        Object object46 = or__5238__auto__22477 = avet_histc;
        if (object46 != null && object46 != Boolean.FALSE) {
            object = or__5238__auto__22477;
            or__5238__auto__22477 = null;
        } else {
            object = const__10;
        }
        objectArray[5] = Numbers.add((Object)number2, (Object)object);
        objectArray[6] = const__13;
        Object object47 = avetc;
        avetc = null;
        objectArray[7] = object47;
        objectArray[8] = const__14;
        Object object48 = avet_midc;
        avet_midc = null;
        objectArray[9] = object48;
        objectArray[10] = const__15;
        Object object49 = kw;
        kw = null;
        objectArray[11] = object49;
        objectArray[12] = const__16;
        Object object50 = avet_histc;
        avet_histc = null;
        objectArray[13] = object50;
        objectArray[14] = const__17;
        Object object51 = aevt_midc;
        aevt_midc = null;
        objectArray[15] = object51;
        objectArray[16] = const__18;
        Object object52 = aevt_histc;
        aevt_histc = null;
        objectArray[17] = object52;
        objectArray[18] = const__19;
        Object object53 = a;
        a = null;
        objectArray[19] = object53;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return integrity$stat_counts.invokeStatic(object3, object4);
    }
}

