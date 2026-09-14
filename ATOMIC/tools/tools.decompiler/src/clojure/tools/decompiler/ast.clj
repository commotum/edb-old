;;   Copyright (c) Nicola Mometto & contributors.


(ns clojure.tools.decompiler.ast
  (:require [clojure.set :as set]
            [clojure.string :as s]
            [clojure.walk :as walk]
            [clojure.tools.decompiler.bc :as bc]
            [clojure.tools.decompiler.utils :refer [peek-n pop-n] :as u]))

;; WIP casting, type hints

(declare bc->ast)

(def initial-ctx {:fields {}
                  :statements []
                  :ast {}})

(def initial-local-ctx {:stack []
                        :pc 0
                        :ast {}
                        :impure-loops #{} ;; loops with no bindings, pure jumps
                        :local-variable-table #{}
                        :exception-table #{}
                        :reachable #{}})

(defn pc= [terminate-at]
  (fn [{:keys [pc]}]
    (= pc terminate-at)))

(defn goto-label [{:insn/keys [jump-offset label]}]
  (+ jump-offset label))

(def nil-expr {:op :const :val nil})

(defn ->do [exprs]
  {:op :do
   :statements (vec (remove #{nil-expr} (butlast exprs)))
   :ret (or (last exprs) nil-expr)})

(defn expr+statements [ctx]
  (->do (conj (-> ctx :statements)
              (-> ctx :stack peek))))

(defn curr-insn [{:keys [insns jump-table pc]}]
  (->> pc
       (get jump-table)
       (nth insns)))

(defn insn-at [{:keys [insns jump-table pc]} {:keys [label offset] :or {label pc offset 0}}]
  (->> label
       (get jump-table)
       (+ offset)
       (nth insns)))

(defn maybe-insn-at [{:keys [insns jump-table pc]} {:keys [label offset] :or {label pc offset 0}}]
  (some->> label
           (get jump-table)
           (+ offset)
           (get insns)))

(defn find-local-variable [{:keys [local-variable-table]} index label]
  (->> local-variable-table
       (filter (comp #{index} :index))
       (filter (comp (partial >= label) :start-label))
       (filter (comp (partial < label) :end-label))
       (sort-by :start-label)
       (first)))

(defn find-init-local [{:keys [local-variable-table]} label]
  (->> local-variable-table
       (filter (comp (partial = label) :start-label))
       ;; why is this here?
       (filter (comp (partial < label) :end-label))
       (sort-by :start-label)
       (first)))

(defn find-no-op-local-init [{:keys [local-variable-table]} index label]
  (->> local-variable-table
       (filter (comp #{index} :index))
       (filter (comp #{label} :start-label))
       (filter (comp #{label} :end-label))
       (sort-by :start-label)
       (first)))

(defn init-local-variable? [{:insn/keys [label length]} {:keys [start-label]}]
  (= (+ label length) start-label))

;; process-* : bc, ctx -> ctx
;; decompile-* : bc, ctx -> AST

(defn restrict [x y]
  (if x (some-fn x y) y))

(defn get-reachable [pc reachable {:keys [insns jump-table] :as ctx}]
  (if (or ;; we're exiting
       (not (get jump-table pc))
       ;; we've already visited this node
       (contains? reachable pc))
    reachable
    (let [insn (curr-insn (assoc ctx :pc pc))
          reachable (conj reachable pc)
          insn-name (:insn/name insn)]
      (cond

        (= insn-name "goto")
        (recur (+ pc (:insn/jump-offset insn)) reachable ctx)

        (= insn-name "athrow")
        (recur -1 reachable ctx)

        (:insn/jump-targets insn)
        (let [{:insn/keys [jump-offsets default-offset]} (:insn/jump-targets insn)
              reachable (reduce (fn [reachable offset]
                                  (get-reachable (+ pc offset) reachable ctx))
                                reachable
                                (conj jump-offsets default-offset))]
          (recur -1 reachable ctx))

        (:insn/jump-offset insn)
        (let [reachable (get-reachable (+ pc (:insn/length insn)) reachable ctx)]
          (recur (+ pc (:insn/jump-offset insn)) reachable ctx))

        :else (recur (+ pc (:insn/length insn)) reachable ctx)))))

(defn collect-reachable [{:keys [exception-table] :as ctx}]
  ;; Exception handlers are control-flow roots just as method entry is. If we
  ;; trace only from pc 0, every real ATHROW in a catch body is mislabeled
  ;; unreachable alongside the compiler's genuinely unreachable padding.
  (let [reachable (reduce (fn [reachable handler-label]
                            (get-reachable handler-label reachable ctx))
                          (get-reachable 0 #{} ctx)
                          (map :handler-label exception-table))]
    (assoc ctx :reachable reachable)))

(def process-insn nil)
(defmulti process-insn
  (fn [_ {:insn/keys [name]}] (keyword name))
  :hierarchy #'bc/insn-h)

(defmethod process-insn :default [ctx {:insn/keys [name]}]
  (println "INSN NOT HANDLED:" name)
  (throw (Exception. ":("))
  ctx)

(defn start-try-block-info [pc exception-table]
  (seq (filter (comp #{pc} :start-label) exception-table)))

(defn coalesce-split-try-ranges
  "Clojure's compiler may split one logical protected region around an
  unreachable loop back-edge. The split ranges retain the same handlers.
  Present them as one range so a return-through-finally slot is not mistaken
  for a lexical binding."
  [pc exception-table]
  (let [handler-key (juxt :handler-label :type)
        initial (vec (start-try-block-info pc exception-table))
        logical-ranges
        (mapv (fn [entry]
                (let [key (handler-key entry)
                      continuations
                      (filter #(and (= key (handler-key %))
                                    (< pc (:start-label %))
                                    (< (:start-label %) (:handler-label entry)))
                              exception-table)
                      consumed (vec (cons entry continuations))]
                  {:entry entry
                   :consumed consumed
                   :end-label (reduce max (map :end-label consumed))}))
              initial)
        ;; When nested try regions start at the same bytecode label, recover
        ;; the widest (outer) region first. Its body retains the narrower
        ;; entries, allowing process-insns to reconstruct them recursively.
        outer-end (reduce max (map :end-label logical-ranges))
        selected (filter #(= outer-end (:end-label %)) logical-ranges)
        handlers (mapv #(assoc (:entry %)
                               :start-label pc
                               :end-label outer-end)
                       selected)
        consumed (vec (mapcat :consumed selected))]
    {:handlers handlers
     :consumed consumed}))

(defn compiler-return-spill?
  "True when LABEL stores through an unnamed slot which is loaded later. The
  Clojure compiler emits this shape to carry a try/catch value across finally."
  [{:keys [insns jump-table] :as ctx} label]
  (let [{:insn/keys [name length local-variable-element]}
        (curr-insn (assoc ctx :pc label))
        index (:insn/target-index local-variable-element)]
    (and (isa? bc/insn-h (keyword name) ::bc/store-insn)
         (nil? (find-local-variable ctx index (+ label length)))
         (some (fn [{:insn/keys [name local-variable-element]}]
                 (and (isa? bc/insn-h (keyword name) ::bc/load-insn)
                      (= index (:insn/target-index local-variable-element))))
               (drop (inc (get jump-table label)) insns)))))

(defn >process-insn [{:keys [pc] :as ctx} {:insn/keys [length] :as insn}]
  (-> (process-insn ctx insn)
      (update :pc (fn [new-pc]
                    (if (= new-pc pc)
                      ;; last insn wasn't an explicit jump, goto next insn
                      (+ new-pc length)
                      new-pc)))))

(declare process-insns)

(defn try-return-label
  "Find the continuation after a reconstructed try. Older Clojure AOT puts a
  positive GOTO immediately before the first handler. Newer AOT can remove an
  unreachable normal path and leave ATHROW/NOP padding there. In that case a
  later handler may still have the return-spill GOTO; otherwise an outer branch
  target reachable from method entry is the continuation. A terminal try has
  no continuation and is sent to the method-end sentinel."
  [{:keys [insns jump-table pc] :as ctx} handlers]
  (let [return-goto
        (some (fn [{:keys [handler-label]}]
                (let [{:insn/keys [name jump-offset] :as prior}
                      (insn-at ctx {:label handler-label :offset -1})]
                  (when (and (= "goto" name) (pos? jump-offset)) prior)))
              (sort-by :handler-label handlers))
        entry-reachable (get-reachable 0 #{} ctx)
        max-handler-label (reduce max (map :handler-label handlers))
        pre-try-targets
        (->> insns
             (take-while #(< (:insn/label %) pc))
             (mapcat
               (fn [{:insn/keys [label jump-offset jump-targets]}]
                 (cond
                   jump-offset
                   [(+ label jump-offset)]

                   jump-targets
                   (mapv (partial + label)
                         (conj (:insn/jump-offsets jump-targets)
                               (:insn/default-offset jump-targets)))

                   :else
                   [])))
             set)
        outer-continuation
        (->> pre-try-targets
             (filter #(and (contains? jump-table %)
                           (> % max-handler-label)
                           (contains? entry-reachable %)))
             sort
             first)]
    (cond
      return-goto
      (:insn/label
        (insn-at ctx {:label (goto-label return-goto) :offset 1}))

      outer-continuation
      outer-continuation

      :else
      (if-let [{:insn/keys [label length]} (peek insns)]
        (+ label length)
        0))))

(defn exceptional-finally-range
  "Return the executable body of a catch-all finally handler, excluding its
  synthetic exception spill and terminal reload/ATHROW. This is needed when a
  newer compiler has removed the unreachable normal-path copy of a finally."
  [{:keys [insns jump-table] :as ctx} {:keys [handler-label]}]
  (let [{:insn/keys [name local-variable-element]}
        (insn-at ctx {:label handler-label})
        exception-index (:insn/target-index local-variable-element)
        start-insn (insn-at ctx {:label handler-label :offset 1})
        tail-load
        (when (isa? bc/insn-h (keyword name) ::bc/store-insn)
          (some (fn [idx]
                  (let [{load-name :insn/name
                         load-local :insn/local-variable-element
                         :as load-insn} (nth insns idx)
                        next-insn (nth insns (inc idx) nil)]
                    (when (and (isa? bc/insn-h (keyword load-name)
                                    ::bc/load-insn)
                               (= exception-index
                                  (:insn/target-index load-local))
                               (= "athrow" (:insn/name next-insn)))
                      load-insn)))
                (range (inc (get jump-table handler-label))
                       (dec (count insns)))))]
    (when (and start-insn tail-load)
      {:start-label (:insn/label start-insn)
       :end-label (:insn/label tail-load)})))

(defn process-try-block [{:keys [pc exception-table enclosing-end-label] :as ctx}]
  (let [{:keys [handlers consumed]} (coalesce-split-try-ranges pc exception-table)

        first-handler (->> handlers (sort-by :handler-label) first)

        body-end-label (:end-label first-handler)

        ret-label (try-return-label ctx handlers)

        expr-ctx (-> ctx
                     (update :exception-table #(apply disj % consumed)))

        ;; WIP: need to backup lvt?
        body-ctx (-> expr-ctx
                     (assoc :statements [])
                     (assoc :enclosing-end-label body-end-label)
                     (assoc :terminate? (restrict (:terminate? expr-ctx) (pc= body-end-label)))
                     (process-insns))

        body-statements (-> body-ctx :statements)
        body-stack (-> body-ctx :stack)
        body-end-insn (curr-insn (assoc body-ctx :pc body-end-label))
        body-terminal-throw? (and (= "athrow" (:insn/name body-end-insn))
                                  (seq body-stack)
                                  (contains? (:reachable body-ctx)
                                             body-end-label))
        body-exprs (cond
                     body-terminal-throw?
                     (conj body-statements
                           {:op :throw :ex (peek body-stack)})

                     (and (empty? body-stack)
                          (= :loop (:op (peek body-statements)))
                          (compiler-return-spill? ctx body-end-label))
                     body-statements

                     :else
                     (conj body-statements (peek body-stack)))
        body (->do body-exprs)

        next-insn (insn-at body-ctx {:offset 1})

        ?finally (when-let [finally-handler (->> handlers
                                                 (remove :type)
                                                 (sort-by :handler-label)
                                                 first)]
                   (let [inline-range
                         (when (and (< (:insn/label next-insn)
                                       (:handler-label first-handler))
                                    (not (#{"athrow" "nop"}
                                           (:insn/name next-insn))))
                           {:start-label (:insn/label next-insn)
                            :end-label
                            (:insn/label
                              (insn-at ctx
                                       {:label (:handler-label first-handler)
                                        :offset -1}))})
                         {:keys [start-label end-label]}
                         (or inline-range
                             (exceptional-finally-range ctx finally-handler))
                         finally-ctx (process-insns (-> expr-ctx
                                                        (assoc :pc start-label)
                                                        (assoc :statements [])
                                                        (assoc :enclosing-end-label end-label)
                                                        (assoc :terminate? (restrict (:terminate? expr-ctx ) (pc= end-label)))))]
                     (->do (-> finally-ctx :statements))))

        ?catches (when-let [catches (seq (filter :type handlers))]
                   (->>
                    (for [{:keys [handler-label type]} catches
                          :let [{:keys [start-label end-label name] :as local} (find-init-local ctx handler-label)]]
                      (let [end-label (:insn/label (insn-at ctx {:label end-label :offset -1}))
                            catch-ctx (process-insns (-> expr-ctx
                                                         (assoc :pc start-label)
                                                         (assoc :exception-table #{})
                                                         (update :stack conj local)
                                                         (assoc :statements [])
                                                         (assoc :enclosing-end-label end-label)
                                                         (assoc :terminate? (restrict (:terminate? expr-ctx) (pc= end-label)))))]
                        {:op :catch
                         :local {:name name
                                 :type type}
                         :body (->do (conj (-> catch-ctx :statements)
                                           (-> catch-ctx :stack peek)))}))
                    (into [])))

        expr (if (or ?finally
                     (seq ?catches))
               {:op :try
                :catches ?catches
                :finally ?finally
                :body body}
               body)

        continuation-label (if (and enclosing-end-label
                                    (< pc enclosing-end-label ret-label))
                             enclosing-end-label
                             ret-label)]

    (-> ctx
        (update :stack conj expr)
        (assoc :recur? (:recur? body-ctx))
        (assoc :pc continuation-label))))

;; doesn't handle wrapping try/catch/finally
(defn forward-goto-value-return-epilogue?
  "Recognize the one stack-preserving control-flow shape in which a value at
  an expression boundary jumps forward to a shared value-return epilogue.
  The jump must have a real, strictly later target, and that target may only
  contain the same inert return scaffolding accepted by will-ret? before an
  explicit JVM value return.  In particular, a second goto is not followed."
  [ctx {:insn/keys [label] :as goto-insn}]
  (let [target-label (goto-label goto-insn)]
    (and (< label target-label)
         (some? (maybe-insn-at ctx {:label target-label}))
         (loop [off 0]
           (let [{:insn/keys [name] :as insn}
                 (maybe-insn-at ctx {:label target-label :offset off})]
             (cond
               (not insn)
               false

               (isa? bc/insn-h (keyword name) ::bc/return-value)
               true

               (or (#{"invokestatic" "checkcast"} name)
                   (isa? bc/insn-h (keyword name) ::bc/no-op)
                   (isa? bc/insn-h
                         (keyword name)
                         ::bc/invoke-instance-method))
               (recur (inc off))

               :else
               false))))))

(defn will-ret? [ctx label]
  (loop [off 0]
    (let [{:insn/keys [name] :as insn} (maybe-insn-at ctx {:label label :offset off})]
      (cond
        (not insn)
        true

        (or (#{"invokestatic" "checkcast"} name)
            (isa? bc/insn-h (keyword name) ::bc/no-op)
            (isa? bc/insn-h (keyword name) ::bc/return-value)
            (isa? bc/insn-h (keyword name) ::bc/invoke-instance-method))
        (recur (inc off))

        (= "goto" name)
        (forward-goto-value-return-epilogue? ctx insn)

        :else

        false))))

(defn process-impure-loop [{:keys [impure-loops pc] :as ctx}]
  (let [loop-entry-label (impure-loops pc)
        enclosing-end-insn (when-let [label (:enclosing-end-label ctx)]
                             (maybe-insn-at ctx {:label label}))
        enclosing-end-is-backedge?
        (and (= "goto" (:insn/name enclosing-end-insn))
             (= pc (goto-label enclosing-end-insn)))
        body-terminate? (if enclosing-end-is-backedge?
                          :recur?
                          (restrict (:terminate? ctx) :recur?))

        {body-stack :stack body-stmnts :statements
         body-recur? :recur? body-pc :pc} (process-insns (-> ctx
                                                              (assoc :loop-args [])
                                                              (update :impure-loops disj pc)
                                                              (assoc :impure-loop-entry pc)
                                                              (assoc :loop-end-label loop-entry-label)
                                                              (assoc :terminate? body-terminate?)
                                                              (assoc :statements [])))
        ;; impure-loops is a set, so its value is the loop entry.  Whether the
        ;; loop supplies the enclosing expression's value is determined only
        ;; by the continuation reached after processing its body.
        statement? (not (will-ret? ctx body-pc))
        body (->do (conj body-stmnts (peek body-stack)))
        loop-expr {:op :loop
                   :local-variables []
                   :body body}]
    (-> ctx
        (assoc :pc body-pc)
        (update (if statement? :statements :stack) conj loop-expr))))

(defn continue-within-enclosing-region
  "Structured bytecode recovery can advance directly to a lexical or try
  continuation beyond the protected region currently being parsed. Stop at
  that enclosing boundary so its caller can reconstruct the surrounding
  catch/finally before resuming the continuation."
  [{start-label :pc end-label :enclosing-end-label} next-ctx]
  (let [continuation-label (:pc next-ctx)]
    (if (and end-label
             continuation-label
             (< start-label end-label continuation-label))
      (assoc next-ctx :pc end-label)
      next-ctx)))

(defn process-insns [{:keys [pc jump-table exception-table terminate? impure-loops]
                      :as ctx}]
  (cond
    (or (not (get jump-table pc))
        (and terminate? (terminate? ctx)))
    ctx

    (start-try-block-info pc exception-table)
    (recur (continue-within-enclosing-region ctx (process-try-block ctx)))

    (contains? impure-loops pc)
    (recur (continue-within-enclosing-region ctx (process-impure-loop ctx)))

    :else
    (let [insn (curr-insn ctx)]
      (recur (continue-within-enclosing-region ctx (>process-insn ctx insn))))))

(defmethod process-insn ::bc/no-op [ctx _]
  ctx)

(defmethod process-insn ::bc/const-insn [ctx {:insn/keys [pool-element]}]
  (-> ctx
      (update :stack conj {:op :const
                           :val (:insn/target-value pool-element)})))

(defmethod process-insn :swap [{:keys [stack] :as ctx} _]
  (let [[v2 v1] (peek-n stack 2)]
    (-> ctx
        (update :stack pop-n 2)
        (update :stack conj v1 v2))))

(defmethod process-insn :dup_x1 [{:keys [stack] :as ctx} _]
  (let [[v2 v1] (peek-n stack 2)]
    (-> ctx
        (update :stack pop-n 2)
        (update :stack conj v1 v2 v1))))

(defmethod process-insn :dup [{:keys [stack] :as ctx} _]
  (let [val (peek stack)]
    (-> ctx
        (update :stack conj val))))

(defmethod process-insn :anewarray [{:keys [stack] :as ctx} _]
  (let [{dimension :val} (peek stack)
        expr {:op :array
              :!items (atom (vec (repeat dimension nil-expr)))}]

    (-> ctx
        (update :stack pop)
        (update :stack conj expr))))

(defmethod process-insn ::bc/array-store [{:keys [stack] :as ctx} _]
  (let [[{:keys [!items]} {index :val} value] (peek-n stack 3)]
    (swap! !items assoc index value)
    (-> ctx
        (update :stack pop-n 3))))

(defmethod process-insn :monitorenter [{:keys [stack] :as ctx} _]
  (let [sentinel (peek stack)]
    (-> ctx
        (update :stack pop)
        (update :statements conj {:op :monitor-enter
                                  :sentinel sentinel}))))

(defmethod process-insn :monitorexit [{:keys [stack] :as ctx} _]
  (let [sentinel (peek stack)]
    (-> ctx
        (update :stack pop)
        (update :statements conj {:op :monitor-exit
                                  :sentinel sentinel}))))

(defmethod process-insn :return [{:keys [stack statements] :as ctx} _]
  (let [ret (peek stack)]
    (-> ctx
        (assoc :ast (->do (conj statements ret))))))

(defmethod process-insn ::bc/return-value [{:keys [stack statements] :as ctx} _]
  (let [ret (peek stack)]
    (-> ctx
        (assoc :stack [] :statements []
               :ast (->do (conj statements ret))))))

(defn process-if [{:keys [pc stack enclosing-end-label] :as ctx} test [start-then end-then]
                  [start-else end-else maybe-one-armed?]]
  (let [then-ctx (process-insns (assoc ctx
                                       :pc start-then
                                       :terminate? (restrict (:terminate? ctx) (pc= end-then))
                                       :statements []))

        else-start-insn (when maybe-one-armed?
                          (maybe-insn-at ctx {:label start-else}))
        protected-value-else?
        (and enclosing-end-label
             (< start-else enclosing-end-label)
             (isa? bc/insn-h
                   (keyword (:insn/name else-start-insn))
                   ::bc/load-insn))

        impure-loop-one-armed? (and (:impure-loop-entry ctx)
                                    (empty? (:loop-args ctx))
                                    maybe-one-armed?
                                    (not protected-value-else?)
                                    (:recur? then-ctx))

        one-armed? (and maybe-one-armed?
                          (or impure-loop-one-armed?
                              (not (:recur? then-ctx))))

        end-else (if (and maybe-one-armed? (not one-armed?))
                     ;; A recur branch inside an implicit function loop has no
                     ;; enclosing :loop-end-label.  Keep the bytecode-derived
                     ;; else end instead of terminating processing at nil.
                     (or (:loop-end-label ctx) end-else)
                     end-else)

        else-ctx (when-not one-armed?
                   (process-insns (assoc ctx
                                         :pc start-else
                                         :terminate? (restrict (:terminate? ctx) (pc= end-else))
                                         :statements [])))

        {then-stack :stack then-stmnts :statements then-recur? :recur?
         then-ast :ast} then-ctx
        {else-stack :stack else-stmnts :statements else-recur? :recur?
         else-ast :ast} else-ctx

        ;; A true one-armed recur must remain the value of the implicit loop.
        ;; Emitting it as a statement makes the nil subsequently supplied by
        ;; process-impure-loop follow the recur, which is both non-tail Clojure
        ;; and a semantic truncation of the loop.
        statement? (if impure-loop-one-armed?
                     false
                     (or one-armed? (= stack then-stack else-stack)))

        terminal-impure-if? (and (:impure-loop-entry ctx)
                                 maybe-one-armed?
                                 then-recur?
                                 (seq else-ast)
                                 (= end-else (:impure-loop-entry ctx)))

        branch-exprs (fn [branch-ast branch-stmnts branch-stack]
                       (if (and terminal-impure-if? (seq branch-ast))
                         [branch-ast]
                         (if statement?
                           branch-stmnts
                           (conj branch-stmnts (peek branch-stack)))))
        then (if impure-loop-one-armed?
               (conj then-stmnts (peek then-stack))
               (branch-exprs then-ast then-stmnts then-stack))
        else (branch-exprs else-ast else-stmnts else-stack)

        continuation-label (if (and enclosing-end-label
                                    end-else
                                    (< pc enclosing-end-label end-else))
                             enclosing-end-label
                             end-else)]
    (-> ctx
        (assoc :pc continuation-label)
        (update (if statement? :statements :stack)
                conj {:op :if
                      :test test
                      :then (->do then)
                      :else (if one-armed?
                              nil-expr
                              (if else (->do else) nil-expr))})
        (cond-> (or (not statement?) impure-loop-one-armed?)
          (assoc :recur? (or then-recur? else-recur?))
          one-armed? (assoc :pc start-else)))))

(defmethod process-insn :ifnull [{:keys [stack] :as ctx} insn]
  (let [null-label (goto-label insn)
        goto-end-insn (insn-at ctx {:label null-label :offset -1})

        goto-else-insn (insn-at ctx {:offset 2})
        else-label (goto-label goto-else-insn)

        {then-label :insn/label} (insn-at ctx {:offset 3})

        [test _] (peek-n stack 2)

        maybe-one-armed? (not (:insn/jump-offset goto-end-insn))
        end-label (if maybe-one-armed?
                        (reduce max 0 (keys (:jump-table ctx)))
                        (goto-label goto-end-insn))]
        (-> ctx
            (update :stack pop-n 2)
            (process-if test [then-label (:insn/label goto-end-insn)] [else-label end-label maybe-one-armed?]))))


(defmethod process-insn :ifeq [{:keys [stack] :as ctx} insn]
  (let [else-label (goto-label insn)]
    (if (and (= else-label (:insn/label (insn-at ctx {:offset 3})))
             (= ((juxt :insn/name :insn/pool-element) (insn-at ctx {:offset 3}))
                ["getstatic" #:insn{:target-class "java.lang.Boolean",
                                    :target-name "FALSE",
                                    :target-type "java.lang.Boolean"}]))
      (-> ctx
          (assoc :pc (:insn/label (insn-at ctx {:offset 4}))))
      (let [goto-end-insn (insn-at ctx {:label else-label :offset -2})
            {then-label :insn/label} (insn-at ctx {:offset 1})
            test (peek stack)
            maybe-one-armed? (not (:insn/jump-offset goto-end-insn))
            end-label (if maybe-one-armed?
                        (reduce max 0 (keys (:jump-table ctx)))
                        (goto-label goto-end-insn))]
        (-> ctx
            (update :stack pop)
            (process-if test [then-label (:insn/label goto-end-insn)] [else-label end-label maybe-one-armed?]))))))

(defmethod process-insn ::bc/aget [{:keys [stack] :as ctx} _]
  (let [[arr i] (peek-n stack 2)]
    (-> ctx
        (update :stack pop-n 2)
        (update :stack conj {:op :invoke
                             :fn {:op :var :ns "clojure.core" :name "aget"}
                             :args [arr i]}))))

(defmethod process-insn :arraylength [{:keys [stack] :as ctx} _]
  (let [arr (peek stack)]
    (-> ctx
        (update :stack pop)
        (update :stack conj {:op :invoke
                             :fn {:op :var :ns "clojure.core" :name "alength"}
                             :args [arr]}))))

(defmethod process-insn ::bc/number-compare [{:keys [stack] :as ctx} insn]
  (let [offset (if (= "if_icmpne" (:insn/name insn)) 0 1)
        insn (insn-at ctx {:offset offset})

        op (case (:insn/name insn)
             "ifle" ">"
             "ifge" "<"
             "ifne" "="
             "iflt" ">="
             "ifgt" "<="
             "if_icmpne" "=")

        else-label (goto-label insn)

        goto-end-insn (insn-at ctx {:label else-label :offset -2})

        {then-label :insn/label} (insn-at ctx {:offset (inc offset)})

        [a b] (peek-n stack 2)

        test {:op :invoke :fn {:op :var :ns "clojure.core" :name op} :args [a b]}

        maybe-one-armed? (not (:insn/jump-offset goto-end-insn))
        end-label (if maybe-one-armed?
                        (reduce max 0 (keys (:jump-table ctx)))
                        (goto-label goto-end-insn))]
        (-> ctx
            (update :stack pop-n 2)
            (process-if test [then-label (:insn/label goto-end-insn)] [else-label end-label maybe-one-armed?]))))

(defmethod process-insn :goto [{:keys [loop-args] :as ctx} {:insn/keys [jump-offset]}]
  (if-not (pos? jump-offset)
    (let [args (for [{:keys [start-label index]} loop-args
                     :let [{:keys [init]} (find-local-variable ctx index start-label)]]
                 init)]
      (-> ctx
          (assoc :recur? true)
          (update :stack conj {:op :recur
                               :args (vec args)})))
    ;; case || proto inline cache
    (-> ctx
        (update :pc + jump-offset))))

(defn skip-locals-clearing-lv [ctx]
  (if (and (= "aconst_null" (:insn/name (insn-at ctx {:offset 1})))
           (isa? bc/insn-h (-> (insn-at ctx {:offset 2}) :insn/name keyword) ::bc/store-insn)
           (= (-> (curr-insn ctx) :insn/local-variable-element :insn/target-index)
              (-> (insn-at ctx {:offset 2}) :insn/local-variable-element :insn/target-index)))
    (-> ctx
        (assoc :pc (:insn/label (insn-at ctx {:offset 3}))))
    ctx))

(defmethod process-insn ::bc/load-insn [{:keys [closed-overs closed-over-values] :as ctx} {:insn/keys [local-variable-element label]}]
  (let [{:insn/keys [target-index]} local-variable-element]
    (if-let [local (find-local-variable ctx target-index label)]
      (-> ctx
          (update :stack conj local)
          (skip-locals-clearing-lv))
      (if (contains? closed-over-values target-index)
        (-> ctx
            ;; Keep the exact caller expression for compiler-generated
            ;; captures whose field name does not match the caller local.
            (update :stack conj {:op :closed-over
                                 :target target-index
                                 :val (get closed-over-values target-index)})
            (skip-locals-clearing-lv))
        (if (contains? closed-overs target-index)
          (-> ctx
              (update :stack conj {:op :closed-over
                                   :target target-index})
              (skip-locals-clearing-lv))
          (throw (Exception. ":(")))))))

(defn find-recur-jump-label [{:keys [jump-table pc insns] :as ctx} {:keys [start-label end-label index]}]
  (loop [[{:insn/keys [name length label local-variable-element] :as insn} & insns] (drop (inc (get jump-table pc)) insns)]

    (cond

      (or (nil? insn)
          (> label end-label))
      false

      (and (isa? bc/insn-h (keyword name) ::bc/store-insn)
           (= (:insn/target-index local-variable-element) index)
           (= (:start-label (find-local-variable ctx index label)) start-label)
           (= "goto" (:insn/name (first insns)))
           (neg? (:insn/jump-offset (first insns)))
           (< (goto-label (first insns)) end-label))
      (+ label length)

      :else
      (recur insns))))

(defn find-loop-info [{:keys [local-variable-table] :as ctx} {:keys [start-label end-label] :as insn}]
  (when-let [jump-label (find-recur-jump-label ctx insn)]
    (let [insn (insn-at ctx {:label jump-label})
          loop-label (goto-label insn)]
      {:loop-label loop-label
       :loop-args (->> (for [local-variable local-variable-table
                             :when (and (= (:end-label local-variable) end-label)
                                        (>= loop-label (:start-label local-variable) start-label))]
                         local-variable)
                       (sort-by :start-label)
                       (into []))})))

(defn process-loop [ctx {:keys [loop-label loop-args]} {:keys [end-label] :as local-variable} init]
  (let [{:insn/keys [length]} (curr-insn ctx)]
    (loop [[arg & loop-args] (rest loop-args)
           args-ctx (-> ctx (update :pc + length))
           args [{:op :local-variable :local-variable local-variable :init init}]]
      (if arg
        (let [pre-insn (insn-at ctx {:label (:start-label arg) :offset -1}) ;; astore
              {:keys [statements stack] :as new-ctx} (process-insns (-> args-ctx
                                                                        (assoc :terminate? (restrict (:terminate? ctx) (pc= (:insn/label pre-insn))))
                                                                        (assoc :statements [])))

              local-variable (find-init-local new-ctx (:start-label arg))
              init (if (seq statements) (->do (conj statements (peek stack))) (peek stack))]
          (recur loop-args
                 (-> new-ctx
                     (update :pc + (:insn/length pre-insn))
                     (update :local-variable-table disj local-variable)
                     (update :local-variable-table conj (assoc local-variable :init init)))
                 (conj args {:op :local-variable :local-variable local-variable :init init})))

        (let [{body-stack :stack body-stmnts :statements} (process-insns (-> ctx
                                                                             (assoc :local-variable (:local-variable args-ctx))
                                                                             (assoc :pc loop-label)
                                                                             (assoc :loop-args (mapv :local-variable args))
                                                                             (assoc :loop-end-label end-label)
                                                                             (assoc :terminate? (restrict (:terminate? ctx) (pc= end-label)))
                                                                             (assoc :statements [])))

              statement? (not (will-ret? ctx end-label))
              body (->do (conj body-stmnts (peek body-stack)))]
          (-> ctx
              (assoc :pc end-label)
              (update (if statement? :statements :stack)
                      conj {:op :loop
                            :local-variables args
                            :body body})))))))

(defn process-let [{:keys [stack] :as ctx} {:keys [end-label] :as local-variable} init]
  (let [{:insn/keys [length]} (curr-insn ctx)
        body-ctx (process-insns (-> ctx
                                    (update :pc + length)
                                    ;; A lexical binding is a structured
                                    ;; control-flow region just like a try
                                    ;; body.  Preserve its nearest end while
                                    ;; reconstructing nested branches: some
                                    ;; Clojure compilers replace an unreachable
                                    ;; branch-to-end GOTO with NOP/NOP/ATHROW,
                                    ;; so the branch alone no longer exposes
                                    ;; the lexical continuation.
                                    (assoc :enclosing-end-label
                                           (if-let [outer-end
                                                    (:enclosing-end-label ctx)]
                                             (min outer-end end-label)
                                             end-label))
                                    (assoc :terminate? (restrict (:terminate? ctx) (pc= end-label)))
                                    (assoc :statements [])))
        {body-stack :stack body-stmnts :statements :keys [recur?]} body-ctx
        statement? (= stack body-stack)
        body (->do (if statement? body-stmnts (conj body-stmnts (peek body-stack))))]
    (-> ctx
        (assoc :pc end-label)
        (update (if statement? :statements :stack)
                conj {:op :let
                      :local-variables [{:op :local-variable
                                          :local-variable local-variable
                                          :init init}]
                      :body body})
        (cond-> (not statement?)
          (assoc :recur? recur?)))))

(defn process-letfn [{:keys [local-variable-table pc bc-for lenient?] :as ctx} target-index]
  (let [{:keys [index start-label end-label]} (->> local-variable-table
                                                   (filter (comp (partial < pc) :start-label))
                                                   (sort-by :index)
                                                   first)]

    (if (= target-index index)
      (let [local-variables (->> local-variable-table
                                 (filter (comp #{start-label} :start-label))
                                 (sort-by :index))
            letfn-fns (loop [pc pc fns []]
                        (let [insn (curr-insn (assoc ctx :pc pc))]
                          (cond
                            (= (count fns) (count local-variables))
                            fns

                            (= "new" (:insn/name insn))
                            (recur (+ pc (:insn/length insn))
                                   (conj fns (-> insn :insn/pool-element :insn/target-value)))

                            :else
                            (recur (+ pc (:insn/length insn)) fns))))

            init-local-variables (map (fn [lv fn]
                                        (let [init (bc->ast (bc-for fn)
                                                            {:bc-for bc-for
                                                             :lenient? lenient?
                                                             :fn-name (:name lv)})]
                                          (assoc lv :init init)))
                                      local-variables letfn-fns)
            {:keys [stack] :as ctx} (update ctx :stack pop)
            body-ctx (-> ctx
                         (assoc :statements [])
                         (update :local-variable-table #(apply disj % local-variables))
                         (update :local-variable-table #(apply conj % init-local-variables))
                         (assoc :pc start-label)
                         (assoc :terminate? (restrict (:terminate? ctx) (pc= end-label)))
                         (process-insns))
            {body-stack :stack body-stmnts :statements :keys [recur?]} body-ctx
            statement? (= stack body-stack)
            body (->do (if statement? body-stmnts (conj body-stmnts (peek body-stack))))]
        (-> ctx
            (assoc :pc end-label)
            (update (if statement? :statements :stack)
                    conj {:op :letfn
                          :local-variables (mapv (fn [{:keys [init] :as lv}]
                                                   {:op :local-variable
                                                    :local-variable (dissoc lv :init)
                                                    :init init})
                                                 init-local-variables)
                          :body body})
            (cond-> (not statement?)
              (assoc :recur? recur?))))
      (throw (ex-info "process-letfn target mismatch"
                      {:pc pc
                       :target-index target-index
                       :candidate {:index index
                                   :start-label start-label
                                   :end-label end-label}
                       :future-locals (->> local-variable-table
                                           (filter (comp (partial < pc) :start-label))
                                           (sort-by (juxt :start-label :index))
                                           (take 20)
                                           vec)})))))

(defn process-lexical-block [ctx local-variable init]
  (if-let [loop-info (find-loop-info ctx local-variable)]
    (process-loop ctx loop-info local-variable init)
    (process-let ctx local-variable init)))

(defmethod process-insn ::bc/pop [{:keys [stack] :as ctx} {:insn/keys [label length]}]
  (if-let [statement (peek stack)]
    (if-let [local-variable (find-init-local ctx (+ label length))]
      (-> ctx
          (update :stack pop)
          (update :local-variable-table disj local-variable)
          (update :local-variable-table conj (assoc local-variable :init statement))
          (process-lexical-block local-variable statement))
      (-> ctx
          (update :stack pop)
          (update :statements conj statement)))
    ctx))

(defmethod process-insn ::bc/store-insn [{:keys [stack] :as ctx} {:insn/keys [local-variable-element label length] :as insn}]
  (let [{:insn/keys [target-index]} local-variable-element]
    (if-let [local-variable (find-local-variable ctx target-index (+ label length))]
      (let [init (peek stack)
            initialized-local-variable (assoc local-variable :init init)
            ctx (-> ctx
                    (update :stack pop)
                    (update :local-variable-table disj local-variable)
                    (update :local-variable-table conj initialized-local-variable))]
        (if (init-local-variable? insn local-variable)
          (process-lexical-block ctx local-variable init)
          ctx))
      (if (find-no-op-local-init ctx target-index (+ label length))
        (let [init (peek stack)]
          (-> ctx
              (update :stack pop)
              (update :statements conj init)))
        (process-letfn ctx target-index)))))

(defn parse-collision-expr [exprs {:keys [test then else]}]
  (let [node [(-> test :args first) then]
        exprs (conj exprs node)]
    (if (= :if (-> else :ret :op))
      (recur exprs (:ret else))
      exprs)))

(defmethod process-insn ::bc/select [{:keys [stack] :as ctx} {:insn/keys [jump-targets label] :as insn}]
  (let [{:insn/keys [jump-offsets default-offset jump-matches]} jump-targets

        test (peek stack)

        shift+mask? (= :invoke (:op test))
        ?shift (when shift+mask?
                 (-> test :args first :args second :val))
        ?mask (when shift+mask?
                (-> test :args second :val))
        test (cond-> test shift+mask? (-> :args first :args first))

        hash-test? (= :invoke-static (:op test))
        test (cond-> test hash-test? (-> :args first))

        jump-labels (mapv (partial + label) jump-offsets)

        default-label (+ default-offset label)


        label-match (->> (for [i (range (count jump-labels))
                               :let [label (nth jump-labels i)
                                     match (nth jump-matches i)]
                               :when (not= label default-label)]
                           [label match])
                         (into []))

        ;; WIP: extract & refactor
        exprs (->> (for [i (range (count label-match))
                         :let [[label match] (nth label-match i)
                               [next-label] (nth (conj label-match [default-label nil]) (inc i))
                               end-label (:insn/label (insn-at ctx {:label next-label :offset -1}))]]

                     (if hash-test?
                       (if (= "getstatic" (:insn/name (insn-at ctx {:label label})))
                         (let [test-ctx (-> ctx
                                            (assoc :pc label
                                                   :statements []
                                                   :terminate? (restrict (:terminate? ctx) (pc= end-label)))
                                            (process-insns))
                               test (expr+statements test-ctx)
                               exprs (-> test :ret :body :ret :body :ret)]
                           [:collision match (parse-collision-expr [] exprs) test (:recur? test-ctx)])

                         (let [{:keys [stack] :as test-ctx} (-> ctx
                                                                (assoc :pc label
                                                                       :statements []
                                                                       :terminate? (restrict (:terminate? ctx)
                                                                                             (fn [ctx]
                                                                                               (#{"if_acmpne" "invokestatic"}
                                                                                                (:insn/name (curr-insn ctx))))))
                                                                (process-insns))
                               test (peek stack)
                               hash-identity? (= "if_acmpne" (:insn/name (curr-insn test-ctx)))
                               start-expr-label (if hash-identity?
                                                  (:insn/label (insn-at test-ctx {:offset 1}))
                                                  (:insn/label (insn-at test-ctx {:offset 2})))
                               expr-ctx (-> ctx
                                            (assoc :pc start-expr-label
                                                   :statements []
                                                   :terminate? (restrict (:terminate? ctx) (pc= end-label)))
                                            (process-insns))]
                           [(if hash-identity? :hash-identity :hash-equiv) match test (expr+statements expr-ctx) (:recur? expr-ctx)]))


                       (if (or (= "invokevirtual" (:insn/name (insn-at ctx {:offset -1})))
                               (and (= "invokevirtual" (:insn/name (maybe-insn-at ctx {:offset -5})))
                                    (= "iand" (:insn/name (insn-at ctx {:offset -1})))))

                         (let [{:keys [stack] :as test-ctx} (-> ctx
                                                                (assoc :pc label
                                                                       :statements []
                                                                       :terminate? (restrict (:terminate? ctx)
                                                                                             (fn [ctx]
                                                                                               (= "invokestatic" (:insn/name (curr-insn ctx))))))
                                                                (process-insns))
                               test (peek stack)

                               expr-ctx (-> ctx
                                            (assoc :pc (:insn/label (insn-at test-ctx {:offset 2}))
                                                   :statements []
                                                   :terminate? (restrict (:terminate? ctx) (pc= end-label)))
                                            (process-insns))]
                           [:int match test (expr+statements expr-ctx) (:recur? expr-ctx)])

                         (let [{:keys [stack] :as test-ctx} (-> ctx
                                                                (assoc :pc label
                                                                       :statements []
                                                                       :terminate? (-> (:terminate? ctx)
                                                                                       (restrict (pc= end-label))
                                                                                       (restrict (fn [ctx]
                                                                                                   (and (= "lcmp" (:insn/name (curr-insn ctx)))
                                                                                                        (= default-label (goto-label (insn-at ctx {:offset 1}))))))))
                                                                (process-insns))]

                           (if (= "lcmp" (:insn/name (curr-insn test-ctx)))
                             (let [[test _] (peek-n stack 2)
                                   expr-ctx (-> ctx
                                                (assoc :pc (:insn/label (insn-at test-ctx {:offset 2}))
                                                       :statements []
                                                       :terminate? (restrict (:terminate? ctx) (pc= end-label)))
                                                (process-insns))]
                               [:int match test (expr+statements expr-ctx) (:recur? expr-ctx)])

                             ;; A tiny switch arm may contain only side effects
                             ;; followed by recur. Preserve those statements;
                             ;; the stack alone is not the arm's full value.
                             [:int match {:op :const :val match}
                              (expr+statements test-ctx)
                              (:recur? test-ctx)])))))

                   (into []))

        end-label (-> (insn-at ctx {:label default-label :offset -1}) (goto-label))

        default-expr (-> ctx
                         (assoc :pc default-label
                                :statements []
                                :terminate? (restrict (:terminate? ctx) (pc= end-label)))
                         (process-insns)
                         (expr+statements))
        expr {:op :case
              :test test
              :shift (or ?shift 0)
              :mask (or ?mask 0)
              :default default-expr
              :type (if (= "lookuptable" (:insn/name insn)) :sparse :compact)
              :switch-type (if hash-test? (if (every? (comp #{:hash-identity} first) exprs) :hash-identity :hash-equiv) :int)
              :skip-check (when hash-test?
                            (->> (for [i (range (count exprs))
                                       :let [[type] (nth exprs i)]
                                       :when (= :collision type)]
                                   i)
                                 (into #{})))
              :exprs exprs}
        recur? (boolean (seq (for [[_ _ _ _ recur?] exprs
                                   :when recur?]
                               true)))]

    (-> ctx
        (assoc :recur? recur?)
        (update :stack pop)
        (update :stack conj expr)
        (assoc :pc end-label))))

(defmethod process-insn :instanceof [{:keys [stack] :as ctx} {:insn/keys [pool-element]}]

  (if (or (isa? bc/insn-h (-> (maybe-insn-at ctx {:offset 5}) :insn/name keyword) ::bc/select)
          (and (isa? bc/insn-h (-> (maybe-insn-at ctx {:offset 9}) :insn/name keyword) ::bc/select)
               (= "ishr" (-> (insn-at ctx {:offset 6}) :insn/name))))

    (-> ctx
        (assoc :pc (:insn/label (insn-at ctx {:offset 5}))))


    (let [{:insn/keys [target-type]} pool-element
          instance (peek stack)]
      (-> ctx
          (update :stack pop)
          (update :stack conj {:op :invoke
                               :fn {:op :var
                                    :ns "clojure.core"
                                    :name "instance?"}
                               :args [{:op :const
                                       :val (symbol target-type)}
                                      instance]})))))

;; WIP new on :new rather than invokespecial
(defmethod process-insn :new [ctx _]
  ctx)

(defn captured-field-values [{:class/keys [fields]} args]
  ;; Classfile field order follows Clojure's capture constructor order for fn
  ;; and reify classes. Seed direct field lookups as well as tracking JVM slots:
  ;; named constructor locals otherwise hide the slot's caller expression.
  (let [field-names (->> fields
                         (remove (comp :static :field/flags))
                         (map :field/name)
                         vec)]
    (when (= (count field-names) (count args))
      (zipmap field-names
              (map (fn [field-name arg]
                     (assoc arg :captured-field field-name))
                   field-names
                   args)))))

(defn constructor-arg-values
  "Map JVM constructor local-variable slots to the expressions supplied by
  the caller. Longs and doubles occupy two slots; all other values occupy one."
  [arg-types args]
  (second
    (reduce (fn [[index values] [arg-type arg]]
              [(+ index ({"long" 2 "double" 2} arg-type 1))
               (assoc values index arg)])
            [1 {}]
            (map vector arg-types args))))

(defmethod process-insn :invokespecial [{:keys [stack bc-for lenient?] :as ctx} {:insn/keys [pool-element]}]
  (let [{:insn/keys [target-class target-arg-types]} pool-element
        argc (count target-arg-types)
        args (peek-n stack argc)]
    (-> ctx
        (update :stack pop-n (inc argc))
        (update :stack conj (let [bc (bc-for target-class)]
                              (if (or (#{"clojure.lang.AFunction" "clojure.lang.RestFn" } (:class/super bc))
                                      (and (some #{"clojure.lang.IObj"} (:class/interfaces bc))
                                           (.contains ^String target-class "$reify__")))
                                (bc->ast bc {:bc-for bc-for
                                            :lenient? lenient?
                                            :fields (captured-field-values bc args)
                                            :closed-over-values (constructor-arg-values
                                                                  target-arg-types
                                                                  args)})
                                {:op :new
                                 :class target-class
                                 :arg-types target-arg-types
                                 :args args
                                 ;; Clojure's proxy class name fully qualifies
                                 ;; only the first base.  Preserve the exact
                                 ;; superclass/interface list from the generated
                                 ;; proxy bytecode so source reconstruction does
                                 ;; not have to guess packages for later bases.
                                 :proxy-bases
                                 (when (and bc
                                            (.contains ^String target-class ".proxy$"))
                                   (mapv identity
                                         (cons (:class/super bc)
                                               (remove #{"clojure.lang.IProxy"}
                                                       (:class/interfaces bc)))))}))))))

(defmethod process-insn :athrow [{:keys [stack statements reachable pc] :as ctx} _]
  (if-not (contains? reachable pc)
    ctx
    (let [ex (peek stack)
          throw-expr {:op :throw :ex ex}]
      (-> ctx
          (update :stack pop)
          (update :statements conj throw-expr)
          ;; A method may terminate solely with ATHROW and never visit one of
          ;; the JVM return handlers that normally finalizes :ast.  Finalize
          ;; the top-level throw here so a prior method's AST cannot leak into
          ;; this method when decompiling generated function classes.
          (assoc :ast (->do (conj statements throw-expr)))))))

(def generated-class-marker-interfaces
  #{"clojure.lang.IMeta"
    "clojure.lang.IObj"
    "clojure.lang.IProxy"
    "clojure.lang.IRecord"
    "clojure.lang.IType"})

(defn class-declares-method?
  [bc-for class-name method-name]
  (or (some #(= method-name (:method/name %))
            (:class/methods (bc-for class-name)))
      (try
        (let [loader (.getContextClassLoader (Thread/currentThread))
              klass (Class/forName class-name false loader)]
          (boolean (some #(= method-name (.getName ^java.lang.reflect.Method %))
                         (.getMethods klass))))
        (catch Throwable _
          false))))

(defn stable-generated-target
  "Replace an AOT-generated concrete reify/proxy cast with a source-stable
  superclass or interface hint. A newly compiled reify gets a different binary
  name, so retaining the original generated class in source cannot compile."
  [bc-for target target-class method-name]
  (if (and (= :local (:op target))
           (= target-class (:cast target))
           (or (s/includes? target-class "$reify")
               (s/includes? target-class ".proxy$")))
    (let [class-bc (bc-for target-class)
          interfaces (remove generated-class-marker-interfaces
                             (:class/interfaces class-bc))
          superclass (:class/super class-bc)
          stable-class (or (some #(when (class-declares-method?
                                          bc-for % method-name)
                                   %)
                                 interfaces)
                           (when (and superclass
                                      (not= "java.lang.Object" superclass)
                                      (class-declares-method?
                                        bc-for superclass method-name))
                             superclass)
                           (first interfaces)
                           (when-not (= "java.lang.Object" superclass)
                             superclass))]
      (cond-> target stable-class (assoc :cast stable-class)))
    target))

(defmethod process-insn ::bc/invoke-instance-method [{:keys [stack bc-for ^String class-name lenient?] :as ctx} {:insn/keys [pool-element]}]
  (let [{:insn/keys [target-class target-name target-ret-type target-arg-types]} pool-element
        argc (count (conj target-arg-types target-class))
        [target & args] (peek-n stack argc)
        target (stable-generated-target bc-for target target-class target-name)
        ?deftype-ast (when (and (= "importClass" target-name)
                                (= "clojure.lang.Namespace" target-class))
                       (let [^String cname (-> args first :args first :val)]
                         (when-let [bc (and (= (subs cname 0 (.lastIndexOf cname "."))
                                               (let [i (.indexOf class-name "$")]
                                                 (-> class-name
                                                     (s/replace "__init" "")
                                                     (cond-> (not= i -1)
                                                       (subs 0 i)))))
                                            (bc-for cname))]
                           (when (some #{"clojure.lang.IType" "clojure.lang.IRecord"} (:class/interfaces bc))
                             (bc->ast bc {:bc-for bc-for :lenient? lenient?})))))]
    (-> ctx
        (update :stack pop-n argc)
        (update (if (= "void" target-ret-type) :statements :stack)
                conj {:op :invoke-instance
                      :method target-name
                      :target target
                      :arg-types target-arg-types
                      :target-class target-class
                      :args args})
        (cond-> ?deftype-ast
          (update :statements conj ?deftype-ast)))))

(defmethod process-insn :putstatic [{:keys [stack class-name] :as ctx} {:insn/keys [pool-element]}]
  (let [{:insn/keys [target-class target-name]} pool-element
        val (peek stack)
        ctx (update ctx :stack pop)]
    (if (= class-name target-class)
      (-> ctx
          (update :fields assoc target-name val))
      (-> ctx
          (update :statements conj {:op :set!
                                    :target {:op :static-field
                                             :target target-class
                                             :field target-name}
                                    :val val})))))

(defn keyword-lookup-fn [fields target-name]
  ;; KeywordLookupSite constructors retain the complete RT.keyword expression.
  ;; Keeping it is essential for namespaced keywords; selecting only the name
  ;; argument silently turns (for example) :db/index into :index.
  (get-in fields [target-name :args 0]))

(defn process-keyword-invoke [{:keys [fields] :as ctx} {:insn/keys [pool-element]}]
  (let [{:insn/keys [target-name]} pool-element
        {:keys [pc statements stack]} (process-insns (assoc ctx
                                                            :pc (:insn/label (insn-at ctx {:offset 2}))
                                                            :terminate? (restrict (:terminate? ctx)
                                                                                  (fn [ctx]
                                                                                    (->> (curr-insn ctx)
                                                                                         :insn/name
                                                                                         (= "dup_x2"))))
                                                            :statements []))
        target (->do (conj statements (peek stack)))]
    (-> ctx
        (assoc :pc (+ pc 36)) ;; why bother writing robust code when we can just hardcode bytecode offsets
        (update :stack conj {:op :invoke
                             :fn (keyword-lookup-fn fields target-name)
                             :args [target]}))))

(defmethod process-insn :getstatic [{:keys [fields class-name] :as ctx} {:insn/keys [pool-element] :as insn}]
  (let [{:insn/keys [target-class target-name target-type]} pool-element]
    (cond

      (and (= target-type "clojure.lang.ILookupThunk")
           (= target-class class-name))
      (process-keyword-invoke ctx insn)

      (= target-class class-name)
      (update ctx :stack conj (get fields target-name))

      :else
      (update ctx :stack conj {:op :static-field
                               :target target-class
                               :field target-name}))))

(defn current-receiver?
  [instance]
  (and (= :local (:op instance))
       (:this? instance)
       (not (:captured-field instance))))

(defn field-instance
  "Retain an explicit receiver for non-`this` field access. When it is another
  instance of the deftype currently being emitted, use the source-level simple
  type name: the fully-qualified generated class does not exist until the
  enclosing deftype form has finished compiling."
  [instance target-class class-name]
  (cond-> instance
    (= target-class class-name)
    (assoc :cast (last (s/split class-name #"\.")))))

(defmethod process-insn :putfield [{:keys [class-name stack] :as ctx} {:insn/keys [pool-element]}]
  (let [{:insn/keys [target-class target-name target-type]} pool-element
        [instance val] (peek-n stack 2)]
    (-> ctx
        (update :stack pop-n 2)
        (cond-> (and (= target-class class-name)
                     (current-receiver? instance)
                     (= :closed-over (:op val))
                     (contains? val :val))
          (update :fields assoc target-name (:val val)))
        (cond-> (not= :closed-over (:op val))
          (update :statements conj {:op :set!
                                    :target (if (and (= target-class class-name)
                                                     (current-receiver? instance))
                                              {:op :local
                                               :name target-name}
                                              {:op :instance-field
                                               :instance (field-instance instance
                                                                         target-class
                                                                         class-name)
                                               :field target-name})
                                    :target-type target-type
                                    :val val})))))

(defn skip-locals-clearing-field [ctx]
  ;; WIP must make sure it's not a mutable deftype field
  (if (and (= "aload_0" (:insn/name (insn-at ctx {:offset 1})))
           (= "aconst_null" (:insn/name (insn-at ctx {:offset 2})))
           (= "putfield" (:insn/name (insn-at ctx {:offset 3})))
           (= (-> (curr-insn ctx) :insn/pool-element :insn/target-name)
              (-> (insn-at ctx {:offset 3}) :insn/pool-element :insn/target-name)))
    (-> ctx
        (assoc :pc (:insn/label (insn-at ctx {:offset 4}))))
    ctx))

(defmethod process-insn :getfield [{:keys [fields class-name stack] :as ctx} {:insn/keys [pool-element]}]
  (let [{:insn/keys [target-class target-name]} pool-element
        instance (peek stack)
        ctx (update ctx :stack pop)]
    (if (and (= target-class class-name)
             (current-receiver? instance))
      (-> ctx
          (update :stack conj (get fields target-name {:op :local :name (bc/fixup-name target-name)}))
          (skip-locals-clearing-field))
      (update ctx :stack conj {:op :instance-field
                               :instance (field-instance instance
                                                         target-class
                                                         class-name)
                               :field target-name}))))

(defmethod process-insn :invokestatic [{:keys [stack] :as ctx} {:insn/keys [pool-element]}]
  (let [{:insn/keys [target-class target-name target-ret-type target-arg-types]} pool-element
        argc (count target-arg-types)
        args (peek-n stack argc)]
    (-> ctx
        (update :stack pop-n argc)
        (update (if (= "void" target-ret-type) :statements :stack)
                conj {:op :invoke-static
                      :target target-class
                      :method target-name
                      :arg-types target-arg-types
                      :args args}))))

(defmethod process-insn ::bc/math-insn [{:keys [stack] :as ctx} {:insn/keys [name]}]
  (let [argc (if (#{"dneg" "lneg"} name) 1 2)
        args (peek-n stack argc)
        op  ({"dadd" "+"
              "ddiv" "/"
              "dmul" "*"
              "dneg" "-"
              "dsub" "-"
              "iadd" "+"
              "iand" "bit-and"
              "idiv" "/"
              "imul" "*"
              "irem" "rem"
              "ineg" "-"
              "ishl" "bit-shift-left"
              "ishr" "bit-shift-right"
              "isub" "-"
              "iushr" "unsigned-bit-shift-right"
              "ladd" "+"
              "land" "bit-and"
              "ldiv" "quot"
              "lneg" "-"
              "lmul" "*"
              "lor" "bit-or"
              "lrem" "rem"
              "lshl" "bit-shift-left"
              "lshr" "bit-shift-right"
              "lsub" "-"
              "lushr" "unsigned-bit-shift-right"
              "lxor" "bit-xor"} name)]
    (-> ctx
        (update :stack pop-n argc)
        (update :stack conj {:op :invoke
                             :fn {:op :var
                                  :ns "clojure.core"
                                  :name op}
                             :args args}))))

(defmethod process-insn :checkcast [{:keys [stack] :as ctx} {:insn/keys [pool-element]}]
  (let [{:insn/keys [target-type]} pool-element
        target (peek stack)]

    (cond-> ctx

      target
      (-> (update :stack pop)
          (update :stack conj (assoc target :cast target-type))))))

;; protocol inline caches
(defmethod process-insn :if_acmpeq [{:keys [stack] :as ctx} _]
  (-> ctx
      (update :stack pop-n 2)
      (update :pc + 17)))

(defn merge-tables [ctx local-variable-table exception-table]
  (let [lvt (->> (for [{:local-variable/keys [name index start-label end-label]} local-variable-table]
                   {:op :local
                    :start-label start-label
                    :end-label end-label
                    :index index
                    :name name})
                 (into #{}))
        et (->> (for [{:exception-handler/keys [type start-label end-label handler-label]} exception-table]
                  {:start-label start-label
                   :end-label end-label
                   :handler-label handler-label
                   :type type})
                (into #{}))]
    (-> ctx
        (assoc :local-variable-table lvt)
        (assoc :exception-table et)
        (assoc :loop-args (->> lvt
                               (filter (comp zero? :start-label))
                               (sort-by :index)
                               (vec))))))

(defn collect-impure-loops-data [{:keys [insns] :as ctx}]
  (loop [[insn & insns] insns data #{}]
    (if insn
      (if (and (= "goto" (:insn/name insn))
               (not (pos? (:insn/jump-offset insn)))
               (not (isa? bc/insn-h (-> ctx
                                        (assoc :pc (:insn/label insn))
                                        (maybe-insn-at {:offset -1})
                                        :insn/name
                                        keyword)
                          ::bc/store-insn)))
        (recur insns (conj data (goto-label insn)))
        (recur insns data))
      (assoc ctx :impure-loops data))))

(defn reachable-return?
  [{:keys [insns reachable]}]
  (boolean
    (some (fn [{:insn/keys [name label]}]
            (and (contains? reachable label)
                 (or (= "return" name)
                     (isa? bc/insn-h (keyword name) ::bc/return-value))))
          insns)))

(defn finalize-nonreturning-method
  [{:keys [ast statements stack] :as ctx}]
  ;; Newer Clojure AOT output can end a method with ATHROW (or a loop) and put
  ;; only another unreachable ATHROW after it.  Such a method never visits a
  ;; JVM return handler, so preserve the body accumulated by process-insns.
  (if (and (empty? ast)
           (not (reachable-return? ctx))
           (or (seq statements) (seq stack)))
    (assoc ctx :ast (expr+statements ctx))
    ctx))

(defn process-method-insns [{:keys [fn-name] :as ctx} {:method/keys [bytecode jump-table local-variable-table flags exception-table]}]
  (-> ctx
      (merge initial-local-ctx {:jump-table jump-table})
      (merge-tables local-variable-table exception-table)
      (cond-> (not (:static flags))
        (-> (update :local-variable-table disj {:op :local
                                                :start-label 0
                                                :end-label (-> bytecode peek :insn/label)
                                                :index 0
                                                :name "this"})
            (update :local-variable-table conj {:op :local
                                                :this? true
                                                :index 0
                                                :name (or fn-name "this")
                                                :start-label 0
                                                :end-label (-> bytecode peek :insn/label)})
            (update :loop-args #(vec (rest %)))))
      (assoc :insns bytecode)
      (collect-impure-loops-data)
      (collect-reachable)
      (process-insns)
      (finalize-nonreturning-method)))

(defn process-static-init [{:keys [bc-for] :as ctx} {:class/keys [methods]}]
  (let [method (u/find-method methods {:method/name "<clinit>"})]
    (-> ctx
        (process-method-insns method))))

(defn process-init [{:keys [bc-for] :as ctx} {:class/keys [methods]}]
  (let [method (u/find-method methods {:method/name "<init>"})
        {:method/keys [arg-types]} method]
    (-> ctx
        (assoc :closed-overs (second (reduce (fn [[i c] a] [(+ i a) (conj c i)]) [1 #{0}]
                                             (map #({"long" 2 "double" 2} % 1)
                                                  arg-types))))
        (process-method-insns method))))

(defn decompile-fn-method [{:keys [fn-name] :as ctx} {:method/keys [local-variable-table flags name return-type] :as method}]
  (let [{:keys [ast]} (process-method-insns ctx method)
        args (for [{:local-variable/keys [index name type start-label]} (->> local-variable-table
                                                                             (sort-by :local-variable/index))
                   :when (and (zero? start-label)
                              (or (:static flags)
                                  (not (zero? index))))]
               {:name name
                :type type})]

    {:op :fn-method
     :fn-name fn-name
     :return-type return-type
     :var-args? (or (= "doInvoke" name)
                    (= "clojure.lang.ISeq" (-> args last :type)))
     :args args
     :body ast}))

(defn decompile-fn-methods [{:keys [fn-name] :as ctx} {:class/keys [methods]}]
  (let [invokes-static (u/find-methods methods {:method/name "invokeStatic"})
        invokes-prim (u/find-methods methods {:method/name "invokePrim"})
        invokes (u/find-methods methods {:method/name "invoke"})
        invoke-vararg (u/find-method methods {:method/name "doInvoke"})
        invoke-methods (-> invokes-static
                           (into (for [{:method/keys [arg-types] :as invoke} invokes-prim
                                       :let [argc (count arg-types)]
                                       :when (empty? (filter (fn [{:method/keys [arg-types]}]
                                                               (= (count arg-types) argc))
                                                             invokes-static))]
                                   invoke)))

        invoke-methods (-> invoke-methods
                           (into (for [{:method/keys [arg-types] :as invoke} (into invokes (when invoke-vararg
                                                                                             [invoke-vararg]))
                                       :let [argc (count arg-types)]
                                       :when (empty? (filter (fn [{:method/keys [arg-types]}]
                                                               (= (count arg-types) argc))
                                                             invoke-methods))]
                                   invoke)))
        methods-asts (mapv (partial decompile-fn-method ctx) invoke-methods)]
    {:op :fn
     :name fn-name
     :fn-methods methods-asts}))

(defn extract-fn-name [^String cname]
  (let [fname (subs cname (inc (.lastIndexOf cname "$")))
        pretty-fname (second (re-matches #"(.+)__[0-9]+$" fname))]
    (if (and pretty-fname
             (not= pretty-fname "fn"))
      pretty-fname
      fname)))

(def invocation-method-names
  #{"invoke" "invokeStatic" "invokePrim" "doInvoke"})

(defn class-field-instruction?
  [class-name field-name instruction-name instruction]
  (let [{:insn/keys [target-class target-name]}
        (:insn/pool-element instruction)]
    (and (= instruction-name (:insn/name instruction))
         (= class-name target-class)
         (= field-name target-name))))

(defn derived-fn-name-is-used-capture?
  "True only when a class-derived fn name is also a proved captured field:
  the class declares the instance field, its constructor writes that exact
  field, and an invocation body reads it. In that bytecode graph, emitting the
  derived name would shadow the closed-over value rather than recover an
  authored recursive fn name."
  [{class-name :class/name
    fields :class/fields
    methods :class/methods}
   derived-name]
  (let [instance-field?
        (some #(and (= derived-name (:field/name %))
                    (not (contains? (:field/flags %) :static)))
              fields)
        constructor-write?
        (some (fn [method]
                (and (= "<init>" (:method/name method))
                     (some #(class-field-instruction?
                              class-name derived-name "putfield" %)
                           (:method/bytecode method))))
              methods)
        invocation-read?
        (some (fn [method]
                (and (contains? invocation-method-names (:method/name method))
                     (some #(class-field-instruction?
                              class-name derived-name "getfield" %)
                           (:method/bytecode method))))
              methods)]
    (boolean (and instance-field? constructor-write? invocation-read?))))

(defn recover-fn-name [bc explicit-name]
  (if explicit-name
    explicit-name
    (let [derived-name (extract-fn-name (:class/name bc))]
      (when-not (derived-fn-name-is-used-capture? bc derived-name)
        derived-name))))

(defn decompile-fn [{class-name :class/name :as bc} {:keys [fn-name] :as ctx}]
  (-> ctx
      (assoc :fn-name (recover-fn-name bc fn-name))
      (assoc :class-name class-name)
      (process-static-init bc)
      (process-init bc)
      (decompile-fn-methods bc)))


(defn process-ns-inits [ctx {:class/keys [methods]}]
  (reduce (fn [ctx i]
            (if-let [method (u/find-method methods {:method/name (str "__init" i)})]
              (process-method-insns ctx method)
              (reduced ctx)))
          ctx (range)))

(defn process-ns-load [ctx {:class/keys [methods]}]
  (let [{:method/keys [bytecode jump-table]} (u/find-method methods {:method/name "load"})
        ctx (-> ctx
                (assoc
                 :terminate? (restrict (:terminate? ctx) (comp seq :statements))
                 :insns bytecode)
                (merge initial-local-ctx {:jump-table jump-table}))
        indicize (fn [s i]
                   (reduce (fn [[s i] insn]
                             (if (::idx insn)
                               [(conj s insn) i]
                               [(conj s (assoc insn ::idx i)) (inc i)]))
                           [[] i] s))]
    (loop [{:keys [stack statements] :as ctx} (process-insns ctx)
           init []
           i 0]
      (let [[stack i] (indicize stack i)
            [statements i] (indicize statements i)]
        (if (and (seq statements))
          (recur (process-insns (assoc ctx :statements [] :stack stack))
                 (into init statements)
                 i)
          (->do (sort-by ::idx (concat init stack statements))))))))

(defn decompile-ns [{class-name :class/name :as bc} {:keys [fn-name] :as ctx}]
  (-> ctx
      (assoc :class-name class-name)
      (process-ns-inits bc)
      (process-ns-load bc)))

(defn process-methods [ctx methods]
  (->> (for [{:method/keys [name return-type local-variable-table] :as method} methods]
         {:op :method
          :name name
          :return-type return-type
          :args (->> (for [{:local-variable/keys [name start-label type]} (->> local-variable-table
                                                                               (sort-by :local-variable/index))
                           :when (zero? start-label)]
                       {:name name
                        :type type})
                     (into []))
          :body (:ast (process-method-insns ctx method))})
       (into [])))

;; deftypes with `this` as a field break
(defn decompile-deftype [{:class/keys [fields interfaces methods ^String name] :as bc} ctx]
  (let [fields (->> (for [{:field/keys [name type flags]} fields
                          :when (not (:static flags))]
                      {:name (bc/fixup-name name)
                       :type type
                       :mutable? (cond
                                   (:volatile flags) :volatile-mutable
                                   (:final flags) false
                                   :else :unsynchronized-mutable)})
                    (into []))
        instance-methods (->> methods
                              (remove (comp :static :method/flags))
                              ;; bridge
                              (remove (comp :volatile :method/flags))
                              (remove (comp #{"<init>"} :method/name)))
        ctx (-> ctx (assoc :class-name name) (process-static-init bc))]
    {:op :deftype
     :name name
     :tname (.replaceFirst name "\\." "/")
     :fields fields
     :methods (process-methods ctx instance-methods)
     :interfaces interfaces}))

(defn disambiguate-reify-receiver
  "Alpha-rename an inner reify receiver when a captured outer local has the
  same source name. Captured field values retain their outer name; only true
  references to the generated inner receiver are renamed."
  [{:keys [args body] :as method}]
  (let [receiver-name (:name (first args))
        captured-names (atom #{})]
    (walk/postwalk (fn [node]
                     (when (and (map? node)
                                (:captured-field node)
                                (:name node))
                       (swap! captured-names conj (:name node)))
                     node)
                   body)
    (if-not (contains? @captured-names receiver-name)
      method
      (let [renamed (str receiver-name "__reify")]
        (-> method
            (assoc-in [:args 0 :name] renamed)
            (assoc-in [:args 0 :type] nil)
            (update :body
                    #(walk/postwalk
                       (fn [node]
                         (if (and (map? node)
                                  (= :local (:op node))
                                  (= receiver-name (:name node))
                                  (not (:captured-field node)))
                           (assoc node :name renamed)
                           node))
                       %)))))))

(defn decompile-reify [{:class/keys [interfaces methods name] :as bc} ctx]
  (let [instance-methods (->> methods
                              (remove (comp :static :method/flags))
                              ;; bridge
                              (remove (comp :volatile :method/flags))
                              (remove (comp #{"<init>" "meta" "withMeta"} :method/name)))
        ctx (-> ctx
                (assoc :class-name name)
                (process-static-init bc))]
    {:op :reify
     :methods (mapv disambiguate-reify-receiver
                    (process-methods ctx instance-methods))
     :interfaces (vec (remove #{"clojure.lang.IObj"} interfaces))}))

(defn bc->ast [{:class/keys [interfaces super ^String name] :as bc} ctx]
  (let [ctx (merge initial-ctx ctx)]
    (try
      (cond
        (#{"clojure.lang.AFunction" "clojure.lang.RestFn"} super)
        (decompile-fn bc ctx)

        (.endsWith name "__init")
        (decompile-ns bc ctx)

        (some #{"clojure.lang.IType" "clojure.lang.IRecord"} interfaces)
        (decompile-deftype bc ctx)

        (and (some #{"clojure.lang.IObj"} interfaces)
             (.contains name "$reify__"))
        (decompile-reify bc ctx)

        :else
        (throw (Exception. ":(")))
      (catch Exception e
        (if (:lenient? ctx)
          {:op :const :val (str "BROKEN DECOMP " name)}
          (throw e))))))

;;; genclass
;; WIP int -> booleans
