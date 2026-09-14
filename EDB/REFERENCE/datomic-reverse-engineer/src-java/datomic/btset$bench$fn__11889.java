/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public final class btset$bench$fn__11889
extends AFunction {
    Object ts;

    public btset$bench$fn__11889(Object object) {
        this.ts = object;
    }

    public Object invoke() {
        Iterator s = ((TreeSet)this.ts).iterator();
        Object ret = Numbers.num((long)0L);
        while (s.hasNext()) {
            Iterator iterator2 = s;
            Iterator iterator3 = s;
            s = null;
            ret = ((Set)((TreeSet)this.ts).tailSet(iterator3.next())).iterator();
            s = iterator2;
        }
        Object var2_2 = null;
        return ret;
    }
}

