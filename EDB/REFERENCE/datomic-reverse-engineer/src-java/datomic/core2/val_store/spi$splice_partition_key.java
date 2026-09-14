/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.regex.Pattern;

public final class spi$splice_partition_key
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"includes?");
    public static final Var const__1 = RT.var((String)"clojure.string", (String)"replace");
    public static final Object const__2 = Pattern.compile("(/[^/]*$)");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object k, Object pk) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(k, (Object)"/");
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = k;
            k = null;
            Object object4 = pk;
            pk = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, const__2, ((IFn)const__3.getRawRoot()).invoke((Object)"/", object4, (Object)"$1"));
        } else {
            Object object5 = pk;
            pk = null;
            Object object6 = k;
            k = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object5, (Object)"/", object6);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return spi$splice_partition_key.invokeStatic(object3, object4);
    }
}

