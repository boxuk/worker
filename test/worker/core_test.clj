
(ns worker.core-test
  (:require [worker.core :refer :all]
            [clojure.test :refer :all]))

(defn do-work [result]
  (Thread/sleep 10)
  result)

(deftest test-result-of-function-returned
  (is (= {:foo 10} (worker :id (do-work {:foo 10}))))
  (is (= 10 (worker :id (do-work 10)))))

