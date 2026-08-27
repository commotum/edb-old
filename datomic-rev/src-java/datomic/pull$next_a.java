/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Datom;
import datomic.db.IDb;
import datomic.impl.db.IDatum;
import datomic.pull$next_a$fn__18983;

public final class pull$next_a
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"windowed");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__2 = RT.keyword(null, (String)"e");
    public static final Keyword const__3 = RT.keyword(null, (String)"a");
    public static final Var const__5 = RT.var((String)"datomic.iter", (String)"iget");

    public static Object invokeStatic(Object db2, Object e, Object a) {
        Object object;
        Object object2;
        Object d;
        Object and__5236__auto__18986;
        Object iter2;
        Object object3 = db2;
        IDb iDb = (IDb)db2;
        Object object4 = db2;
        db2 = null;
        Object object5 = a;
        a = null;
        Object object6 = iter2 = ((IFn)const__0.getRawRoot()).invoke(object3, (Object)new pull$next_a$fn__18983(e), (Object)iDb.seekEAVT((IDatum)((IFn)const__1.getRawRoot()).invoke(object4, (Object)const__2, e, (Object)const__3, (Object)Numbers.inc((Object)object5))));
        iter2 = null;
        Object object7 = and__5236__auto__18986 = (d = ((IFn)const__5.getRawRoot()).invoke(object6));
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = e;
            e = null;
            object2 = Util.equiv((Object)object8, (Object)((Datom)d).e()) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object2 = and__5236__auto__18986;
            and__5236__auto__18986 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object9 = d;
            d = null;
            object = ((Datom)object9).a();
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return pull$next_a.invokeStatic(object4, object5, object6);
    }
}

