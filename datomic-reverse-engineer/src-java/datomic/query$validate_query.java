/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.List;

public final class query$validate_query
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__6 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__19 = RT.var((String)"datomic.datalog", (String)"source?");
    public static final Var const__20 = RT.var((String)"datomic.datalog", (String)"rules?");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"symbol?");
    public static final Var const__22 = RT.var((String)"datomic.datalog", (String)"callees");
    public static final Var const__24 = RT.var((String)"clojure.set", (String)"difference");
    public static final Var const__25 = RT.var((String)"datomic.query", (String)"variables-in-clause");
    public static final Var const__26 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__27 = RT.keyword((String)"db.error", (String)"unfound-construct-variables");
    public static final Keyword const__28 = RT.keyword(null, (String)"variables");
    public static final Var const__29 = RT.var((String)"clojure.set", (String)"union");
    public static final Keyword const__30 = RT.keyword((String)"db.error", (String)"unbound-query-variables");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"find"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"in"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"where"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"find"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"with"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"where"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"in"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"construct"));
    static ILookupThunk __thunk__7__ = __site__7__;
    static final KeywordLookupSite __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"construct"));
    static ILookupThunk __thunk__8__ = __site__8__;
    static final KeywordLookupSite __site__9__ = new KeywordLookupSite(RT.keyword(null, (String)"find"));
    static ILookupThunk __thunk__9__ = __site__9__;
    static final KeywordLookupSite __site__10__ = new KeywordLookupSite(RT.keyword(null, (String)"with"));
    static ILookupThunk __thunk__10__ = __site__10__;
    static final KeywordLookupSite __site__11__ = new KeywordLookupSite(RT.keyword(null, (String)"find"));
    static ILookupThunk __thunk__11__ = __site__11__;
    static final KeywordLookupSite __site__12__ = new KeywordLookupSite(RT.keyword(null, (String)"in"));
    static ILookupThunk __thunk__12__ = __site__12__;
    static final KeywordLookupSite __site__13__ = new KeywordLookupSite(RT.keyword(null, (String)"where"));
    static ILookupThunk __thunk__13__ = __site__13__;

    public static Object invokeStatic(Object qmap) {
        Object temp__5457__auto__19298;
        Object temp__5457__auto__19297;
        Object f;
        Object f2;
        Object object;
        Object or__5238__auto__19284;
        IFn iFn = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = qmap;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = iFn.invoke(object3);
        if (object4 == null || object4 == Boolean.FALSE) {
            throw (Throwable)new IllegalArgumentException("No :find clause specified");
        }
        IFn iFn2 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object5 = qmap;
        Object object6 = iLookupThunk2.get(object5);
        if (iLookupThunk2 == object6) {
            __thunk__1__ = __site__1__.fault(object5);
            object6 = __thunk__1__.get(object5);
        }
        Object object7 = or__5238__auto__19284 = iFn2.invoke(object6);
        if (object7 != null && object7 != Boolean.FALSE) {
            object = or__5238__auto__19284;
            or__5238__auto__19284 = null;
        } else {
            IFn iFn3 = (IFn)const__0.getRawRoot();
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object8 = qmap;
            Object object9 = iLookupThunk3.get(object8);
            if (iLookupThunk3 == object9) {
                __thunk__2__ = __site__2__.fault(object8);
                object9 = __thunk__2__.get(object8);
            }
            object = iFn3.invoke(object9);
        }
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)new IllegalArgumentException("No :in or :where clause specified");
        }
        IFn iFn4 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk4 = __thunk__3__;
        Object object10 = qmap;
        Object object11 = iLookupThunk4.get(object10);
        if (iLookupThunk4 == object11) {
            __thunk__3__ = __site__3__.fault(object10);
            object11 = __thunk__3__.get(object10);
        }
        Object seq_19267 = iFn4.invoke(object11);
        Object chunk_19268 = null;
        long count_19269 = 0L;
        long i_19270 = 0L;
        while (true) {
            Object temp__5457__auto__19286;
            if (i_19270 < count_19269) {
                f2 = ((Indexed)chunk_19268).nth(RT.intCast((long)i_19270));
                Object object12 = ((IFn)const__6.getRawRoot()).invoke(f2);
                if (object12 == null || object12 == Boolean.FALSE) {
                    Object object13 = f2;
                    f2 = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)const__7.getRawRoot()).invoke((Object)"Argument ", object13, (Object)" in :find is not a variable"));
                }
                Object object14 = seq_19267;
                seq_19267 = null;
                Object object15 = chunk_19268;
                chunk_19268 = null;
                ++i_19270;
                chunk_19268 = object15;
                seq_19267 = object14;
                continue;
            }
            Object object16 = seq_19267;
            seq_19267 = null;
            Object object17 = temp__5457__auto__19286 = ((IFn)const__0.getRawRoot()).invoke(object16);
            if (object17 == null || object17 == Boolean.FALSE) break;
            Object object18 = temp__5457__auto__19286;
            temp__5457__auto__19286 = null;
            Object seq_192672 = object18;
            Object object19 = ((IFn)const__9.getRawRoot()).invoke(seq_192672);
            if (object19 != null && object19 != Boolean.FALSE) {
                Object c__5719__auto__19285 = ((IFn)const__10.getRawRoot()).invoke(seq_192672);
                Object object20 = seq_192672;
                seq_192672 = null;
                Object object21 = c__5719__auto__19285;
                Object object22 = c__5719__auto__19285;
                c__5719__auto__19285 = null;
                i_19270 = RT.intCast((long)0L);
                count_19269 = RT.intCast((int)RT.count((Object)object22));
                chunk_19268 = object21;
                seq_19267 = ((IFn)const__11.getRawRoot()).invoke(object20);
                continue;
            }
            f = ((IFn)const__14.getRawRoot()).invoke(seq_192672);
            Object object23 = ((IFn)const__6.getRawRoot()).invoke(f);
            if (object23 == null || object23 == Boolean.FALSE) {
                Object object24 = f;
                f = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__7.getRawRoot()).invoke((Object)"Argument ", object24, (Object)" in :find is not a variable"));
            }
            Object object25 = seq_192672;
            seq_192672 = null;
            i_19270 = 0L;
            count_19269 = 0L;
            chunk_19268 = null;
            seq_19267 = ((IFn)const__15.getRawRoot()).invoke(object25);
        }
        IFn iFn5 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk5 = __thunk__4__;
        Object object26 = qmap;
        Object object27 = iLookupThunk5.get(object26);
        if (iLookupThunk5 == object27) {
            __thunk__4__ = __site__4__.fault(object26);
            object27 = __thunk__4__.get(object26);
        }
        Object seq_19271 = iFn5.invoke(object27);
        Object chunk_19272 = null;
        long count_19273 = 0L;
        long i_19274 = 0L;
        while (true) {
            Object temp__5457__auto__19288;
            if (i_19274 < count_19273) {
                f2 = ((Indexed)chunk_19272).nth(RT.intCast((long)i_19274));
                Object object28 = ((IFn)const__6.getRawRoot()).invoke(f2);
                if (object28 == null || object28 == Boolean.FALSE) {
                    Object object29 = f2;
                    f2 = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)const__7.getRawRoot()).invoke((Object)"Argument ", object29, (Object)" in :with is not a variable"));
                }
                Object object30 = seq_19271;
                seq_19271 = null;
                Object object31 = chunk_19272;
                chunk_19272 = null;
                ++i_19274;
                chunk_19272 = object31;
                seq_19271 = object30;
                continue;
            }
            Object object32 = seq_19271;
            seq_19271 = null;
            Object object33 = temp__5457__auto__19288 = ((IFn)const__0.getRawRoot()).invoke(object32);
            if (object33 == null || object33 == Boolean.FALSE) break;
            Object object34 = temp__5457__auto__19288;
            temp__5457__auto__19288 = null;
            Object seq_192712 = object34;
            Object object35 = ((IFn)const__9.getRawRoot()).invoke(seq_192712);
            if (object35 != null && object35 != Boolean.FALSE) {
                Object c__5719__auto__19287 = ((IFn)const__10.getRawRoot()).invoke(seq_192712);
                Object object36 = seq_192712;
                seq_192712 = null;
                Object object37 = c__5719__auto__19287;
                Object object38 = c__5719__auto__19287;
                c__5719__auto__19287 = null;
                i_19274 = RT.intCast((long)0L);
                count_19273 = RT.intCast((int)RT.count((Object)object38));
                chunk_19272 = object37;
                seq_19271 = ((IFn)const__11.getRawRoot()).invoke(object36);
                continue;
            }
            f = ((IFn)const__14.getRawRoot()).invoke(seq_192712);
            Object object39 = ((IFn)const__6.getRawRoot()).invoke(f);
            if (object39 == null || object39 == Boolean.FALSE) {
                Object object40 = f;
                f = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__7.getRawRoot()).invoke((Object)"Argument ", object40, (Object)" in :with is not a variable"));
            }
            Object object41 = seq_192712;
            seq_192712 = null;
            i_19274 = 0L;
            count_19273 = 0L;
            chunk_19272 = null;
            seq_19271 = ((IFn)const__15.getRawRoot()).invoke(object41);
        }
        IFn iFn6 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk6 = __thunk__5__;
        Object object42 = qmap;
        Object object43 = iLookupThunk6.get(object42);
        if (iLookupThunk6 == object43) {
            __thunk__5__ = __site__5__.fault(object42);
            object43 = __thunk__5__.get(object42);
        }
        Object seq_19275 = iFn6.invoke(object43);
        Object chunk_19276 = null;
        long count_19277 = 0L;
        long i_19278 = 0L;
        while (true) {
            Object temp__5457__auto__19290;
            if (i_19278 < count_19277) {
                Object w = ((Indexed)chunk_19276).nth(RT.intCast((long)i_19278));
                if (!(w instanceof List)) {
                    Object object44 = w;
                    w = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)const__7.getRawRoot()).invoke((Object)"Argument ", object44, (Object)" in :where is not a list"));
                }
                Object object45 = seq_19275;
                seq_19275 = null;
                Object object46 = chunk_19276;
                chunk_19276 = null;
                ++i_19278;
                chunk_19276 = object46;
                seq_19275 = object45;
                continue;
            }
            Object object47 = seq_19275;
            seq_19275 = null;
            Object object48 = temp__5457__auto__19290 = ((IFn)const__0.getRawRoot()).invoke(object47);
            if (object48 == null || object48 == Boolean.FALSE) break;
            Object object49 = temp__5457__auto__19290;
            temp__5457__auto__19290 = null;
            Object seq_192752 = object49;
            Object object50 = ((IFn)const__9.getRawRoot()).invoke(seq_192752);
            if (object50 != null && object50 != Boolean.FALSE) {
                Object c__5719__auto__19289 = ((IFn)const__10.getRawRoot()).invoke(seq_192752);
                Object object51 = seq_192752;
                seq_192752 = null;
                Object object52 = c__5719__auto__19289;
                Object object53 = c__5719__auto__19289;
                c__5719__auto__19289 = null;
                i_19278 = RT.intCast((long)0L);
                count_19277 = RT.intCast((int)RT.count((Object)object53));
                chunk_19276 = object52;
                seq_19275 = ((IFn)const__11.getRawRoot()).invoke(object51);
                continue;
            }
            Object w = ((IFn)const__14.getRawRoot()).invoke(seq_192752);
            if (!(w instanceof List)) {
                Object object54 = w;
                w = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__7.getRawRoot()).invoke((Object)"Argument ", object54, (Object)" in :where is not a list"));
            }
            Object object55 = seq_192752;
            seq_192752 = null;
            i_19278 = 0L;
            count_19277 = 0L;
            chunk_19276 = null;
            seq_19275 = ((IFn)const__15.getRawRoot()).invoke(object55);
        }
        IFn iFn7 = (IFn)const__0.getRawRoot();
        ILookupThunk iLookupThunk7 = __thunk__6__;
        Object object56 = qmap;
        Object object57 = iLookupThunk7.get(object56);
        if (iLookupThunk7 == object57) {
            __thunk__6__ = __site__6__.fault(object56);
            object57 = __thunk__6__.get(object56);
        }
        Object seq_19279 = iFn7.invoke(object57);
        Object chunk_19280 = null;
        long count_19281 = 0L;
        long i_19282 = 0L;
        while (true) {
            Object object58;
            Object or__5238__auto__19295;
            Object temp__5457__auto__19296;
            if (i_19282 < count_19281) {
                Object object59;
                Object or__5238__auto__19292;
                Object i = ((Indexed)chunk_19280).nth(RT.intCast((long)i_19282));
                Object object60 = or__5238__auto__19292 = ((IFn)const__19.getRawRoot()).invoke(i);
                if (object60 != null && object60 != Boolean.FALSE) {
                    object59 = or__5238__auto__19292;
                    or__5238__auto__19292 = null;
                } else {
                    Object or__5238__auto__19291;
                    Object object61 = or__5238__auto__19291 = ((IFn)const__20.getRawRoot()).invoke(i);
                    if (object61 != null && object61 != Boolean.FALSE) {
                        object59 = or__5238__auto__19291;
                        or__5238__auto__19291 = null;
                    } else {
                        object59 = ((IFn)const__21.getRawRoot()).invoke(i);
                    }
                }
                if (object59 == null || object59 == Boolean.FALSE) {
                    Object object62 = i;
                    i = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)const__7.getRawRoot()).invoke((Object)"Argument ", object62, (Object)" in :in is not a source"));
                }
                Object object63 = seq_19279;
                seq_19279 = null;
                Object object64 = chunk_19280;
                chunk_19280 = null;
                ++i_19282;
                chunk_19280 = object64;
                seq_19279 = object63;
                continue;
            }
            Object object65 = seq_19279;
            seq_19279 = null;
            Object object66 = temp__5457__auto__19296 = ((IFn)const__0.getRawRoot()).invoke(object65);
            if (object66 == null || object66 == Boolean.FALSE) break;
            Object object67 = temp__5457__auto__19296;
            temp__5457__auto__19296 = null;
            Object seq_192792 = object67;
            Object object68 = ((IFn)const__9.getRawRoot()).invoke(seq_192792);
            if (object68 != null && object68 != Boolean.FALSE) {
                Object c__5719__auto__19293 = ((IFn)const__10.getRawRoot()).invoke(seq_192792);
                Object object69 = seq_192792;
                seq_192792 = null;
                Object object70 = c__5719__auto__19293;
                Object object71 = c__5719__auto__19293;
                c__5719__auto__19293 = null;
                i_19282 = RT.intCast((long)0L);
                count_19281 = RT.intCast((int)RT.count((Object)object71));
                chunk_19280 = object70;
                seq_19279 = ((IFn)const__11.getRawRoot()).invoke(object69);
                continue;
            }
            Object i = ((IFn)const__14.getRawRoot()).invoke(seq_192792);
            Object object72 = or__5238__auto__19295 = ((IFn)const__19.getRawRoot()).invoke(i);
            if (object72 != null && object72 != Boolean.FALSE) {
                object58 = or__5238__auto__19295;
                or__5238__auto__19295 = null;
            } else {
                Object or__5238__auto__19294;
                Object object73 = or__5238__auto__19294 = ((IFn)const__20.getRawRoot()).invoke(i);
                if (object73 != null && object73 != Boolean.FALSE) {
                    object58 = or__5238__auto__19294;
                    or__5238__auto__19294 = null;
                } else {
                    object58 = ((IFn)const__21.getRawRoot()).invoke(i);
                }
            }
            if (object58 == null || object58 == Boolean.FALSE) {
                Object object74 = i;
                i = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__7.getRawRoot()).invoke((Object)"Argument ", object74, (Object)" in :in is not a source"));
            }
            Object object75 = seq_192792;
            seq_192792 = null;
            i_19282 = 0L;
            count_19281 = 0L;
            chunk_19280 = null;
            seq_19279 = ((IFn)const__15.getRawRoot()).invoke(object75);
        }
        IFn iFn8 = (IFn)const__0.getRawRoot();
        IFn iFn9 = (IFn)const__22.getRawRoot();
        ILookupThunk iLookupThunk8 = __thunk__7__;
        Object object76 = qmap;
        Object object77 = iLookupThunk8.get(object76);
        if (iLookupThunk8 == object77) {
            __thunk__7__ = __site__7__.fault(object76);
            object77 = __thunk__7__.get(object76);
        }
        Object object78 = temp__5457__auto__19297 = iFn8.invoke(iFn9.invoke(object77));
        if (object78 != null && object78 != Boolean.FALSE) {
            Object callees2;
            Object object79 = temp__5457__auto__19297;
            temp__5457__auto__19297 = null;
            Object object80 = callees2 = object79;
            callees2 = null;
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__7.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(object80), (Object)" is not valid in :construct"));
        }
        IFn iFn10 = (IFn)const__0.getRawRoot();
        IFn iFn11 = (IFn)const__24.getRawRoot();
        IFn iFn12 = (IFn)const__25.getRawRoot();
        ILookupThunk iLookupThunk9 = __thunk__8__;
        Object object81 = qmap;
        Object object82 = iLookupThunk9.get(object81);
        if (iLookupThunk9 == object82) {
            __thunk__8__ = __site__8__.fault(object81);
            object82 = __thunk__8__.get(object81);
        }
        Object object83 = iFn12.invoke(object82);
        IFn iFn13 = (IFn)const__25.getRawRoot();
        ILookupThunk iLookupThunk10 = __thunk__9__;
        Object object84 = qmap;
        Object object85 = iLookupThunk10.get(object84);
        if (iLookupThunk10 == object85) {
            __thunk__9__ = __site__9__.fault(object84);
            object85 = __thunk__9__.get(object84);
        }
        Object object86 = temp__5457__auto__19298 = iFn10.invoke(iFn11.invoke(object83, iFn13.invoke(object85)));
        if (object86 != null && object86 != Boolean.FALSE) {
            Object object87 = temp__5457__auto__19298;
            temp__5457__auto__19298 = null;
            Object unfound = object87;
            Object object88 = ((IFn)const__7.getRawRoot()).invoke((Object)"Query construct references variables not in :find: ", unfound);
            Object[] objectArray = new Object[2];
            objectArray[0] = const__28;
            Object object89 = unfound;
            unfound = null;
            objectArray[1] = object89;
            ((IFn)const__26.getRawRoot()).invoke((Object)const__27, object88, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        IFn iFn14 = (IFn)const__29.getRawRoot();
        IFn iFn15 = (IFn)const__25.getRawRoot();
        ILookupThunk iLookupThunk11 = __thunk__10__;
        Object object90 = qmap;
        Object object91 = iLookupThunk11.get(object90);
        if (iLookupThunk11 == object91) {
            __thunk__10__ = __site__10__.fault(object90);
            object91 = __thunk__10__.get(object90);
        }
        Object object92 = iFn15.invoke(object91);
        IFn iFn16 = (IFn)const__25.getRawRoot();
        ILookupThunk iLookupThunk12 = __thunk__11__;
        Object object93 = qmap;
        Object object94 = iLookupThunk12.get(object93);
        if (iLookupThunk12 == object94) {
            __thunk__11__ = __site__11__.fault(object93);
            object94 = __thunk__11__.get(object93);
        }
        Object outvars = iFn14.invoke(object92, iFn16.invoke(object94));
        IFn iFn17 = (IFn)const__29.getRawRoot();
        IFn iFn18 = (IFn)const__25.getRawRoot();
        ILookupThunk iLookupThunk13 = __thunk__12__;
        Object object95 = qmap;
        Object object96 = iLookupThunk13.get(object95);
        if (iLookupThunk13 == object96) {
            __thunk__12__ = __site__12__.fault(object95);
            object96 = __thunk__12__.get(object95);
        }
        Object object97 = iFn18.invoke(object96);
        IFn iFn19 = (IFn)const__25.getRawRoot();
        ILookupThunk iLookupThunk14 = __thunk__13__;
        Object object98 = qmap;
        Object object99 = iLookupThunk14.get(object98);
        if (iLookupThunk14 == object99) {
            __thunk__13__ = __site__13__.fault(object98);
            object99 = __thunk__13__.get(object98);
        }
        Object invars = iFn17.invoke(object97, iFn19.invoke(object99));
        Object object100 = outvars;
        outvars = null;
        Object object101 = invars;
        invars = null;
        Object missing = ((IFn)const__24.getRawRoot()).invoke(object100, object101);
        Object object102 = ((IFn)const__0.getRawRoot()).invoke(missing);
        if (object102 != null && object102 != Boolean.FALSE) {
            Object object103 = ((IFn)const__7.getRawRoot()).invoke((Object)"Query is referencing unbound variables: ", missing);
            Object[] objectArray = new Object[2];
            objectArray[0] = const__28;
            Object object104 = missing;
            missing = null;
            objectArray[1] = object104;
            ((IFn)const__26.getRawRoot()).invoke((Object)const__30, object103, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        Object object105 = null;
        return qmap;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$validate_query.invokeStatic(object2);
    }
}

