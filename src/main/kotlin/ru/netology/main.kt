package ru.netology

import java.time.Instant
import java.time.format.DateTimeFormatter

//  расчет текущего времени
val timestamp = System.currentTimeMillis()
val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
val sdfFormat = sdf.format(timestamp)

//  расчет World Time API
val timestampApi = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(timestamp / 1000))

class NoteNotFoundException(message: String) : RuntimeException(message)
class CommentNotFoundException(message: String) : RuntimeException(message)
class AccessDeniedException(message: String) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)

data class Note(
    val note_id: Int,                                       // Идентификатор заметки
    val owner_id: Int,                                      // Идентификатор владельца заметки
    var title: String,                                      // Заголовок заметки
    var text: String,                                       // Текст заметки
    val comment: MutableList<Comment> = mutableListOf(),    // Список комментариев
    val privacy: Int = 0,                                   // Уровень доступа к заметке. Возможные значения: 0 — все пользователи, 1 — только друзья, 2 — друзья и друзья друзей, 3 — только пользователь.
    val isDeleted: Boolean = false,                         // Заметка удалена
    val date: Long = timestamp                              // Время публикации записи в формате unixtime
)

data class Comment(
    val comment_id: Int,                                    // Идентификатор комментария
    val uid: Int,                                           // Идентификатор автора комментария
    var message: String,                                    // Текст комментария
    val comment_privacy: Int = 0,                           // Уровень доступа к комментированию заметки. Возможные значения: 0 — все пользователи, 1 — только друзья, 2 — друзья и друзья друзей, 3 — только пользователь.
    var isDeleted: Boolean = false,                         // Комментарий невидим
    val date: Long = timestamp                              // Время публикации записи в формате unixtime
)

data class User(
    val user_id: Int,                                       // Идентификатор пользователя, информацию о заметках которого требуется получить
    val first_name: String,                                 // Имя
    val last_name: String                                   // Фамилия
)

object UserService {

    private val users = mutableListOf<User>()
    private var user_id = 0

    fun addUser(first_name: String, last_name: String): Int {
        ++user_id
        val user = User(user_id = user_id, first_name = first_name, last_name = last_name)
        users.add(user)
        return user.user_id
    }

    fun getById(user_id: Int): User {
        for (user in users) {
            if (user.user_id == user_id) {
                return user
            }
        }
        throw UserNotFoundException("User $user_id not found")
    }

    override fun toString(): String {
        return "${users.forEach(::println)}"
    }
}

object NoteService {

    private var notes = mutableListOf<Note>()
    private var deletedNotes = mutableListOf<Note>()        // Архив удаленных заметок
    private var comment_id = 0
    private var note_id = 0

    fun add(title: String, text: String, user_id: Int): Int {
        ++note_id
        notes.add(Note(note_id = note_id, owner_id = user_id, title = title, text = text))
        return note_id
    }

    fun getById(note_id: Int): Note {
        for (note in notes) {
            if (note.note_id == note_id) {
                return note
            }
        }
        throw NoteNotFoundException("Note $note_id not found")
    }

    fun get(owner_id: Int, sort: Int = 0): MutableList<Note> {
        val getNotes = mutableListOf<Note>()
        for (note in notes) {
            if (note.owner_id == owner_id) {
                getNotes.add(note)
                getNotes.sortBy { it.date }
                getNotes.reverse()
                if (sort == 1) getNotes.reverse()
            }
        }
        return getNotes
    }

    fun edit(note_id: Int, title: String, text: String): Boolean {
        for (note in notes) {
            if (note.note_id == note_id) {
                note.title = title
                note.text = text
                return true
            }
        }
        throw NoteNotFoundException("Note $note_id not found")
    }

    fun delete(note_id: Int): Boolean {     // Удаляет из списка заметку и отправляет его в архив
        for (note in notes) {
            if (note.note_id == note_id) {
                deletedNotes.add(note.copy(isDeleted = true))
                notes.remove(note)
                return true
            }
        }
        throw NoteNotFoundException("Note $note_id not found")
    }

