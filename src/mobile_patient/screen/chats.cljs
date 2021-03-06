(ns mobile-patient.screen.chats
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [mobile-patient.ui :as ui]
            [reagent.core :as r]
            [clojure.string :as str]
            [mobile-patient.color :as color]
            [mobile-patient.model.chat :as chat-model]
            [mobile-patient.model.user :as user-model]
            [mobile-patient.component.person-row :refer [person-row-component]]))

(def font-size 16)

(defn group-row-component [{:keys [item index]}
                          navigation
                          on-press
                          icon-name]
  (let [chat item
        patient-id (chat-model/get-patient-id chat)
        patient @(subscribe [:user-by-id patient-id])
        chat-name (str (user-model/get-official-name patient) " PG")
        unread (get-in item [:unread])]
    [ui/touchable-highlight {:style {:border-color "#E9E9EF"
                                     :border-width 1
                                     :border-top-width (if (zero? index) 0 1)
                                     :border-bottom-width 0
                                     :padding 10}
                             :on-press #(on-press item navigation chat-name)
                             :underlay-color "white"}
     [ui/view {:style {:flex 1
                       :flex-direction :row
                       :justify-content :space-between
                       :padding-left 15
                       :padding-right 10}}

      [ui/view {:style {:flex 0.9}}

       [ui/view {:flex-direction :row}
        [ui/text {:style {:margin-right 5
                          :color "black"
                          :font-weight :bold
                          :font-size font-size}} chat-name]
        (if unread
          [ui/badge (str unread " new")])]

       [ui/text {:style {:color "#919291"
                         :font-size 13}}
        (str/join ", " (->> (:participants chat)
                                 (map :id)
                                 (map (fn [x] @(subscribe [:user-by-id x])))
                                 (map user-model/get-official-name)))]]


      [ui/view {:style {:justify-content :center}}
       [ui/icon { ;; :style {:flex 0.1
                 ;;         :align-self :center}
                 :name "chevron-right"
                 :size 30
                 :color color/pink}]]]]))


(defn on-group-press-callback [group navigation chat-name]
  (let [chat group]
    (dispatch [:get-messages (:id chat)])
    (dispatch [:mark-read (:id chat)])
    (dispatch-sync [:set-chat chat])
    (navigation.navigate "Chat" #js{:chat-name chat-name})))

(defn on-person-press-callback [person navigation chat-name]
  (let [chat (:chat person)]
    (dispatch [:get-messages (:id chat)])
    (dispatch [:mark-read (:id chat)])
    (dispatch-sync [:set-chat chat])
    (navigation.navigate "Chat" #js{:chat-name chat-name})))


(defn Header [text]
  [ui/text {:style {:margin-left 20
                    :margin-top 20
                    :margin-bottom 20
                    :color color/pink
                    :font-size 14
                    :font-weight :bold}}
        text])

(defn ChatsScreen [{:keys [navigation]}]
  (let [chats-timer (atom nil)
        messages-timer (atom nil)
        i-am-patient (= "Patient" (:resourceType @(subscribe [:domain-user])))
        groups (subscribe [:practice-groups])
        people (subscribe [:personal-chats])]

    (r/create-class
     {:display-name "ChatsScreen"
      :component-did-mount (fn []
                             (reset! chats-timer (js/setInterval #(dispatch [:do-get-chats]) 3000))
                             (reset! messages-timer (js/setInterval #(dispatch [:do-get-new-messages]) 3000))
                             )
      :component-will-unmount (fn []
                                (js/clearInterval @chats-timer)
                                (js/clearInterval @messages-timer)
                                )
      :reagent-render
      (fn [_]
        [ui/scroll-view {:style {:background-color "white"
                                 :flex 1}}
         ;; groups
         [Header "Practice Groups"]
         (if (empty? @groups)
           [ui/text {:style {:margin-left 20}} "No chats yet"]
           [ui/flat-list {:style {:background-color :white}
                          :data (clj->js @groups)
                          :key-extractor #(.-id %)
                          :render-item (fn [row]
                                         (r/as-element [group-row-component
                                                        (js->clj row :keywordize-keys true)
                                                        navigation
                                                        on-group-press-callback
                                                        "chevron-right"]))}])
         ;; persons
         [Header (if i-am-patient "Practitioners" "Patients")]
         (if (empty? @people)
           [ui/text {:style {:margin-left 20}} "No chats yet"]
           [ui/flat-list {:style {:background-color :white}
                          :data (clj->js @people)
                          :key-extractor #(.-id %)
                          :render-item (fn [row]
                                         (r/as-element [person-row-component
                                                        (js->clj row :keywordize-keys true)
                                                        navigation
                                                        on-person-press-callback
                                                        "chevron-right"]))}])])
      })
    ))
