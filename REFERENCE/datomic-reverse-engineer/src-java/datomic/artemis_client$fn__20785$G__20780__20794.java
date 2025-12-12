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

public final class artemis_client$fn__20785$G__20780__20794
extends AFunction {
    Object G__20781;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.artemis_client.HornetImpl");

    public artemis_client$fn__20785$G__20780__20794(Object object) {
        this.G__20781 = object;
    }

    public Object invoke(Object gf_____20791, Object gf__creds__20792, Object gf__args__20793) {
        Object object;
        artemis_client$fn__20785$G__20780__20794 this_;
        IFn f__7644__auto__20797;
        MethodImplCache cache__7643__auto__20796;
        MethodImplCache methodImplCache = cache__7643__auto__20796 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__20796 = null;
        IFn iFn = f__7644__auto__20797 = methodImplCache.fnFor(Util.classOf((Object)gf_____20791));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__20797;
            f__7644__auto__20797 = null;
            Object object2 = gf_____20791;
            gf_____20791 = null;
            Object object3 = gf__creds__20792;
            gf__creds__20792 = null;
            Object object4 = gf__args__20793;
            gf__args__20793 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____20791, const__1, this_.G__20781);
            Object object5 = gf_____20791;
            gf_____20791 = null;
            Object object6 = gf__creds__20792;
            gf__creds__20792 = null;
            Object object7 = gf__args__20793;
            gf__args__20793 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}

