package ru.netology

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class NoteServiceTest {

    @Before
    fun clearBeforeTest() {
        NoteService.clear()
    }

    @Test
    fun addNewNote() {
        val result = NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)

        assertEquals(1, result)
    }

    @Test
    fun getByIdNote() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)

        val result = NoteService.getById(1)

        assertEquals(Note(1, 1, "Тест(1) - заголовок", "Тестовая заметка - 1"), result)
    }

    @Test (expected = NoteNotFoundException::class)
    fun getByIdException() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)

        NoteService.getById(3)
    }

    @Test
    fun getAllNotesOwnerSortOne() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.add("Тест(3) - заголовок", "Тестовая заметка - 3", 1)

        val result = NoteService.get(1, 1)

        assertEquals(mutableListOf(Note(1, 1, "Тест(1) - заголовок", "Тестовая заметка - 1"), Note(2, 1, "Тест(3) - заголовок", "Тестовая заметка - 3")), result)
    }

    @Test
    fun getAllNotesOwnerSortZero() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.add("Тест(3) - заголовок", "Тестовая заметка - 3", 1)

        val result = NoteService.get(1, 0)

        assertEquals(mutableListOf(Note(2, 1, "Тест(3) - заголовок", "Тестовая заметка - 3"), Note(1, 1, "Тест(1) - заголовок", "Тестовая заметка - 1")), result)
    }

    @Test
    fun editNoteTest() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.add("Тест(3) - заголовок", "Тестовая заметка - 3", 1)

        val result = NoteService.edit(1, "Test(1) - heading", "Test note in English - 1")

        assertEquals(true, result)
    }

    @Test (expected = NoteNotFoundException::class)
    fun editNoteException() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.add("Тест(3) - заголовок", "Тестовая заметка - 3", 1)

        NoteService.edit(3, "Test(1) - heading", "Test note in English - 1")
    }

    @Test
    fun deleteNoteTest() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.add("Тест(3) - заголовок", "Тестовая заметка - 3", 1)

        val result = NoteService.delete(1)

        assertEquals(true, result)
    }

    @Test (expected = NoteNotFoundException::class)
    fun deleteNoteException() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.add("Тест(3) - заголовок", "Тестовая заметка - 3", 1)

        NoteService.delete(3)
    }

    @Test (expected = NoteNotFoundException::class)
    fun createCommentException() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)

        NoteService.createComment(3, 1,"Первый комментарий")
    }

    @Test
    fun createCommentTest() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)

        val result = NoteService.createComment(1, 1,"Первый комментарий")

        assertEquals(1, result)
    }


    @Test
    fun getCommentsTestSortOne() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.createComment(1, 1, "Первый комментарий")
        NoteService.createComment(1, 1, "Второй комментарий")

        val result = NoteService.getComments(1, 1)

        assertEquals(mutableListOf(Comment(1,1, "Первый комментарий"), Comment(2,1, "Второй комментарий")), result)
    }

    @Test
    fun getCommentsTestSortZero() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.createComment(1, 1, "Первый комментарий")
        NoteService.createComment(1, 1, "Второй комментарий")

        val result = NoteService.getComments(1, 0)

        assertEquals(mutableListOf(Comment(2,1, "Второй комментарий"), Comment(1,1, "Первый комментарий")), result)
    }

    @Test (expected = NoteNotFoundException::class)
    fun getCommentsException() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)

        val result = NoteService.getComments(2, 0)
    }

    @Test
    fun editCommentTest() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.createComment(1, 1, "Первый комментарий")

        val result = NoteService.editComment(1, 1, "Второй комментарий")

        assertEquals(true, result)
    }

    @Test (expected = CommentNotFoundException::class)
    fun editCommentException() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.createComment(1, 1, "Первый комментарий")

        NoteService.editComment(3, 1, "Второй комментарий")
    }

    @Test (expected = AccessDeniedException::class)
    fun editCommentAccessException() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.createComment(1, 1, "Первый комментарий")

        NoteService.editComment(1, 0, "Второй комментарий")
    }

    @Test
    fun deleteCommentTest() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.createComment(1, 1, "Первый комментарий")

        val result = NoteService.deleteComment(1, 1)

        assertEquals(true, result)
    }

    @Test (expected = CommentNotFoundException::class)
    fun deleteCommentException() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.createComment(1, 1, "Первый комментарий")

        NoteService.deleteComment(3, 1)
    }

    @Test (expected = AccessDeniedException::class)
    fun deleteCommentAccessException() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.createComment(1, 1, "Первый комментарий")

        NoteService.deleteComment(1, 3)
    }

    @Test
    fun restoreComment() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.createComment(1, 1, "Первый комментарий")
        NoteService.deleteComment(1, 1)

        val result = NoteService.restoreComment(1, 1)

        assertEquals(true, result)
    }

    @Test (expected = CommentNotFoundException::class)
    fun restoreCommentException() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.createComment(1, 1, "Первый комментарий")
        NoteService.deleteComment(1, 1)

        NoteService.restoreComment(3, 1)
    }

    @Test (expected = AccessDeniedException::class)
    fun restoreCommentAccessException() {
        NoteService.add("Тест(1) - заголовок", "Тестовая заметка - 1", 1)
        NoteService.createComment(1, 1, "Первый комментарий")
        NoteService.deleteComment(1, 1)

        NoteService.restoreComment(1, 3)
    }
}