(do
  (clojure.core/in-ns 'datomic.tools.rebuild-log-leaf)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.data :as 'data]
        ['clojure.edn :as 'edn]
        ['clojure.pprint :as 'pp]
        ['clojure.spec.alpha :as 's]
        ['clojure.string :as 'str]
        ['cognitect.anomalies :as 'anom]
        ['datomic.db :as 'db]
        ['datomic.db.specs :as 'dspec]
        ['datomic.cluster :as 'cluster]
        ['datomic.common :as 'common]
        ['datomic.integrity :as 'int]
        ['datomic.io :as 'io]
        ['datomic.log :as 'log]
        ['datomic.log.specs :as 'lspec]
        ['datomic.memory-size :as 'msize]
        ['datomic.spec :as 'ds :refer (clojure.core/list 'conform!)]
        ['datomic.slf4j :as 'logger]
        ['datomic.tools :as 'tools])
      (clojure.core/import 'datomic.impl.db.IDatum)
      (clojure.core/import 'java.util.ArrayList)
      (clojure.core/import 'java.util.Collection)))
  (when-not (.equals 'datomic.tools.rebuild-log-leaf 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.rebuild-log-leaf))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.data :as 'data]
          ['clojure.edn :as 'edn]
          ['clojure.pprint :as 'pp]
          ['clojure.spec.alpha :as 's]
          ['clojure.string :as 'str]
          ['cognitect.anomalies :as 'anom]
          ['datomic.db :as 'db]
          ['datomic.db.specs :as 'dspec]
          ['datomic.cluster :as 'cluster]
          ['datomic.common :as 'common]
          ['datomic.integrity :as 'int]
          ['datomic.io :as 'io]
          ['datomic.log :as 'log]
          ['datomic.log.specs :as 'lspec]
          ['datomic.memory-size :as 'msize]
          ['datomic.spec :as 'ds :refer (clojure.core/list 'conform!)]
          ['datomic.slf4j :as 'logger]
          ['datomic.tools :as 'tools])
        (clojure.core/import 'datomic.impl.db.IDatum)
        (clojure.core/import 'java.util.ArrayList)
        (clojure.core/import 'java.util.Collection))))
  (set! *warn-on-reflection* true)
  (s/def-impl
    :datomic.tools.rebuild-log-leaf/bounding-segments
    (clojure.core/list 'clojure.spec.alpha/tuple :datomic.log/node-entry :datomic.log/node-entry)
    (s/tuple-impl
      [:datomic.log/node-entry :datomic.log/node-entry]
      [:datomic.log/node-entry :datomic.log/node-entry]))
  (s/def-impl
    :datomic.tools.rebuild-log-leaf/rebuild
    (clojure.core/list
      'clojure.spec.alpha/keys
      :req
      [:datomic.log/leaf :datomic.log/leaf-key :datomic.tools.rebuild-log-leaf/bounding-segments])
    (s/map-spec-impl
      {:req-un nil,
       :opt-un nil,
       :gfn nil,
       :pred-exprs
       [(fn fn__21507 ([G__21506] (map? G__21506)))
        (fn fn__21509 ([G__21506] (contains? G__21506 :datomic.log/leaf)))
        (fn fn__21511 ([G__21506] (contains? G__21506 :datomic.log/leaf-key)))
        (fn fn__21513
          ([G__21506] (contains? G__21506 :datomic.tools.rebuild-log-leaf/bounding-segments)))],
       :keys-pred
       (fn fn__21515
         ([G__21506]
           (and
             (map? G__21506)
             (contains? G__21506 :datomic.log/leaf)
             (contains? G__21506 :datomic.log/leaf-key)
             (contains? G__21506 :datomic.tools.rebuild-log-leaf/bounding-segments)))),
       :opt-keys [],
       :req-specs
       [:datomic.log/leaf :datomic.log/leaf-key :datomic.tools.rebuild-log-leaf/bounding-segments],
       :req
       [:datomic.log/leaf :datomic.log/leaf-key :datomic.tools.rebuild-log-leaf/bounding-segments],
       :req-keys
       [:datomic.log/leaf :datomic.log/leaf-key :datomic.tools.rebuild-log-leaf/bounding-segments],
       :opt-specs [],
       :pred-forms
       [(clojure.core/list 'clojure.core/fn ['%] (clojure.core/list 'clojure.core/map? '%))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :datomic.log/leaf))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list 'clojure.core/contains? '% :datomic.log/leaf-key))
        (clojure.core/list
          'clojure.core/fn
          ['%]
          (clojure.core/list
            'clojure.core/contains?
            '%
            :datomic.tools.rebuild-log-leaf/bounding-segments))],
       :opt nil}))
  (s/def-impl
    'datomic.tools.rebuild-log-leaf/find-bounding-segments
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :args
      (clojure.core/list 'clojure.spec.alpha/cat :log :datomic.log/log :k 'clojure.core/string?)
      :ret
      (clojure.core/list
        'clojure.spec.alpha/nilable
        :datomic.tools.rebuild-log-leaf/bounding-segments))
    (s/fspec-impl
      (s/spec-impl
        (clojure.core/list 'clojure.spec.alpha/cat :log :datomic.log/log :k 'clojure.core/string?)
        (s/cat-impl [:log :k] [:datomic.log/log string?] [:datomic.log/log 'clojure.core/string?])
        nil
        nil)
      (clojure.core/list 'clojure.spec.alpha/cat :log :datomic.log/log :k 'clojure.core/string?)
      (s/spec-impl
        (clojure.core/list
          'clojure.spec.alpha/nilable
          :datomic.tools.rebuild-log-leaf/bounding-segments)
        (s/nilable-impl
          :datomic.tools.rebuild-log-leaf/bounding-segments
          :datomic.tools.rebuild-log-leaf/bounding-segments
          nil)
        nil
        nil)
      (clojure.core/list
        'clojure.spec.alpha/nilable
        :datomic.tools.rebuild-log-leaf/bounding-segments)
      nil
      nil
      nil))
  (defn find-bounding-segments
    ([log k]
      (into
        []
        (first
          (filter
            (fn fn__21522
              ([p__21521]
                (let [vec__21523 p__21521
                      map__21526 (nth vec__21523 (int 0) nil)
                      map__21526 (if (seq? map__21526)
                                   (if (next map__21526)
                                     (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                       (to-array map__21526))
                                     (if (seq map__21526) (first map__21526) {}))
                                   map__21526)
                      uuid (get map__21526 :uuid)]
                  (= (str uuid) k))))
            (partition
              2
              1
              (eduction
                cat
                (map (fn fn__21528 ([p1__21520#] (into {} p1__21520#))))
                (log/log-dir-seq (log/seek-tx log 0)))))))))
  (reset-meta!
    #'find-bounding-segments
    (assoc
      {:arglists (clojure.core/list ['log 'k]), :column (int 1)}
      :name
      'find-bounding-segments
      :ns
      *ns*))
  (defn log-leaf-entries
    ([uri]
      (sequence
        cat
        (log/log-dir-seq (log/seek-tx (tools/log (tools/connection-resources uri)) 0)))))
  (reset-meta!
    #'log-leaf-entries
    (assoc
      {:arglists (clojure.core/list ['uri]), :column (int 1)}
      :name
      'log-leaf-entries
      :ns
      *ns*))
  (defn t ([d] (long (.getT ^datomic.impl.db.IDatum d))))
  (reset-meta!
    #'t
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'd {:tag 'IDatum})]),
       :column (int 1)}
      :name
      't
      :ns
      *ns*))
  (defn e ([d] (long (.getE ^datomic.impl.db.IDatum d))))
  (reset-meta!
    #'e
    (assoc
      {:private true,
       :arglists (clojure.core/list [(.withMeta 'd {:tag 'IDatum})]),
       :column (int 1)}
      :name
      'e
      :ns
      *ns*))
  (s/def-impl
    'datomic.tools.rebuild-log-leaf/build-leaf-segment
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :args
      (clojure.core/list
        'clojure.spec.alpha/cat
        :db
        'clojure.core/any?
        :initial-t
        :datomic.log/t
        :boundary-t
        :datomic.log/t)
      :ret
      :datomic.log/leaf)
    (s/fspec-impl
      (s/spec-impl
        (clojure.core/list
          'clojure.spec.alpha/cat
          :db
          'clojure.core/any?
          :initial-t
          :datomic.log/t
          :boundary-t
          :datomic.log/t)
        (s/cat-impl
          [:db :initial-t :boundary-t]
          [any? :datomic.log/t :datomic.log/t]
          ['clojure.core/any? :datomic.log/t :datomic.log/t])
        nil
        nil)
      (clojure.core/list
        'clojure.spec.alpha/cat
        :db
        'clojure.core/any?
        :initial-t
        :datomic.log/t
        :boundary-t
        :datomic.log/t)
      (s/spec-impl :datomic.log/leaf :datomic.log/leaf nil nil)
      :datomic.log/leaf
      nil
      nil
      nil))
  (defn build-leaf-segment
    ([db initial_t boundary_t]
      (mapv
        (fn fn__21536
          ([p__21535]
            (let [vec__21537 p__21535
                  t (nth vec__21537 (int 0) nil)
                  data (nth vec__21537 (int 1) nil)]
              (zipmap
                [:id :t :data]
                [(common/rand-uuid) t (java.util.ArrayList. ^java.util.Collection data)]))))
        (sort-by
          first
          (group-by
            t
            (filter
              (fn fn__21541 ([p1__21534#] (<= initial_t (t p1__21534#) (dec boundary_t))))
              (db/datoms db :eavt nil)))))))
  (reset-meta!
    #'build-leaf-segment
    (assoc
      {:arglists (clojure.core/list ['db 'initial-t 'boundary-t]), :column (int 1)}
      :name
      'build-leaf-segment
      :ns
      *ns*))
  (s/def-impl
    'datomic.tools.rebuild-log-leaf/semantic-tx
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :args
      (clojure.core/list 'clojure.spec.alpha/cat :tx :datomic.log/leaf-entry)
      :ret
      :datomic.log/semantic-tx)
    (s/fspec-impl
      (s/spec-impl
        (clojure.core/list 'clojure.spec.alpha/cat :tx :datomic.log/leaf-entry)
        (s/cat-impl [:tx] [:datomic.log/leaf-entry] [:datomic.log/leaf-entry])
        nil
        nil)
      (clojure.core/list 'clojure.spec.alpha/cat :tx :datomic.log/leaf-entry)
      (s/spec-impl :datomic.log/semantic-tx :datomic.log/semantic-tx nil nil)
      :datomic.log/semantic-tx
      nil
      nil
      nil))
  (defn semantic-tx ([tx] (update (dissoc tx :id) :data set)))
  (reset-meta!
    #'semantic-tx
    (assoc {:arglists (clojure.core/list ['tx]), :column (int 1)} :name 'semantic-tx :ns *ns*))
  (s/def-impl
    'datomic.tools.rebuild-log-leaf/semantic-diffs
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :args
      (clojure.core/list 'clojure.spec.alpha/cat :s1 :datomic.log/leaf :s2 :datomic.log/leaf))
    (s/fspec-impl
      (s/spec-impl
        (clojure.core/list 'clojure.spec.alpha/cat :s1 :datomic.log/leaf :s2 :datomic.log/leaf)
        (s/cat-impl
          [:s1 :s2]
          [:datomic.log/leaf :datomic.log/leaf]
          [:datomic.log/leaf :datomic.log/leaf])
        nil
        nil)
      (clojure.core/list 'clojure.spec.alpha/cat :s1 :datomic.log/leaf :s2 :datomic.log/leaf)
      (s/spec-impl 'clojure.core/any? any? nil nil)
      'clojure.core/any?
      nil
      nil
      nil))
  (defn semantic-diffs ([s1 s2] (data/diff (mapv semantic-tx s1) (mapv semantic-tx s2))))
  (reset-meta!
    #'semantic-diffs
    (assoc
      {:arglists (clojure.core/list ['s1 's2]), :column (int 1)}
      :name
      'semantic-diffs
      :ns
      *ns*))
  (s/def-impl
    'datomic.tools.rebuild-log-leaf/path-to-t
    (clojure.core/list
      'clojure.spec.alpha/fspec
      :args
      (clojure.core/list
        'clojure.spec.alpha/cat
        :log
        :datomic.log/log
        :olookup
        'clojure.core/any?
        :t
        :datomic.log/t))
    (s/fspec-impl
      (s/spec-impl
        (clojure.core/list
          'clojure.spec.alpha/cat
          :log
          :datomic.log/log
          :olookup
          'clojure.core/any?
          :t
          :datomic.log/t)
        (s/cat-impl
          [:log :olookup :t]
          [:datomic.log/log any? :datomic.log/t]
          [:datomic.log/log 'clojure.core/any? :datomic.log/t])
        nil
        nil)
      (clojure.core/list
        'clojure.spec.alpha/cat
        :log
        :datomic.log/log
        :olookup
        'clojure.core/any?
        :t
        :datomic.log/t)
      (s/spec-impl 'clojure.core/any? any? nil nil)
      'clojure.core/any?
      nil
      nil
      nil))
  (defn path-to-t
    ([log olookup t]
      (let [comp (common/key-comparator log/log-key)
            root_id (log/get-root-id log)
            root (common/getx olookup root_id)
            ridx (log/btree-search root t comp)
            dir_id (str (:uuid (nth root (int ^java.lang.Number ridx))))
            dir (common/getx olookup dir_id)
            didx (log/btree-search dir t comp)]
        {:root-id root_id,
         :root-size (java.lang.Integer/valueOf (int (count root))),
         :root (vec root),
         :root-idx ridx,
         :dir-id dir_id,
         :dir-size (java.lang.Integer/valueOf (int (count dir))),
         :dir (vec dir),
         :dir-idx didx})))
  (reset-meta!
    #'path-to-t
    (assoc
      {:arglists (clojure.core/list ['log 'olookup 't]), :column (int 1)}
      :name
      'path-to-t
      :ns
      *ns*))
  (defn rebuild-log-leaf
    ([uri k]
      (let [cr (tools/connection-resources uri)
            db (tools/index-db cr)
            log (tools/log cr)
            bounds (find-bounding-segments log k)]
        (if (s/valid? :datomic.tools.rebuild-log-leaf/bounding-segments bounds)
          (if (< (:indexBasisT db) (:t (second bounds)))
            #:cognitect.anomalies{:category :cognitect.anomalies/conflict,
                                  :message
                                  (str "Log leaf not in index up to t=" (:t (second bounds)))}
            (let [repair_id (str (common/rand-uuid))
                  leaf (build-leaf-segment db (:t (first bounds)) (:t (second bounds)))
                  leaf (assoc-in
                         leaf
                         [0 :datomic.tools.rebuild-log-leaf/rebuilt]
                         (java.util.Date.))
                  _ (ds/conform! :datomic.log/leaf leaf)]
              (push-thread-bindings (hash-map #'*print-length* nil #'*print-level* nil))
              (try
                (spit
                  repair_id
                  {:datomic.log/leaf leaf,
                   :datomic.log/leaf-key k,
                   :datomic.tools.rebuild-log-leaf/bounding-segments bounds})
                (finally (pop-thread-bindings)))
              {:leaf-txes (java.lang.Integer/valueOf (int (count leaf))),
               :leaf-datoms
               (reduce
                 +
                 (map
                   (fn fn__21550
                     ([p1__21547#] (java.lang.Integer/valueOf (int (count (:data p1__21547#))))))
                   leaf)),
               :leaf-size (long (msize/memory-size leaf)),
               :leaf-id repair_id,
               :install-cmd
               (str/join " " ["bin/run -m datomic.tools.rebuild-log-leaf -i" uri repair_id])}))
          #:cognitect.anomalies{:category :cognitect.anomalies/conflict,
                                :message (str "Unable to find rewriteable log segment " k)}))))
  (reset-meta!
    #'rebuild-log-leaf
    (assoc
      {:arglists (clojure.core/list ['uri 'k]), :column (int 1)}
      :name
      'rebuild-log-leaf
      :ns
      *ns*))
  (defn race-to-adopt
    ([cr root_id retry_limit]
      (let [cluster (:cluster cr)]
        (loop [n 0]
          (do
            (when (>= n retry_limit)
              (throw (ex-info "Unable to update log root" {:attempts (long n)})))
            (or
              (try
                [(long n) (:desc (log/adopt-root (tools/log cr) cluster root_id nil))]
                (catch
                  java.lang.Throwable
                  t
                  (do
                    (let [logger (org.slf4j.LoggerFactory/getLogger
                                   "datomic.tools.rebuild-log-leaf")
                          ex t]
                      (when (.isInfoEnabled ^org.slf4j.Logger logger)
                        (.info
                          ^org.slf4j.Logger logger
                          (logger/process "Lost race to replace log root")
                          ^java.lang.Throwable ex)
                        (logger/caused-by logger ex))
                      nil)
                    nil)))
              (recur (inc n))))))))
  (reset-meta!
    #'race-to-adopt
    (assoc
      {:arglists (clojure.core/list ['cr 'root-id 'retry-limit]), :column (int 1)}
      :name
      'race-to-adopt
      :ns
      *ns*))
  (defn install-rebuilt-leaf
    ([uri rebuild]
      (ds/conform! :datomic.tools.rebuild-log-leaf/rebuild rebuild)
      (let [cr (tools/connection-resources uri)
            db (tools/index-db cr)
            log (tools/log cr)
            bounds (find-bounding-segments log (:datomic.log/leaf-key rebuild))]
        (if (= bounds (:datomic.tools.rebuild-log-leaf/bounding-segments rebuild))
          (let [vec__21557 (repeatedly (fn fn__21563 ([] (common/rand-uuid))))
                root_id (nth vec__21557 (int 0) nil)
                dir_id (nth vec__21557 (int 1) nil)
                leaf_id (nth vec__21557 (int 2) nil)
                leaf (:datomic.log/leaf rebuild)
                path (path-to-t log (:olookup cr) (:t (first bounds)))
                _ (ds/conform! :datomic.log/path path)
                dir (assoc-in (:dir path) [(:dir-idx path) :uuid] leaf_id)
                root (assoc-in (:root path) [(:root-idx path) :uuid] dir_id)
                cs (:cluster cr)
                leaf_res (deref (log/zip-and-create cs leaf_id (log/fressianed-leaf leaf)))
                dir_res (deref (log/zip-and-create cs dir_id (log/fressianed-dir dir)))
                root_res (deref (log/zip-and-create cs root_id (log/fressianed-dir root)))
                vec__21560 (when (= :created leaf_res dir_res root_res)
                             (race-to-adopt cr root_id 10))
                retries (nth vec__21560 (int 0) nil)
                tail_desc (nth vec__21560 (int 1) nil)]
            {:before path,
             :after
             {:root-id root_id,
              :root root,
              :root-idx (:root-idx path),
              :root-size (java.lang.Integer/valueOf (int (count root))),
              :dir-id dir_id,
              :dir dir,
              :dir-idx (:dir-idx path),
              :dir-size (java.lang.Integer/valueOf (int (count dir)))},
             :updates
             {:leaf-res leaf_res,
              :dir-res dir_res,
              :root-res root_res,
              :retries retries,
              :tail-desc tail_desc}})
          #:cognitect.anomalies{:category :cognitect.anomalies/conflict,
                                :message "Segment to be repaired does not exist"}))))
  (reset-meta!
    #'install-rebuilt-leaf
    (assoc
      {:arglists (clojure.core/list ['uri 'rebuild]), :column (int 1)}
      :name
      'install-rebuilt-leaf
      :ns
      *ns*))
  (defn crosscheck-log
    ([uri]
      (let [cr (tools/connection-resources uri)
            map__21566 (tools/db-resources cr)
            map__21566 (if (seq? map__21566)
                         (if (next map__21566)
                           (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                             (to-array map__21566))
                           (if (seq map__21566) (first map__21566) {}))
                         map__21566)
            db (get map__21566 :db)
            log (get map__21566 :log)
            ct (atom 0)]
        (int/crosscheck-log
          log
          db
          :eavt
          (fn fn__21567 ([_] (when (zero? (mod (swap! ct inc) 10000)) (print ".") (flush))))))))
  (reset-meta!
    #'crosscheck-log
    (assoc {:arglists (clojure.core/list ['uri]), :column (int 1)} :name 'crosscheck-log :ns *ns*))
  (defn -main*
    ([flag uri k]
      (if (= flag "-r")
        (pp/pprint (rebuild-log-leaf uri k))
        (if (= flag "-i")
          (pp/pprint
            (install-rebuilt-leaf
              uri
              (edn/read-string {:readers {'datom #'tools/datom-read-handler}} (slurp k))))
          (do
            (when :default (throw (java.lang.IllegalArgumentException. "Invalid command.")))
            nil)))))
  (reset-meta!
    #'-main*
    (assoc
      {:arglists (clojure.core/list ['flag 'uri 'k]), :column (int 1)}
      :name
      '-main*
      :ns
      *ns*))
  (defn -main ([& args] (try (apply -main* args) (finally (shutdown-agents)))))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name '-main :ns *ns*)))