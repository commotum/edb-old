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
import datomic.db$require_tuple_ids$fn__12619;
import datomic.db$require_tuple_ids$fn__12621;
import datomic.db$require_tuple_ids$resolve__12616;

public final class db$require_tuple_ids
extends AFunction {
    public static final Keyword const__2 = RT.keyword((String)"db.type", (String)"ref");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"some");
    public static final Keyword const__8 = RT.keyword(null, (String)"default");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleType"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleTypes"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleTypes"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"tupleTypes"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object db2, Object attr, Object tup) {
        Object object;
        boolean or__5238__auto__12624 = Util.identical((Object)attr, null);
        if (or__5238__auto__12624 ? or__5238__auto__12624 : Util.identical((Object)tup, null)) {
            object = tup;
            tup = null;
        } else {
            Object object2 = db2;
            db2 = null;
            db$require_tuple_ids$resolve__12616 resolve = new db$require_tuple_ids$resolve__12616(tup, object2);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object3 = attr;
            Object object4 = iLookupThunk.get(object3);
            if (iLookupThunk == object4) {
                __thunk__0__ = __site__0__.fault(object3);
                object4 = __thunk__0__.get(object3);
            }
            if (Util.equiv((Object)const__2, (Object)object4)) {
                db$require_tuple_ids$resolve__12616 db$require_tuple_ids$resolve__12616 = resolve;
                resolve = null;
                Object object5 = tup;
                tup = null;
                object = ((IFn)const__4.getRawRoot()).invoke((Object)db$require_tuple_ids$resolve__12616, object5);
            } else {
                Object object6;
                Object and__5236__auto__12625;
                IFn iFn = (IFn)const__5.getRawRoot();
                db$require_tuple_ids$fn__12619 db$require_tuple_ids$fn__12619 = new db$require_tuple_ids$fn__12619();
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object7 = attr;
                Object object8 = iLookupThunk2.get(object7);
                if (iLookupThunk2 == object8) {
                    __thunk__1__ = __site__1__.fault(object7);
                    object8 = __thunk__1__.get(object7);
                }
                Object object9 = and__5236__auto__12625 = iFn.invoke((Object)db$require_tuple_ids$fn__12619, object8);
                if (object9 != null && object9 != Boolean.FALSE) {
                    ILookupThunk iLookupThunk3 = __thunk__2__;
                    Object object10 = attr;
                    Object object11 = iLookupThunk3.get(object10);
                    if (iLookupThunk3 == object11) {
                        __thunk__2__ = __site__2__.fault(object10);
                        object11 = __thunk__2__.get(object10);
                    }
                    object6 = Util.equiv((long)RT.count((Object)object11), (long)RT.count((Object)tup)) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    object6 = and__5236__auto__12625;
                    and__5236__auto__12625 = null;
                }
                if (object6 != null && object6 != Boolean.FALSE) {
                    IFn iFn2 = (IFn)const__4.getRawRoot();
                    db$require_tuple_ids$resolve__12616 db$require_tuple_ids$resolve__12616 = resolve;
                    resolve = null;
                    db$require_tuple_ids$fn__12621 db$require_tuple_ids$fn__12621 = new db$require_tuple_ids$fn__12621((Object)db$require_tuple_ids$resolve__12616);
                    ILookupThunk iLookupThunk4 = __thunk__3__;
                    Object object12 = attr;
                    attr = null;
                    Object object13 = iLookupThunk4.get(object12);
                    if (iLookupThunk4 == object13) {
                        __thunk__3__ = __site__3__.fault(object12);
                        object13 = __thunk__3__.get(object12);
                    }
                    Object object14 = tup;
                    tup = null;
                    object = iFn2.invoke((Object)db$require_tuple_ids$fn__12621, object13, object14);
                } else {
                    Keyword keyword = const__8;
                    if (keyword != null && keyword != Boolean.FALSE) {
                        object = tup;
                        tup = null;
                    } else {
                        object = null;
                    }
                }
            }
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
        return db$require_tuple_ids.invokeStatic(object4, object5, object6);
    }
}

