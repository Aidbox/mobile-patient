(ns mobile-patient.screen.chats
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [reagent.core :as r]
            [clojure.string :as str]
            [mobile-patient.color :as color]
            [mobile-patient.model.chat :as chat-model]
            [mobile-patient.model.user :as user-model]
            [mobile-patient.component.person-row :refer [person-row-component]]))

(def ds (ui/ReactNative.ListView.DataSource. #js{:rowHasChanged (fn[a b] false)}))
(def font-size 18)

(def chat-row-comp
  (r/reactify-component
   (fn [props]
     (let [row (props :row)
           navigation (props :navigation)
           chat @row
           other-id (chat-model/other-participant-id chat @(subscribe [:domain-user]))
           other-user @(subscribe [:user-by-id other-id])
           chat-name (user-model/get-official-name other-user)]
       [ui/touchable-highlight {:on-press #(do
                                             (dispatch [:set-chat chat])
                                             (navigation.navigate "Chat" #js{:chat-name (:name chat)}))
                                :underlay-color "white"}
        [ui/view {:style {:flex 1
                          :flex-direction :row
                          :margin-top 10
                          :margin-bottom 10
                          :margin-left 20}}
         [ui/view {:style {:flex 0.9}}
          [ui/text {:style {:color "black"
                            :font-weight :bold
                            :font-size font-size}}
           chat-name]
          #_[ui/text {:style {:color "#919291"
                            :font-size font-size}}
           (str/join ", " (map :id (:participants chat)))]]
          [ui/icon {:style {:flex 0.1
                            :align-self :center}
                    :name "chevron-right"
                    :size 36
                    :color color/pink}]]]))))

(def chat-row-sep
  (r/create-element
   (r/reactify-component
    (fn [_] [ui/view {:style {:border-bottom-width 1
                              :border-color "#eee"}}]))
   #js{}))

(defn ChatsScreen0 [{:keys [navigation]}]
  (let [chats (subscribe [:chats])
        source (map #(r/atom %) @chats)]
    (fn [_]
      [ui/view {:style {:background-color "white"
                        :flex 1}}
       [ui/text {:style {:margin-left 20
                         :margin-top 20
                         :margin-bottom 20
                         :color color/pink
                         :font-size font-size}}
        "Practitioners"]
       (if (empty? source)
         [ui/text {:style {:margin-left 20}} "No chats yet"]
         [ui/list-view {:dataSource (.cloneWithRows ds (clj->js source))
                        :enableEmptySections true
                        :render-separator (fn [_] chat-row-sep)
                        :render-row (fn [row]
                                      (r/create-element
                                       chat-row-comp
                                       #js{:row row
                                           :navigation navigation}))}])])))

(defn header-component []
  [ui/text {:style {:color color/pink
                    :font-weight :bold
                    :padding 25}}
   "Select a Person"])

(defn row-component [{:keys [item index]} navigation]
  [ui/touchable-highlight {:style {:border-color "#E9E9EF"
                                   :border-width 1
                                   :border-top-width (if (zero? index) 0 1)
                                   :border-bottom-width 0
                                   :padding 10
                                   :background-color "#ffffff"}
                           :on-press (fn []
                                       (dispatch [:create-chat item])
                                       (navigation.goBack nil))}
   [ui/text "row"]]
  )

(defn on-press-callback [person navigation]
  (let [chat (:chat person)]
    (dispatch [:set-chat chat])
    (navigation.navigate "Chat" #js{:chat-name (:name chat)})))

(defn ChatsScreen [{:keys [navigation]}]
  (let [i-am-patient true
        groups @(subscribe [:practice-groups])
        people @(subscribe [:personal-chats])]
    (fn [_]
      [ui/scroll-view {:style {:background-color "white"
                               :flex 1}}
       ;; groups
       [ui/text {:style {:margin-left 20
                         :margin-top 20
                         :margin-bottom 20
                         :color color/pink
                         :font-size font-size}}
        "Practice Groups"]
       (if (empty? groups)
         [ui/text {:style {:margin-left 20}} "No chats yet"]
         [ui/flat-list {:style {:background-color :white}
                        :data (clj->js groups)
                        :key-extractor #(.-id %)
                        :render-item (fn [row]
                                       (r/as-element [row-component (js->clj row :keywordize-keys true) navigation]))}]
         )
       ;; persons
       [ui/text {:style {:margin-left 20
                         :margin-top 20
                         :margin-bottom 20
                         :color color/pink
                         :font-size font-size}}
        (if i-am-patient "Practitioners" "Patients")]
       (if (empty? people)
         [ui/text {:style {:margin-left 20}} "No chats yet"]
         [ui/flat-list {:style {:background-color :white}
                        :data (clj->js people)
                        :key-extractor #(.-id %)
                        :render-item (fn [row]
                                       (r/as-element [person-row-component
                                                      (js->clj row :keywordize-keys true)
                                                      navigation
                                                      on-press-callback
                                                      "chevron-right"
                                                      ]))}]
         )
       ]
      )))
