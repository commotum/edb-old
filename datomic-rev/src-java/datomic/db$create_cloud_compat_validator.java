/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$create_cloud_compat_validator$fn__13347;

public final class db$create_cloud_compat_validator
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"cloud-compat-validator");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"cloud-compat"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2) {
        Object object;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = db2;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = db2;
            db2 = null;
            object = ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), object4);
        } else {
            object = new db$create_cloud_compat_validator$fn__13347();
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$create_cloud_compat_validator.invokeStatic(object2);
    }
}

