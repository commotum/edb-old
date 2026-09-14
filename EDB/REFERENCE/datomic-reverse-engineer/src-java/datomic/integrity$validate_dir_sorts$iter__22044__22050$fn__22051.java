/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052;

public final class integrity$validate_dir_sorts$iter__22044__22050$fn__22051
extends AFunction {
    Object s__22045;
    Object iter__22044;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final AFn const__6 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"eavt"), (Object)RT.keyword(null, (String)"aevt"), (Object)RT.keyword(null, (String)"avet"), (Object)RT.keyword(null, (String)"vaet"));
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"rest");

    public integrity$validate_dir_sorts$iter__22044__22050$fn__22051(Object object, Object object2) {
        this.s__22045 = object;
        this.iter__22044 = object2;
    }

    public Object invoke() {
        Object object;
        block2: {
            Object s__22045 = this_.s__22045 = null;
            while (true) {
                Object fs__6022__auto__22063;
                integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052 iterys__6021__auto__22062;
                Object tier;
                Object xs__6012__auto__22064;
                Object temp__5457__auto__22065;
                Object object2 = temp__5457__auto__22065 = ((IFn)const__0.getRawRoot()).invoke(s__22045);
                if (object2 == null || object2 == Boolean.FALSE) break;
                Object object3 = temp__5457__auto__22065;
                temp__5457__auto__22065 = null;
                Object object4 = xs__6012__auto__22064 = object3;
                xs__6012__auto__22064 = null;
                Object object5 = tier = ((IFn)const__1.getRawRoot()).invoke(object4);
                tier = null;
                integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052 integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052 = iterys__6021__auto__22062 = new integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052(object5);
                iterys__6021__auto__22062 = null;
                Object object6 = fs__6022__auto__22063 = ((IFn)const__0.getRawRoot()).invoke(((IFn)integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052).invoke((Object)const__6));
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object object7 = fs__6022__auto__22063;
                    fs__6022__auto__22063 = null;
                    Object object8 = s__22045;
                    s__22045 = null;
                    integrity$validate_dir_sorts$iter__22044__22050$fn__22051 this_ = null;
                    object = ((IFn)const__7.getRawRoot()).invoke(object7, ((IFn)this_.iter__22044).invoke(((IFn)const__8.getRawRoot()).invoke(object8)));
                    break block2;
                }
                Object object9 = s__22045;
                s__22045 = null;
                s__22045 = ((IFn)const__8.getRawRoot()).invoke(object9);
            }
            object = null;
        }
        return object;
    }
}

