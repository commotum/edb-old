/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic.btset;

import clojure.lang.AFunction;
import datomic.btset.BTSetIterLink;
import datomic.btset.IBTSetBranch;

public final class BTSetIter$fn__11822
extends AFunction {
    Object branches;

    public BTSetIter$fn__11822(Object object) {
        this.branches = object;
    }

    public Object invoke() {
        Object object;
        block2: {
            Object link = this.branches;
            while (true) {
                Object b;
                Object object2 = link;
                if (object2 == null || object2 == Boolean.FALSE) break;
                Object object3 = b = ((BTSetIterLink)link).branch;
                b = null;
                if (((BTSetIterLink)link).offset() + 1L < ((IBTSetBranch)object3).count()) {
                    object = link;
                    link = null;
                    break block2;
                }
                Object object4 = link;
                link = null;
                link = ((BTSetIterLink)object4).parent;
            }
            object = null;
        }
        return object;
    }
}

