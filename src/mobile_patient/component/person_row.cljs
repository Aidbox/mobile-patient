(ns mobile-patient.component.person-row
  (:require [reagent.core :as r]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [mobile-patient.model.patient :as patient-model]))

(defn person-row-component [{:keys [item index]} navigation on-press icon-name]
  [ui/touchable-highlight {:style {:border-color "#E9E9EF"
                                   :border-width 1
                                   :border-top-width (if (zero? index) 0 1)
                                   :border-bottom-width 0
                                   :padding 10
                                   :background-color "#ffffff"}
                           :on-press #(on-press item navigation)}
   [ui/view {:style {:flex-direction :row
                     :flex 1
                     :justify-content :space-between
                     :padding-left 15
                     :padding-right 10
                     }}
    [ui/avatar (str "data:image/png;base64," (patient-model/get-photo item))
               20]
    [ui/text {:style {:flex 1
                      :margin-left 15
                      :text-align "left"
                      :font-size 16
                      :padding-top 4
                      :color "#333"
                      :font-weight :bold}} (patient-model/get-official-name item)]
    [ui/icon {:name icon-name :size 30 :color "#FF485C"}]]])
