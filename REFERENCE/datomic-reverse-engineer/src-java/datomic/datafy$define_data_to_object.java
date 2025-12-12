/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.datafy$define_data_to_object$fn__17292;

public final class datafy$define_data_to_object
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"resolve");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"ancestors");
    public static final Object const__4 = RT.classForName((String)"java.lang.Enum");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__8 = (AFn)Symbol.intern((String)"clojure.core", (String)"defmethod");
    public static final AFn const__9 = (AFn)Symbol.intern((String)"datomic.datafy", (String)"data-to-object");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"vector");
    public static final Keyword const__12 = RT.keyword(null, (String)"atom");
    public static final AFn const__13 = (AFn)Symbol.intern(null, (String)"n");
    public static final AFn const__14 = (AFn)Symbol.intern(null, (String)"_");
    public static final AFn const__15 = (AFn)Symbol.intern(null, (String)".");
    public static final AFn const__16 = (AFn)Symbol.intern(null, (String)"valueOf");
    public static final AFn const__17 = (AFn)Symbol.intern((String)"clojure.core", (String)"name");
    public static final AFn const__18 = (AFn)Symbol.intern(null, (String)"n");
    public static final Var const__19 = RT.var((String)"datomic.datafy", (String)"filter-overlapped-setters");
    public static final Var const__20 = RT.var((String)"datomic.datafy", (String)"setters");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"map");
    public static final AFn const__23 = (AFn)Symbol.intern((String)"clojure.core", (String)"defmethod");
    public static final AFn const__24 = (AFn)Symbol.intern((String)"datomic.datafy", (String)"data-to-object");
    public static final Keyword const__25 = RT.keyword(null, (String)"map");
    public static final AFn const__26 = (AFn)Symbol.intern(null, (String)"m");
    public static final AFn const__27 = (AFn)Symbol.intern(null, (String)"_");
    public static final AFn const__28 = (AFn)Symbol.intern((String)"clojure.core", (String)"when-let");
    public static final AFn const__29 = (AFn)Symbol.intern(null, (String)"bad-ks");
    public static final AFn const__30 = (AFn)Symbol.intern((String)"clojure.core", (String)"seq");
    public static final AFn const__31 = (AFn)Symbol.intern((String)"clojure.core", (String)"remove");
    public static final AFn const__32 = (AFn)Symbol.intern((String)"clojure.core", (String)"keys");
    public static final AFn const__33 = (AFn)Symbol.intern(null, (String)"m");
    public static final AFn const__34 = (AFn)Symbol.intern(null, (String)"throw");
    public static final AFn const__35 = (AFn)Symbol.intern((String)"clojure.core", (String)"ex-info");
    public static final AFn const__36 = (AFn)Symbol.intern((String)"clojure.core", (String)"apply");
    public static final AFn const__37 = (AFn)Symbol.intern((String)"clojure.core", (String)"str");
    public static final AFn const__38 = (AFn)Symbol.intern(null, (String)"bad-ks");
    public static final Var const__39 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Keyword const__40 = RT.keyword(null, (String)"keys");
    public static final AFn const__41 = (AFn)Symbol.intern(null, (String)"bad-ks");
    public static final Keyword const__42 = RT.keyword(null, (String)"legal-keys");
    public static final Keyword const__43 = RT.keyword(null, (String)"constructor");
    public static final AFn const__44 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final AFn const__45 = (AFn)Symbol.intern(null, (String)"o");
    public static final AFn const__46 = (AFn)Symbol.intern(null, (String)"new");
    public static final Var const__47 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__48 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__49 = RT.var((String)"datomic.datafy", (String)"invoke-setter");
    public static final AFn const__50 = (AFn)Symbol.intern(null, (String)"o");
    public static final AFn const__51 = (AFn)Symbol.intern(null, (String)"m");
    public static final AFn const__52 = (AFn)Symbol.intern(null, (String)"o");

    public static Object invokeStatic(Object bean_class) {
        Object object;
        Object or__5238__auto__17295;
        Object object2 = or__5238__auto__17295 = ((IFn)const__0.getRawRoot()).invoke(bean_class);
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__1.getRawRoot()).invoke((Object)"Unable to resolve symbol as class: ", bean_class));
        }
        Object object3 = or__5238__auto__17295;
        or__5238__auto__17295 = null;
        Object cls = object3;
        Object object4 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(cls), const__4);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__12), ((IFn)const__7.getRawRoot()).invoke(bean_class)))));
            Object object6 = bean_class;
            bean_class = null;
            object = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__8), ((IFn)const__7.getRawRoot()).invoke((Object)const__9), object5, ((IFn)const__7.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__13), ((IFn)const__7.getRawRoot()).invoke((Object)const__14))))), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__15), ((IFn)const__7.getRawRoot()).invoke(object6), ((IFn)const__7.getRawRoot()).invoke((Object)const__16), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__17), ((IFn)const__7.getRawRoot()).invoke((Object)const__18)))))))));
        } else {
            Object object7 = cls;
            cls = null;
            Object s = ((IFn)const__19.getRawRoot()).invoke(((IFn)const__20.getRawRoot()).invoke(object7));
            Object legal_keys = ((IFn)const__21.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__22.getRawRoot()).invoke((Object)new datafy$define_data_to_object$fn__17292(), s));
            Object object8 = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__25), ((IFn)const__7.getRawRoot()).invoke(bean_class)))));
            Object object9 = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__29), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__30), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__31), ((IFn)const__7.getRawRoot()).invoke(legal_keys), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__32), ((IFn)const__7.getRawRoot()).invoke((Object)const__33))))))))))))));
            Object object10 = legal_keys;
            legal_keys = null;
            Object object11 = ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__28), object9, ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__34), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__35), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__36), ((IFn)const__7.getRawRoot()).invoke((Object)const__37), ((IFn)const__7.getRawRoot()).invoke((Object)"Unexpected keys "), ((IFn)const__7.getRawRoot()).invoke((Object)const__38)))), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(const__39.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__40), ((IFn)const__7.getRawRoot()).invoke((Object)const__41), ((IFn)const__7.getRawRoot()).invoke((Object)const__42), ((IFn)const__7.getRawRoot()).invoke(object10), ((IFn)const__7.getRawRoot()).invoke((Object)const__43), ((IFn)const__7.getRawRoot()).invoke(bean_class))))))))))))));
            Object object12 = bean_class;
            bean_class = null;
            Object object13 = s;
            s = null;
            object = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__23), ((IFn)const__7.getRawRoot()).invoke((Object)const__24), object8, ((IFn)const__7.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__26), ((IFn)const__7.getRawRoot()).invoke((Object)const__27))))), object11, ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__44), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__45), ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)const__46), ((IFn)const__7.getRawRoot()).invoke(object12)))))))), ((IFn)const__47.getRawRoot()).invoke(((IFn)const__48.getRawRoot()).invoke(const__49.getRawRoot(), (Object)const__50, (Object)const__51), object13), ((IFn)const__7.getRawRoot()).invoke((Object)const__52))))));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$define_data_to_object.invokeStatic(object2);
    }
}

