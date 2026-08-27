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

public final class aws$credentials_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"aws-access-key-id"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"aws-secret-key"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object m) {
        Object object;
        Object and__5236__auto__17384;
        Object object2 = and__5236__auto__17384 = ((IFn)const__0.getRawRoot()).invoke(m);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object and__5236__auto__17383;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object3 = m;
            Object object4 = iLookupThunk.get(object3);
            if (iLookupThunk == object4) {
                __thunk__0__ = __site__0__.fault(object3);
                object4 = __thunk__0__.get(object3);
            }
            Object object5 = and__5236__auto__17383 = object4;
            if (object5 != null && object5 != Boolean.FALSE) {
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object6 = m;
                m = null;
                object = iLookupThunk2.get(object6);
                if (iLookupThunk2 == object) {
                    __thunk__1__ = __site__1__.fault(object6);
                    object = __thunk__1__.get(object6);
                }
            } else {
                object = and__5236__auto__17383;
                Object var2_2 = null;
            }
        } else {
            object = and__5236__auto__17384;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aws$credentials_QMARK_.invokeStatic(object2);
    }
}

