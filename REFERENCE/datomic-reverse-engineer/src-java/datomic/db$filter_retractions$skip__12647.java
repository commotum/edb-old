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
import datomic.impl.db.IDatum;

public final class db$filter_retractions$skip__12647
extends AFunction {
    Object eat_past;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"dget");

    public db$filter_retractions$skip__12647(Object object) {
        this.eat_past = object;
    }

    public Object invoke(Object i) {
        Object object;
        block2: {
            while (true) {
                Object temp__5457__auto__12649;
                Object object2 = temp__5457__auto__12649 = ((IFn)const__0.getRawRoot()).invoke(i);
                if (object2 == null || object2 == Boolean.FALSE) break;
                Object object3 = temp__5457__auto__12649;
                temp__5457__auto__12649 = null;
                Object d = object3;
                if (((IDatum)d).isAssertion()) {
                    object = i;
                    i = null;
                    break block2;
                }
                Object object4 = d;
                d = null;
                Object object5 = i;
                i = null;
                i = ((IFn)this.eat_past).invoke(object4, object5);
            }
            object = null;
        }
        return object;
    }
}

