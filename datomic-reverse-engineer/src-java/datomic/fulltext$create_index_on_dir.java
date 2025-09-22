/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.datomic.lucene.index.IndexReader
 *  com.datomic.lucene.store.Directory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datomic.lucene.index.IndexReader;
import com.datomic.lucene.store.Directory;
import datomic.fulltext$create_index_on_dir$fn__14606;
import datomic.fulltext$create_index_on_dir$fn__14608;

public final class fulltext$create_index_on_dir
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.lucene", (String)"index-writer");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");

    public static Object invokeStatic(Object dir, Object add_data, Object remove_data) {
        Object writer2 = ((IFn)const__0.getRawRoot()).invoke(dir);
        Object object = add_data;
        add_data = null;
        Object object2 = writer2;
        writer2 = null;
        ((IFn)new fulltext$create_index_on_dir$fn__14606(object, object2)).invoke();
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(remove_data);
        if (object3 != null && object3 != Boolean.FALSE) {
            IndexReader reader2 = IndexReader.open((Directory)((Directory)dir), (boolean)Boolean.FALSE);
            Object object4 = remove_data;
            remove_data = null;
            IndexReader indexReader = reader2;
            reader2 = null;
            ((IFn)new fulltext$create_index_on_dir$fn__14608(object4, indexReader)).invoke();
        }
        Object object5 = null;
        return dir;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return fulltext$create_index_on_dir.invokeStatic(object4, object5, object6);
    }
}

