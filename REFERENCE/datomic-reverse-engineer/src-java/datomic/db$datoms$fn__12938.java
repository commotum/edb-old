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
import datomic.db$datoms$fn__12938$fn__12939;

public final class db$datoms$fn__12938
extends AFunction {
    Object db;
    Object t;
    Object attrid;
    Object eid;
    Object v;
    Object op;
    Object a;
    Object e;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"windowed");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__2 = RT.keyword(null, (String)"e");
    public static final Keyword const__3 = RT.keyword(null, (String)"a");
    public static final Keyword const__4 = RT.keyword(null, (String)"v");
    public static final Keyword const__5 = RT.keyword(null, (String)"t");

    public db$datoms$fn__12938(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.db = object;
        this.t = object2;
        this.attrid = object3;
        this.eid = object4;
        this.v = object5;
        this.op = object6;
        this.a = object7;
        this.e = object8;
    }

    public Object invoke() {
        db$datoms$fn__12938 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, (Object)new db$datoms$fn__12938$fn__12939(this_.t, this_.attrid, this_.eid, this_.v, this_.a, this_.e), ((IFn)this_.op).invoke(this_.db, ((IFn)const__1.getRawRoot()).invoke(this_.db, (Object)const__2, this_.eid, (Object)const__3, this_.attrid, (Object)const__4, this_.v, (Object)const__5, this_.t)));
    }
}

