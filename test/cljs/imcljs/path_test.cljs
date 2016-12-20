(ns imcljs.path-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [cljs.core.async :refer [<!]]
            [imcljs.path :as path]
            [imcljs.fetch :as fetch]))

(def flymine-beta {:root  "beta.flymine.org/beta"
                   :model {:name "genomic"}})

(deftest walk
  (testing "Should walk a path and return parts of the model"
    (async done
      (go
        (let [model (<! (fetch/model flymine-beta))]
          (let [walked (path/walk model "Gene.downstreamIntergenicRegion.adjacentGenes.microArrayResults.affyCall")]
            (is (= (map :name walked) '("Gene" "IntergenicRegion" "Gene" "MicroArrayResult" "affyCall")))
            (done)))))))