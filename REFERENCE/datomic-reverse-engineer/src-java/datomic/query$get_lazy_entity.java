/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.IDb;
import datomic.impl.db.IDatum;
import datomic.query$get_lazy_entity$fn__19162;
import datomic.query$get_lazy_entity$fn__19168;

public final class query$get_lazy_entity
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__1 = RT.var((String)"datomic.iter", (String)"reduce");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"windowed");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__4 = RT.keyword(null, (String)"e");

    public static Object invokeStatic(Object db2, Object ent) {
        Object object = ent;
        ent = null;
        Object eid = ((IFn)const__0.getRawRoot()).invoke(db2, object);
        query$get_lazy_entity$fn__19162 query$get_lazy_entity$fn__19162 = new query$get_lazy_entity$fn__19162(db2);
        Object object2 = db2;
        query$get_lazy_entity$fn__19168 query$get_lazy_entity$fn__19168 = new query$get_lazy_entity$fn__19168(eid);
        IDb iDb = (IDb)db2;
        Object object3 = db2;
        db2 = null;
        Object object4 = eid;
        eid = null;
        Object ret = ((IFn)const__1.getRawRoot()).invoke((Object)query$get_lazy_entity$fn__19162, null, ((IFn)const__2.getRawRoot()).invoke(object2, (Object)query$get_lazy_entity$fn__19168, (Object)iDb.seekEAVT((IDatum)((IFn)const__3.getRawRoot()).invoke(object3, (Object)const__4, object4))));
        Object var3_3 = null;
        return ret;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return query$get_lazy_entity.invokeStatic(object3, object4);
    }
}

