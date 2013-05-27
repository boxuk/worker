
(ns worker.core)

(def workers (atom {}))

;; Public
;; ------

(defmulti make-id
  "Return a vector for an ID"
  class)

(defn fetch
  "Try and fetch a future for the worker if it exists"
  [id]
  (get-in @workers (make-id id)))

(defn store
  "Store the future for the specified worker"
  [id f]
  (swap! workers assoc-in (make-id id) f))

(defn clear
  "A worker has finished, clear it from the ref"
  [id]
  (store id nil))

(defmacro worker
  "Use futures to handle waiting on tasks already being processed"
  [id & body]
  `(if-let [waiting# (fetch ~id)]
     (deref waiting#)
     (let [f# (future ~@body)]
       (store ~id f#)
       (try
         (deref f#)
         (finally
           (clear ~id))))))

;; Worker ID Impls
;; ---------------

(defmethod make-id
  java.lang.String
  [id] [id])

(defmethod make-id
  clojure.lang.PersistentVector
  [id] id)

(defmethod make-id
  clojure.lang.Keyword
  [id] [id])

