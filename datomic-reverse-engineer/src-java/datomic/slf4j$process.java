/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class slf4j$process
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map?");
    public static final Keyword const__1 = RT.keyword(null, (String)"message");
    public static final Var const__2 = RT.var((String)"datomic.slf4j", (String)"print-safely");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__4 = RT.keyword(null, (String)"pid");
    public static final Var const__5 = RT.var((String)"datomic.slf4j", (String)"pid");
    public static final Keyword const__6 = RT.keyword(null, (String)"tid");

    public static Object invokeStatic(Object msg) {
        Object msg2;
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(msg);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = msg;
            msg = null;
        } else {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__1;
            Object object3 = msg;
            msg = null;
            objectArray[1] = object3;
            object = RT.mapUniqueKeys((Object[])objectArray);
        }
        Object object4 = msg2 = object;
        msg2 = null;
        return ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(object4, (Object)const__4, const__5.getRawRoot(), (Object)const__6, (Object)Numbers.num((long)Thread.currentThread().getId())));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return slf4j$process.invokeStatic(object2);
    }
}

