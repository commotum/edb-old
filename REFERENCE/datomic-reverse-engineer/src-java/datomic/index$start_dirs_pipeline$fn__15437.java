/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class index$start_dirs_pipeline$fn__15437
extends RestFn {
    Object dir_entries_ch;
    Object serialized_dirs_ch;
    public static final Var const__0 = RT.var((String)"clojure.core.async", (String)"close!");

    public index$start_dirs_pipeline$fn__15437(Object object, Object object2) {
        this.dir_entries_ch = object;
        this.serialized_dirs_ch = object2;
    }

    public Object doInvoke(Object _) {
        ((IFn)const__0.getRawRoot()).invoke(this_.dir_entries_ch);
        index$start_dirs_pipeline$fn__15437 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.serialized_dirs_ch);
    }

    public int getRequiredArity() {
        return 0;
    }
}

