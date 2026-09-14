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

public final class aws_monitor$fn__23574$G__23569__23579
extends AFunction {
    Object G__23570;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.aws_monitor.Qn");

    public aws_monitor$fn__23574$G__23569__23579(Object object) {
        this.G__23570 = object;
    }

    public Object invoke(Object gf_____23578) {
        Object object;
        aws_monitor$fn__23574$G__23569__23579 this_;
        IFn f__7644__auto__23582;
        MethodImplCache cache__7643__auto__23581;
        MethodImplCache methodImplCache = cache__7643__auto__23581 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__23581 = null;
        IFn iFn = f__7644__auto__23582 = methodImplCache.fnFor(Util.classOf((Object)gf_____23578));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__23582;
            f__7644__auto__23582 = null;
            Object object2 = gf_____23578;
            gf_____23578 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____23578, const__1, this_.G__23570);
            Object object3 = gf_____23578;
            gf_____23578 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}

