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

public final class log$fn__16030$G__15984__16041
extends AFunction {
    Object G__15985;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.log.Log");

    public log$fn__16030$G__15984__16041(Object object) {
        this.G__15985 = object;
    }

    public Object invoke(Object gf__log__16037, Object gf__cs__16038, Object gf__root_id__16039, Object gf__t__16040) {
        Object object;
        log$fn__16030$G__15984__16041 this_;
        IFn f__7644__auto__16044;
        MethodImplCache cache__7643__auto__16043;
        MethodImplCache methodImplCache = cache__7643__auto__16043 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__16043 = null;
        IFn iFn = f__7644__auto__16044 = methodImplCache.fnFor(Util.classOf((Object)gf__log__16037));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__16044;
            f__7644__auto__16044 = null;
            Object object2 = gf__log__16037;
            gf__log__16037 = null;
            Object object3 = gf__cs__16038;
            gf__cs__16038 = null;
            Object object4 = gf__root_id__16039;
            gf__root_id__16039 = null;
            Object object5 = gf__t__16040;
            gf__t__16040 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__log__16037, const__1, this_.G__15985);
            Object object6 = gf__log__16037;
            gf__log__16037 = null;
            Object object7 = gf__cs__16038;
            gf__cs__16038 = null;
            Object object8 = gf__root_id__16039;
            gf__root_id__16039 = null;
            Object object9 = gf__t__16040;
            gf__t__16040 = null;
            this_ = null;
            object = iFn3.invoke(object6, object7, object8, object9);
        }
        return object;
    }
}

