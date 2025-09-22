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

public final class function$construct$fn__11991
extends AFunction {
    Object params;
    Object requires;
    Object code;
    Object imports;
    public static final Var const__0 = RT.var((String)"datomic.function", (String)"compile-clojure");

    public function$construct$fn__11991(Object object, Object object2, Object object3, Object object4) {
        this.params = object;
        this.requires = object2;
        this.code = object3;
        this.imports = object4;
    }

    public Object invoke() {
        function$construct$fn__11991 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.imports, this_.requires, this_.params, this_.code);
    }
}

