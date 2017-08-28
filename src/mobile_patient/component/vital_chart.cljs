(ns mobile-patient.component.vital-chart
  (:require [reagent.core :as r]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]))


(defn vital-chart [data]
  [ui/victory-group {:style {:data {:border "1px solid red"}}
                     ;;:padding 0
                     :color color/pink
                     :domain #js {:x #js [0 5]
                                  :y #js [0 10]}}
   [ui/victory-scatter {:size 5
                        :data (clj->js data)

                        }]
   [ui/victory-line {:data [{:x 0 :y 5} {:x 300 :y 5}]
                     :style {:data {:stroke-dasharray [5 5]
                                    }}}]

   ])
