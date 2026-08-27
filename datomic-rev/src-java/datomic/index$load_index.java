/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LOO
 *  clojure.lang.IFn$OL
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.IndexSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class index$load_index
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__1 = RT.keyword(null, (String)"event");
    public static final Keyword const__2 = RT.keyword((String)"index", (String)"load-index");
    public static final Keyword const__3 = RT.keyword(null, (String)"olookup");
    public static final Keyword const__4 = RT.keyword(null, (String)"index-root-id");
    public static final Var const__5 = RT.var((String)"datomic.common", (String)"getx");
    public static final Var const__6 = RT.var((String)"datomic.index", (String)"valid-version");
    public static final Var const__7 = RT.var((String)"datomic.index", (String)"lookup-index");
    public static final Var const__8 = RT.var((String)"datomic.index", (String)"eavt-cmpi");
    public static final Var const__10 = RT.var((String)"datomic.index", (String)"version-root-key");
    public static final Keyword const__11 = RT.keyword(null, (String)"eavt-main");
    public static final Var const__12 = RT.var((String)"datomic.index", (String)"avet-cmpi");
    public static final Keyword const__13 = RT.keyword(null, (String)"avet-main");
    public static final Var const__14 = RT.var((String)"datomic.index", (String)"aevt-cmpi");
    public static final Keyword const__15 = RT.keyword(null, (String)"aevt-main");
    public static final Var const__16 = RT.var((String)"datomic.index", (String)"raet-cmpi");
    public static final Keyword const__17 = RT.keyword(null, (String)"raet-main");
    public static final Var const__18 = RT.var((String)"datomic.fulltext", (String)"clustered-fulltext");
    public static final Keyword const__29 = RT.keyword(null, (String)"birth-level");
    public static final Var const__30 = RT.var((String)"datomic.db", (String)"MIN_SCHEMA_LEVEL");
    public static final Keyword const__31 = RT.keyword(null, (String)"root-id");
    public static final Keyword const__32 = RT.keyword(null, (String)"mid-index");
    public static final Keyword const__33 = RT.keyword(null, (String)"nextT");
    public static final Keyword const__34 = RT.keyword(null, (String)"schema-level");
    public static final Keyword const__35 = RT.keyword(null, (String)"index");
    public static final Keyword const__36 = RT.keyword(null, (String)"basisT");
    public static final Keyword const__37 = RT.keyword(null, (String)"history");
    public static final Keyword const__38 = RT.keyword(null, (String)"rev");
    public static final Keyword const__39 = RT.keyword(null, (String)"buildRevision");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"fulltext"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"eavt-hist"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"eavt-hist"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"avet-hist"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"avet-hist"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"aevt-hist"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"aevt-hist"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"raet-hist"));
    static ILookupThunk __thunk__7__ = __site__7__;
    static final KeywordLookupSite __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"raet-hist"));
    static ILookupThunk __thunk__8__ = __site__8__;
    static final KeywordLookupSite __site__9__ = new KeywordLookupSite(RT.keyword(null, (String)"fulltext-hist"));
    static ILookupThunk __thunk__9__ = __site__9__;
    static final KeywordLookupSite __site__10__ = new KeywordLookupSite(RT.keyword(null, (String)"eavt-mid"));
    static ILookupThunk __thunk__10__ = __site__10__;
    static final KeywordLookupSite __site__11__ = new KeywordLookupSite(RT.keyword(null, (String)"eavt-mid"));
    static ILookupThunk __thunk__11__ = __site__11__;
    static final KeywordLookupSite __site__12__ = new KeywordLookupSite(RT.keyword(null, (String)"avet-mid"));
    static ILookupThunk __thunk__12__ = __site__12__;
    static final KeywordLookupSite __site__13__ = new KeywordLookupSite(RT.keyword(null, (String)"avet-mid"));
    static ILookupThunk __thunk__13__ = __site__13__;
    static final KeywordLookupSite __site__14__ = new KeywordLookupSite(RT.keyword(null, (String)"aevt-mid"));
    static ILookupThunk __thunk__14__ = __site__14__;
    static final KeywordLookupSite __site__15__ = new KeywordLookupSite(RT.keyword(null, (String)"aevt-mid"));
    static ILookupThunk __thunk__15__ = __site__15__;
    static final KeywordLookupSite __site__16__ = new KeywordLookupSite(RT.keyword(null, (String)"raet-mid"));
    static ILookupThunk __thunk__16__ = __site__16__;
    static final KeywordLookupSite __site__17__ = new KeywordLookupSite(RT.keyword(null, (String)"raet-mid"));
    static ILookupThunk __thunk__17__ = __site__17__;
    static final KeywordLookupSite __site__18__ = new KeywordLookupSite(RT.keyword(null, (String)"birth-level"));
    static ILookupThunk __thunk__18__ = __site__18__;
    static final KeywordLookupSite __site__19__ = new KeywordLookupSite(RT.keyword(null, (String)"nextT"));
    static ILookupThunk __thunk__19__ = __site__19__;
    static final KeywordLookupSite __site__20__ = new KeywordLookupSite(RT.keyword(null, (String)"schema-level"));
    static ILookupThunk __thunk__20__ = __site__20__;
    static final KeywordLookupSite __site__21__ = new KeywordLookupSite(RT.keyword(null, (String)"basisT"));
    static ILookupThunk __thunk__21__ = __site__21__;
    static final KeywordLookupSite __site__22__ = new KeywordLookupSite(RT.keyword(null, (String)"rev"));
    static ILookupThunk __thunk__22__ = __site__22__;
    static final KeywordLookupSite __site__23__ = new KeywordLookupSite(RT.keyword(null, (String)"buildRevision"));
    static ILookupThunk __thunk__23__ = __site__23__;

    public static Object invokeStatic(Object olookup, Object index_root_id) {
        IPersistentMap iPersistentMap;
        Object temp__5457__auto__15301;
        Logger logger = LoggerFactory.getLogger((String)"datomic.index");
        if (logger.isDebugEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.debug((String)((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, const__2, const__3, olookup, const__4, index_root_id})));
        }
        Object object = temp__5457__auto__15301 = ((IFn)const__5.getRawRoot()).invoke(olookup, index_root_id);
        if (object != null && object != Boolean.FALSE) {
            Object object2;
            Object or__5238__auto__15300;
            Object object3;
            Object or__5238__auto__15299;
            Object object4;
            Object and__5236__auto__15298;
            Object object5;
            Object and__5236__auto__15297;
            Object object6;
            Object and__5236__auto__15296;
            Object object7;
            Object and__5236__auto__15295;
            Object object8;
            Object and__5236__auto__15294;
            Object object9;
            Object and__5236__auto__15293;
            Object object10;
            Object and__5236__auto__15292;
            Object object11;
            Object and__5236__auto__15291;
            Object object12 = temp__5457__auto__15301;
            temp__5457__auto__15301 = null;
            Object root_map = object12;
            long version2 = ((IFn.OL)const__6.getRawRoot()).invokePrim(root_map);
            Object eavt2 = ((IFn)const__7.getRawRoot()).invoke(olookup, const__8.getRawRoot(), RT.get((Object)root_map, (Object)((IFn.LOO)const__10.getRawRoot()).invokePrim(version2, (Object)const__11)));
            Object avet2 = ((IFn)const__7.getRawRoot()).invoke(olookup, const__12.getRawRoot(), RT.get((Object)root_map, (Object)((IFn.LOO)const__10.getRawRoot()).invokePrim(version2, (Object)const__13)));
            Object aevt2 = ((IFn)const__7.getRawRoot()).invoke(olookup, const__14.getRawRoot(), RT.get((Object)root_map, (Object)((IFn.LOO)const__10.getRawRoot()).invokePrim(version2, (Object)const__15)));
            Object raet2 = ((IFn)const__7.getRawRoot()).invoke(olookup, const__16.getRawRoot(), RT.get((Object)root_map, (Object)((IFn.LOO)const__10.getRawRoot()).invokePrim(version2, (Object)const__17)));
            IFn iFn = (IFn)const__18.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object13 = root_map;
            Object object14 = iLookupThunk.get(object13);
            if (iLookupThunk == object14) {
                __thunk__0__ = __site__0__.fault(object13);
                object14 = __thunk__0__.get(object13);
            }
            Object fulltext2 = iFn.invoke(olookup, object14);
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object15 = root_map;
            Object object16 = iLookupThunk2.get(object15);
            if (iLookupThunk2 == object16) {
                __thunk__1__ = __site__1__.fault(object15);
                object16 = __thunk__1__.get(object15);
            }
            Object object17 = and__5236__auto__15291 = object16;
            if (object17 != null && object17 != Boolean.FALSE) {
                IFn iFn2 = (IFn)const__7.getRawRoot();
                Object object18 = const__8.getRawRoot();
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object19 = root_map;
                Object object20 = iLookupThunk3.get(object19);
                if (iLookupThunk3 == object20) {
                    __thunk__2__ = __site__2__.fault(object19);
                    object20 = __thunk__2__.get(object19);
                }
                object11 = iFn2.invoke(olookup, object18, object20);
            } else {
                object11 = and__5236__auto__15291;
                and__5236__auto__15291 = null;
            }
            Object eavt_hist = object11;
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object21 = root_map;
            Object object22 = iLookupThunk4.get(object21);
            if (iLookupThunk4 == object22) {
                __thunk__3__ = __site__3__.fault(object21);
                object22 = __thunk__3__.get(object21);
            }
            Object object23 = and__5236__auto__15292 = object22;
            if (object23 != null && object23 != Boolean.FALSE) {
                IFn iFn3 = (IFn)const__7.getRawRoot();
                Object object24 = const__12.getRawRoot();
                ILookupThunk iLookupThunk5 = __thunk__4__;
                Object object25 = root_map;
                Object object26 = iLookupThunk5.get(object25);
                if (iLookupThunk5 == object26) {
                    __thunk__4__ = __site__4__.fault(object25);
                    object26 = __thunk__4__.get(object25);
                }
                object10 = iFn3.invoke(olookup, object24, object26);
            } else {
                object10 = and__5236__auto__15292;
                and__5236__auto__15292 = null;
            }
            Object avet_hist = object10;
            ILookupThunk iLookupThunk6 = __thunk__5__;
            Object object27 = root_map;
            Object object28 = iLookupThunk6.get(object27);
            if (iLookupThunk6 == object28) {
                __thunk__5__ = __site__5__.fault(object27);
                object28 = __thunk__5__.get(object27);
            }
            Object object29 = and__5236__auto__15293 = object28;
            if (object29 != null && object29 != Boolean.FALSE) {
                IFn iFn4 = (IFn)const__7.getRawRoot();
                Object object30 = const__14.getRawRoot();
                ILookupThunk iLookupThunk7 = __thunk__6__;
                Object object31 = root_map;
                Object object32 = iLookupThunk7.get(object31);
                if (iLookupThunk7 == object32) {
                    __thunk__6__ = __site__6__.fault(object31);
                    object32 = __thunk__6__.get(object31);
                }
                object9 = iFn4.invoke(olookup, object30, object32);
            } else {
                object9 = and__5236__auto__15293;
                and__5236__auto__15293 = null;
            }
            Object aevt_hist = object9;
            ILookupThunk iLookupThunk8 = __thunk__7__;
            Object object33 = root_map;
            Object object34 = iLookupThunk8.get(object33);
            if (iLookupThunk8 == object34) {
                __thunk__7__ = __site__7__.fault(object33);
                object34 = __thunk__7__.get(object33);
            }
            Object object35 = and__5236__auto__15294 = object34;
            if (object35 != null && object35 != Boolean.FALSE) {
                IFn iFn5 = (IFn)const__7.getRawRoot();
                Object object36 = const__16.getRawRoot();
                ILookupThunk iLookupThunk9 = __thunk__8__;
                Object object37 = root_map;
                Object object38 = iLookupThunk9.get(object37);
                if (iLookupThunk9 == object38) {
                    __thunk__8__ = __site__8__.fault(object37);
                    object38 = __thunk__8__.get(object37);
                }
                object8 = iFn5.invoke(olookup, object36, object38);
            } else {
                object8 = and__5236__auto__15294;
                and__5236__auto__15294 = null;
            }
            Object raet_hist = object8;
            IFn iFn6 = (IFn)const__18.getRawRoot();
            ILookupThunk iLookupThunk10 = __thunk__9__;
            Object object39 = root_map;
            Object object40 = iLookupThunk10.get(object39);
            if (iLookupThunk10 == object40) {
                __thunk__9__ = __site__9__.fault(object39);
                object40 = __thunk__9__.get(object39);
            }
            Object fulltext_hist = iFn6.invoke(olookup, object40);
            ILookupThunk iLookupThunk11 = __thunk__10__;
            Object object41 = root_map;
            Object object42 = iLookupThunk11.get(object41);
            if (iLookupThunk11 == object42) {
                __thunk__10__ = __site__10__.fault(object41);
                object42 = __thunk__10__.get(object41);
            }
            Object object43 = and__5236__auto__15295 = object42;
            if (object43 != null && object43 != Boolean.FALSE) {
                IFn iFn7 = (IFn)const__7.getRawRoot();
                Object object44 = const__8.getRawRoot();
                ILookupThunk iLookupThunk12 = __thunk__11__;
                Object object45 = root_map;
                Object object46 = iLookupThunk12.get(object45);
                if (iLookupThunk12 == object46) {
                    __thunk__11__ = __site__11__.fault(object45);
                    object46 = __thunk__11__.get(object45);
                }
                object7 = iFn7.invoke(olookup, object44, object46);
            } else {
                object7 = and__5236__auto__15295;
                and__5236__auto__15295 = null;
            }
            Object eavt_mid = object7;
            ILookupThunk iLookupThunk13 = __thunk__12__;
            Object object47 = root_map;
            Object object48 = iLookupThunk13.get(object47);
            if (iLookupThunk13 == object48) {
                __thunk__12__ = __site__12__.fault(object47);
                object48 = __thunk__12__.get(object47);
            }
            Object object49 = and__5236__auto__15296 = object48;
            if (object49 != null && object49 != Boolean.FALSE) {
                IFn iFn8 = (IFn)const__7.getRawRoot();
                Object object50 = const__12.getRawRoot();
                ILookupThunk iLookupThunk14 = __thunk__13__;
                Object object51 = root_map;
                Object object52 = iLookupThunk14.get(object51);
                if (iLookupThunk14 == object52) {
                    __thunk__13__ = __site__13__.fault(object51);
                    object52 = __thunk__13__.get(object51);
                }
                object6 = iFn8.invoke(olookup, object50, object52);
            } else {
                object6 = and__5236__auto__15296;
                and__5236__auto__15296 = null;
            }
            Object avet_mid = object6;
            ILookupThunk iLookupThunk15 = __thunk__14__;
            Object object53 = root_map;
            Object object54 = iLookupThunk15.get(object53);
            if (iLookupThunk15 == object54) {
                __thunk__14__ = __site__14__.fault(object53);
                object54 = __thunk__14__.get(object53);
            }
            Object object55 = and__5236__auto__15297 = object54;
            if (object55 != null && object55 != Boolean.FALSE) {
                IFn iFn9 = (IFn)const__7.getRawRoot();
                Object object56 = const__14.getRawRoot();
                ILookupThunk iLookupThunk16 = __thunk__15__;
                Object object57 = root_map;
                Object object58 = iLookupThunk16.get(object57);
                if (iLookupThunk16 == object58) {
                    __thunk__15__ = __site__15__.fault(object57);
                    object58 = __thunk__15__.get(object57);
                }
                object5 = iFn9.invoke(olookup, object56, object58);
            } else {
                object5 = and__5236__auto__15297;
                and__5236__auto__15297 = null;
            }
            Object aevt_mid = object5;
            ILookupThunk iLookupThunk17 = __thunk__16__;
            Object object59 = root_map;
            Object object60 = iLookupThunk17.get(object59);
            if (iLookupThunk17 == object60) {
                __thunk__16__ = __site__16__.fault(object59);
                object60 = __thunk__16__.get(object59);
            }
            Object object61 = and__5236__auto__15298 = object60;
            if (object61 != null && object61 != Boolean.FALSE) {
                IFn iFn10 = (IFn)const__7.getRawRoot();
                Object object62 = olookup;
                olookup = null;
                Object object63 = const__16.getRawRoot();
                ILookupThunk iLookupThunk18 = __thunk__17__;
                Object object64 = root_map;
                Object object65 = iLookupThunk18.get(object64);
                if (iLookupThunk18 == object65) {
                    __thunk__17__ = __site__17__.fault(object64);
                    object65 = __thunk__17__.get(object64);
                }
                object4 = iFn10.invoke(object62, object63, object65);
            } else {
                object4 = and__5236__auto__15298;
                and__5236__auto__15298 = null;
            }
            Object raet_mid = object4;
            Object[] objectArray = new Object[20];
            objectArray[0] = const__29;
            ILookupThunk iLookupThunk19 = __thunk__18__;
            Object object66 = root_map;
            Object object67 = iLookupThunk19.get(object66);
            if (iLookupThunk19 == object67) {
                __thunk__18__ = __site__18__.fault(object66);
                object67 = __thunk__18__.get(object66);
            }
            Object object68 = or__5238__auto__15299 = object67;
            if (object68 != null && object68 != Boolean.FALSE) {
                object3 = or__5238__auto__15299;
                or__5238__auto__15299 = null;
            } else {
                object3 = const__30.getRawRoot();
            }
            objectArray[1] = object3;
            objectArray[2] = const__31;
            Object object69 = index_root_id;
            index_root_id = null;
            objectArray[3] = object69;
            objectArray[4] = const__32;
            Object object70 = eavt_mid;
            eavt_mid = null;
            Object object71 = avet_mid;
            avet_mid = null;
            Object object72 = aevt_mid;
            aevt_mid = null;
            Object object73 = raet_mid;
            raet_mid = null;
            objectArray[5] = new IndexSet(object70, object71, object72, object73, null);
            objectArray[6] = const__33;
            ILookupThunk iLookupThunk20 = __thunk__19__;
            Object object74 = root_map;
            Object object75 = iLookupThunk20.get(object74);
            if (iLookupThunk20 == object75) {
                __thunk__19__ = __site__19__.fault(object74);
                object75 = __thunk__19__.get(object74);
            }
            objectArray[7] = object75;
            objectArray[8] = const__34;
            ILookupThunk iLookupThunk21 = __thunk__20__;
            Object object76 = root_map;
            Object object77 = iLookupThunk21.get(object76);
            if (iLookupThunk21 == object77) {
                __thunk__20__ = __site__20__.fault(object76);
                object77 = __thunk__20__.get(object76);
            }
            Object object78 = or__5238__auto__15300 = object77;
            if (object78 != null && object78 != Boolean.FALSE) {
                object2 = or__5238__auto__15300;
                or__5238__auto__15300 = null;
            } else {
                object2 = const__30.getRawRoot();
            }
            objectArray[9] = object2;
            objectArray[10] = const__35;
            Object object79 = eavt2;
            eavt2 = null;
            Object object80 = avet2;
            avet2 = null;
            Object object81 = aevt2;
            aevt2 = null;
            Object object82 = raet2;
            raet2 = null;
            Object object83 = fulltext2;
            fulltext2 = null;
            objectArray[11] = new IndexSet(object79, object80, object81, object82, object83);
            objectArray[12] = const__36;
            ILookupThunk iLookupThunk22 = __thunk__21__;
            Object object84 = root_map;
            Object object85 = iLookupThunk22.get(object84);
            if (iLookupThunk22 == object85) {
                __thunk__21__ = __site__21__.fault(object84);
                object85 = __thunk__21__.get(object84);
            }
            objectArray[13] = object85;
            objectArray[14] = const__37;
            Object object86 = eavt_hist;
            eavt_hist = null;
            Object object87 = avet_hist;
            avet_hist = null;
            Object object88 = aevt_hist;
            aevt_hist = null;
            Object object89 = raet_hist;
            raet_hist = null;
            Object object90 = fulltext_hist;
            fulltext_hist = null;
            objectArray[15] = new IndexSet(object86, object87, object88, object89, object90);
            objectArray[16] = const__38;
            ILookupThunk iLookupThunk23 = __thunk__22__;
            Object object91 = root_map;
            Object object92 = iLookupThunk23.get(object91);
            if (iLookupThunk23 == object92) {
                __thunk__22__ = __site__22__.fault(object91);
                object92 = __thunk__22__.get(object91);
            }
            objectArray[17] = object92;
            objectArray[18] = const__39;
            ILookupThunk iLookupThunk24 = __thunk__23__;
            Object object93 = root_map;
            root_map = null;
            Object object94 = iLookupThunk24.get(object93);
            if (iLookupThunk24 == object94) {
                __thunk__23__ = __site__23__.fault(object93);
                object94 = __thunk__23__.get(object93);
            }
            objectArray[19] = object94;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        return iPersistentMap;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$load_index.invokeStatic(object3, object4);
    }
}

