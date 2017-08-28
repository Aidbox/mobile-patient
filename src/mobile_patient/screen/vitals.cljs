(ns mobile-patient.screen.vitals
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [mobile-patient.component.vital-chart :refer [vital-chart]]
            [mobile-patient.component.gesture-view :refer [gesture-view]]
            ))

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

(def state1 (r/atom 1))
(def state2 (r/atom 2))

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

   [gesture-view
    [vital-chart [{:x @state1 :y @state1} ]]]

   #_[vital-chart [{:x @state2 :y @state2} ]]
   ])
