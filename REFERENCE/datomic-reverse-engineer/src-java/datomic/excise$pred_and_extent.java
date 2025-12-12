/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Database;
import datomic.excise$pred_and_extent$datoms__14833;
import datomic.excise$pred_and_extent$id__14815;
import datomic.excise$pred_and_extent$ref_QMARK___14818;
import datomic.excise$pred_and_extent$reify__14840;
import datomic.excise$pred_and_extent$remove_QMARK___14820;

public final class excise$pred_and_extent
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__4 = RT.var((String)"datomic.excise", (String)"get-before-t");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__6 = RT.keyword(null, (String)"e");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"eid->part");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Keyword const__10 = RT.keyword(null, (String)"a");
    public static final Keyword const__11 = RT.keyword(null, (String)"else");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"eid->eidx");
    public static final Var const__15 = RT.var((String)"datomic.excise", (String)"component-es-set");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"disj");
    public static final AFn const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 110, RT.keyword(null, (String)"column"), 6});
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"excise"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword((String)"db.excise", (String)"attrs"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword((String)"db", (String)"id"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object db2, Object spec) {
        Object object;
        excise$pred_and_extent$id__14815 id = new excise$pred_and_extent$id__14815(db2);
        IFn iFn = (IFn)id;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = spec;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object target2 = iFn.invoke(object3);
        IFn iFn2 = (IFn)const__1.getRawRoot();
        IFn iFn3 = (IFn)const__2.getRawRoot();
        excise$pred_and_extent$id__14815 excise$pred_and_extent$id__14815 = id;
        id = null;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object4 = spec;
        Object object5 = iLookupThunk2.get(object4);
        if (iLookupThunk2 == object5) {
            __thunk__1__ = __site__1__.fault(object4);
            object5 = __thunk__1__.get(object4);
        }
        Object attrs = iFn2.invoke((Object)PersistentHashSet.EMPTY, iFn3.invoke((Object)excise$pred_and_extent$id__14815, object5));
        Object before_t = ((IFn)const__4.getRawRoot()).invoke(db2, spec);
        Object object6 = ((IFn)const__5.getRawRoot()).invoke(attrs);
        if (object6 != null && object6 != Boolean.FALSE) {
            object = const__6;
        } else {
            boolean and__5236__auto__14843 = Numbers.isZero((long)((IFn.LL)const__8.getRawRoot()).invokePrim(RT.longCast((Object)((Number)target2))));
            Object object7 = and__5236__auto__14843 ? ((IFn)const__9.getRawRoot()).invoke(db2, target2) : (and__5236__auto__14843 ? Boolean.TRUE : Boolean.FALSE);
            if (object7 != null && object7 != Boolean.FALSE) {
                object = const__10;
            } else {
                Keyword keyword = const__11;
                object = keyword != null && keyword != Boolean.FALSE ? const__6 : null;
            }
        }
        Keyword type = object;
        IFn.LL lL = (IFn.LL)const__12.getRawRoot();
        ILookupThunk iLookupThunk3 = __thunk__2__;
        Object object8 = spec;
        spec = null;
        Object object9 = iLookupThunk3.get(object8);
        if (iLookupThunk3 == object9) {
            __thunk__2__ = __site__2__.fault(object8);
            object9 = __thunk__2__.get(object8);
        }
        long t = lL.invokePrim(RT.longCast((Object)((Number)object9)));
        Object extent = Util.equiv((Object)type, (Object)const__6) ? ((IFn)const__15.getRawRoot()).invoke((Object)((Database)db2).history(), target2, attrs) : null;
        Object component_QMARK_ = ((IFn)const__16.getRawRoot()).invoke(extent, target2);
        excise$pred_and_extent$ref_QMARK___14818 ref_QMARK_ = new excise$pred_and_extent$ref_QMARK___14818(db2);
        Object object10 = component_QMARK_;
        component_QMARK_ = null;
        Object object11 = before_t;
        before_t = null;
        excise$pred_and_extent$ref_QMARK___14818 excise$pred_and_extent$ref_QMARK___14818 = ref_QMARK_;
        ref_QMARK_ = null;
        Object object12 = attrs;
        attrs = null;
        excise$pred_and_extent$remove_QMARK___14820 remove_QMARK_2 = new excise$pred_and_extent$remove_QMARK___14820(object10, object11, (Object)excise$pred_and_extent$ref_QMARK___14818, t, target2, object12, type);
        Object object13 = db2;
        db2 = null;
        Object object14 = target2;
        target2 = null;
        Keyword keyword = type;
        type = null;
        excise$pred_and_extent$datoms__14833 datoms2 = new excise$pred_and_extent$datoms__14833(extent, object13, (Object)remove_QMARK_2, object14, keyword);
        excise$pred_and_extent$remove_QMARK___14820 excise$pred_and_extent$remove_QMARK___14820 = remove_QMARK_2;
        remove_QMARK_2 = null;
        excise$pred_and_extent$datoms__14833 excise$pred_and_extent$datoms__14833 = datoms2;
        datoms2 = null;
        Object object15 = extent;
        extent = null;
        return Tuple.create((Object)((IObj)new excise$pred_and_extent$reify__14840(null, (Object)excise$pred_and_extent$remove_QMARK___14820, (Object)excise$pred_and_extent$datoms__14833)).withMeta((IPersistentMap)const__21), (Object)object15);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return excise$pred_and_extent.invokeStatic(object3, object4);
    }
}

