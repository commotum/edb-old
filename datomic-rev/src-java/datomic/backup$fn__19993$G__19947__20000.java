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

public final class backup$fn__19993$G__19947__20000
extends AFunction {
    Object G__19948;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.backup.Storage");

    public backup$fn__19993$G__19947__20000(Object object) {
        this.G__19948 = object;
    }

    public Object invoke(Object gf_____19998, Object gf__k__19999) {
        Object object;
        backup$fn__19993$G__19947__20000 this_;
        IFn f__7644__auto__20003;
        MethodImplCache cache__7643__auto__20002;
        MethodImplCache methodImplCache = cache__7643__auto__20002 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__20002 = null;
        IFn iFn = f__7644__auto__20003 = methodImplCache.fnFor(Util.classOf((Object)gf_____19998));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__20003;
            f__7644__auto__20003 = null;
            Object object2 = gf_____19998;
            gf_____19998 = null;
            Object object3 = gf__k__19999;
            gf__k__19999 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____19998, const__1, this_.G__19948);
            Object object4 = gf_____19998;
            gf_____19998 = null;
            Object object5 = gf__k__19999;
            gf__k__19999 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}

