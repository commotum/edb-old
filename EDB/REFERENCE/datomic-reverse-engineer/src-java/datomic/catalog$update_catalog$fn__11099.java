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

public final class catalog$update_catalog$fn__11099
extends AFunction {
    Object f;
    Object condition;
    Object cluster;
    public static final Var const__0 = RT.var((String)"datomic.catalog", (String)"get-catalog");
    public static final Var const__1 = RT.var((String)"datomic.catalog", (String)"put-catalog");
    public static final Keyword const__3 = RT.keyword(null, (String)"old");
    public static final Keyword const__4 = RT.keyword(null, (String)"new");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"failed"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public catalog$update_catalog$fn__11099(Object object, Object object2, Object object3) {
        this.f = object;
        this.condition = object2;
        this.cluster = object3;
    }

    public Object invoke() {
        Object object;
        Object temp__5455__auto__11101;
        Object catalog2 = ((IFn)const__0.getRawRoot()).invoke(this.cluster);
        Object object2 = temp__5455__auto__11101 = ((IFn)this.condition).invoke(catalog2);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object result2;
            Object object3 = temp__5455__auto__11101;
            temp__5455__auto__11101 = null;
            object = result2 = object3;
            result2 = null;
        } else {
            Object resp = ((IFn)const__1.getRawRoot()).invoke(this.cluster, ((IFn)this.f).invoke(catalog2));
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object4 = resp;
            Object object5 = iLookupThunk.get(object4);
            if (iLookupThunk == object5) {
                __thunk__0__ = __site__0__.fault(object4);
                object5 = __thunk__0__.get(object4);
            }
            if (object5 != null && object5 != Boolean.FALSE) {
                object = resp;
                resp = null;
            } else {
                Object[] objectArray = new Object[4];
                objectArray[0] = const__3;
                Object object6 = catalog2;
                catalog2 = null;
                objectArray[1] = object6;
                objectArray[2] = const__4;
                Object object7 = resp;
                resp = null;
                objectArray[3] = object7;
                object = RT.mapUniqueKeys((Object[])objectArray);
            }
        }
        return object;
    }
}

