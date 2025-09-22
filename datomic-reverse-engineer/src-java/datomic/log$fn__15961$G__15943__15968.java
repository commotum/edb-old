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

public final class log$fn__15961$G__15943__15968
extends AFunction {
    Object G__15944;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.log.LogSeek");

    public log$fn__15961$G__15943__15968(Object object) {
        this.G__15944 = object;
    }

    public Object invoke(Object gf__log__15966, Object gf__t__15967) {
        Object object;
        log$fn__15961$G__15943__15968 this_;
        IFn f__7644__auto__15971;
        MethodImplCache cache__7643__auto__15970;
        MethodImplCache methodImplCache = cache__7643__auto__15970 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__15970 = null;
        IFn iFn = f__7644__auto__15971 = methodImplCache.fnFor(Util.classOf((Object)gf__log__15966));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__15971;
            f__7644__auto__15971 = null;
            Object object2 = gf__log__15966;
            gf__log__15966 = null;
            Object object3 = gf__t__15967;
            gf__t__15967 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__log__15966, const__1, this_.G__15944);
            Object object4 = gf__log__15966;
            gf__log__15966 = null;
            Object object5 = gf__t__15967;
            gf__t__15967 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}

