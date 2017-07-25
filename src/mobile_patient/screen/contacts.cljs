(ns mobile-patient.screen.contacts
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]))


(defn row-component [{:keys [item index]} navigation]
  (print item)
  [ui/touchable-highlight {:style {:border-color "#E9E9EF"
                                   :border-width 1
                                   :border-top-width (if (zero? index) 0 1)
                                   :border-bottom-width 0
                                   :padding 10
                                   :background-color "#ffffff"}
                           :on-press (fn []
                                       (dispatch [:create-chat [(:id item)]])
                                       (navigation.goBack nil))}
   [ui/view {:style {:flex-direction :row
                     :justify-content :space-between
                     :padding-left 20
                     :padding-right 15
                     }}
    [ui/text {:style {:text-align "left"
                      :font-size 16
                      :padding-top 4
                      :color "#333"
                      :font-weight :bold}} (:id item)]
    [ui/icon {:name "add" :size 30 :color "#FF485C"}]]])


(defn header-component []
  [ui/text {:style {:color color/pink
                    :font-weight :bold
                    :padding 25}}
      "Select a Person"])

(defn ContactsScreen [{:keys [navigation]}]
  (let [contacts (subscribe [:contacts])]
    [ui/flat-list {:style {:background-color :white}
                   :data (clj->js @contacts)
                   :key-extractor #(.-id %)
                   :ListHeaderComponent (r/reactify-component header-component)
                   :render-item (fn [row] (r/as-element [row-component (js->clj row :keywordize-keys true) navigation]))}]))
