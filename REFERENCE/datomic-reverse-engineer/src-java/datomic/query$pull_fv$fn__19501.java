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
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.query$pull_fv$fn__19501$fn__19503;

public final class query$pull_fv$fn__19501
extends AFunction {
    Object srcs;
    Object smap;
    Object srcmap;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"db");
    public static final Keyword const__4 = RT.keyword(null, (String)"var");
    public static final Keyword const__5 = RT.keyword(null, (String)"pattern");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__7 = RT.var((String)"datomic.common", (String)"getx");

    public query$pull_fv$fn__19501(Object object, Object object2, Object object3) {
        this.srcs = object;
        this.smap = object2;
        this.srcmap = object3;
    }

    public Object invoke(Object m, Object p__19500) {
        Object object;
        Object object2 = p__19500;
        p__19500 = null;
        Object map__19502 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__19502);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__19502;
            map__19502 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__19502;
            map__19502 = null;
        }
        Object map__195022 = object;
        Object db2 = RT.get((Object)map__195022, (Object)const__3);
        Object var = RT.get((Object)map__195022, (Object)const__4);
        Object object5 = map__195022;
        map__195022 = null;
        Object pattern = RT.get((Object)object5, (Object)const__5);
        Object object6 = m;
        m = null;
        Object object7 = var;
        var = null;
        Object object8 = pattern;
        pattern = null;
        Object object9 = db2;
        db2 = null;
        query$pull_fv$fn__19501 this_ = null;
        return ((IFn)const__6.getRawRoot()).invoke(object6, ((IFn)const__7.getRawRoot()).invoke(this_.smap, object7), (Object)new query$pull_fv$fn__19501$fn__19503(object8, this_.srcs, this_.srcmap, object9));
    }
}

