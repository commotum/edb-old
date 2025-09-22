/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class datafy$define_method_to_fn$fn__17307
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__5 = (AFn)Symbol.intern((String)"datomic.datafy", (String)"data-to-object");
    public static final Keyword const__6 = RT.keyword(null, (String)"tag");

    public Object invoke(Object p__17306) {
        Object object = p__17306;
        p__17306 = null;
        Object vec__17308 = object;
        Object aname = RT.nth((Object)vec__17308, (int)RT.intCast((long)0L), null);
        Object object2 = vec__17308;
        vec__17308 = null;
        Object atype = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = aname;
        aname = null;
        Object object4 = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, object3, atype);
        Object[] objectArray = new Object[2];
        objectArray[0] = const__6;
        Object object5 = atype;
        atype = null;
        objectArray[1] = object5;
        datafy$define_method_to_fn$fn__17307 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object4, (Object)RT.mapUniqueKeys((Object[])objectArray));
    }
}

