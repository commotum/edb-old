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
import datomic.Entity;
import datomic.db.Attribute;

public final class db$recalc_elements$fn__13630
extends AFunction {
    Object basis_db;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__6 = RT.keyword(null, (String)"storageHasAVET");
    public static final Var const__7 = RT.var((String)"datomic.db", (String)"needs-avet?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"id"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword((String)"db.install", (String)"_attribute"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public db$recalc_elements$fn__13630(Object object) {
        this.basis_db = object;
    }

    public Object invoke(Object e) {
        Object object;
        if (e instanceof Attribute) {
            Object has_attr_QMARK_;
            Database database = (Database)this_.basis_db;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object2 = e;
            Object object3 = iLookupThunk.get(object2);
            if (iLookupThunk == object3) {
                __thunk__0__ = __site__0__.fault(object2);
                object3 = __thunk__0__.get(object2);
            }
            Entity ent = database.entity(object3);
            IFn iFn = (IFn)const__3.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Entity entity2 = ent;
            Object object4 = iLookupThunk2.get((Object)entity2);
            if (iLookupThunk2 == object4) {
                __thunk__1__ = __site__1__.fault((Object)entity2);
                object4 = __thunk__1__.get((Object)entity2);
            }
            Object object5 = has_attr_QMARK_ = iFn.invoke(object4);
            has_attr_QMARK_ = null;
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = e;
                e = null;
                Entity entity3 = ent;
                ent = null;
                db$recalc_elements$fn__13630 this_ = null;
                object = ((IFn)const__5.getRawRoot()).invoke(object6, (Object)const__6, ((IFn)const__7.getRawRoot()).invoke((Object)entity3));
            } else {
                object = e;
                e = null;
            }
        } else {
            object = e;
            Object var1_1 = null;
        }
        return object;
    }
}

