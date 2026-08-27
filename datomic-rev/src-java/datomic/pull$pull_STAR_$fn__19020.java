/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Database;

public final class pull$pull_STAR_$fn__19020
extends AFunction {
    Object wildcard;
    Object eid;
    Object db;
    Object forward;
    Object mk_xf;
    public static final Var const__0 = RT.var((String)"datomic.pull", (String)"denormalize-kw");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__2 = RT.var((String)"datomic.pull", (String)"default-spec");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__6 = RT.keyword(null, (String)"valfn");
    public static final Keyword const__7 = RT.keyword(null, (String)"keyfn");
    public static final Keyword const__8 = RT.keyword(null, (String)"subspec");
    public static final Var const__9 = RT.var((String)"datomic.pull", (String)"ea->v");
    public static final Var const__10 = RT.var((String)"datomic.pull", (String)"limit-default-from-map");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"assoc!");

    public pull$pull_STAR_$fn__19020(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.wildcard = object;
        this.eid = object2;
        this.db = object3;
        this.forward = object4;
        this.mk_xf = object5;
    }

    public Object invoke(Object ret, Object a) {
        Object object;
        Object or__5238__auto__19024;
        Object object2;
        Object or__5238__auto__19023;
        Object map__19021;
        Object object3;
        Object kw = ((IFn)const__0.getRawRoot()).invoke(((Database)this_.db).ident(a), this_.wildcard);
        Object object4 = a;
        a = null;
        Object attr = ((IFn)const__1.getRawRoot()).invoke(this_.db, object4);
        Object def_subspec = ((IFn)const__2.getRawRoot()).invoke(attr, kw, this_.db);
        Object map__190212 = RT.get((Object)this_.forward, (Object)kw);
        Object object5 = ((IFn)const__4.getRawRoot()).invoke(map__190212);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__190212;
            map__190212 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__5.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__190212;
            map__190212 = null;
        }
        Object forward_args = map__19021 = object3;
        Object valfn = RT.get((Object)map__19021, (Object)const__6);
        Object keyfn = RT.get((Object)map__19021, (Object)const__7);
        Object object7 = map__19021;
        map__19021 = null;
        Object subspec = RT.get((Object)object7, (Object)const__8);
        IFn iFn = (IFn)const__9.getRawRoot();
        Object object8 = attr;
        attr = null;
        Object object9 = subspec;
        subspec = null;
        Object object10 = def_subspec;
        def_subspec = null;
        Object object11 = ((IFn)this_.mk_xf).invoke((Object)PersistentVector.EMPTY, object9, object10);
        Object object12 = forward_args;
        forward_args = null;
        Object object13 = ((IFn)const__10.getRawRoot()).invoke(object12);
        Object object14 = valfn;
        valfn = null;
        Object object15 = or__5238__auto__19023 = object14;
        if (object15 != null && object15 != Boolean.FALSE) {
            object2 = or__5238__auto__19023;
            or__5238__auto__19023 = null;
        } else {
            object2 = const__11.getRawRoot();
        }
        Object v = iFn.invoke(this_.db, this_.eid, object8, object11, object13, object2);
        IFn iFn2 = (IFn)const__12.getRawRoot();
        Object object16 = ret;
        ret = null;
        Object object17 = keyfn;
        keyfn = null;
        Object object18 = or__5238__auto__19024 = object17;
        if (object18 != null && object18 != Boolean.FALSE) {
            object = or__5238__auto__19024;
            or__5238__auto__19024 = null;
        } else {
            object = const__11.getRawRoot();
        }
        Object object19 = kw;
        kw = null;
        Object object20 = v;
        v = null;
        pull$pull_STAR_$fn__19020 this_ = null;
        return iFn2.invoke(object16, ((IFn)object).invoke(object19), object20);
    }
}

