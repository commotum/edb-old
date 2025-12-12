/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.index$paired_assertion_state$fn__15671;
import datomic.index$paired_assertion_state$fn__15678;
import datomic.index$paired_assertion_state$fn__15683;

public final class index$paired_assertion_state
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"cons");
    public static final Keyword const__5 = RT.keyword(null, (String)"absent");
    public static final Var const__6 = RT.var((String)"datomic.index", (String)"retract-assert-pair?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"not=");
    public static final Keyword const__10 = RT.keyword(null, (String)"segmented");
    public static final Keyword const__11 = RT.keyword(null, (String)"present");
    public static final Keyword const__12 = RT.keyword(null, (String)"else");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__16 = RT.keyword(null, (String)"e");
    public static final Keyword const__17 = RT.keyword(null, (String)"a");
    public static final Keyword const__18 = RT.keyword(null, (String)"v");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"take-while");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"some");
    public static final Keyword const__21 = RT.keyword(null, (String)"separated");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"item"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"item"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"segid"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"segid"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"item"));
    static ILookupThunk __thunk__4__ = __site__4__;

    public static Object invokeStatic(Object sd1, Object p__15657) {
        Keyword keyword;
        Object G__15667;
        Object object = p__15657;
        p__15657 = null;
        Object vec__15658 = object;
        RT.nth((Object)vec__15658, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__15658;
        vec__15658 = null;
        Object more = object2;
        Object object3 = sd1;
        sd1 = null;
        Object object4 = more;
        more = null;
        Object vec__15668 = G__15667 = ((IFn)const__2.getRawRoot()).invoke(object3, object4);
        RT.nth((Object)vec__15668, (int)RT.uncheckedIntCast((long)0L), null);
        RT.nth((Object)vec__15668, (int)RT.uncheckedIntCast((long)1L), null);
        vec__15668 = null;
        Object object5 = G__15667;
        G__15667 = null;
        Object vec__15661 = ((IFn)new index$paired_assertion_state$fn__15671(object5)).invoke();
        Object sd12 = RT.nth((Object)vec__15661, (int)RT.uncheckedIntCast((long)0L), null);
        Object sd2 = RT.nth((Object)vec__15661, (int)RT.uncheckedIntCast((long)1L), null);
        Object object6 = vec__15661;
        vec__15661 = null;
        Object more2 = object6;
        if (Util.identical((Object)sd2, null)) {
            keyword = const__5;
        } else {
            IFn iFn = (IFn)const__6.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object7 = sd12;
            Object object8 = iLookupThunk.get(object7);
            if (iLookupThunk == object8) {
                __thunk__0__ = __site__0__.fault(object7);
                object8 = __thunk__0__.get(object7);
            }
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object9 = sd2;
            Object object10 = iLookupThunk2.get(object9);
            if (iLookupThunk2 == object10) {
                __thunk__1__ = __site__1__.fault(object9);
                object10 = __thunk__1__.get(object9);
            }
            Object object11 = iFn.invoke(object8, object10);
            if (object11 != null && object11 != Boolean.FALSE) {
                IFn iFn2 = (IFn)const__8.getRawRoot();
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object12 = sd12;
                sd12 = null;
                Object object13 = iLookupThunk3.get(object12);
                if (iLookupThunk3 == object13) {
                    __thunk__2__ = __site__2__.fault(object12);
                    object13 = __thunk__2__.get(object12);
                }
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object14 = sd2;
                sd2 = null;
                Object object15 = iLookupThunk4.get(object14);
                if (iLookupThunk4 == object15) {
                    __thunk__3__ = __site__3__.fault(object14);
                    object15 = __thunk__3__.get(object14);
                }
                Object object16 = iFn2.invoke(object13, object15);
                keyword = object16 != null && object16 != Boolean.FALSE ? const__10 : const__11;
            } else {
                Keyword keyword2 = const__12;
                if (keyword2 != null && keyword2 != Boolean.FALSE) {
                    Object object17;
                    ILookupThunk iLookupThunk5 = __thunk__4__;
                    Object object18 = sd12;
                    Object object19 = iLookupThunk5.get(object18);
                    if (iLookupThunk5 == object19) {
                        __thunk__4__ = __site__4__.fault(object18);
                        object19 = __thunk__4__.get(object18);
                    }
                    Object map__15676 = object19;
                    Object object20 = ((IFn)const__13.getRawRoot()).invoke(map__15676);
                    if (object20 != null && object20 != Boolean.FALSE) {
                        Object object21 = map__15676;
                        map__15676 = null;
                        object17 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__14.getRawRoot()).invoke(object21)));
                    } else {
                        object17 = map__15676;
                        map__15676 = null;
                    }
                    Object map__156762 = object17;
                    Object e = RT.get((Object)map__156762, (Object)const__16);
                    Object a = RT.get((Object)map__156762, (Object)const__17);
                    Object object22 = map__156762;
                    map__156762 = null;
                    Object v = RT.get((Object)object22, (Object)const__18);
                    Object object23 = e;
                    e = null;
                    Object object24 = v;
                    v = null;
                    Object object25 = a;
                    a = null;
                    Object object26 = more2;
                    more2 = null;
                    Object lookahead = ((IFn)const__14.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke((Object)new index$paired_assertion_state$fn__15678(object23, object24, object25), object26));
                    Object object27 = sd12;
                    sd12 = null;
                    Object object28 = lookahead;
                    lookahead = null;
                    Object object29 = ((IFn)const__20.getRawRoot()).invoke((Object)new index$paired_assertion_state$fn__15683(object27), object28);
                    keyword = object29 != null && object29 != Boolean.FALSE ? const__21 : const__5;
                } else {
                    keyword = null;
                }
            }
        }
        return keyword;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$paired_assertion_state.invokeStatic(object3, object4);
    }
}

