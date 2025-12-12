/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$filter_retractions$eat_past__12641;
import datomic.db$filter_retractions$next_skip__12650;
import datomic.db$filter_retractions$reify__12652;
import datomic.db$filter_retractions$skip__12647;

public final class db$filter_retractions
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 814, RT.keyword(null, (String)"column"), 7});

    public static Object invokeStatic(Object iter2) {
        IObj iObj;
        db$filter_retractions$eat_past__12641 eat_past;
        db$filter_retractions$eat_past__12641 db$filter_retractions$eat_past__12641 = eat_past = new db$filter_retractions$eat_past__12641();
        eat_past = null;
        db$filter_retractions$skip__12647 skip = new db$filter_retractions$skip__12647((Object)db$filter_retractions$eat_past__12641);
        Object object = iter2;
        iter2 = null;
        Object iter3 = ((IFn)skip).invoke(object);
        Object iter_atom = ((IFn)const__0.getRawRoot()).invoke(iter3);
        db$filter_retractions$skip__12647 db$filter_retractions$skip__12647 = skip;
        skip = null;
        db$filter_retractions$next_skip__12650 next_skip = new db$filter_retractions$next_skip__12650((Object)db$filter_retractions$skip__12647);
        Object object2 = iter3;
        iter3 = null;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = iter_atom;
            iter_atom = null;
            db$filter_retractions$next_skip__12650 db$filter_retractions$next_skip__12650 = next_skip;
            next_skip = null;
            iObj = ((IObj)new db$filter_retractions$reify__12652(null, object3, (Object)db$filter_retractions$next_skip__12650)).withMeta((IPersistentMap)const__5);
        } else {
            iObj = null;
        }
        return iObj;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$filter_retractions.invokeStatic(object2);
    }
}

