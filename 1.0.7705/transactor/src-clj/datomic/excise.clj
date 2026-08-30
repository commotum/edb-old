(do
  (clojure.core/in-ns (.withMeta 'datomic.excise {:author "Rich Hickey"}))
  (.resetMeta
    (clojure.lang.Namespace/find (.withMeta 'datomic.excise {:author "Rich Hickey"}))
    {:doc "Excise utilities", :author "Rich Hickey"})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.btset :as 'btset]
        ['datomic.db :as 'db]
        ['datomic.common :as 'common]
        ['clojure.set :as 'set])
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'datomic.db.Attribute)
      (clojure.core/import 'datomic.db.IDb)
      (clojure.core/import 'datomic.Database)))
  (when-not (.equals (.withMeta 'datomic.excise {:author "Rich Hickey"}) 'clojure.core)
    (dosync
      (commute
        (deref #'clojure.core/*loaded-libs*)
        conj
        (.withMeta 'datomic.excise {:author "Rich Hickey"})))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.btset :as 'btset]
          ['datomic.db :as 'db]
          ['datomic.common :as 'common]
          ['clojure.set :as 'set])
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'datomic.db.Attribute)
        (clojure.core/import 'datomic.db.IDb)
        (clojure.core/import 'datomic.Database))))
  (set! *warn-on-reflection* true)
  (defn component-attr? ([db a] (.-isComponent (.elementAt ^datomic.db.IDbImpl db a))))
  (reset-meta!
    #'component-attr?
    (assoc
      {:arglists (clojure.core/list ['db 'a]), :column (int 1)}
      :name
      'component-attr?
      :ns
      *ns*))
  (defn component-es-set
    ([db e via_attrs]
      (let [es #{e}
            G__14023 [e]
            vec__14024 G__14023
            seq__14025 (seq vec__14024)
            first__14026 (first seq__14025)
            seq__14025 (next seq__14025)
            check first__14026
            more seq__14025
            via via_attrs]
        (loop [es es G__14023 G__14023 via via]
          (let [es es
                vec__14027 G__14023
                seq__14028 (seq vec__14027)
                first__14029 (first seq__14028)
                seq__14028 (next seq__14028)
                check first__14029
                more seq__14028
                via via]
            (if check
              (let [comps (reduce
                            (fn fn__14030
                              ([s d]
                                (if (and
                                      (or
                                        (empty? via)
                                        (contains?
                                          via
                                          (java.lang.Integer/valueOf
                                            (int (.getA ^datomic.impl.db.IDatum d)))))
                                      (datomic.excise/component-attr?
                                        db
                                        (java.lang.Integer/valueOf
                                          (int (.getA ^datomic.impl.db.IDatum d)))))
                                  (conj s (.getV ^datomic.impl.db.IDatum d))
                                  s)))
                            #{}
                            (db/datoms db :eavt [check]))]
                (recur (into es comps) (into more (set/difference comps es)) nil))
              es)))))
    ([db e] (datomic.excise/component-es-set db e nil)))
  (reset-meta!
    #'component-es-set
    (assoc
      {:arglists (clojure.core/list ['db 'e] ['db 'e 'via-attrs]), :column (int 1)}
      :name
      'component-es-set
      :ns
      *ns*))
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol ExcisePred (ep-datoms [epred]) (ep-remove? [epred datom]))
    (reset-meta!
      (clojure.lang.RT/var "datomic.excise" "ExcisePred")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'ExcisePred :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'ep-datoms
                                        {:arglists (clojure.core/list ['epred])}),
                                      :arglists (clojure.core/list ['epred]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.excise" "ExcisePred"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.excise" "ep-datoms")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*)))
    (let [protocol_signature__7466 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'ep-remove?
                                        {:arglists (clojure.core/list ['epred 'datom])}),
                                      :arglists (clojure.core/list ['epred 'datom]),
                                      :doc nil}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.excise" "ExcisePred"))
          protocol_method_name__7467 (with-meta
                                       (:name protocol_signature__7466)
                                       protocol_signature__7466)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.excise" "ep-remove?")
        (assoc protocol_signature__7466 :name protocol_method_name__7467 :ns *ns*))))
  (.setMeta (clojure.lang.RT/var "datomic.excise" "bootids") {:private true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.excise" "bootids") (into #{} (vals db/BOOT-IDS)))
  (defn keeper?
    ([d]
      (or
        (zero? (.getP ^datomic.impl.db.IDatum d))
        (contains?
          datomic.excise/bootids
          (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d)))))))
  (reset-meta!
    #'keeper?
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'd {:tag 'IDatum})]), :column (int 1)}
      :name
      'keeper?
      :ns
      *ns*))
  (defn get-before-t
    ([db spec]
      (let [before_t (:db.excise/beforeT spec) before (:db.excise/before spec)]
        (if before
          (min (or before_t (long java.lang.Long/MAX_VALUE)) (db/as-of-t db before))
          before_t))))
  (reset-meta!
    #'get-before-t
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'spec]), :column (int 1)}
      :name
      'get-before-t
      :ns
      *ns*))
  (defn pred-and-extent
    ([db spec]
      (let [id (fn id ([p1__14070#] (or (:db/id p1__14070#) (db/resolve-id db p1__14070#))))
            target (^clojure.lang.IFn id (:db/excise spec))
            attrs (into #{} (map id (:db.excise/attrs spec)))
            before_t (datomic.excise/get-before-t db spec)
            type (cond
                   (seq attrs) :e
                   (and
                     (zero? (db/eid->part (long ^java.lang.Number target)))
                     (db/attribute db target)) :a
                   :else (do :e))
            t (db/eid->eidx (long (:db/id spec)))
            extent (when (= type :e)
                     (datomic.excise/component-es-set
                       (.asOf (.history ^datomic.Database db) (long t))
                       target
                       attrs))
            component? (disj extent target)
            ref? (fn ref_QMARK_ ([p1__14071#] (= (.-vtypeid (db/attribute db p1__14071#)) 20)))
            remove? (fn remove_QMARK_
                      ([d]
                        (when (and
                                (not (datomic.excise/keeper? d))
                                (< (.getT ^datomic.impl.db.IDatum d) t)
                                (or
                                  (nil? before_t)
                                  (<
                                    (.getT ^datomic.impl.db.IDatum d)
                                    (db/eid->eidx (long ^java.lang.Number before_t)))))
                          (let [G__14080 type]
                            (case
                              G__14080
                              :a
                              (= target (long (.getA ^datomic.impl.db.IDatum d)))
                              :e
                              (or
                                (and
                                  (or
                                    (= target (long (.getE ^datomic.impl.db.IDatum d)))
                                    (and
                                      (^clojure.lang.IFn ref?
                                        (java.lang.Integer/valueOf
                                          (int (.getA ^datomic.impl.db.IDatum d))))
                                      (= target (.getV ^datomic.impl.db.IDatum d))))
                                  (or
                                    (empty? attrs)
                                    (contains?
                                      attrs
                                      (java.lang.Integer/valueOf
                                        (int (.getA ^datomic.impl.db.IDatum d))))))
                                (^clojure.lang.IFn component?
                                  (long (.getE ^datomic.impl.db.IDatum d)))
                                (and
                                  (^clojure.lang.IFn ref?
                                    (java.lang.Integer/valueOf
                                      (int (.getA ^datomic.impl.db.IDatum d))))
                                  (^clojure.lang.IFn component?
                                    (.getV ^datomic.impl.db.IDatum d)))))))))
            datoms (fn datoms
                     ([]
                       (let [G__14093 type]
                         (case
                           G__14093
                           :a
                           (filter
                             remove?
                             (db/datoms (.history ^datomic.Database db) :aevt [target]))
                           :e
                           (filter
                             remove?
                             (concat
                               (mapcat
                                 (fn fn__14094
                                   ([p1__14072#]
                                     (db/datoms
                                       (.history ^datomic.Database db)
                                       :eavt
                                       [p1__14072#])))
                                 extent)
                               (mapcat
                                 (fn fn__14096
                                   ([p1__14073#]
                                     (db/datoms
                                       (.history ^datomic.Database db)
                                       :vaet
                                       [p1__14073#])))
                                 extent)))))))]
        [(reify
           datomic.excise.ExcisePred
           (ep-remove? [this d] (^clojure.lang.IFn remove? d))
           (ep-datoms [this] (^clojure.lang.IFn datoms)))
         extent])))
  (reset-meta!
    #'pred-and-extent
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'db {:tag 'Database}) 'spec]),
       :column (int 1)}
      :name
      'pred-and-extent
      :ns
      *ns*))
  (defn pred ([db spec] (first (datomic.excise/pred-and-extent db spec))))
  (reset-meta!
    #'pred
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'spec]), :column (int 1)}
      :name
      'pred
      :ns
      *ns*))
  (defn e-target? ([id] (not (zero? (db/eid->part (long ^java.lang.Number id))))))
  (reset-meta!
    #'e-target?
    (assoc
      {:private true, :arglists (clojure.core/list ['id]), :column (int 1)}
      :name
      'e-target?
      :ns
      *ns*))
  (defn a-target? ([id] (zero? (db/eid->part (long ^java.lang.Number id)))))
  (reset-meta!
    #'a-target?
    (assoc
      {:private true, :arglists (clojure.core/list ['id]), :column (int 1)}
      :name
      'a-target?
      :ns
      *ns*))
  (defn ref-datom?
    ([db d]
      (=
        20
        (.-vtypeid
          (db/require-attr
            db
            (java.lang.Integer/valueOf (int (.getA ^datomic.impl.db.IDatum d))))))))
  (reset-meta!
    #'ref-datom?
    (assoc
      {:private true,
       :arglists (clojure.core/list ['db (.withMeta 'd {:tag 'IDatum})]),
       :column (int 1)}
      :name
      'ref-datom?
      :ns
      *ns*))
  (defn target ([db spec] (let [id (:db/excise spec)] (or (:db/id id) (db/resolve-id db id)))))
  (reset-meta!
    #'target
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'spec]), :column (int 1)}
      :name
      'target
      :ns
      *ns*))
  (defn create-e->xpreds
    ([db specs]
      (reduce
        (fn fn__14109
          ([m spec]
            (let [e (datomic.excise/target db spec)]
              (if (datomic.excise/e-target? e)
                (let [vec__14110 (datomic.excise/pred-and-extent db spec)
                      epred (nth vec__14110 (int 0) nil)
                      es (nth vec__14110 (int 1) nil)]
                  (reduce (fn fn__14113 ([m e] (update m e conj epred))) m es))
                m))))
        {}
        specs)))
  (reset-meta!
    #'create-e->xpreds
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'specs]), :column (int 1)}
      :name
      'create-e->xpreds
      :ns
      *ns*))
  (defn create-es-pred
    ([db specs]
      (let [e_>xpreds (datomic.excise/create-e->xpreds db specs)
            xpreds (into #{} cat (vals e_>xpreds))]
        (reify
          datomic.excise.ExcisePred
          (ep-remove?
            [this d]
            (let [temp__5825__auto__ (concat
                                       (get e_>xpreds (long (.getE ^datomic.impl.db.IDatum d)))
                                       (when (datomic.excise/ref-datom? db d)
                                         (get e_>xpreds (.getV ^datomic.impl.db.IDatum d))))]
              (when temp__5825__auto__
                (let [xpreds temp__5825__auto__]
                  (some
                    (fn fn__14119 ([p1__14117#] (datomic.excise/ep-remove? p1__14117# d)))
                    xpreds)))))
          (ep-datoms [this] (mapcat datomic.excise/ep-datoms xpreds))))))
  (reset-meta!
    #'create-es-pred
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'specs]), :column (int 1)}
      :name
      'create-es-pred
      :ns
      *ns*))
  (defn create-a->xpreds
    ([db specs]
      (reduce
        (fn fn__14124
          ([m spec]
            (let [a (datomic.excise/target db spec)]
              (if (datomic.excise/a-target? a)
                (update m a conj (datomic.excise/pred db spec))
                m))))
        {}
        specs)))
  (reset-meta!
    #'create-a->xpreds
    (assoc
      {:private true, :arglists (clojure.core/list ['db 'specs]), :column (int 1)}
      :name
      'create-a->xpreds
      :ns
      *ns*))
  (defn create-as-pred
    ([db specs]
      (let [a_>xpreds (datomic.excise/create-a->xpreds db specs)]
        (reify
          datomic.excise.ExcisePred
          (ep-remove?
            [this d]
            (let [temp__5825__auto__ (get a_>xpreds (:a d))]
              (when temp__5825__auto__
                (let [xpreds temp__5825__auto__]
                  (some
                    (fn fn__14131 ([p1__14127#] (datomic.excise/ep-remove? p1__14127# d)))
                    xpreds)))))
          (ep-datoms
            [this]
            (mapcat
              (fn fn__14129 ([xpreds] (mapcat datomic.excise/ep-datoms xpreds)))
              (vals a_>xpreds)))))))
  (reset-meta!
    #'create-as-pred
    (assoc
      {:arglists (clojure.core/list ['db 'specs]), :column (int 1)}
      :name
      'create-as-pred
      :ns
      *ns*))
  (defn create-xpreds
    ([db specs]
      (when (seq specs)
        [(datomic.excise/create-es-pred db specs) (datomic.excise/create-as-pred db specs)])))
  (reset-meta!
    #'create-xpreds
    (assoc
      {:arglists (clojure.core/list ['db 'specs]), :column (int 1)}
      :name
      'create-xpreds
      :ns
      *ns*))
  (defn datoms ([epred] (datomic.excise/ep-datoms epred)))
  (reset-meta!
    #'datoms
    (assoc {:arglists (clojure.core/list ['epred]), :column (int 1)} :name 'datoms :ns *ns*))
  (defn remove? ([epred datom] (datomic.excise/ep-remove? epred datom)))
  (reset-meta!
    #'remove?
    (assoc
      {:arglists (clojure.core/list ['epred 'datom]), :column (int 1)}
      :name
      'remove?
      :ns
      *ns*)))