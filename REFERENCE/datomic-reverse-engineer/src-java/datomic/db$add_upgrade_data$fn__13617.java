/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Database;
import datomic.db.DbId;
import datomic.db.IDbImpl;
import java.util.List;
import java.util.Map;

public final class db$add_upgrade_data$fn__13617
extends AFunction {
    Object epoch;
    public static final Object const__1 = 0L;
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__7 = RT.keyword(null, (String)"basisT");
    public static final Object const__8 = -1L;
    public static final Keyword const__9 = RT.keyword(null, (String)"nextT");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"cons");
    public static final Keyword const__11 = RT.keyword((String)"db", (String)"id");
    public static final Object const__12 = DbId.create(RT.map((Object[])new Object[]{RT.keyword(null, (String)"idx"), -1000001L, RT.keyword(null, (String)"part"), RT.keyword((String)"db.part", (String)"tx")}));
    public static final Keyword const__13 = RT.keyword((String)"db", (String)"txInstant");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-after"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public db$add_upgrade_data$fn__13617(Object object) {
        this.epoch = object;
    }

    public Object invoke(Object db2, Object p__13616) {
        Object object;
        Object object2;
        Object object3 = p__13616;
        p__13616 = null;
        Object vec__13618 = object3;
        Object k = RT.nth((Object)vec__13618, (int)RT.uncheckedIntCast((long)0L), null);
        Object object4 = vec__13618;
        vec__13618 = null;
        Object data2 = RT.nth((Object)object4, (int)RT.uncheckedIntCast((long)1L), null);
        Object object5 = k;
        k = null;
        Object G__13621 = ((IFn)const__3.getRawRoot()).invoke(db2, object5);
        if (Util.identical((Object)G__13621, null)) {
            object2 = null;
        } else {
            Object object6 = G__13621;
            G__13621 = null;
            object2 = ((IDbImpl)db2).elementAt(object6);
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            object = db2;
            db2 = null;
        } else {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object7 = db2;
            db2 = null;
            Object object8 = data2;
            data2 = null;
            Map map2 = ((Database)((IFn)const__6.getRawRoot()).invoke(object7, (Object)const__7, const__8, (Object)const__9, const__1)).with((List)((IFn)const__10.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__11, const__12, const__13, this.epoch}), object8));
            object = iLookupThunk.get((Object)map2);
            if (iLookupThunk == object) {
                __thunk__0__ = __site__0__.fault((Object)map2);
                object = __thunk__0__.get((Object)map2);
            }
        }
        return object;
    }
}

