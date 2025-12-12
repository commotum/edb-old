/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  com.amazonaws.services.s3.model.Owner
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.amazonaws.services.s3.model.Owner;

public final class s3_api$fn__23190
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"displayName");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"id");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        String temp__5457__auto__23195;
        IPersistentVector iPersistentVector2;
        String temp__5457__auto__23193;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        String string = temp__5457__auto__23193 = ((Owner)o).getDisplayName();
        if (string != null && string != Boolean.FALSE) {
            String v__17285__auto__23192;
            String string2 = temp__5457__auto__23193;
            temp__5457__auto__23193 = null;
            String string3 = v__17285__auto__23192 = string2;
            v__17285__auto__23192 = null;
            iPersistentVector2 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string3));
        } else {
            iPersistentVector2 = null;
        }
        Object object2 = o;
        o = null;
        String string4 = temp__5457__auto__23195 = ((Owner)object2).getId();
        if (string4 != null && string4 != Boolean.FALSE) {
            String v__17285__auto__23194;
            String string5 = temp__5457__auto__23195;
            temp__5457__auto__23195 = null;
            String string6 = v__17285__auto__23194 = string5;
            v__17285__auto__23194 = null;
            iPersistentVector = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string6));
        } else {
            iPersistentVector = null;
        }
        return iFn.invoke(object, iFn2.invoke(iPersistentVector2, iPersistentVector));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3_api$fn__23190.invokeStatic(object2);
    }
}

