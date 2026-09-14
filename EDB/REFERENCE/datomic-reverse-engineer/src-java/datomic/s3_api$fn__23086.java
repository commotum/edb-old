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
 *  com.amazonaws.services.s3.model.MultiObjectDeleteException$DeleteError
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.amazonaws.services.s3.model.MultiObjectDeleteException;

public final class s3_api$fn__23086
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"code");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"versionId");
    public static final Keyword const__6 = RT.keyword(null, (String)"key");
    public static final Keyword const__7 = RT.keyword(null, (String)"message");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        String temp__5457__auto__23095;
        IPersistentVector iPersistentVector2;
        String temp__5457__auto__23093;
        IPersistentVector iPersistentVector3;
        String temp__5457__auto__23091;
        IPersistentVector iPersistentVector4;
        String temp__5457__auto__23089;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        String string = temp__5457__auto__23089 = ((MultiObjectDeleteException.DeleteError)o).getCode();
        if (string != null && string != Boolean.FALSE) {
            String v__17285__auto__23088;
            String string2 = temp__5457__auto__23089;
            temp__5457__auto__23089 = null;
            String string3 = v__17285__auto__23088 = string2;
            v__17285__auto__23088 = null;
            iPersistentVector4 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string3));
        } else {
            iPersistentVector4 = null;
        }
        String string4 = temp__5457__auto__23091 = ((MultiObjectDeleteException.DeleteError)o).getVersionId();
        if (string4 != null && string4 != Boolean.FALSE) {
            String v__17285__auto__23090;
            String string5 = temp__5457__auto__23091;
            temp__5457__auto__23091 = null;
            String string6 = v__17285__auto__23090 = string5;
            v__17285__auto__23090 = null;
            iPersistentVector3 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string6));
        } else {
            iPersistentVector3 = null;
        }
        String string7 = temp__5457__auto__23093 = ((MultiObjectDeleteException.DeleteError)o).getKey();
        if (string7 != null && string7 != Boolean.FALSE) {
            String v__17285__auto__23092;
            String string8 = temp__5457__auto__23093;
            temp__5457__auto__23093 = null;
            String string9 = v__17285__auto__23092 = string8;
            v__17285__auto__23092 = null;
            iPersistentVector2 = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string9));
        } else {
            iPersistentVector2 = null;
        }
        Object object2 = o;
        o = null;
        String string10 = temp__5457__auto__23095 = ((MultiObjectDeleteException.DeleteError)object2).getMessage();
        if (string10 != null && string10 != Boolean.FALSE) {
            String v__17285__auto__23094;
            String string11 = temp__5457__auto__23095;
            temp__5457__auto__23095 = null;
            String string12 = v__17285__auto__23094 = string11;
            v__17285__auto__23094 = null;
            iPersistentVector = Tuple.create((Object)const__7, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string12));
        } else {
            iPersistentVector = null;
        }
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector4, (Object)iPersistentVector3, iPersistentVector2, iPersistentVector));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3_api$fn__23086.invokeStatic(object2);
    }
}

