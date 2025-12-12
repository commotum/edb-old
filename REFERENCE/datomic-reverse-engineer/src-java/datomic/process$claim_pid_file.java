/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class process$claim_pid_file
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"spit");
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"pid");

    public static Object invokeStatic() {
        Object object;
        Object temp__5457__auto__14989;
        Object object2 = temp__5457__auto__14989 = ((IFn)const__0.getRawRoot()).invoke((Object)"datomic.pidFile");
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5457__auto__14989;
            temp__5457__auto__14989 = null;
            Object pid_file = object3;
            Object object4 = ((IFn)const__1.getRawRoot()).invoke(pid_file);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = pid_file;
                pid_file = null;
                object = ((IFn)const__2.getRawRoot()).invoke(object5, const__3.getRawRoot());
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke() {
        return process$claim_pid_file.invokeStatic();
    }
}

