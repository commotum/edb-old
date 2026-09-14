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
 *  com.amazonaws.services.s3.model.DeleteObjectsResult
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import java.util.List;

public final class s3_api$fn__22974
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"requesterCharged");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"deletedObjects");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        List temp__5457__auto__22979;
        IPersistentVector iPersistentVector2;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        boolean temp__5457__auto__22977 = ((DeleteObjectsResult)o).isRequesterCharged();
        if (temp__5457__auto__22977) {
            boolean v__17285__auto__22976 = temp__5457__auto__22977;
            iPersistentVector2 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)(v__17285__auto__22976 ? Boolean.TRUE : Boolean.FALSE)));
        } else {
            iPersistentVector2 = null;
        }
        Object object2 = o;
        o = null;
        List list = temp__5457__auto__22979 = ((DeleteObjectsResult)object2).getDeletedObjects();
        if (list != null && list != Boolean.FALSE) {
            List v__17285__auto__22978;
            List list2 = temp__5457__auto__22979;
            temp__5457__auto__22979 = null;
            List list3 = v__17285__auto__22978 = list2;
            v__17285__auto__22978 = null;
            iPersistentVector = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)list3));
        } else {
            iPersistentVector = null;
        }
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector2, iPersistentVector));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3_api$fn__22974.invokeStatic(object2);
    }
}

