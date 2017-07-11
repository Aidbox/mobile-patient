(ns mobile-patient.screen.settings
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]))


(defn switch []
  (let [value (r/atom false)]
    (fn []
      [ui/switch {:value @value :on-value-change #(swap! value not)}]
      )))

(defn SettingsScreen [{:keys [navigation]}]
  [ui/table-view
   [ui/section {:header "Account"}
    [ui/cell {:title "Medication 1" :on-press #()}]
    [ui/cell {:title "Medication 2" :on-press #()}]
    [ui/cell {:title "Medication 3" :on-press #()}]
    ]
   [ui/section {:header "Notifications"}
    [ui/cell {:title "Medication 1"
              :cell-accessory-view (r/as-element [switch]) :on-press #()}]
    [ui/cell {:title "Medication 2"
              :cell-accessory-view (r/as-element [switch]) :on-press #()}]
    [ui/cell {:title "Medication 3"
              :cell-accessory-view (r/as-element [switch]) :on-press #()}]
    ]]
  )
