/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  com.amazonaws.services.s3.model.DeleteObjectsRequest
 *  com.amazonaws.services.s3.model.MultiObjectDeleteException
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.MultiObjectDeleteException;
import datomic.s3.Name;

public final class s3$delete_objects
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Object const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Keyword const__6;
    public static final Var const__7;
    public static final Keyword const__8;

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public static Object invokeStatic(Object s3, Object bucket, Object keys) {
        try {
            block5: {
                block4: {
                    v0 = keys;
                    keys = null;
                    str_keys = ((IFn)s3$delete_objects.const__0.getRawRoot()).invoke(s3$delete_objects.const__1, ((IFn)s3$delete_objects.const__2.getRawRoot()).invoke(s3$delete_objects.const__3.getRawRoot(), v0));
                    v1 = ((IFn)s3$delete_objects.const__4.getRawRoot()).invoke(str_keys);
                    if (v1 == null || v1 == Boolean.FALSE) break block4;
                    v2 = (IFn)s3$delete_objects.const__5.getRawRoot();
                    v3 = s3;
                    s3 = null;
                    v4 = bucket;
                    bucket = null;
                    v5 = v4;
                    if (Util.classOf((Object)v4) == s3$delete_objects.__cached_class__0) ** GOTO lbl17
                    if (!(v5 instanceof Name)) {
                        v5 = v5;
                        s3$delete_objects.__cached_class__0 = Util.classOf((Object)v5);
lbl17:
                        // 2 sources

                        v6 = s3$delete_objects.const__3.getRawRoot().invoke(v5);
                    } else {
                        v6 = ((Name)v5).s3_name();
                    }
                    v7 = str_keys;
                    str_keys = null;
                    v8 /* !! */  = v2.invoke(v3, (Object)new DeleteObjectsRequest((String)v6).withKeys((String[])v7));
                    break block5;
                }
                v8 /* !! */  = RT.mapUniqueKeys((Object[])new Object[]{s3$delete_objects.const__6, PersistentVector.EMPTY});
            }
            var4_6 = v8 /* !! */ ;
        }
        catch (MultiObjectDeleteException e) {
            v9 = new Object[4];
            v9[0] = s3$delete_objects.const__6;
            v9[1] = ((IFn)s3$delete_objects.const__2.getRawRoot()).invoke(s3$delete_objects.const__7.getRawRoot(), (Object)e.getDeletedObjects());
            v9[2] = s3$delete_objects.const__8;
            e = null;
            v9[3] = ((IFn)s3$delete_objects.const__2.getRawRoot()).invoke(s3$delete_objects.const__7.getRawRoot(), (Object)e.getErrors());
            var4_6 = RT.mapUniqueKeys((Object[])v9);
        }
        return var4_6;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return s3$delete_objects.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"into-array");
        const__1 = RT.classForName((String)"java.lang.String");
        const__2 = RT.var((String)"clojure.core", (String)"map");
        const__3 = RT.var((String)"datomic.s3", (String)"s3-name");
        const__4 = RT.var((String)"clojure.core", (String)"seq");
        const__5 = RT.var((String)"datomic.s3-api", (String)"delete-objects");
        const__6 = RT.keyword(null, (String)"deletedObjects");
        const__7 = RT.var((String)"datomic.datafy", (String)"object-to-data");
        const__8 = RT.keyword(null, (String)"errors");
    }
}

