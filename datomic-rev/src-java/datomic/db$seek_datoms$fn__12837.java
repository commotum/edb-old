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

public final class db$seek_datoms$fn__12837
extends AFunction {
    Object eid;
    Object attrid;
    Object op;
    Object v;
    Object t;
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"windowed");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__2 = RT.keyword(null, (String)"e");
    public static final Keyword const__3 = RT.keyword(null, (String)"a");
    public static final Keyword const__4 = RT.keyword(null, (String)"v");
    public static final Keyword const__5 = RT.keyword(null, (String)"t");

    public db$seek_datoms$fn__12837(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.eid = object;
        this.attrid = object2;
        this.op = object3;
        this.v = object4;
        this.t = object5;
        this.db = object6;
    }

    public Object invoke() {
        db$seek_datoms$fn__12837 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, null, ((IFn)this_.op).invoke(this_.db, ((IFn)const__1.getRawRoot()).invoke(this_.db, (Object)const__2, this_.eid, (Object)const__3, this_.attrid, (Object)const__4, this_.v, (Object)const__5, this_.t)));
    }
}

