/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class extensions$fulltext
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.extensions", (String)"project");
    public static final Var const__1 = RT.var((String)"datomic.fulltext", (String)"search");
    public static final AFn const__6 = (AFn)Tuple.create((Object)2L, (Object)3L, (Object)4L, (Object)5L);

    public static Object invokeStatic(Object db2, Object attr, Object qmap) {
        Object object = db2;
        db2 = null;
        Object object2 = attr;
        attr = null;
        Object object3 = qmap;
        qmap = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object, object2, object3), (Object)const__6);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return extensions$fulltext.invokeStatic(object4, object5, object6);
    }
}

