(ns mobile-patient.screen.vitals
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]))

(def code->title {"9279-1" "Respiratory Rate"
                  "8867-4" "Heart rate"
                  "8310-5" "Body temperature"
                  "85354-9" "Blood pressure"
                  })

(defn format-quantity
  [{{value :value code :code} :Quantity}] (str value " " code))

(defn format-range
  [{{{low-value :value code :code} :low {high-value :value} :high} :Range}] (str high-value "/" low-value " " code))

(defn format-value [item]
  (case (keys item)
    [:Quantity] (format-quantity item)
    [:Range] (format-range item)))

(defn VitalsScreen [{:keys [navigation]}]
  [ui/scroll-view {:style {:flex 1
                           :background-color "#fff"
                           }}
   [ui/shadow-box
    (ui/show-remote-data
     @(rf/subscribe [:get-observations])
     (fn [data]
       [ui/view
        {:style {:flex 1
                 :flex-direction :column}
         }
        (for [[group values] data
              :let [value (:value (first values))]]
          [ui/view
           {:key group
            :style {:flex 1
                    :flex-direction :row
                    :justify-content :space-between
                    }
            }
           [ui/text (code->title group)]
           [ui/text (format-value value)]
           ]
          )]))]

   [ui/view {:style {:border-width 1}}
    [ui/victory-group {:style {:data {:border "1px solid red"}}
                       :padding 0
                       :color color/pink
                       :domain #js {:x #js [0 5]
                                    :y #js [0 10]}}
     [ui/victory-scatter {
                          :size 5
                          :data #js [#js{:x 1 :y 2}
                                     #js{:x 2 :y 3}]

                          }]
     [ui/victory-line {:data [{:x 0 :y 5} {:x 300 :y 5}]
                       :style {:data {:stroke-dasharray [5 5]
                                      }}}]

     ]]


   ])
