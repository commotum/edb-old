/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.MethodImplCache
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.MethodImplCache;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class cluster$fn__10427$G__10422__10432
extends AFunction {
    Object G__10423;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cluster.Dbid");

    public cluster$fn__10427$G__10422__10432(Object object) {
        this.G__10423 = object;
    }

    public Object invoke(Object gf__c__10431) {
        Object object;
        cluster$fn__10427$G__10422__10432 this_;
        IFn f__7644__auto__10435;
        MethodImplCache cache__7643__auto__10434;
        MethodImplCache methodImplCache = cache__7643__auto__10434 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10434 = null;
        IFn iFn = f__7644__auto__10435 = methodImplCache.fnFor(Util.classOf((Object)gf__c__10431));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10435;
            f__7644__auto__10435 = null;
            Object object2 = gf__c__10431;
            gf__c__10431 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__c__10431, const__1, this_.G__10423);
            Object object3 = gf__c__10431;
            gf__c__10431 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}

