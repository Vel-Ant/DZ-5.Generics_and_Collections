# Опущенные параметры:

1. need_wiki: Checkbox      // Определяет, требуется ли в ответе wiki-представление заметки (работает, только если запрашиваются заметки текущего пользователя).
2. reply_to: Int            // Идентификатор пользователя, ответом на комментарий которого является добавляемый комментарий (не передаётся, если комментарий не является ответом).
3. guid: String             // Уникальный идентификатор, предназначенный для предотвращения повторной отправки одинакового комментария
4. privacy_view: Int        // Настройки приватности просмотра заметки в специальном формате
5. privacy_comment: Int     // Настройки приватности комментирования заметки в специальном формате
6. offset: Int              // Смещение, необходимое для выборки определенного подмножества заметок.
7. note_ids: String        // Идентификаторы заметок, информацию о которых необходимо получить.