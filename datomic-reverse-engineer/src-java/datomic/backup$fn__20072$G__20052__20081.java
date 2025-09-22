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

public final class backup$fn__20072$G__20052__20081
extends AFunction {
    Object G__20053;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.backup.IValueBackup");

    public backup$fn__20072$G__20052__20081(Object object) {
        this.G__20053 = object;
    }

    public Object invoke(Object gf_____20078, Object gf__k__20079, Object gf__backup_k__20080) {
        Object object;
        backup$fn__20072$G__20052__20081 this_;
        IFn f__7644__auto__20084;
        MethodImplCache cache__7643__auto__20083;
        MethodImplCache methodImplCache = cache__7643__auto__20083 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__20083 = null;
        IFn iFn = f__7644__auto__20084 = methodImplCache.fnFor(Util.classOf((Object)gf_____20078));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__20084;
            f__7644__auto__20084 = null;
            Object object2 = gf_____20078;
            gf_____20078 = null;
            Object object3 = gf__k__20079;
            gf__k__20079 = null;
            Object object4 = gf__backup_k__20080;
            gf__backup_k__20080 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____20078, const__1, this_.G__20053);
            Object object5 = gf_____20078;
            gf_____20078 = null;
            Object object6 = gf__k__20079;
            gf__k__20079 = null;
            Object object7 = gf__backup_k__20080;
            gf__backup_k__20080 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}

