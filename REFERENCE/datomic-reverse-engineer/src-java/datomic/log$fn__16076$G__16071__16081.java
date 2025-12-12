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

public final class log$fn__16076$G__16071__16081
extends AFunction {
    Object G__16072;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.log.TailTxes");

    public log$fn__16076$G__16071__16081(Object object) {
        this.G__16072 = object;
    }

    public Object invoke(Object gf_____16080) {
        Object object;
        log$fn__16076$G__16071__16081 this_;
        IFn f__7644__auto__16084;
        MethodImplCache cache__7643__auto__16083;
        MethodImplCache methodImplCache = cache__7643__auto__16083 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__16083 = null;
        IFn iFn = f__7644__auto__16084 = methodImplCache.fnFor(Util.classOf((Object)gf_____16080));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__16084;
            f__7644__auto__16084 = null;
            Object object2 = gf_____16080;
            gf_____16080 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____16080, const__1, this_.G__16072);
            Object object3 = gf_____16080;
            gf_____16080 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}

