(ns imcljs.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [imcljs.fetch :as fetch]))

(def root "http://localhost:8080/biotestmine")

(defn run-query []
  (let [model (fetch/model {:root root})]
    (:body (fetch/rows {:root root :model model}
                       {:from "Gene"
                        :select ["id"]
                        :where [{:path "symbol" :op "=" :value "zen"}]}))))

(deftest a-test
  (testing "JVM can perform a POST request"
    (let [{:keys [results statusCode]} (run-query)]
      (and
       (is (= 200 statusCode)
           (> 0 (count results)))))))

