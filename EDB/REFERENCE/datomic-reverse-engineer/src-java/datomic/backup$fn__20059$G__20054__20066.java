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

public final class backup$fn__20059$G__20054__20066
extends AFunction {
    Object G__20055;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.backup.IValueBackup");

    public backup$fn__20059$G__20054__20066(Object object) {
        this.G__20055 = object;
    }

    public Object invoke(Object gf_____20064, Object gf__node__20065) {
        Object object;
        backup$fn__20059$G__20054__20066 this_;
        IFn f__7644__auto__20069;
        MethodImplCache cache__7643__auto__20068;
        MethodImplCache methodImplCache = cache__7643__auto__20068 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__20068 = null;
        IFn iFn = f__7644__auto__20069 = methodImplCache.fnFor(Util.classOf((Object)gf_____20064));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__20069;
            f__7644__auto__20069 = null;
            Object object2 = gf_____20064;
            gf_____20064 = null;
            Object object3 = gf__node__20065;
            gf__node__20065 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____20064, const__1, this_.G__20055);
            Object object4 = gf_____20064;
            gf_____20064 = null;
            Object object5 = gf__node__20065;
            gf__node__20065 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}

