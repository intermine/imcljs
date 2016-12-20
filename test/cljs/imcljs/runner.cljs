(ns imcljs.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [imcljs.core-test]
            [imcljs.path-test]))

(doo-tests 'imcljs.path-test)
