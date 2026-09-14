(do
  (clojure.core/in-ns 'datomic.tools.rebuild-log-root)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['clojure.pprint :as 'pp]
        ['datomic.cluster :as 'cluster]
        ['datomic.common :as 'common]
        ['datomic.log :as 'log]
        ['datomic.log.specs :as 'log-specs]
        ['datomic.spec :refer (clojure.core/list 'conform!)]
        ['datomic.tools :as 'tools]
        ['datomic.tools.log-tools :as 'lt])))
  (when-not (.equals 'datomic.tools.rebuild-log-root 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.tools.rebuild-log-root))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['clojure.pprint :as 'pp]
          ['datomic.cluster :as 'cluster]
          ['datomic.common :as 'common]
          ['datomic.log :as 'log]
          ['datomic.log.specs :as 'log-specs]
          ['datomic.spec :refer (clojure.core/list 'conform!)]
          ['datomic.tools :as 'tools]
          ['datomic.tools.log-tools :as 'lt]))))
  (set! *warn-on-reflection* true)
  (defn -main*
    ([& args]
      (let [G__27576 (count args)]
        (case
          G__27576
          1
          (let [cr (tools/connection-resources (first args))]
            (pp/pprint
              (lt/race-to-transform-root
                (:cluster cr)
                (fn fn__27577
                  ([root_key]
                    (let [map__27578 (lt/rebuild-root (:olookup cr) root_key)
                          map__27578 (if (seq? map__27578)
                                       (if (next map__27578)
                                         (clojure.lang.PersistentArrayMap/createAsIfByAssoc
                                           (to-array map__27578))
                                         (if (seq map__27578) (first map__27578) {}))
                                       map__27578)
                          rebuilt (get map__27578 :rebuilt)
                          root (get map__27578 :root)]
                      (pp/pprint {:rebuilt rebuilt})
                      (if (seq rebuilt)
                        (let [id (common/rand-uuid)]
                          (datomic.spec/conform! :datomic.log/node root)
                          (lt/write* (:cluster cr) id (log/fressianed-dir root))
                          (cluster/uuid->val-key id))
                        root_key))))
                10)))
          2
          (let [cr (tools/connection-resources (second args))
                root_id (:d/r (first (log/read-tail-descriptor (:cluster cr))))]
            (println "Dry run. Changes needed: ")
            (pp/pprint (:rebuilt (lt/rebuild-root (:olookup cr) root_id))))))))
  (reset-meta!
    #'-main*
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name '-main* :ns *ns*))
  (defn -main ([& args] (try (apply -main* args) (finally (shutdown-agents)))))
  (reset-meta!
    #'-main
    (assoc {:arglists (clojure.core/list ['& 'args]), :column (int 1)} :name '-main :ns *ns*)))