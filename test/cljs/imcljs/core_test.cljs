(ns imcljs.core-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :refer-macros [async deftest testing is]]
            [cljs.core.async :as async :refer [<!]]
            [imcljs.fetch :as fetch]))

(def flymine {:root  "www.flymine.org/query"
              :model {:name "genomic"}})

(deftest templates
  (let [request (fetch/templates flymine)]
    (testing "flymine returns some spotchecked templates"
      (async done
        (go
          (let [{:keys [AnatomyTerm_Alleles Gene_GOAll Gene_Protein]} (async/<! request)]
            (is (every? some? [AnatomyTerm_Alleles Gene_GOAll Gene_Protein])))
          (done))))))

