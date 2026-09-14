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

public final class connector$fn__21096$G__21072__21105
extends AFunction {
    Object G__21073;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.connector.TransactorConnector");

    public connector$fn__21096$G__21072__21105(Object object) {
        this.G__21073 = object;
    }

    public Object invoke(Object gf_____21102, Object gf__handler__21103, Object gf__failure_handler__21104) {
        Object object;
        connector$fn__21096$G__21072__21105 this_;
        IFn f__7644__auto__21108;
        MethodImplCache cache__7643__auto__21107;
        MethodImplCache methodImplCache = cache__7643__auto__21107 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__21107 = null;
        IFn iFn = f__7644__auto__21108 = methodImplCache.fnFor(Util.classOf((Object)gf_____21102));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__21108;
            f__7644__auto__21108 = null;
            Object object2 = gf_____21102;
            gf_____21102 = null;
            Object object3 = gf__handler__21103;
            gf__handler__21103 = null;
            Object object4 = gf__failure_handler__21104;
            gf__failure_handler__21104 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21102, const__1, this_.G__21073);
            Object object5 = gf_____21102;
            gf_____21102 = null;
            Object object6 = gf__handler__21103;
            gf__handler__21103 = null;
            Object object7 = gf__failure_handler__21104;
            gf__failure_handler__21104 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}

