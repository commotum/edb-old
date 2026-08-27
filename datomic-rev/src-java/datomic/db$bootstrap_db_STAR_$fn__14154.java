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
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Database;
import datomic.db.DbId;
import java.util.List;
import java.util.Map;

public final class db$bootstrap_db_STAR_$fn__14154
extends AFunction {
    Object epoch;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"conj");
    public static final Keyword const__2 = RT.keyword((String)"db", (String)"id");
    public static final Object const__3 = DbId.create(RT.map((Object[])new Object[]{RT.keyword(null, (String)"idx"), -1000004L, RT.keyword(null, (String)"part"), RT.keyword((String)"db.part", (String)"tx")}));
    public static final Keyword const__4 = RT.keyword((String)"db", (String)"txInstant");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"db-after"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public db$bootstrap_db_STAR_$fn__14154(Object object) {
        this.epoch = object;
    }

    public Object invoke(Object p1__14146_SHARP_, Object p2__14147_SHARP_) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__14146_SHARP_;
        p1__14146_SHARP_ = null;
        Object object2 = p2__14147_SHARP_;
        p2__14147_SHARP_ = null;
        Map map2 = ((Database)object).with((List)((IFn)const__1.getRawRoot()).invoke(object2, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__2, const__3, const__4, this.epoch})));
        Object object3 = iLookupThunk.get((Object)map2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault((Object)map2);
            object3 = __thunk__0__.get((Object)map2);
        }
        return object3;
    }
}

