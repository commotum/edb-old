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

public final class connector$host_order
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"nil?");
    public static final Keyword const__1 = RT.keyword(null, (String)"default");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__3 = RT.var((String)"datomic.aws-detect", (String)"running-in-ec2?");

    public static Object invokeStatic(Object host, Object alt_host) {
        Object object;
        if (Util.identical((Object)host, null)) {
            Object object2 = alt_host;
            alt_host = null;
            object = Tuple.create((Object)object2);
        } else if (Util.identical((Object)alt_host, null)) {
            Object object3 = host;
            host = null;
            object = Tuple.create((Object)object3);
        } else {
            Keyword keyword = const__1;
            if (keyword != null && keyword != Boolean.FALSE) {
                IPersistentVector iPersistentVector;
                IFn iFn = (IFn)const__2.getRawRoot();
                Object object4 = const__0.getRawRoot();
                Object object5 = ((IFn)const__3.getRawRoot()).invoke();
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object object6 = host;
                    host = null;
                    Object object7 = alt_host;
                    alt_host = null;
                    iPersistentVector = Tuple.create((Object)object6, (Object)object7);
                } else {
                    Object object8 = alt_host;
                    alt_host = null;
                    Object object9 = host;
                    host = null;
                    iPersistentVector = Tuple.create((Object)object8, (Object)object9);
                }
                object = iFn.invoke(object4, (Object)iPersistentVector);
            } else {
                object = null;
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return connector$host_order.invokeStatic(object3, object4);
    }
}

