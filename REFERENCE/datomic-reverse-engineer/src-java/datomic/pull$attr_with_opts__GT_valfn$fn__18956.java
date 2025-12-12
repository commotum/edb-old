/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Util;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class pull$attr_with_opts__GT_valfn$fn__18956
extends AFunction {
    Object default;

    public pull$attr_with_opts__GT_valfn$fn__18956(Object object) {
        this.default = object;
    }

    public Object invoke(Object v) {
        Object object;
        if (Util.identical((Object)v, null)) {
            object = this.default;
        } else {
            object = v;
            Object var1_1 = null;
        }
        return object;
    }
}

