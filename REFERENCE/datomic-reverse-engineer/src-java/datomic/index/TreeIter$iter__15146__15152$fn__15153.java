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
import datomic.index.TreeIter$iter__15146__15152$fn__15153$iter__15148__15154;

public final class TreeIter$iter__15146__15152$fn__15153
extends AFunction {
    Object lookup;
    Object root;
    Object iter__15146;
    Object s__15147;
    int ridx;
    int didx;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"datomic.index", (String)"get-dir-node");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"rest");

    public TreeIter$iter__15146__15152$fn__15153(Object object, Object object2, Object object3, Object object4, int n, int n2) {
        this.lookup = object;
        this.root = object2;
        this.iter__15146 = object3;
        this.s__15147 = object4;
        this.ridx = n;
        this.didx = n2;
    }

    public Object invoke() {
        Object object;
        block2: {
            Object s__15147 = this_.s__15147 = null;
            while (true) {
                Object fs__6022__auto__15167;
                TreeIter$iter__15146__15152$fn__15153$iter__15148__15154 iterys__6021__auto__15166;
                Object xs__6012__auto__15168;
                Object temp__5457__auto__15169;
                Object object2 = temp__5457__auto__15169 = ((IFn)const__0.getRawRoot()).invoke(s__15147);
                if (object2 == null || object2 == Boolean.FALSE) break;
                Object object3 = temp__5457__auto__15169;
                temp__5457__auto__15169 = null;
                Object object4 = xs__6012__auto__15168 = object3;
                xs__6012__auto__15168 = null;
                Object ri = ((IFn)const__1.getRawRoot()).invoke(object4);
                Object d = ((IFn)const__2.getRawRoot()).invoke(this_.root, ri, this_.lookup, (Object)Boolean.FALSE);
                Object object5 = ri;
                ri = null;
                TreeIter$iter__15146__15152$fn__15153$iter__15148__15154 treeIter$iter__15146__15152$fn__15153$iter__15148__15154 = iterys__6021__auto__15166 = new TreeIter$iter__15146__15152$fn__15153$iter__15148__15154(object5, d, this_.ridx, this_.didx);
                iterys__6021__auto__15166 = null;
                Object object6 = d;
                d = null;
                Object object7 = fs__6022__auto__15167 = ((IFn)const__0.getRawRoot()).invoke(((IFn)treeIter$iter__15146__15152$fn__15153$iter__15148__15154).invoke(((IFn)const__3.getRawRoot()).invoke((Object)RT.count((Object)((DirNode)object6).segids))));
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = fs__6022__auto__15167;
                    fs__6022__auto__15167 = null;
                    Object object9 = s__15147;
                    s__15147 = null;
                    TreeIter$iter__15146__15152$fn__15153 this_ = null;
                    object = ((IFn)const__5.getRawRoot()).invoke(object8, ((IFn)this_.iter__15146).invoke(((IFn)const__6.getRawRoot()).invoke(object9)));
                    break block2;
                }
                Object object10 = s__15147;
                s__15147 = null;
                s__15147 = ((IFn)const__6.getRawRoot()).invoke(object10);
            }
            object = null;
        }
        return object;
    }
}

