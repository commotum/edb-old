(do
  (clojure.core/in-ns 'datomic.garbage.fressian)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.garbage.fressian)
    {:doc
     "Fressian representations for persistent garbage trees. Root and directory nodes reference child values; leaf nodes contain immutable segment identifiers eligible for later reclamation."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['datomic.fressian :as 'fressian])
      (clojure.core/import 'org.fressian.handlers.ReadHandler)
      (clojure.core/import 'org.fressian.handlers.WriteHandler)))
  (when-not (.equals 'datomic.garbage.fressian 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.garbage.fressian))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['datomic.fressian :as 'fressian])
        (clojure.core/import 'org.fressian.handlers.ReadHandler)
        (clojure.core/import 'org.fressian.handlers.WriteHandler))))
  (.setMeta
    (clojure.lang.RT/var "datomic.garbage.fressian" "->GarbageRoot")
    {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.garbage.fressian" "map->GarbageRoot")
    {:declared true, :column (int 1)})
  (defrecord GarbageRoot [children])
  (clojure.core/import 'datomic.garbage.fressian.GarbageRoot)
  (defn ->GarbageRoot ([children] (datomic.garbage.fressian.GarbageRoot. children)))
  (reset-meta!
    #'->GarbageRoot
    (assoc
      {:arglists (clojure.core/list ['children]), :column (int 1)}
      :name
      '->GarbageRoot
      :ns
      *ns*))
  (defn map->GarbageRoot
    ([m__8001__auto__]
      (GarbageRoot/create
        (if (instance? clojure.lang.MapEquivalence m__8001__auto__)
          m__8001__auto__
          (into {} m__8001__auto__)))))
  (reset-meta!
    #'map->GarbageRoot
    (assoc
      {:arglists (clojure.core/list ['m__8001__auto__]), :column (int 1)}
      :name
      'map->GarbageRoot
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.garbage.fressian" "->GarbageDir")
    {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.garbage.fressian" "map->GarbageDir")
    {:declared true, :column (int 1)})
  (defrecord GarbageDir [children])
  (clojure.core/import 'datomic.garbage.fressian.GarbageDir)
  (defn ->GarbageDir ([children] (datomic.garbage.fressian.GarbageDir. children)))
  (reset-meta!
    #'->GarbageDir
    (assoc
      {:arglists (clojure.core/list ['children]), :column (int 1)}
      :name
      '->GarbageDir
      :ns
      *ns*))
  (defn map->GarbageDir
    ([m__8001__auto__]
      (GarbageDir/create
        (if (instance? clojure.lang.MapEquivalence m__8001__auto__)
          m__8001__auto__
          (into {} m__8001__auto__)))))
  (reset-meta!
    #'map->GarbageDir
    (assoc
      {:arglists (clojure.core/list ['m__8001__auto__]), :column (int 1)}
      :name
      'map->GarbageDir
      :ns
      *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.garbage.fressian" "->GarbageLeaf")
    {:declared true, :column (int 1)})
  (.setMeta
    (clojure.lang.RT/var "datomic.garbage.fressian" "map->GarbageLeaf")
    {:declared true, :column (int 1)})
  (defrecord GarbageLeaf [children])
  (clojure.core/import 'datomic.garbage.fressian.GarbageLeaf)
  (defn ->GarbageLeaf ([children] (datomic.garbage.fressian.GarbageLeaf. children)))
  (reset-meta!
    #'->GarbageLeaf
    (assoc
      {:arglists (clojure.core/list ['children]), :column (int 1)}
      :name
      '->GarbageLeaf
      :ns
      *ns*))
  (defn map->GarbageLeaf
    ([m__8001__auto__]
      (GarbageLeaf/create
        (if (instance? clojure.lang.MapEquivalence m__8001__auto__)
          m__8001__auto__
          (into {} m__8001__auto__)))))
  (reset-meta!
    #'map->GarbageLeaf
    (assoc
      {:arglists (clojure.core/list ['m__8001__auto__]), :column (int 1)}
      :name
      'map->GarbageLeaf
      :ns
      *ns*))
  (defn write-handler
    ([tag]
      (reify
        org.fressian.handlers.WriteHandler
        (^void write
          [this ^org.fressian.Writer w o]
          (do
            (.writeTag ^org.fressian.Writer w tag (int 1))
            (.writeObject ^org.fressian.Writer w (:children o))
            nil)))))
  (reset-meta!
    #'write-handler
    (assoc
      {:private true, :arglists (clojure.core/list ['tag]), :column (int 1)}
      :name
      'write-handler
      :ns
      *ns*))
  (defn read-handler
    ([factory]
      (reify
        org.fressian.handlers.ReadHandler
        (read
          [this ^org.fressian.Reader rdr tag ^int component_count]
          (^clojure.lang.IFn factory (into [] (.readObject ^org.fressian.Reader rdr)))))))
  (reset-meta!
    #'read-handler
    (assoc
      {:private true, :arglists (clojure.core/list ['factory]), :column (int 1)}
      :name
      'read-handler
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.garbage.fressian" "write-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.garbage.fressian" "write-handlers")
    (assoc
      fressian/user-write-handlers
      datomic.garbage.fressian.GarbageRoot
      {"garbage-root" (write-handler "garbage-root")}
      datomic.garbage.fressian.GarbageDir
      {"garbage-dir" (write-handler "garbage-dir")}
      datomic.garbage.fressian.GarbageLeaf
      {"garbage-leaf" (write-handler "garbage-leaf")}
      clojure.lang.PersistentVector
      {"vec"
       (reify
         org.fressian.handlers.WriteHandler
         (^void write
           [this ^org.fressian.Writer w o]
           (do
             (.writeTag ^org.fressian.Writer w "vec" (int 1))
             (.writeObject ^org.fressian.Writer w (seq o))
             nil)))}))
  (.setMeta (clojure.lang.RT/var "datomic.garbage.fressian" "read-handlers") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.garbage.fressian" "read-handlers")
    (assoc
      fressian/user-read-handlers
      "garbage-root"
      (read-handler ->GarbageRoot)
      "garbage-dir"
      (read-handler ->GarbageDir)
      "garbage-leaf"
      (read-handler ->GarbageLeaf)
      "vec"
      (reify
        org.fressian.handlers.ReadHandler
        (read
          [this ^org.fressian.Reader rdr _ ^int _]
          (let [os (.readObject ^org.fressian.Reader rdr)]
            (clojure.lang.PersistentVector/create (seq os))))))))
