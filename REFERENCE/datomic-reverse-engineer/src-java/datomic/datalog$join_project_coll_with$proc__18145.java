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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class datalog$join_project_coll_with$proc__18145
extends AFunction {
    Object ys;
    Object match_QMARK_;
    Object ht;
    Object ret;
    Object project;
    Object hashx;
    Object predctor;
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"iterator");
    public static final Var const__1 = RT.var((String)"datomic.datalog", (String)"maybe-cancel");

    public datalog$join_project_coll_with$proc__18145(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.ys = object;
        this.match_QMARK_ = object2;
        this.ht = object3;
        this.ret = object4;
        this.project = object5;
        this.hashx = object6;
        this.predctor = object7;
    }

    public Object invoke(Object xs) {
        Object v10;
        Object pred2 = ((IFn)this.predctor).invoke();
        Object object = xs;
        xs = null;
        Object xiter = ((IFn)const__0.getRawRoot()).invoke(object);
        ((IFn)const__1.getRawRoot()).invoke();
        Object object2 = this.ht;
        if (object2 != null && object2 != Boolean.FALSE) {
            while (((Iterator)xiter).hasNext()) {
                Object ys;
                Object temp__5457__auto__18147;
                Object x = ((Iterator)xiter).next();
                Object v = temp__5457__auto__18147 = ((Map)this.ht).get(((IFn)this.hashx).invoke(x));
                if (v == null || v == Boolean.FALSE) continue;
                Object v2 = temp__5457__auto__18147;
                temp__5457__auto__18147 = null;
                Object v3 = ys = v2;
                ys = null;
                Object yiter = ((IFn)const__0.getRawRoot()).invoke(v3);
                while (((Iterator)yiter).hasNext()) {
                    Object y = ((Iterator)yiter).next();
                    Object object3 = ((IFn)this.match_QMARK_).invoke(x, y);
                    if (object3 == null || object3 == Boolean.FALSE) continue;
                    Object e = y;
                    y = null;
                    Object p = ((IFn)this.project).invoke(x, e);
                    Object object4 = ((IFn)pred2).invoke(p);
                    if (object4 == null || object4 == Boolean.FALSE) continue;
                    Object object5 = p;
                    p = null;
                    Boolean bl = ((Set)this.ret).add(object5) ? Boolean.TRUE : Boolean.FALSE;
                }
            }
            v10 = null;
        } else {
            while (((Iterator)xiter).hasNext()) {
                Object x = ((Iterator)xiter).next();
                Object yiter = ((IFn)const__0.getRawRoot()).invoke(this.ys);
                while (((Iterator)yiter).hasNext()) {
                    Object y = ((Iterator)yiter).next();
                    Object object6 = ((IFn)this.match_QMARK_).invoke(x, y);
                    if (object6 == null || object6 == Boolean.FALSE) continue;
                    Object e = y;
                    y = null;
                    Object p = ((IFn)this.project).invoke(x, e);
                    Object object7 = ((IFn)pred2).invoke(p);
                    if (object7 == null || object7 == Boolean.FALSE) continue;
                    Object object8 = p;
                    p = null;
                    Boolean bl = ((Set)this.ret).add(object8) ? Boolean.TRUE : Boolean.FALSE;
                }
            }
            v10 = null;
        }
        return v10;
    }
}

