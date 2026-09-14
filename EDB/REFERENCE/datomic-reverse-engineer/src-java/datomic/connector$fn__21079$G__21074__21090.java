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

public final class connector$fn__21079$G__21074__21090
extends AFunction {
    Object G__21075;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.connector.TransactorConnector");

    public connector$fn__21079$G__21074__21090(Object object) {
        this.G__21075 = object;
    }

    public Object invoke(Object gf_____21086, Object gf__update_queue__21087, Object gf__push_handler__21088, Object gf__failure_handler__21089) {
        Object object;
        connector$fn__21079$G__21074__21090 this_;
        IFn f__7644__auto__21093;
        MethodImplCache cache__7643__auto__21092;
        MethodImplCache methodImplCache = cache__7643__auto__21092 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__21092 = null;
        IFn iFn = f__7644__auto__21093 = methodImplCache.fnFor(Util.classOf((Object)gf_____21086));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__21093;
            f__7644__auto__21093 = null;
            Object object2 = gf_____21086;
            gf_____21086 = null;
            Object object3 = gf__update_queue__21087;
            gf__update_queue__21087 = null;
            Object object4 = gf__push_handler__21088;
            gf__push_handler__21088 = null;
            Object object5 = gf__failure_handler__21089;
            gf__failure_handler__21089 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21086, const__1, this_.G__21075);
            Object object6 = gf_____21086;
            gf_____21086 = null;
            Object object7 = gf__update_queue__21087;
            gf__update_queue__21087 = null;
            Object object8 = gf__push_handler__21088;
            gf__push_handler__21088 = null;
            Object object9 = gf__failure_handler__21089;
            gf__failure_handler__21089 = null;
            this_ = null;
            object = iFn3.invoke(object6, object7, object8, object9);
        }
        return object;
    }
}

