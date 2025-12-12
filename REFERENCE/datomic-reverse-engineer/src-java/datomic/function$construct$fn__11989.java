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

public final class function$construct$fn__11989
extends AFunction {
    Object params;
    Object code;
    public static final Var const__0 = RT.var((String)"datomic.janino", (String)"java-data-fn");

    public function$construct$fn__11989(Object object, Object object2) {
        this.params = object;
        this.code = object2;
    }

    public Object invoke() {
        function$construct$fn__11989 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.params, this_.code);
    }
}

