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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public final class common$cx$alist__9029
extends AFunction {
    Object cmp;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");

    public common$cx$alist__9029(Object object) {
        this.cmp = object;
    }

    public Object invoke(Object x) {
        Object xs;
        Object object = x;
        x = null;
        Object object2 = xs = ((IFn)const__0.getRawRoot()).invoke(object);
        xs = null;
        ArrayList G__9030 = new ArrayList((Collection)object2);
        Collections.sort(G__9030, (Comparator)this.cmp);
        Object var3_3 = null;
        return G__9030;
    }
}

