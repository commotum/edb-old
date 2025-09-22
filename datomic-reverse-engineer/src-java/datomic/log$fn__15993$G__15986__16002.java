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

public final class log$fn__15993$G__15986__16002
extends AFunction {
    Object G__15987;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.log.Log");

    public log$fn__15993$G__15986__16002(Object object) {
        this.G__15987 = object;
    }

    public Object invoke(Object gf__log__15999, Object gf__cs__16000, Object gf__msgs__16001) {
        Object object;
        log$fn__15993$G__15986__16002 this_;
        IFn f__7644__auto__16005;
        MethodImplCache cache__7643__auto__16004;
        MethodImplCache methodImplCache = cache__7643__auto__16004 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__16004 = null;
        IFn iFn = f__7644__auto__16005 = methodImplCache.fnFor(Util.classOf((Object)gf__log__15999));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__16005;
            f__7644__auto__16005 = null;
            Object object2 = gf__log__15999;
            gf__log__15999 = null;
            Object object3 = gf__cs__16000;
            gf__cs__16000 = null;
            Object object4 = gf__msgs__16001;
            gf__msgs__16001 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__log__15999, const__1, this_.G__15987);
            Object object5 = gf__log__15999;
            gf__log__15999 = null;
            Object object6 = gf__cs__16000;
            gf__cs__16000 = null;
            Object object7 = gf__msgs__16001;
            gf__msgs__16001 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}

