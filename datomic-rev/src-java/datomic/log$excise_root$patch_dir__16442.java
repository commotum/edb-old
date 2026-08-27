/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log$excise_root$patch_dir__16442$fn__16443;

public final class log$excise_root$patch_dir__16442
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public Object invoke(Object replacements, Object dir) {
        Object object = replacements;
        replacements = null;
        Object object2 = dir;
        dir = null;
        log$excise_root$patch_dir__16442 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new log$excise_root$patch_dir__16442$fn__16443(object), (Object)PersistentVector.EMPTY, object2);
    }
}

