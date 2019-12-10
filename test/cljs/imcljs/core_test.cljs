(ns imcljs.core-test
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.test :refer-macros [async deftest testing is]]
            [cljs.core.async :refer [<!]]
            [imcljs.env :refer [service]]
            [imcljs.fetch :as fetch]))

(deftest templates
  (let [request (fetch/templates service)]
    (testing "mine returns some spotchecked templates"
      (async done
        (go
          (let [{:keys [All_Proteins_In_Organism_To_Publications
                        Gene_Protein
                        Organism_Protein]} (<! request)]
            (is (every? not-empty [All_Proteins_In_Organism_To_Publications
                                   Gene_Protein
                                   Organism_Protein]))
            (done)))))))
