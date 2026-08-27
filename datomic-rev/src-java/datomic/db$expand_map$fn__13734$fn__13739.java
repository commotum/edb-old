/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class db$expand_map$fn__13734$fn__13739
extends AFunction {
    Object dbid;
    Object db;
    Object local_tempids;
    Object attrid;
    Object part_reqs;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"nested-entity-map?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"expand-submap");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"conj");
    public static final Keyword const__4 = RT.keyword((String)"db", (String)"add");

    public db$expand_map$fn__13734$fn__13739(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.dbid = object;
        this.db = object2;
        this.local_tempids = object3;
        this.attrid = object4;
        this.part_reqs = object5;
    }

    public Object invoke(Object result2, Object v) {
        Object object;
        db$expand_map$fn__13734$fn__13739 this_;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(this_.db, this_.attrid, v);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = result2;
            result2 = null;
            Object object4 = v;
            v = null;
            this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, ((IFn)const__2.getRawRoot()).invoke(this_.db, this_.dbid, this_.attrid, object4, this_.part_reqs, this_.local_tempids));
        } else {
            Object object5 = result2;
            result2 = null;
            Object object6 = v;
            v = null;
            this_ = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object5, (Object)Tuple.create((Object)const__4, (Object)this_.dbid, (Object)this_.attrid, (Object)object6));
        }
        return object;
    }
}

