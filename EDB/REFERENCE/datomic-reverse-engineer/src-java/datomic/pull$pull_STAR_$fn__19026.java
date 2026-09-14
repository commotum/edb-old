/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;

public final class pull$pull_STAR_$fn__19026
extends AFunction {
    Object wildcard;
    Object eid;
    Object db;
    Object kw__GT_attr;
    Object prefer_aevt_QMARK_;
    Object mk_xf;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"limit");
    public static final Keyword const__4 = RT.keyword(null, (String)"valfn");
    public static final Keyword const__5 = RT.keyword(null, (String)"keyfn");
    public static final Keyword const__6 = RT.keyword(null, (String)"subspec");
    public static final Var const__7 = RT.var((String)"datomic.pull", (String)"denormalize-kw");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"type");
    public static final Var const__9 = RT.var((String)"datomic.pull", (String)"default-spec");
    public static final Var const__10 = RT.var((String)"datomic.pull", (String)"ea->v");
    public static final Keyword const__11 = RT.keyword(null, (String)"forward");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"assoc!");

    public pull$pull_STAR_$fn__19026(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.wildcard = object;
        this.eid = object2;
        this.db = object3;
        this.kw__GT_attr = object4;
        this.prefer_aevt_QMARK_ = object5;
        this.mk_xf = object6;
    }

    public Object invoke(Object ret, Object kw, Object p__19025) {
        Object object;
        Object object2;
        Object object3 = p__19025;
        p__19025 = null;
        Object map__19027 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__19027);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__19027;
            map__19027 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__19027;
            map__19027 = null;
        }
        Object map__190272 = object2;
        Object limit2 = RT.get((Object)map__190272, (Object)const__3);
        Object valfn = RT.get((Object)map__190272, (Object)const__4);
        Object keyfn = RT.get((Object)map__190272, (Object)const__5);
        Object object6 = map__190272;
        map__190272 = null;
        Object subspec = RT.get((Object)object6, (Object)const__6);
        Object object7 = RT.get((Object)ret, (Object)((IFn)keyfn).invoke(((IFn)const__7.getRawRoot()).invoke(kw, ((IFn)const__8.getRawRoot()).invoke(kw))));
        if (object7 != null && object7 != Boolean.FALSE) {
            object = ret;
            ret = null;
        } else {
            Object object8;
            Object and__5236__auto__19029;
            Object attr = ((IFn)this_.kw__GT_attr).invoke(kw);
            Object def_subspec = ((IFn)const__9.getRawRoot()).invoke(attr, kw, this_.db);
            IFn iFn = (IFn)const__10.getRawRoot();
            Object object9 = attr;
            attr = null;
            Object object10 = subspec;
            subspec = null;
            Object object11 = def_subspec;
            def_subspec = null;
            Object object12 = ((IFn)this_.mk_xf).invoke((Object)Tuple.create((Object)const__11, (Object)kw), object10, object11);
            Object object13 = limit2;
            limit2 = null;
            Object object14 = valfn;
            valfn = null;
            Object object15 = and__5236__auto__19029 = this_.prefer_aevt_QMARK_;
            if (object15 != null && object15 != Boolean.FALSE) {
                object8 = ((IFn)const__12.getRawRoot()).invoke(this_.wildcard);
            } else {
                object8 = and__5236__auto__19029;
                and__5236__auto__19029 = null;
            }
            Object v = iFn.invoke(this_.db, this_.eid, object9, object12, object13, object14, object8);
            if (Util.identical((Object)v, null)) {
                object = ret;
                ret = null;
            } else {
                Object object16 = ret;
                ret = null;
                Object object17 = keyfn;
                keyfn = null;
                Object object18 = kw;
                Object object19 = kw;
                kw = null;
                Object object20 = v;
                v = null;
                pull$pull_STAR_$fn__19026 this_ = null;
                object = ((IFn)const__14.getRawRoot()).invoke(object16, ((IFn)object17).invoke(((IFn)const__7.getRawRoot()).invoke(object18, ((IFn)const__8.getRawRoot()).invoke(object19))), object20);
            }
        }
        return object;
    }
}

