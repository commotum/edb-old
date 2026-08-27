/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.AttrInfo;

public final class db$fn__12714$__GT_AttrInfo__12726
extends AFunction {
    public Object invoke(Object attr, Object vtype_kw) {
        Object object = attr;
        attr = null;
        Object object2 = vtype_kw;
        vtype_kw = null;
        return new AttrInfo(object, object2);
    }
}

