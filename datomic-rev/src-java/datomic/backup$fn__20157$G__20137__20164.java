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

public final class backup$fn__20157$G__20137__20164
extends AFunction {
    Object G__20138;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.backup.IValueRestore");

    public backup$fn__20157$G__20137__20164(Object object) {
        this.G__20138 = object;
    }

    public Object invoke(Object gf_____20162, Object gf__k__20163) {
        Object object;
        backup$fn__20157$G__20137__20164 this_;
        IFn f__7644__auto__20167;
        MethodImplCache cache__7643__auto__20166;
        MethodImplCache methodImplCache = cache__7643__auto__20166 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__20166 = null;
        IFn iFn = f__7644__auto__20167 = methodImplCache.fnFor(Util.classOf((Object)gf_____20162));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__20167;
            f__7644__auto__20167 = null;
            Object object2 = gf_____20162;
            gf_____20162 = null;
            Object object3 = gf__k__20163;
            gf__k__20163 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____20162, const__1, this_.G__20138);
            Object object4 = gf_____20162;
            gf_____20162 = null;
            Object object5 = gf__k__20163;
            gf__k__20163 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}

