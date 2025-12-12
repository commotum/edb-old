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

public final class pull$pull_STAR_$fn__19031
extends AFunction {
    Object eid;
    Object db;
    Object kw__GT_attr;
    Object mk_xf;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"limit");
    public static final Keyword const__4 = RT.keyword(null, (String)"valfn");
    public static final Keyword const__5 = RT.keyword(null, (String)"keyfn");
    public static final Keyword const__6 = RT.keyword(null, (String)"subspec");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"forward-attr");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"to-kw");
    public static final Var const__9 = RT.var((String)"datomic.pull", (String)"default-spec");
    public static final Var const__10 = RT.var((String)"datomic.pull", (String)"ra->e");
    public static final Keyword const__11 = RT.keyword(null, (String)"reverse");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"assoc!");
    public static final Var const__14 = RT.var((String)"datomic.pull", (String)"denormalize-kw");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"type");

    public pull$pull_STAR_$fn__19031(Object object, Object object2, Object object3, Object object4) {
        this.eid = object;
        this.db = object2;
        this.kw__GT_attr = object3;
        this.mk_xf = object4;
    }

    public Object invoke(Object ret, Object kw, Object p__19030) {
        Object object;
        Object object2;
        Object object3 = p__19030;
        p__19030 = null;
        Object map__19032 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__19032);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__19032;
            map__19032 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__19032;
            map__19032 = null;
        }
        Object map__190322 = object2;
        Object limit2 = RT.get((Object)map__190322, (Object)const__3);
        Object valfn = RT.get((Object)map__190322, (Object)const__4);
        Object keyfn = RT.get((Object)map__190322, (Object)const__5);
        Object object6 = map__190322;
        map__190322 = null;
        Object subspec = RT.get((Object)object6, (Object)const__6);
        Object attr = ((IFn)this_.kw__GT_attr).invoke(((IFn)const__7.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(kw)));
        Object def_subspec = ((IFn)const__9.getRawRoot()).invoke(attr, kw, this_.db);
        Object object7 = attr;
        attr = null;
        Object object8 = subspec;
        subspec = null;
        Object object9 = def_subspec;
        def_subspec = null;
        Object object10 = limit2;
        limit2 = null;
        Object object11 = valfn;
        valfn = null;
        Object r = ((IFn)const__10.getRawRoot()).invoke(this_.db, this_.eid, object7, ((IFn)this_.mk_xf).invoke((Object)Tuple.create((Object)const__11, (Object)kw), object8, object9), object10, object11);
        if (Util.identical((Object)r, null)) {
            object = ret;
            ret = null;
        } else {
            Object object12 = ret;
            ret = null;
            Object object13 = keyfn;
            keyfn = null;
            Object object14 = kw;
            Object object15 = kw;
            kw = null;
            Object object16 = r;
            r = null;
            pull$pull_STAR_$fn__19031 this_ = null;
            object = ((IFn)const__13.getRawRoot()).invoke(object12, ((IFn)object13).invoke(((IFn)const__14.getRawRoot()).invoke(object14, ((IFn)const__15.getRawRoot()).invoke(object15))), object16);
        }
        return object;
    }
}

