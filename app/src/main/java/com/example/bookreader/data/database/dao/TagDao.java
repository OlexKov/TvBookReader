package com.example.bookreader.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.bookreader.data.database.dto.BookTagDto;
import com.example.bookreader.data.database.dto.TagDto;
import com.example.bookreader.data.database.entity.Tag;

import java.util.List;

@Dao
public interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Tag tag);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    List<Long> insertAll(List<Tag> tags);

    @Query("""
            INSERT INTO books_tags (tagId,bookId) VALUES (:tagId,:bookId)
           """)
    Long addTagToBook(long tagId,Long bookId);

    @Query("""
             DELETE FROM books_tags  WHERE bookId = :bookId AND tagId =:tagId
           """)
    int removeTagFromBook(long tagId,Long bookId);

    @Query("""
             DELETE FROM books_tags
             WHERE bookId = :bookId AND tagId IN (:tagIds)
           """)
    int removeTagsFromBook(List<Long> tagIds,Long bookId);

    @Query("""
             SELECT * FROM books_tags
             WHERE bookId = :bookId AND tagId =:tagId
           """)
    BookTagDto getTagBook(long tagId, Long bookId);

    @Query("SELECT * FROM tags WHERE id = :tagId")
    TagDto getById(long tagId);

    @Query("SELECT * FROM tags WHERE name = :tagName")
    TagDto getByName(String tagName);

    @Query("DELETE FROM tags WHERE id = :tagId")
    int deleteById(long tagId);

    @Query("SELECT * FROM tags ORDER BY name")
    List<TagDto> getAll();

    @Query("""
            SELECT tg.id AS id, tg.name AS name
            FROM tags AS tg
            JOIN books_tags as bt ON bt.tagId = tg.id
            WHERE bt.bookId = :bookId
            ORDER BY tg.name
            """)
    List<TagDto> getTagsByBookId(Long bookId);

    @Query("""
            SELECT * FROM tags AS tg
            WHERE tg.id IN (:ids)
            ORDER BY tg.name
            """)
    List<TagDto> getTagsByIds(List<Long> ids);

}
