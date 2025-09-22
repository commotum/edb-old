/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.index;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index.DirNode;
import datomic.index.TreeIter$iter__15092__15098$fn__15099$iter__15094__15100;

public final class TreeIter$iter__15092__15098$fn__15099
extends AFunction {
    Object s__15093;
    Object lookup;
    Object root;
    Object iter__15092;
    int ridx;
    int didx;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"datomic.index", (String)"get-dir-node");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"rest");

    public TreeIter$iter__15092__15098$fn__15099(Object object, Object object2, Object object3, Object object4, int n, int n2) {
        this.s__15093 = object;
        this.lookup = object2;
        this.root = object3;
        this.iter__15092 = object4;
        this.ridx = n;
        this.didx = n2;
    }

    public Object invoke() {
        Object object;
        block2: {
            Object s__15093 = this_.s__15093 = null;
            while (true) {
                Object fs__6022__auto__15113;
                TreeIter$iter__15092__15098$fn__15099$iter__15094__15100 iterys__6021__auto__15112;
                Object xs__6012__auto__15114;
                Object temp__5457__auto__15115;
                Object object2 = temp__5457__auto__15115 = ((IFn)const__0.getRawRoot()).invoke(s__15093);
                if (object2 == null || object2 == Boolean.FALSE) break;
                Object object3 = temp__5457__auto__15115;
                temp__5457__auto__15115 = null;
                Object object4 = xs__6012__auto__15114 = object3;
                xs__6012__auto__15114 = null;
                Object ri = ((IFn)const__1.getRawRoot()).invoke(object4);
                Object d = ((IFn)const__2.getRawRoot()).invoke(this_.root, ri, this_.lookup, (Object)Boolean.FALSE);
                Object object5 = ri;
                ri = null;
                TreeIter$iter__15092__15098$fn__15099$iter__15094__15100 treeIter$iter__15092__15098$fn__15099$iter__15094__15100 = iterys__6021__auto__15112 = new TreeIter$iter__15092__15098$fn__15099$iter__15094__15100(d, object5, this_.ridx, this_.didx);
                iterys__6021__auto__15112 = null;
                Object object6 = d;
                d = null;
                Object object7 = fs__6022__auto__15113 = ((IFn)const__0.getRawRoot()).invoke(((IFn)treeIter$iter__15092__15098$fn__15099$iter__15094__15100).invoke(((IFn)const__3.getRawRoot()).invoke((Object)RT.count((Object)((DirNode)object6).segids))));
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = fs__6022__auto__15113;
                    fs__6022__auto__15113 = null;
                    Object object9 = s__15093;
                    s__15093 = null;
                    TreeIter$iter__15092__15098$fn__15099 this_ = null;
                    object = ((IFn)const__5.getRawRoot()).invoke(object8, ((IFn)this_.iter__15092).invoke(((IFn)const__6.getRawRoot()).invoke(object9)));
                    break block2;
                }
                Object object10 = s__15093;
                s__15093 = null;
                s__15093 = ((IFn)const__6.getRawRoot()).invoke(object10);
            }
            object = null;
        }
        return object;
    }
}

