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

public final class connector$fn__20979$G__20974__20984
extends AFunction {
    Object G__20975;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.connector.Startable");

    public connector$fn__20979$G__20974__20984(Object object) {
        this.G__20975 = object;
    }

    public Object invoke(Object gf_____20983) {
        Object object;
        connector$fn__20979$G__20974__20984 this_;
        IFn f__7644__auto__20987;
        MethodImplCache cache__7643__auto__20986;
        MethodImplCache methodImplCache = cache__7643__auto__20986 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__20986 = null;
        IFn iFn = f__7644__auto__20987 = methodImplCache.fnFor(Util.classOf((Object)gf_____20983));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__20987;
            f__7644__auto__20987 = null;
            Object object2 = gf_____20983;
            gf_____20983 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____20983, const__1, this_.G__20975);
            Object object3 = gf_____20983;
            gf_____20983 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}

