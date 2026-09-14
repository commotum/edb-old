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

public final class backup$ensure_claim
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.backup", (String)"claimed-by");
    public static final Var const__2 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__3 = RT.keyword((String)"backup", (String)"claim-failed");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__5 = RT.keyword(null, (String)"ok");
    public static final Var const__7 = RT.var((String)"datomic.backup", (String)"claim");
    public static final Var const__8 = RT.var((String)"datomic.error", (String)"raise");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"k"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object storage, Object id) {
        while (true) {
            Object k;
            Object temp__5455__auto__20025;
            Object object = temp__5455__auto__20025 = ((IFn)const__0.getRawRoot()).invoke(storage);
            if (object != null && object != Boolean.FALSE) {
                Object object2 = temp__5455__auto__20025;
                temp__5455__auto__20025 = null;
                Object claimant = object2;
                Object object3 = id;
                id = null;
                if (Util.equiv((Object)claimant, (Object)object3)) {
                    break;
                }
                Object object4 = claimant;
                claimant = null;
                ((IFn)const__2.getRawRoot()).invoke((Object)const__3, ((IFn)const__4.getRawRoot()).invoke((Object)"Backup storage already used by ", object4));
                break;
            }
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object5 = ((IFn)const__7.getRawRoot()).invoke(storage, id);
            Object object6 = iLookupThunk.get(object5);
            if (iLookupThunk == object6) {
                __thunk__0__ = __site__0__.fault(object5);
                object6 = __thunk__0__.get(object5);
            }
            Object object7 = k = object6;
            k = null;
            if (object7 != null && object7 != Boolean.FALSE) {
            } else {
                ((IFn)const__8.getRawRoot()).invoke((Object)const__3, (Object)"Unable to write to backup storage");
            }
            Object object8 = storage;
            storage = null;
            Object object9 = id;
            id = null;
            id = object9;
            storage = object8;
        }
        return const__5;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$ensure_claim.invokeStatic(object3, object4);
    }
}

