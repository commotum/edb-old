/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.AttrInfo;
import datomic.db.IDbImpl;

public final class db$attr_info
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"attribute");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"vtypeid"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"kw"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object db2, Object attrid) {
        AttrInfo attrInfo;
        Object temp__5457__auto__12733;
        Object object;
        Object object2 = attrid;
        attrid = null;
        Object G__12731 = ((IFn)const__0.getRawRoot()).invoke(db2, object2);
        if (Util.identical((Object)G__12731, null)) {
            object = null;
        } else {
            Object object3 = G__12731;
            G__12731 = null;
            object = ((IFn)const__2.getRawRoot()).invoke(db2, object3);
        }
        Object object4 = temp__5457__auto__12733 = object;
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5457__auto__12733;
            temp__5457__auto__12733 = null;
            Object attr = object5;
            ILookupThunk iLookupThunk = __thunk__1__;
            Object object6 = db2;
            db2 = null;
            IDbImpl iDbImpl = (IDbImpl)object6;
            ILookupThunk iLookupThunk2 = __thunk__0__;
            Object object7 = attr;
            attr = null;
            Object object8 = iLookupThunk2.get(object7);
            if (iLookupThunk2 == object8) {
                __thunk__0__ = __site__0__.fault(object7);
                object8 = __thunk__0__.get(object7);
            }
            Object object9 = iDbImpl.elementAt(object8);
            Object object10 = iLookupThunk.get(object9);
            if (iLookupThunk == object10) {
                __thunk__1__ = __site__1__.fault(object9);
                object10 = __thunk__1__.get(object9);
            }
            attrInfo = new AttrInfo(attr, object10);
        } else {
            attrInfo = null;
        }
        return attrInfo;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$attr_info.invokeStatic(object3, object4);
    }
}

