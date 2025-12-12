/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  com.datomic.lucene.store.Directory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import com.datomic.lucene.store.Directory;
import datomic.impl.lucene.HybridDirectory;

public final class fulltext$hybrid_dir
extends AFunction {
    public static Object invokeStatic(Object writer2, Object reader2, Object delete_handler) {
        Object object = writer2;
        writer2 = null;
        Object object2 = reader2;
        reader2 = null;
        Object object3 = delete_handler;
        delete_handler = null;
        return new HybridDirectory((Directory)object, (Directory)object2, (IFn)object3);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return fulltext$hybrid_dir.invokeStatic(object4, object5, object6);
    }
}

