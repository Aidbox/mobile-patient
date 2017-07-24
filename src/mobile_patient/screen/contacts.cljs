(ns mobile-patient.screen.contacts
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]))


(defn row-component [{:keys [item]} navigation]
  [ui/touchable-highlight {:style {:border-color "#E9E9EF"
                                   :border-width 1
                                   ;;:border-top-width 0
                                   :padding 10
                                   :background-color "#ffffff"}
                           :on-press (fn []
                                       (dispatch [:create-chat [(:id item)]])
                                       (navigation.goBack nil))}
   [ui/view {:style {:flex-direction :row :justify-content :space-between}}
    [ui/text {:style {:text-align "left" :font-size 18}} (:id item)]
    [ui/icon {:name "chevron-right" :size 30 :color "#FF485C"}]]])


(defn ContactsScreen [{:keys [navigation]}]
  (let [contacts (subscribe [:contacts])]
    [ui/flat-list {:data (clj->js @contacts)
                   :key-extractor #(.-id %)
                   :render-item (fn [row] (r/as-element [row-component (js->clj row :keywordize-keys true) navigation]))}]))
