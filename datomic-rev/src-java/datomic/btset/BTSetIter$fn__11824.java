/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.btset;

import clojure.lang.AFunction;
import datomic.btset.BTSetIterLink;

public final class BTSetIter$fn__11824
extends AFunction {
    Object branches;

    public BTSetIter$fn__11824(Object object) {
        this.branches = object;
    }

    public Object invoke() {
        Object object;
        block2: {
            Object link = this.branches;
            while (true) {
                Object object2 = link;
                if (object2 == null || object2 == Boolean.FALSE) break;
                Object cfr_ignored_0 = ((BTSetIterLink)link).branch;
                if (((BTSetIterLink)link).offset() > 0L) {
                    object = link;
                    link = null;
                    break block2;
                }
                Object object3 = link;
                link = null;
                link = ((BTSetIterLink)object3).parent;
            }
            object = null;
        }
        return object;
    }
}

