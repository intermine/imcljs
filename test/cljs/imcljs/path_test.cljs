(ns imcljs.path-test
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.test :refer-macros [async deftest testing is use-fixtures]]
            [cljs.core.async :refer [<!]]
            [imcljs.path :as path]
            [imcljs.fetch :as fetch]))

(def service {:root  "beta.flymine.org/beta"
              :model {:name "genomic"}})

(deftest walk-subclass
  (testing "Should be able to walk a path that with"
    (async done
      (go
        (let [model (<! (fetch/model service))]
          (let [walked (path/walk model "Gene.homologues.homologue")]
            (is (= (map :name walked) '("Gene" "Homologue" "Gene")))
            (done)))))))

(deftest walk-subclasses
  (testing "Should be able to walk a path with multiple subclasses and return parts of the model"
    (async done
      (go
        (let [model (<! (fetch/model service))]
          (let [walked (path/walk model "Gene.downstreamIntergenicRegion.adjacentGenes.microArrayResults.affyCall")]
            (is (= (map :name walked) '("Gene" "IntergenicRegion" "Gene" "MicroArrayResult" "affyCall")))
            (done)))))))

(deftest walk-root
  (testing "Should be able to walk a path that is a single root and return parts of the model"
    (async done
      (go
        (let [model (<! (fetch/model service))]
          (let [walked (path/walk model "Gene")]
            (is (= (map :name walked) '("Gene")))
            (done)))))))

(deftest path-root
  (testing "Should be able to parse a root that is just a path"
    (async done
      (go
        (let [model (<! (fetch/model service))
              path  "Gene"]
          (is (= :Gene (path/class model path)))
          (is (= "Gene" (path/trim-to-last-class model path)))
          (done))))))

(deftest path-normal
  (testing "Should be able to parse a path that is normal"
    (async done
      (go
        (let [model (<! (fetch/model service))
              path  "Gene.organism.name"]
          (is (= :Organism (path/class model path)))
          (is (= "Gene.organism" (path/trim-to-last-class model path)))
          (done))))))

(deftest path-subclass
  (testing "Should be able to parse a path with a subclass"
    (async done
      (go
        (let [model (<! (fetch/model service))
              path  "Gene.homologues.homologue.name"]
          (is (= :Gene (path/class model path)))
          (is (= "Gene.homologues.homologue" (path/trim-to-last-class model path)))
          (done))))))

(deftest path-subclasses
  (testing "Should be able to parse a path with multiple subclass"
    (async done
      (go
        (let [model (<! (fetch/model service))
              path  "Gene.downstreamIntergenicRegion.adjacentGenes.microArrayResults.affyCall"]
          (is (= :MicroArrayResult (path/class model path)))
          (is (= "Gene.downstreamIntergenicRegion.adjacentGenes.microArrayResults"
                 (path/trim-to-last-class model path)))
          (done))))))