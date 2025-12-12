/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;

public final class db$create_simple_alter$fn__13202
extends AFunction {
    Object elem_key;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc-in");
    public static final Keyword const__1 = RT.keyword(null, (String)"elements");
    public static final Keyword const__3 = RT.keyword(null, (String)"disabled");

    public db$create_simple_alter$fn__13202(Object object) {
        this.elem_key = object;
    }

    public Object invoke(Object db2, Object aid, Object _, Object vafter) {
        Object object;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = db2;
        db2 = null;
        Object object3 = aid;
        aid = null;
        IPersistentVector iPersistentVector = Tuple.create((Object)const__1, (Object)object3, (Object)this.elem_key);
        if (Util.equiv((Object)const__3, (Object)vafter)) {
            object = Boolean.FALSE;
        } else {
            object = vafter;
            vafter = null;
        }
        return Tuple.create((Object)iFn.invoke(object2, (Object)iPersistentVector, object));
    }
}

