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
import datomic.btset.IDataSet;
import datomic.integrity$unpaired_history_assertions$fn__22097;

public final class integrity$unpaired_history_assertions
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.tools", (String)"unsorted-seq");
    public static final Var const__3 = RT.var((String)"datomic.iter", (String)"iter-seq");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"history"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2, Object sort, Object progress) {
        Object object;
        Object temp__5457__auto__22101;
        Object object2;
        Object G__22096;
        Object object3;
        Object object4 = db2;
        db2 = null;
        Object G__220962 = object4;
        if (Util.identical((Object)G__220962, null)) {
            object3 = null;
        } else {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object5 = G__220962;
            G__220962 = null;
            object3 = iLookupThunk.get(object5);
            if (iLookupThunk == object3) {
                __thunk__0__ = __site__0__.fault(object5);
                object3 = G__22096 = __thunk__0__.get(object5);
            }
        }
        if (Util.identical(G__22096, null)) {
            object2 = null;
        } else {
            Object object6 = sort;
            sort = null;
            Object object7 = G__22096;
            G__22096 = null;
            object2 = ((IFn)object6).invoke(object7);
        }
        Object object8 = temp__5457__auto__22101 = object2;
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = temp__5457__auto__22101;
            temp__5457__auto__22101 = null;
            Object hist = object9;
            Object object10 = progress;
            progress = null;
            Object object11 = hist;
            hist = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)new integrity$unpaired_history_assertions$fn__22097(object10), ((IFn)const__3.getRawRoot()).invoke((Object)((IDataSet)object11).seek()));
        } else {
            object = null;
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
        return integrity$unpaired_history_assertions.invokeStatic(object4, object5, object6);
    }
}

