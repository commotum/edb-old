/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.query$process_aggregates$fn__19357;
import datomic.query$process_aggregates$list_QMARK___19355;

public final class query$process_aggregates
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"find");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"distinct");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__7 = RT.keyword(null, (String)"group");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"into");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"find"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"with"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"with"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"find"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object qmap) {
        Object qmap2;
        Object object;
        Object temp__5455__auto__19361;
        Object object2;
        Object object3;
        Object or__5238__auto__19360;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object4 = qmap;
        Object object5 = iLookupThunk.get(object4);
        if (iLookupThunk == object5) {
            __thunk__0__ = __site__0__.fault(object4);
            object5 = __thunk__0__.get(object4);
        }
        Object finds = object5;
        query$process_aggregates$list_QMARK___19355 list_QMARK_ = new query$process_aggregates$list_QMARK___19355();
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object6 = qmap;
        Object object7 = iLookupThunk2.get(object6);
        if (iLookupThunk2 == object7) {
            __thunk__1__ = __site__1__.fault(object6);
            object7 = __thunk__1__.get(object6);
        }
        Object object8 = or__5238__auto__19360 = object7;
        if (object8 != null && object8 != Boolean.FALSE) {
            object3 = or__5238__auto__19360;
            or__5238__auto__19360 = null;
        } else {
            object3 = ((IFn)const__2.getRawRoot()).invoke((Object)list_QMARK_, finds);
        }
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object9 = qmap;
            qmap = null;
            query$process_aggregates$list_QMARK___19355 query$process_aggregates$list_QMARK___19355 = list_QMARK_;
            list_QMARK_ = null;
            Object object10 = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke((Object)new query$process_aggregates$fn__19357((Object)query$process_aggregates$list_QMARK___19355), finds)));
            Object object11 = finds;
            finds = null;
            object2 = ((IFn)const__3.getRawRoot()).invoke(object9, (Object)const__0, object10, (Object)const__7, ((IFn)const__4.getRawRoot()).invoke(object11));
        } else {
            object2 = qmap;
            qmap = null;
        }
        Object qmap3 = object2;
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object12 = qmap3;
        Object object13 = iLookupThunk3.get(object12);
        if (iLookupThunk3 == object13) {
            __thunk__2__ = __site__2__.fault(object12);
            object13 = __thunk__2__.get(object12);
        }
        Object object14 = temp__5455__auto__19361 = object13;
        if (object14 != null && object14 != Boolean.FALSE) {
            Object object15 = temp__5455__auto__19361;
            temp__5455__auto__19361 = null;
            Object w = object15;
            IFn iFn = (IFn)const__3.getRawRoot();
            Object object16 = qmap3;
            IFn iFn2 = (IFn)const__8.getRawRoot();
            IFn iFn3 = (IFn)const__4.getRawRoot();
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object17 = qmap3;
            qmap3 = null;
            Object object18 = iLookupThunk4.get(object17);
            if (iLookupThunk4 == object18) {
                __thunk__3__ = __site__3__.fault(object17);
                object18 = __thunk__3__.get(object17);
            }
            Object object19 = w;
            w = null;
            object = iFn.invoke(object16, (Object)const__0, iFn2.invoke(iFn3.invoke(object18), object19));
        } else {
            object = qmap3;
            qmap3 = null;
        }
        Object object20 = qmap2 = object;
        qmap2 = null;
        return object20;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$process_aggregates.invokeStatic(object2);
    }
}

