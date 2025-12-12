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

public final class simple_kv$fn__16714$G__16679__16721
extends AFunction {
    Object G__16680;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.simple_kv.KV");

    public simple_kv$fn__16714$G__16679__16721(Object object) {
        this.G__16680 = object;
    }

    public Object invoke(Object gf_____16719, Object gf__key__16720) {
        Object object;
        simple_kv$fn__16714$G__16679__16721 this_;
        IFn f__7644__auto__16724;
        MethodImplCache cache__7643__auto__16723;
        MethodImplCache methodImplCache = cache__7643__auto__16723 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__16723 = null;
        IFn iFn = f__7644__auto__16724 = methodImplCache.fnFor(Util.classOf((Object)gf_____16719));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__16724;
            f__7644__auto__16724 = null;
            Object object2 = gf_____16719;
            gf_____16719 = null;
            Object object3 = gf__key__16720;
            gf__key__16720 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____16719, const__1, this_.G__16680);
            Object object4 = gf_____16719;
            gf_____16719 = null;
            Object object5 = gf__key__16720;
            gf__key__16720 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}

