/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class integrity$pod_storage_seq$mkv__22545$fn__22546
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.fressian", (String)"defressian");
    public static final Keyword const__1 = RT.keyword(null, (String)"handlers");
    public static final Var const__2 = RT.var((String)"datomic.log", (String)"read-handlers");

    public Object invoke(Object bb) {
        Object object;
        Object object2 = bb;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = bb;
            bb = null;
            integrity$pod_storage_seq$mkv__22545$fn__22546 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object3, (Object)const__1, const__2.getRawRoot());
        } else {
            object = null;
        }
        return object;
    }
}

