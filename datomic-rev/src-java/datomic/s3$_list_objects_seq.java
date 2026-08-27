/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.LazySeq
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.LazySeq;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.s3$_list_objects_seq$fn__23282;

public final class s3$_list_objects_seq
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"cons");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"truncated"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"objectSummaries"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"objectSummaries"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object s32, Object results) {
        Object object;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = results;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        if (object3 != null && object3 != Boolean.FALSE) {
            IFn iFn = (IFn)const__1.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object4 = results;
            Object object5 = iLookupThunk2.get(object4);
            if (iLookupThunk2 == object5) {
                __thunk__1__ = __site__1__.fault(object4);
                object5 = __thunk__1__.get(object4);
            }
            Object object6 = s32;
            s32 = null;
            Object object7 = results;
            results = null;
            object = iFn.invoke(object5, (Object)new LazySeq((IFn)new s3$_list_objects_seq$fn__23282(object6, object7)));
        } else {
            IFn iFn = (IFn)const__1.getRawRoot();
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object8 = results;
            results = null;
            Object object9 = iLookupThunk3.get(object8);
            if (iLookupThunk3 == object9) {
                __thunk__2__ = __site__2__.fault(object8);
                object9 = __thunk__2__.get(object8);
            }
            object = iFn.invoke(object9, null);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return s3$_list_objects_seq.invokeStatic(object3, object4);
    }
}

