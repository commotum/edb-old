/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.datomic.lucene.index.IndexWriter
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datomic.lucene.index.IndexWriter;

public final class fulltext$create_index_on_dir$fn__14606
extends AFunction {
    Object add_data;
    Object writer;
    public static final Var const__0 = RT.var((String)"datomic.fulltext", (String)"add-chunked-data-to-writer");

    public fulltext$create_index_on_dir$fn__14606(Object object, Object object2) {
        this.add_data = object;
        this.writer = object2;
    }

    public Object invoke() {
        Object object;
        try {
            this.add_data = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this.writer, this.add_data);
        }
        finally {
            this.writer = null;
            ((IndexWriter)this.writer).close();
        }
        return object;
    }
}

