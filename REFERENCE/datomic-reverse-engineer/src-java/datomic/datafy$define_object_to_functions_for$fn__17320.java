/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class datafy$define_object_to_functions_for$fn__17320
extends AFunction {
    Object cls;
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"vector?");
    public static final Keyword const__8 = RT.keyword(null, (String)"else");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"resolve");
    public static final Var const__11 = RT.var((String)"datomic.datafy", (String)"get-java-method");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"name");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__16 = RT.keyword(null, (String)"cls");
    public static final Keyword const__17 = RT.keyword(null, (String)"arity");
    public static final Keyword const__18 = RT.keyword(null, (String)"types");

    public datafy$define_object_to_functions_for$fn__17320(Object object) {
        this.cls = object;
    }

    public Object invoke(Object p__17319) {
        Object meth;
        Object types;
        IPersistentVector iPersistentVector;
        Object object = p__17319;
        p__17319 = null;
        Object vec__17321 = object;
        Object mname = RT.nth((Object)vec__17321, (int)RT.intCast((long)0L), null);
        Object arity = RT.nth((Object)vec__17321, (int)RT.intCast((long)1L), null);
        Object fname = RT.nth((Object)vec__17321, (int)RT.intCast((long)2L), null);
        Object docstring_QMARK_ = RT.nth((Object)vec__17321, (int)RT.intCast((long)3L), null);
        Object object2 = vec__17321;
        vec__17321 = null;
        Object types_QMARK_ = RT.nth((Object)object2, (int)RT.intCast((long)4L), null);
        Object object3 = ((IFn)const__6.getRawRoot()).invoke(docstring_QMARK_);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = docstring_QMARK_;
            docstring_QMARK_ = null;
            Object object5 = types_QMARK_;
            types_QMARK_ = null;
            iPersistentVector = Tuple.create((Object)object4, (Object)object5);
        } else {
            Object object6 = ((IFn)const__7.getRawRoot()).invoke(docstring_QMARK_);
            if (object6 != null && object6 != Boolean.FALSE) {
                Object object7 = docstring_QMARK_;
                docstring_QMARK_ = null;
                iPersistentVector = Tuple.create((Object)PersistentVector.EMPTY, (Object)object7);
            } else {
                Keyword keyword = const__8;
                iPersistentVector = keyword != null && keyword != Boolean.FALSE ? null : null;
            }
        }
        IPersistentVector vec__17324 = iPersistentVector;
        Object docstring = RT.nth((Object)vec__17324, (int)RT.intCast((long)0L), null);
        IPersistentVector iPersistentVector2 = vec__17324;
        vec__17324 = null;
        Object object8 = types = RT.nth((Object)iPersistentVector2, (int)RT.intCast((long)1L), null);
        types = null;
        Object types2 = ((IFn)const__9.getRawRoot()).invoke(const__10.getRawRoot(), object8);
        Object object9 = meth = ((IFn)const__11.getRawRoot()).invoke(this.cls, mname, arity, types2);
        if (object9 == null || object9 == Boolean.FALSE) {
            Object object10 = mname;
            mname = null;
            Object[] objectArray = new Object[6];
            objectArray[0] = const__16;
            objectArray[1] = this.cls;
            objectArray[2] = const__17;
            Object object11 = arity;
            arity = null;
            objectArray[3] = object11;
            objectArray[4] = const__18;
            Object object12 = types2;
            types2 = null;
            objectArray[5] = object12;
            throw (Throwable)((IFn)const__14.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke((Object)"Unable to find method ", object10, (Object)", you may need to specify types in define-objects-to-functions"), (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        Object object13 = meth;
        meth = null;
        Object object14 = fname;
        fname = null;
        Object object15 = docstring;
        docstring = null;
        return Tuple.create((Object)object13, (Object)((IFn)const__12.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke(object14)), (Object)object15);
    }
}

