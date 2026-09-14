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

public final class puts_pool$fn__9835$G__9828__9846
extends AFunction {
    Object G__9829;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.valcache.puts_pool.PutsPool");

    public puts_pool$fn__9835$G__9828__9846(Object object) {
        this.G__9829 = object;
    }

    public Object invoke(Object gf_____9842, Object gf__k__9843, Object gf__data__9844, Object gf__f__9845) {
        Object object;
        puts_pool$fn__9835$G__9828__9846 this_;
        IFn f__7644__auto__9849;
        MethodImplCache cache__7643__auto__9848;
        MethodImplCache methodImplCache = cache__7643__auto__9848 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__9848 = null;
        IFn iFn = f__7644__auto__9849 = methodImplCache.fnFor(Util.classOf((Object)gf_____9842));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__9849;
            f__7644__auto__9849 = null;
            Object object2 = gf_____9842;
            gf_____9842 = null;
            Object object3 = gf__k__9843;
            gf__k__9843 = null;
            Object object4 = gf__data__9844;
            gf__data__9844 = null;
            Object object5 = gf__f__9845;
            gf__f__9845 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____9842, const__1, this_.G__9829);
            Object object6 = gf_____9842;
            gf_____9842 = null;
            Object object7 = gf__k__9843;
            gf__k__9843 = null;
            Object object8 = gf__data__9844;
            gf__data__9844 = null;
            Object object9 = gf__f__9845;
            gf__f__9845 = null;
            this_ = null;
            object = iFn3.invoke(object6, object7, object8, object9);
        }
        return object;
    }
}

