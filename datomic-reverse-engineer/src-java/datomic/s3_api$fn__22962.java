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
 *  com.amazonaws.services.s3.model.Bucket
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
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.Owner;
import java.util.Date;

public final class s3_api$fn__22962
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"creationDate");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");
    public static final Keyword const__5 = RT.keyword(null, (String)"name");
    public static final Keyword const__6 = RT.keyword(null, (String)"owner");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        Owner temp__5457__auto__22969;
        IPersistentVector iPersistentVector2;
        String temp__5457__auto__22967;
        IPersistentVector iPersistentVector3;
        Date temp__5457__auto__22965;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        Date date = temp__5457__auto__22965 = ((Bucket)o).getCreationDate();
        if (date != null && date != Boolean.FALSE) {
            Date v__17285__auto__22964;
            Date date2 = temp__5457__auto__22965;
            temp__5457__auto__22965 = null;
            Date date3 = v__17285__auto__22964 = date2;
            v__17285__auto__22964 = null;
            iPersistentVector3 = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)date3));
        } else {
            iPersistentVector3 = null;
        }
        String string = temp__5457__auto__22967 = ((Bucket)o).getName();
        if (string != null && string != Boolean.FALSE) {
            String v__17285__auto__22966;
            String string2 = temp__5457__auto__22967;
            temp__5457__auto__22967 = null;
            String string3 = v__17285__auto__22966 = string2;
            v__17285__auto__22966 = null;
            iPersistentVector2 = Tuple.create((Object)const__5, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string3));
        } else {
            iPersistentVector2 = null;
        }
        Object object2 = o;
        o = null;
        Owner owner = temp__5457__auto__22969 = ((Bucket)object2).getOwner();
        if (owner != null && owner != Boolean.FALSE) {
            Owner v__17285__auto__22968;
            Owner owner2 = temp__5457__auto__22969;
            temp__5457__auto__22969 = null;
            Owner owner3 = v__17285__auto__22968 = owner2;
            v__17285__auto__22968 = null;
            iPersistentVector = Tuple.create((Object)const__6, (Object)((IFn)const__4.getRawRoot()).invoke((Object)owner3));
        } else {
            iPersistentVector = null;
        }
        return iFn.invoke(object, iFn2.invoke((Object)iPersistentVector3, iPersistentVector2, iPersistentVector));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3_api$fn__22962.invokeStatic(object2);
    }
}

