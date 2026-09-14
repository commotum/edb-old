/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class aws$endpoint_for
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"name");
    public static final String const__1 = "iam";
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object service, Object region) {
        Object object;
        Object object2 = service;
        service = null;
        Object sname = ((IFn)const__0.getRawRoot()).invoke(object2);
        Object object3 = region;
        region = null;
        Object rname = ((IFn)const__0.getRawRoot()).invoke(object3);
        Object G__17389 = sname;
        switch (Util.hash((Object)G__17389)) {
            case 104021: {
                if (Util.equiv((Object)G__17389, (Object)const__1)) {
                    object = "iam.amazonaws.com";
                    break;
                }
            }
            default: {
                Object object4 = sname;
                sname = null;
                Object object5 = rname;
                rname = null;
                object = ((IFn)const__2.getRawRoot()).invoke(object4, (Object)".", object5, (Object)".amazonaws.com");
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return aws$endpoint_for.invokeStatic(object3, object4);
    }
}