    fun createComment(note_id: Int, user_id: Int, message: String): Int {
        for (note in notes) {
            if (note.note_id == note_id) {
                ++comment_id
                note.comment.add(Comment(comment_id, uid = user_id, message))
                return comment_id
            }
        }
        throw NoteNotFoundException("Note $note_id not found")
    }

    fun getComments(note_id: Int, sort: Int = 0): MutableList<Comment> {
        for (note in notes) {
            if (note.note_id == note_id) {
                note.comment.sortBy { it.date }
                note.comment.reverse()
                if (sort == 1) note.comment.reverse()
                return note.comment
            }
        }
        throw NoteNotFoundException("Note $note_id not found")
    }

    fun editComment(comment_id: Int, user_id: Int, message: String): Boolean {  // редактировать комментарий может только его автор
        for (note in notes) {
            for (comment in note.comment) {
                if (comment.comment_id == comment_id && !comment.isDeleted) {
                    if (comment.uid == user_id) {
                        comment.message = message
                        return true
                    }
                    throw AccessDeniedException("Access denied")
                }
            }
        }
        throw CommentNotFoundException("Comment $comment_id not found")
    }

    fun deleteComment(comment_id: Int, user_id: Int): Boolean {     // удалить комментарий может только его автор или владелец заметки
        for (note in notes) {
            for (comment in note.comment) {
                if (comment.comment_id == comment_id && !comment.isDeleted) {
                    if (comment.uid == user_id || note.owner_id == user_id) {
                        comment.isDeleted = true
                        return true
                    }
                    throw AccessDeniedException("Access denied")
                }
            }
        }
        throw CommentNotFoundException("Comment $comment_id not found")
    }

    fun restoreComment(comment_id: Int, user_id: Int): Boolean {     // Восстановить комментарий может только его автор или владелец заметки
        for (note in notes) {
            for (comment in note.comment) {
                if (comment.comment_id == comment_id && comment.isDeleted) {
                    if (comment.uid == user_id || note.owner_id == user_id) {
                        comment.isDeleted = false
                        return true
                    }
                    throw AccessDeniedException("Access denied")
                }
            }
        }
        throw CommentNotFoundException("Comment $comment_id not found")
    }

    fun clear() {
        notes = mutableListOf()
        deletedNotes = mutableListOf()
        comment_id = 0
        note_id = 0
    }

    override fun toString(): String {
        return "${notes.forEach(::println)}"
//        return "${notes.forEach(::println)} ${deletedNotes.forEach(::println)}"
    }
}

fun main() {

    println("Создаем юзеров")
    UserService.addUser("Alex", "Bobrikov")
    UserService.addUser("Марк", "Твен")
    UserService.addUser("Том", "Сойер")
    println(UserService)

    println("\nСоздаем заметки")
    println(NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1))
    println(NoteService.add("Тест(2) - заголовок", "Тестовая заметка - 2", 2))
    println(NoteService.add("Тест(3) - заголовок", "Тестовая заметка - 3", 1))
    println(NoteService.add("Тест(4) - заголовок", "Тестовая заметка - 4", 1))
    println(NoteService)

    println("\nВыводим заметку по id")
    println(NoteService.getById(2))

    println("\nВозвращаем список заметок, созданных пользователем")
    println(NoteService.get(1,0))

    println("\nУдаляем заметку")
    NoteService.delete(1)
    println(NoteService)

    println("\nРедактируем заметку")
    NoteService.edit(2, "Test(2) - heading", "Test note in English - 2")
    println(NoteService)

    println("\nСоздаем комментарии к заметке")
    NoteService.createComment(2, 1, "Первый комментарий")
    NoteService.createComment(2, 3, "Второй комментарий")
    println(NoteService)

    println("\nВозвращаем список комментариев к заметке")
    println(NoteService.getComments(2,0))

    println("\nРедактируем комментарии к заметке")
    NoteService.editComment(2, 3, "Second comment")
    println(NoteService)

    println("\nУдаляем комментарии к заметке")
    NoteService.deleteComment(1, 1)
    println(NoteService)

    println("\nВосстанавливаем комментарии к заметке")
    NoteService.restoreComment(1, 1)
    println(NoteService)
}