/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic.db;

import clojure.lang.AFunction;
import clojure.lang.IFn;

public final class ProcessExpander$replace_tempids__14011$fn__14018
extends AFunction {
    Object tempid_at_index_QMARK_;
    Object replace_tempid;

    public ProcessExpander$replace_tempids__14011$fn__14018(Object object, Object object2) {
        this.tempid_at_index_QMARK_ = object;
        this.replace_tempid = object2;
    }

    public Object invoke(Object idx, Object elem) {
        Object object;
        Object object2 = idx;
        idx = null;
        Object object3 = ((IFn)this_.tempid_at_index_QMARK_).invoke(object2, elem);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = elem;
            elem = null;
            ProcessExpander$replace_tempids__14011$fn__14018 this_ = null;
            object = ((IFn)this_.replace_tempid).invoke(object4);
        } else {
            object = elem;
            Object var2_2 = null;
        }
        return object;
    }
}

