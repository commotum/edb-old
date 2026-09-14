/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Indexed
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.log;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Indexed;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class LogTxIter$iter__16252__16256$fn__16257$fn__16258
extends AFunction {
    Object b__16255;
    Object root_val;
    Object lookup;
    int size__6024__auto__;
    Object c__6023__auto__;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"chunk-append");
    public static final Var const__4 = RT.var((String)"datomic.common", (String)"getx");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"uuid"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public LogTxIter$iter__16252__16256$fn__16257$fn__16258(Object object, Object object2, Object object3, int n, Object object4) {
        this.b__16255 = object;
        this.root_val = object2;
        this.lookup = object3;
        this.size__6024__auto__ = n;
        this.c__6023__auto__ = object4;
    }

    public Object invoke() {
        for (long i__16254 = (long)RT.intCast((long)0L); i__16254 < (long)this.size__6024__auto__; ++i__16254) {
            Object ri = ((Indexed)this.c__6023__auto__).nth(RT.intCast((long)i__16254));
            IFn iFn = (IFn)const__3.getRawRoot();
            IFn iFn2 = (IFn)const__4.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object = ri;
            ri = null;
            Object object2 = RT.nth((Object)this.root_val, (int)RT.intCast((Object)((Number)object)));
            Object object3 = iLookupThunk.get(object2);
            if (iLookupThunk == object3) {
                __thunk__0__ = __site__0__.fault(object2);
                object3 = __thunk__0__.get(object2);
            }
            iFn.invoke(this.b__16255, iFn2.invoke(this.lookup, object3));
        }
        return Boolean.TRUE;
    }
}

