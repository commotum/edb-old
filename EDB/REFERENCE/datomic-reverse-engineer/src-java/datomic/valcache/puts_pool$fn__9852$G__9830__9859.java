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
package datomic.valcache;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.MethodImplCache;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class puts_pool$fn__9852$G__9830__9859
extends AFunction {
    Object G__9831;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.valcache.puts_pool.PutsPool");

    public puts_pool$fn__9852$G__9830__9859(Object object) {
        this.G__9831 = object;
    }

    public Object invoke(Object gf_____9857, Object gf__k__9858) {
        Object object;
        puts_pool$fn__9852$G__9830__9859 this_;
        IFn f__7644__auto__9862;
        MethodImplCache cache__7643__auto__9861;
        MethodImplCache methodImplCache = cache__7643__auto__9861 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__9861 = null;
        IFn iFn = f__7644__auto__9862 = methodImplCache.fnFor(Util.classOf((Object)gf_____9857));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__9862;
            f__7644__auto__9862 = null;
            Object object2 = gf_____9857;
            gf_____9857 = null;
            Object object3 = gf__k__9858;
            gf__k__9858 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____9857, const__1, this_.G__9831);
            Object object4 = gf_____9857;
            gf_____9857 = null;
            Object object5 = gf__k__9858;
            gf__k__9858 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}

