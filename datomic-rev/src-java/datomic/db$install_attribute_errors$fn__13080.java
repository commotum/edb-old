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

public final class db$install_attribute_errors$fn__13080
extends AFunction {
    Object eafter;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"cons");
    public static final Keyword const__2 = RT.keyword((String)"db", (String)"error");
    public static final Keyword const__3 = RT.keyword((String)"db.error", (String)"schema-attribute-missing");
    public static final Keyword const__4 = RT.keyword(null, (String)"attribute");
    public static final Keyword const__5 = RT.keyword(null, (String)"entity");

    public db$install_attribute_errors$fn__13080(Object object) {
        this.eafter = object;
    }

    public Object invoke(Object errs, Object k) {
        Object object;
        Object object2 = RT.get((Object)this_.eafter, (Object)k);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = errs;
            errs = null;
        } else {
            Object[] objectArray = new Object[6];
            objectArray[0] = const__2;
            objectArray[1] = const__3;
            objectArray[2] = const__4;
            Object object3 = k;
            k = null;
            objectArray[3] = object3;
            objectArray[4] = const__5;
            objectArray[5] = this_.eafter;
            Object object4 = errs;
            errs = null;
            db$install_attribute_errors$fn__13080 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray), object4);
        }
        return object;
    }
}

