(ns mobile-patient.screen.vitals
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]))

(defn VitalsScreen [{:keys [navigation]}]
  (ui/show-remote-data
   @(rf/subscribe [:get-observations])
   (fn [data]
     [ui/text (str data)])))
