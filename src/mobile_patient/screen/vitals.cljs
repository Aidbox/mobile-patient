(ns mobile-patient.screen.vitals
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]))


(def INTERPRITATION {"N" "Normal"
                     "H" "High"
                     "L" "Low"
                     })

(defn show-interpritation [code]
  (get INTERPRITATION code))

(defn row-component [{:keys [item index header?] :or {header? false}}]
  (let [abnormal? (#{"H" "L"} (:interpretation item))
        border-color "#333"
        cell-style {:flex 2
                    :border-bottom-width 1
                    :border-color border-color
                    :padding 10}
        text-style {:color "#000"}
        text-style (if header? (assoc text-style :font-weight :bold) text-style)
        text-style (if abnormal? (assoc text-style :color "red") text-style)
        ]

    [ui/view {:style {:border-top-width (if header? 1 0)
                      :border-color border-color
                      :flex-direction :row
                      :background-color (if (odd? index) "#fff" "#eee")
                      }}
     [ui/view {:style (merge cell-style {:border-right-width 1 :border-color border-color})}
      [ui/text {:style text-style}
       (:title item)]]

     [ui/view {:style (merge cell-style {:border-right-width 1 :border-color border-color})}
      [ui/text {:style text-style}
       (:value item)]]

     [ui/view {:style cell-style}
      [ui/text {:style text-style}
       (if header?
         (:interpretation item)
         (show-interpritation (:interpretation item)))]]
     ]))

(defn header-component []
  [row-component {:item {:title "Vital Sign" :value "Value" :interpretation "Interpretation"}
                  :index 1
                  :header? true}])

(defn VitalsScreen [{:keys [navigation]}]
  (let [data @(rf/subscribe [:get-observations])]
    (print "data" data)
    (if (empty? data)
      [ui/text "No data"]
      [ui/view {:style {:flex 1 :margin-top 0}}
       [header-component]
       [ui/flat-list
        {:data (clj->js data)
         :render-item (fn [row] (r/as-element [row-component (js->clj row :keywordize-keys true)]))}]])))
