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
import datomic.pull$ra__GT_e$mk_iter__18906$fn__18907;
import datomic.pull$ra__GT_e$mk_iter__18906$fn__18909;

public final class pull$ra__GT_e$mk_iter__18906
extends AFunction {
    Object db;
    Object rid;
    Object attrid;
    public static final Var const__0 = RT.var((String)"datomic.iter", (String)"map");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"windowed");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__3 = RT.keyword(null, (String)"v");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");

    public pull$ra__GT_e$mk_iter__18906(Object object, Object object2, Object object3) {
        this.db = object;
        this.rid = object2;
        this.attrid = object3;
    }

    public Object invoke() {
        pull$ra__GT_e$mk_iter__18906 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new pull$ra__GT_e$mk_iter__18906$fn__18907(), ((IFn)const__1.getRawRoot()).invoke(this_.db, (Object)new pull$ra__GT_e$mk_iter__18906$fn__18909(this_.rid, this_.attrid), (Object)((IDb)this_.db).seekRAET((IDatum)((IFn)const__2.getRawRoot()).invoke(this_.db, (Object)const__3, this_.rid, (Object)const__4, this_.attrid))));
    }
}

