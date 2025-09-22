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

public final class integrity$validate_garbage$fn__22461
extends AFunction {
    Object root_key;
    Object valid_QMARK_;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reduced");
    public static final Keyword const__2 = RT.keyword(null, (String)"valid");
    public static final Keyword const__3 = RT.keyword(null, (String)"desc");
    public static final Keyword const__4 = RT.keyword(null, (String)"root-key");
    public static final Keyword const__5 = RT.keyword(null, (String)"leaves");

    public integrity$validate_garbage$fn__22461(Object object, Object object2) {
        this.root_key = object;
        this.valid_QMARK_ = object2;
    }

    public Object invoke(Object n, Object k) {
        Object object;
        integrity$validate_garbage$fn__22461 this_;
        Object object2 = k;
        k = null;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(((IFn)this_.valid_QMARK_).invoke(object2));
        if (object3 != null && object3 != Boolean.FALSE) {
            Object[] objectArray = new Object[8];
            objectArray[0] = const__2;
            objectArray[1] = Boolean.FALSE;
            objectArray[2] = const__3;
            objectArray[3] = "Invalid key in garbage leaf seq";
            objectArray[4] = const__4;
            objectArray[5] = this_.root_key;
            objectArray[6] = const__5;
            Object object4 = n;
            n = null;
            objectArray[7] = object4;
            this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
        } else {
            Object object5 = n;
            n = null;
            this_ = null;
            object = Numbers.inc((Object)object5);
        }
        return object;
    }
}

