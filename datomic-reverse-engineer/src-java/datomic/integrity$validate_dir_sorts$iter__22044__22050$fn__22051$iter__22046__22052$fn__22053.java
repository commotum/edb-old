/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052$fn__22053$fn__22054;

public final class integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052$fn__22053
extends AFunction {
    Object tier;
    Object s__22047;
    Object iter__22046;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunk-buffer");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunk-cons");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunk");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"rest");

    public integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052$fn__22053(Object object, Object object2, Object object3) {
        this.tier = object;
        this.s__22047 = object2;
        this.iter__22046 = object3;
    }

    public Object invoke() {
        Object object;
        Object temp__5457__auto__22059;
        Object s__22047;
        Object object2 = s__22047 = (this_.s__22047 = null);
        s__22047 = null;
        Object object3 = temp__5457__auto__22059 = ((IFn)const__0.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052$fn__22053 this_;
            Object object4 = temp__5457__auto__22059;
            temp__5457__auto__22059 = null;
            Object s__220472 = object4;
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(s__220472);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object c__6023__auto__22057 = ((IFn)const__2.getRawRoot()).invoke(s__220472);
                int size__6024__auto__22058 = RT.intCast((int)RT.count((Object)c__6023__auto__22057));
                Object b__22049 = ((IFn)const__5.getRawRoot()).invoke((Object)size__6024__auto__22058);
                Object object6 = c__6023__auto__22057;
                c__6023__auto__22057 = null;
                Object object7 = ((IFn)new integrity$validate_dir_sorts$iter__22044__22050$fn__22051$iter__22046__22052$fn__22053$fn__22054(this_.tier, object6, size__6024__auto__22058, b__22049)).invoke();
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = b__22049;
                    b__22049 = null;
                    Object object9 = s__220472;
                    s__220472 = null;
                    this_ = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object8), ((IFn)this_.iter__22046).invoke(((IFn)const__8.getRawRoot()).invoke(object9)));
                } else {
                    Object object10 = b__22049;
                    b__22049 = null;
                    this_ = null;
                    object = ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object10), null);
                }
            } else {
                Object sort;
                Object object11 = sort = ((IFn)const__9.getRawRoot()).invoke(s__220472);
                sort = null;
                Object object12 = s__220472;
                s__220472 = null;
                this_ = null;
                object = ((IFn)const__10.getRawRoot()).invoke((Object)Tuple.create((Object)this_.tier, (Object)object11), ((IFn)this_.iter__22046).invoke(((IFn)const__11.getRawRoot()).invoke(object12)));
            }
        } else {
            object = null;
        }
        return object;
    }
}

