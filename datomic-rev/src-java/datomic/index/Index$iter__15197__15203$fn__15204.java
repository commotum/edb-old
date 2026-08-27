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
import datomic.index.Index$iter__15197__15203$fn__15204$iter__15199__15205;

public final class Index$iter__15197__15203$fn__15204
extends AFunction {
    Object root;
    Object lookup;
    Object s__15198;
    Object iter__15197;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"datomic.index", (String)"get-dir-node");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"rest");

    public Index$iter__15197__15203$fn__15204(Object object, Object object2, Object object3, Object object4) {
        this.root = object;
        this.lookup = object2;
        this.s__15198 = object3;
        this.iter__15197 = object4;
    }

    public Object invoke() {
        Object object;
        block2: {
            Object s__15198 = this_.s__15198 = null;
            while (true) {
                Object fs__6022__auto__15216;
                Index$iter__15197__15203$fn__15204$iter__15199__15205 iterys__6021__auto__15215;
                Object ri;
                Object xs__6012__auto__15217;
                Object temp__5457__auto__15218;
                Object object2 = temp__5457__auto__15218 = ((IFn)const__0.getRawRoot()).invoke(s__15198);
                if (object2 == null || object2 == Boolean.FALSE) break;
                Object object3 = temp__5457__auto__15218;
                temp__5457__auto__15218 = null;
                Object object4 = xs__6012__auto__15217 = object3;
                xs__6012__auto__15217 = null;
                Object object5 = ri = ((IFn)const__1.getRawRoot()).invoke(object4);
                ri = null;
                Object d = ((IFn)const__2.getRawRoot()).invoke(this_.root, object5, this_.lookup, (Object)Boolean.FALSE);
                Index$iter__15197__15203$fn__15204$iter__15199__15205 index$iter__15197__15203$fn__15204$iter__15199__15205 = iterys__6021__auto__15215 = new Index$iter__15197__15203$fn__15204$iter__15199__15205(d);
                iterys__6021__auto__15215 = null;
                Object object6 = d;
                d = null;
                Object object7 = fs__6022__auto__15216 = ((IFn)const__0.getRawRoot()).invoke(((IFn)index$iter__15197__15203$fn__15204$iter__15199__15205).invoke(((IFn)const__3.getRawRoot()).invoke((Object)RT.count((Object)((DirNode)object6).segids))));
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = fs__6022__auto__15216;
                    fs__6022__auto__15216 = null;
                    Object object9 = s__15198;
                    s__15198 = null;
                    Index$iter__15197__15203$fn__15204 this_ = null;
                    object = ((IFn)const__5.getRawRoot()).invoke(object8, ((IFn)this_.iter__15197).invoke(((IFn)const__6.getRawRoot()).invoke(object9)));
                    break block2;
                }
                Object object10 = s__15198;
                s__15198 = null;
                s__15198 = ((IFn)const__6.getRawRoot()).invoke(object10);
            }
            object = null;
        }
        return object;
    }
}

