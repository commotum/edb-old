/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2.log.ddb;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class Log$next_r__20646
extends AFunction {
    Object direction;
    public static final Keyword const__4 = RT.keyword(null, (String)"forward");
    public static final Keyword const__6 = RT.keyword(null, (String)"backward");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"LastEvaluatedKey"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"r"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"N"));
    static ILookupThunk __thunk__2__ = __site__2__;

    public Log$next_r__20646(Object object) {
        this.direction = object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object resp) {
        Number temp__5804__auto__20650;
        Object G__20647;
        Object object;
        Object G__206472;
        Object object2;
        Object G__206473;
        Object object3;
        Object object4 = resp;
        resp = null;
        Object G__206474 = object4;
        if (Util.identical((Object)G__206474, null)) {
            object3 = null;
        } else {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object5 = G__206474;
            G__206474 = null;
            object3 = iLookupThunk.get(object5);
            if (iLookupThunk == object3) {
                __thunk__0__ = __site__0__.fault(object5);
                object3 = G__206473 = __thunk__0__.get(object5);
            }
        }
        if (Util.identical(G__206473, null)) {
            object2 = null;
        } else {
            ILookupThunk iLookupThunk = __thunk__1__;
            Object object6 = G__206473;
            G__206473 = null;
            object2 = iLookupThunk.get(object6);
            if (iLookupThunk == object2) {
                __thunk__1__ = __site__1__.fault(object6);
                object2 = G__206472 = __thunk__1__.get(object6);
            }
        }
        if (Util.identical(G__206472, null)) {
            object = null;
        } else {
            ILookupThunk iLookupThunk = __thunk__2__;
            Object object7 = G__206472;
            G__206472 = null;
            object = iLookupThunk.get(object7);
            if (iLookupThunk == object) {
                __thunk__2__ = __site__2__.fault(object7);
                object = G__20647 = __thunk__2__.get(object7);
            }
        }
        if (Util.identical(G__20647, null)) {
            return null;
        }
        Object object8 = G__20647;
        G__20647 = null;
        Number number = Numbers.num((long)Long.parseLong((String)object8));
        Number number2 = temp__5804__auto__20650 = number;
        if (number2 == null) return null;
        if (number2 == Boolean.FALSE) return null;
        Number number3 = temp__5804__auto__20650;
        temp__5804__auto__20650 = null;
        Number r = number3;
        Object G__20648 = this_.direction;
        switch (Util.hash((Object)G__20648) >> 0 & 1) {
            case 0: {
                if (G__20648 != const__4) break;
                Number number4 = r;
                r = null;
                Log$next_r__20646 this_ = null;
                Number number5 = Numbers.inc((Object)number4);
                return number5;
            }
            case 1: {
                if (G__20648 != const__6) break;
                Number number6 = r;
                r = null;
                Log$next_r__20646 this_ = null;
                Number number5 = Numbers.dec((Object)number6);
                return number5;
            }
        }
        Object object9 = G__20648;
        G__20648 = null;
        throw (Throwable)new IllegalArgumentException((String)((IFn)const__8.getRawRoot()).invoke((Object)"No matching clause: ", object9));
    }
}

