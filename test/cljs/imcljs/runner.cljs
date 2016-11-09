(ns imcljs.runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [imcljs.core-test]))

(doo-tests 'imcljs.core-test)
