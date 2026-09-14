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

public final class qtune$partial_queries$fn__23433
extends AFunction {
    Object allow_cross;
    Object rclauses;
    Object query;
    Object clauses;
    Object preds;
    public static final Var const__0 = RT.var((String)"datomic.qtune", (String)"partial-query");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"disj");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"assoc");

    public qtune$partial_queries$fn__23433(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.allow_cross = object;
        this.rclauses = object2;
        this.query = object3;
        this.clauses = object4;
        this.preds = object5;
    }

    public Object invoke(Object m, Object c) {
        Object object;
        Object temp__5455__auto__23435;
        Object object2 = temp__5455__auto__23435 = ((IFn)const__0.getRawRoot()).invoke(this_.query, this_.preds, this_.clauses, c, ((IFn)const__1.getRawRoot()).invoke(this_.rclauses, c), this_.allow_cross);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5455__auto__23435;
            temp__5455__auto__23435 = null;
            Object q2 = object3;
            Object object4 = m;
            m = null;
            Object object5 = c;
            c = null;
            Object object6 = q2;
            q2 = null;
            qtune$partial_queries$fn__23433 this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object4, object5, object6);
        } else {
            object = m;
            Object var1_1 = null;
        }
        return object;
    }
}

