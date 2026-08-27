/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.Attribute;

public final class db$fn__12548$__GT_Attribute__12594
extends AFunction {
    public Object invoke(Object id, Object kw, Object vtypeid, Object cardinality, Object isComponent, Object unique, Object index2, Object storageHasAVET, Object needsAVET, Object noHistory, Object fulltext2) {
        Object object = id;
        id = null;
        Object object2 = kw;
        kw = null;
        Object object3 = vtypeid;
        vtypeid = null;
        Object object4 = cardinality;
        cardinality = null;
        Object object5 = isComponent;
        isComponent = null;
        Object object6 = unique;
        unique = null;
        Object object7 = index2;
        index2 = null;
        Object object8 = storageHasAVET;
        storageHasAVET = null;
        Object object9 = needsAVET;
        needsAVET = null;
        Object object10 = noHistory;
        noHistory = null;
        Object object11 = fulltext2;
        fulltext2 = null;
        return new Attribute(object, object2, object3, object4, object5, object6, object7, object8, object9, object10, object11);
    }
}

