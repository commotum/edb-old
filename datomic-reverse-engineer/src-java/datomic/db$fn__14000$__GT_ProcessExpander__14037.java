/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.ProcessExpander;

public final class db$fn__14000$__GT_ProcessExpander__14037
extends AFunction {
    public Object invoke(Object db2, Object part_reqs, Object arraylist, Object attr_hook_attrs, Object prefetch_dispatcher, Object tx_stat_registers) {
        Object object = db2;
        db2 = null;
        Object object2 = part_reqs;
        part_reqs = null;
        Object object3 = arraylist;
        arraylist = null;
        Object object4 = attr_hook_attrs;
        attr_hook_attrs = null;
        Object object5 = prefetch_dispatcher;
        prefetch_dispatcher = null;
        Object object6 = tx_stat_registers;
        tx_stat_registers = null;
        return new ProcessExpander(object, object2, object3, object4, object5, object6);
    }
}

