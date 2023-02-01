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
    var isDeleted: Boolean = false,                         // Заметка удалена
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
    val nickname: String,                                   // Ник
)

object UserService {

    private val users = mutableListOf<User>()
    private var user_id = 0

    fun addUser(nickname: String): User {
        users.also { it.add(User(++user_id, nickname = nickname)) }.run { return last() }
    }

    fun getById(user_id: Int) = users.find {it.user_id == user_id} ?: throw UserNotFoundException("User $user_id not found")

    override fun toString() = "${users.forEach(::println)}"
}

object NoteService {

    private var notes = mutableListOf<Note>()
    private var deletedNotes = mutableListOf<Note>()        // Архив удаленных заметок
    private var comment_id = 0
    private var note_id = 0
    private var getNotes = mutableListOf<Note>()

    fun add(title: String, text: String, user_id: Int): Int {
        notes.also { it.add(Note(++note_id, owner_id = user_id, title = title, text = text)) }.run { return note_id }
    }

    fun getById(note_id: Int) =
        notes.find { it.note_id == note_id } ?: throw NoteNotFoundException("Note $note_id not found")

    fun get(owner_id: Int, sort: Int = 1): MutableList<Note> {
        notes.filter { it.owner_id == owner_id }.forEach {getNotes.add(it)}.also { getNotes.sortBy { it.date } }.
        also { if (sort == 0) getNotes.reverse() }.run { return getNotes }
    }

    fun edit(note_id: Int, title: String, text: String): Boolean {
        notes.find { it.note_id == note_id }?.also { it.title = title }?.also { it.text = text }?.run { return true } ?: throw NoteNotFoundException("Note $note_id not found")
    }

    fun delete(note_id: Int): Boolean {     // Удаляет из списка заметку и отправляет его в архив
        notes.find { it.note_id == note_id }?.also { it.isDeleted = true }?.also { notes.remove(it) }
            ?.let { deletedNotes.add(it) }?.run { return true } ?: throw NoteNotFoundException("Note $note_id not found")
    }

    fun createComment(note_id: Int, user_id: Int, message: String): Int {
        notes.find { it.note_id == note_id }?.also { it.comment.add(Comment(++comment_id, uid = user_id, message)) }
            ?.run { return comment_id } ?: throw NoteNotFoundException("Note $note_id not found")
    }

    fun getComments(note_id: Int, sort: Int = 0): MutableList<Comment> {
        notes.find { it.note_id == note_id }?.also { it.comment.sortBy { comment -> comment.date } }
            ?.also { it.comment.reverse() }?.also { if (sort == 1) it.comment.reverse() }?.run { return comment } ?: throw NoteNotFoundException("Note $note_id not found")
    }

    fun editComment(comment_id: Int, user_id: Int, message: String): Boolean {  // редактировать комментарий может только его автор
        for (note in notes) {
            note.comment.find { it.comment_id == comment_id && !it.isDeleted }?.
            also {if (it.uid == user_id) { it.message = message } else throw AccessDeniedException("Access denied") }?.run { return true }
        }
        throw CommentNotFoundException("Comment $comment_id not found")
    }

    fun deleteComment(comment_id: Int, user_id: Int): Boolean {     // удалить комментарий может только его автор или владелец заметки
        for (note in notes) {
            note.comment.find { it.comment_id == comment_id && !it.isDeleted }?.
            also {if (it.uid == user_id || note.owner_id == user_id) { it.isDeleted = true } else throw AccessDeniedException("Access denied") }?.run { return true }
        }
        throw CommentNotFoundException("Comment $comment_id not found")
    }

    fun restoreComment(comment_id: Int, user_id: Int): Boolean {     // Восстановить комментарий может только его автор или владелец заметки
        for (note in notes) {
            note.comment.find { it.comment_id == comment_id && it.isDeleted }?.
            also {if (it.uid == user_id || note.owner_id == user_id) { it.isDeleted = false } else throw AccessDeniedException("Access denied") }?.run { return true }
        }
        throw CommentNotFoundException("Comment $comment_id not found")
    }

    fun clear() {
        notes = mutableListOf()
        deletedNotes = mutableListOf()
        getNotes = mutableListOf()
        comment_id = 0
        note_id = 0
    }

    override fun toString() = "${notes.forEach(::println)}"

    fun printDeletedNotes() = "${deletedNotes.forEach(::println)}"
}

fun main() {

    println("Создаем юзеров")
    println(UserService.addUser("Alex"))
    println(UserService.addUser("Марк"))
    println(UserService.addUser("Том"))

    println("\nПоиск юзера")
    println(UserService.getById(2))

    println("\nСоздаем заметки")
    NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
    NoteService.add("Тест(2) - заголовок", "Тестовая заметка - 2", 2)
    NoteService.add("Тест(3) - заголовок", "Тестовая заметка - 3", 1)
    println(NoteService.add("Тест(4) - заголовок", "Тестовая заметка - 4", 1))
    println(NoteService)

    println("\nВыводим заметку по id")
    println(NoteService.getById(2))

    println("\nВозвращаем список заметок, созданных пользователем")
    println(NoteService.get(1,0))

    println("\nУдаляем заметку")
    println(NoteService.delete(1))
    println(NoteService)
    NoteService.printDeletedNotes()

    println("\nРедактируем заметку")
    println(NoteService.edit(2, "Test(2) - heading", "Test note in English - 2"))

    println("\nСоздаем комментарии к заметке")
    println(NoteService.createComment(2, 1, "Первый комментарий"))
    println( NoteService.createComment(2, 3, "Второй комментарий"))
    println(NoteService)

    println("\nВозвращаем список комментариев к заметке")
    println(NoteService.getComments(2,0))

    println("\nРедактируем комментарии к заметке")
    NoteService.editComment(2, 3, "Second comment")
    println(NoteService)

    println("\nУдаляем комментарии к заметке")
    NoteService.deleteComment(1, 2)
    println(NoteService)

    println("\nВосстанавливаем комментарии к заметке")
    NoteService.restoreComment(1, 1)
    println(NoteService)
}