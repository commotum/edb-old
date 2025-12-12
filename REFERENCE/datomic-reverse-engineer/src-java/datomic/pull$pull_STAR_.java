/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Database;
import datomic.pull$pull_STAR_$fn__19020;
import datomic.pull$pull_STAR_$fn__19026;
import datomic.pull$pull_STAR_$fn__19031;
import datomic.pull$pull_STAR_$mk_xf__19010;

public final class pull$pull_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"wildcard");
    public static final Keyword const__4 = RT.keyword(null, (String)"dbid");
    public static final Var const__5 = RT.var((String)"datomic.pull", (String)"fix-specs-for-underscore-prefix-attrs");
    public static final Keyword const__6 = RT.keyword(null, (String)"forward");
    public static final Keyword const__7 = RT.keyword(null, (String)"reverse");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__9 = RT.var((String)"datomic.pull", (String)"resolve-attr");
    public static final Var const__10 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"transient");
    public static final Var const__12 = RT.var((String)"datomic.pull", (String)"denormalize-kw");
    public static final Keyword const__13 = RT.keyword((String)"db", (String)"id");
    public static final Var const__14 = RT.var((String)"datomic.iter", (String)"reduce");
    public static final Var const__15 = RT.var((String)"datomic.pull", (String)"a-iter");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"reduce-kv");
    public static final Var const__17 = RT.var((String)"datomic.pull", (String)"nilify-empty");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"persistent!");

    public static Object invokeStatic(Object db2, Object p__19007, Object recursed, Object prefer_aevt_QMARK_, Object e) {
        Object ret;
        Object object;
        PersistentArrayMap persistentArrayMap;
        Object object2;
        Object map__19008;
        Object object3;
        Object object4 = p__19007;
        p__19007 = null;
        Object map__190082 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__190082);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__190082;
            map__190082 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__190082;
            map__190082 = null;
        }
        Object spec = map__19008 = object3;
        Object wildcard = RT.get((Object)map__19008, (Object)const__3);
        Object object7 = map__19008;
        map__19008 = null;
        Object dbid = RT.get((Object)object7, (Object)const__4);
        if (((Database)db2).isHistory()) {
            throw (Throwable)new IllegalStateException("Can't pull from history");
        }
        Object map__19009 = ((IFn)const__5.getRawRoot()).invoke(spec, db2);
        Object object8 = ((IFn)const__0.getRawRoot()).invoke(map__19009);
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = map__19009;
            map__19009 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object9)));
        } else {
            object2 = map__19009;
            map__19009 = null;
        }
        Object map__190092 = object2;
        Object forward = RT.get((Object)map__190092, (Object)const__6);
        Object object10 = map__190092;
        map__190092 = null;
        Object reverse = RT.get((Object)object10, (Object)const__7);
        Object kw__GT_attr = ((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), db2);
        Object object11 = spec;
        spec = null;
        Object object12 = recursed;
        recursed = null;
        pull$pull_STAR_$mk_xf__19010 mk_xf = new pull$pull_STAR_$mk_xf__19010(object11, db2, object12, prefer_aevt_QMARK_, e);
        Object object13 = e;
        e = null;
        Object eid = ((IFn)const__10.getRawRoot()).invoke(db2, object13);
        IFn iFn = (IFn)const__11.getRawRoot();
        Object object14 = dbid;
        if (object14 != null && object14 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            Object object15 = dbid;
            dbid = null;
            objectArray[0] = ((IFn)const__12.getRawRoot()).invoke((Object)const__13, object15);
            objectArray[1] = eid;
            persistentArrayMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            persistentArrayMap = PersistentArrayMap.EMPTY;
        }
        Object ret2 = iFn.invoke((Object)persistentArrayMap);
        Object object16 = wildcard;
        if (object16 != null && object16 != Boolean.FALSE) {
            Object object17 = ret2;
            ret2 = null;
            object = ((IFn)const__14.getRawRoot()).invoke((Object)new pull$pull_STAR_$fn__19020(wildcard, eid, db2, forward, (Object)mk_xf), object17, ((IFn)const__15.getRawRoot()).invoke(db2, eid));
        } else {
            object = ret2;
            ret2 = null;
        }
        Object ret3 = object;
        Object object18 = wildcard;
        wildcard = null;
        Object object19 = prefer_aevt_QMARK_;
        prefer_aevt_QMARK_ = null;
        Object object20 = ret3;
        ret3 = null;
        Object object21 = forward;
        forward = null;
        Object ret4 = ((IFn)const__16.getRawRoot()).invoke((Object)new pull$pull_STAR_$fn__19026(object18, eid, db2, kw__GT_attr, object19, (Object)mk_xf), object20, object21);
        Object object22 = eid;
        eid = null;
        Object object23 = db2;
        db2 = null;
        Object object24 = kw__GT_attr;
        kw__GT_attr = null;
        pull$pull_STAR_$mk_xf__19010 pull$pull_STAR_$mk_xf__19010 = mk_xf;
        mk_xf = null;
        Object object25 = ret4;
        ret4 = null;
        Object object26 = reverse;
        reverse = null;
        Object object27 = ret = ((IFn)const__16.getRawRoot()).invoke((Object)new pull$pull_STAR_$fn__19031(object22, object23, object24, (Object)pull$pull_STAR_$mk_xf__19010), object25, object26);
        ret = null;
        return ((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke(object27));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return pull$pull_STAR_.invokeStatic(object6, object7, object8, object9, object10);
    }
}

