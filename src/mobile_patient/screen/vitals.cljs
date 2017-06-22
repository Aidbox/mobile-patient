(ns mobile-patient.screen.vitals
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]))


(defn row-component [{:keys [item index header?] :or {header? false}}]
  (let [border-color "#333"
        cell-style {:flex 2
                    :border-bottom-width 1
                    :border-color border-color
                    :padding 10}
        text-style {:color "#000"}
        text-style (if header? (assoc text-style :font-weight :bold) text-style)
        ]

    [ui/view {:style {:border-top-width (if header? 1 0)
                      :border-color border-color
                      :flex-direction :row
                      :background-color (if (odd? index) "#fff" "#eee")
                      }}
     [ui/view {:style (merge cell-style {:border-right-width 1 :border-color border-color})}
      [ui/text {:style text-style}
       (:title item)]]

     [ui/view {:style cell-style}
      [ui/text {:style text-style}
       (:value item)]]
     ]))

(defn header-component []
  [row-component {:item {:title "Vital Sign" :value "Normal Value"}
                  :index 1
                  :header? true}])

(defn VitalsScreen [{:keys [navigation]}]
  [ui/view {:style {:flex 1 :margin-top 0}}
   [header-component]
   [ui/flat-list
    {
     :data #js [#js {:key "a" :title "Temperature" :value "97.8 - 99.1 F"}
                #js {:key "b" :title "Pulse" :value "60 - 100 beats/minute"}
                #js {:key "c" :title "Respirations / Breathing" :value "12 - 18 breaths per minute"}
                #js {:key "d" :title "Blood Pressure (SBP / DBP)" :value "90/60 to 120/80"}
                ]
     :render-item (fn [row] (r/as-element [row-component (js->clj row :keywordize-keys true)]))}
    ]])
