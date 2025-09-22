/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import datomic.Database;

public final class index$excise_ents$fn__15515
extends AFunction {
    Object db;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"e"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public index$excise_ents$fn__15515(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__15514_SHARP_) {
        Database database = (Database)this.db;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__15514_SHARP_;
        p1__15514_SHARP_ = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return database.entity(object2);
    }
}

