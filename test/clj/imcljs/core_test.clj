(ns imcljs.core-test
  (:require [clojure.test :refer :all]
            [imcljs.fetch :as fetch]))

(defn run-query []
  (let [model (fetch/model {:root "www.flymine.org/flymine"})]
    (:body (fetch/rows {:root "www.flymine.org/flymine" :model model}
                       {:from "Gene"
                        :select ["id"]
                        :where [{:path "symbol" :op "=" :value "zen"}]}))))

(deftest a-test
  (testing "JVM can perform a POST request"
    (let [{:keys [results statusCode]} (run-query)]
      (and
        (is (= 200 statusCode)
            (> 0 (count results)))))))

