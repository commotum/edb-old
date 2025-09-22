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
import datomic.index.TreeIter$iter__15121__15127$fn__15128$iter__15123__15129;

public final class TreeIter$iter__15121__15127$fn__15128
extends AFunction {
    Object lookup;
    Object root;
    Object iter__15121;
    Object s__15122;
    int ridx;
    int didx;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"datomic.index", (String)"get-dir-node");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"rest");

    public TreeIter$iter__15121__15127$fn__15128(Object object, Object object2, Object object3, Object object4, int n, int n2) {
        this.lookup = object;
        this.root = object2;
        this.iter__15121 = object3;
        this.s__15122 = object4;
        this.ridx = n;
        this.didx = n2;
    }

    public Object invoke() {
        Object object;
        block2: {
            Object s__15122 = this_.s__15122 = null;
            while (true) {
                Object fs__6022__auto__15142;
                TreeIter$iter__15121__15127$fn__15128$iter__15123__15129 iterys__6021__auto__15141;
                Object xs__6012__auto__15143;
                Object temp__5457__auto__15144;
                Object object2 = temp__5457__auto__15144 = ((IFn)const__0.getRawRoot()).invoke(s__15122);
                if (object2 == null || object2 == Boolean.FALSE) break;
                Object object3 = temp__5457__auto__15144;
                temp__5457__auto__15144 = null;
                Object object4 = xs__6012__auto__15143 = object3;
                xs__6012__auto__15143 = null;
                Object ri = ((IFn)const__1.getRawRoot()).invoke(object4);
                Object d = ((IFn)const__2.getRawRoot()).invoke(this_.root, ri, this_.lookup, (Object)Boolean.FALSE);
                Object object5 = ri;
                ri = null;
                TreeIter$iter__15121__15127$fn__15128$iter__15123__15129 treeIter$iter__15121__15127$fn__15128$iter__15123__15129 = iterys__6021__auto__15141 = new TreeIter$iter__15121__15127$fn__15128$iter__15123__15129(object5, this_.lookup, d, this_.ridx, this_.didx);
                iterys__6021__auto__15141 = null;
                Object object6 = d;
                d = null;
                Object object7 = fs__6022__auto__15142 = ((IFn)const__0.getRawRoot()).invoke(((IFn)treeIter$iter__15121__15127$fn__15128$iter__15123__15129).invoke(((IFn)const__3.getRawRoot()).invoke((Object)RT.count((Object)((DirNode)object6).segids))));
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = fs__6022__auto__15142;
                    fs__6022__auto__15142 = null;
                    Object object9 = s__15122;
                    s__15122 = null;
                    TreeIter$iter__15121__15127$fn__15128 this_ = null;
                    object = ((IFn)const__5.getRawRoot()).invoke(object8, ((IFn)this_.iter__15121).invoke(((IFn)const__6.getRawRoot()).invoke(object9)));
                    break block2;
                }
                Object object10 = s__15122;
                s__15122 = null;
                s__15122 = ((IFn)const__6.getRawRoot()).invoke(object10);
            }
            object = null;
        }
        return object;
    }
}

