(ns imcljs.query-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [cljs.core.async :refer [<!]]
            [imcljs.path :as path]
            [imcljs.fetch :as fetch]
            [imcljs.query :as query]))

(def service {:root  "beta.flymine.org/beta"
              :model {:name "genomic"}})

(def normal-query {:from   "Gene"
                   :select ["Gene.symbol" "Gene.organism.name" "Gene.homologues.homologue.symbol"]
                   :where  [{:path  "Gene.symbol"
                             :op    "="
                             :value "mad"
                             :code "A"}]})

(deftest deconstruct-by-class
  (testing "Should be able to deconstruct a query into its classes"
    (async done
      (go
        (let [model (<! (fetch/model service))]
          (let [result (query/deconstruct-by-class model normal-query)]
            (is (= result {:Gene
                           {"Gene.homologues.homologue"
                            {:query
                             {:where   [{:path "Gene.symbol", :value "mad", :op "=", :code "A"}]
                              :from    "Gene"
                              :select  ["Gene.homologues.homologue.id"]
                              :orderBy '()}}
                            "Gene"
                            {:query
                             {:where   [{:path "Gene.symbol", :value "mad", :op "=", :code "A"}]
                              :from    "Gene"
                              :select  ["Gene.id"]
                              :orderBy '()}}}
                           :Organism
                           {"Gene.organism"
                            {:query
                             {:where   [{:path "Gene.symbol", :value "mad", :op "=", :code "A"}]
                              :from    "Gene"
                              :select  ["Gene.organism.id"]
                              :orderBy '()}}}}))
            (done)))))))


