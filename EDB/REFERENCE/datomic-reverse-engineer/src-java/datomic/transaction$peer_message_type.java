/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;

public final class transaction$peer_message_type
extends AFunction {
    public static final Keyword const__3 = RT.keyword(null, (String)"tx");
    public static final Keyword const__4 = RT.keyword(null, (String)"error");
    public static final Keyword const__5 = RT.keyword(null, (String)"index");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"type"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"id"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"data"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public static Object invokeStatic(Object msg) {
        Object object;
        Object or__5238__auto__15891;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = msg;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = or__5238__auto__15891 = object3;
        if (object4 != null && object4 != Boolean.FALSE) {
            object = or__5238__auto__15891;
            or__5238__auto__15891 = null;
        } else {
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object5 = msg;
            Object object6 = iLookupThunk2.get(object5);
            if (iLookupThunk2 == object6) {
                __thunk__1__ = __site__1__.fault(object5);
                object6 = __thunk__1__.get(object5);
            }
            if (object6 != null && object6 != Boolean.FALSE) {
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object7 = msg;
                msg = null;
                Object object8 = iLookupThunk3.get(object7);
                if (iLookupThunk3 == object8) {
                    __thunk__2__ = __site__2__.fault(object7);
                    object8 = __thunk__2__.get(object7);
                }
                object = object8 != null && object8 != Boolean.FALSE ? const__3 : const__4;
            } else {
                object = const__5;
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return transaction$peer_message_type.invokeStatic(object2);
    }
}

