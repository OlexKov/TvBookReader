package com.example.bookreader.data.database.repository;

import com.example.bookreader.BookReaderApp;
import com.example.bookreader.data.database.dao.TagDao;
import com.example.bookreader.data.database.dto.BookTagDto;
import com.example.bookreader.data.database.dto.TagDto;
import com.example.bookreader.data.database.entity.Tag;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TagRepository {
    private final TagDao tagDao;

    public TagRepository(){
        this.tagDao = BookReaderApp.getInstance().getAppDatabase().tagDao();
    }

    public Long insert(Tag tag){
        TagDto existingTag = tagDao.getByName(tag.name);
        if(existingTag != null){
            return existingTag.id;
        }
        return tagDao.insert(tag);
    }

    public CompletableFuture<Long> insertAsync(Tag tag){
        return CompletableFuture.supplyAsync(()->insert(tag)) ;
    }

    public List<Long> insertAll(List<Tag> tags){
        return tagDao.insertAll(tags).stream().filter(id-> id > 0).collect(Collectors.toList());
    }

    public CompletableFuture<List<Long>> insertAllAsync(List<Tag> tags){
        return CompletableFuture.supplyAsync(()->tagDao.insertAll(tags).stream().filter(id-> id > 0).collect(Collectors.toList())) ;
    }

    public Long addTagToBook(Long tagId,Long bookId){
        BookTagDto existing = tagDao.getTagBook(tagId,bookId);
        if(existing != null){
            return existing.id;
        }
        return tagDao.addTagToBook(tagId,bookId);
    }

    public CompletableFuture<Long> addTagToBookAsync(Long tagId,Long bookId){
        return CompletableFuture.supplyAsync(()->addTagToBook(tagId,bookId)) ;
    }

    public int removeTagFromBook(Long tagId,Long bookId){
        return tagDao.removeTagFromBook(tagId,bookId);
    }

    public CompletableFuture<Integer> removeTagFromBookAsync(Long tagId,Long bookId){
        return CompletableFuture.supplyAsync(()->tagDao.removeTagFromBook(tagId,bookId));
    }

    public int removeTagsFromBook(List<Long> tagIds,Long bookId){
        return tagDao.removeTagsFromBook(tagIds,bookId);
    }

    public CompletableFuture<Integer> removeTagsFromBookAsync(List<Long> tagIds,Long bookId){
        return CompletableFuture.supplyAsync(()->tagDao.removeTagsFromBook(tagIds,bookId));
    }

    public TagDto getById(Long tagId){
        return tagDao.getById(tagId);
    }

    public CompletableFuture<TagDto> getByIdAsync(Long tagId){
        return CompletableFuture.supplyAsync(()->tagDao.getById(tagId));
    }

    public int deleteById(Long tagId){
        return tagDao.deleteById(tagId);
    }

    public CompletableFuture<Integer> deleteByIdAsync(Long tagId){
        return CompletableFuture.supplyAsync(()->tagDao.deleteById(tagId));
    }

    public List<TagDto> getAll(){
        return tagDao.getAll();
    }

    public CompletableFuture<List<TagDto>> getAllAsync(){
        return CompletableFuture.supplyAsync(tagDao::getAll);
    }

    public List<TagDto> getByBookId(Long bookId){
        return tagDao.getTagsByBookId(bookId);
    }

    public CompletableFuture<List<TagDto>> getByBookIdAsync(Long bookId){
        return CompletableFuture.supplyAsync(()->tagDao.getTagsByBookId(bookId));
    }

    public List<TagDto> getByIds(List<Long> ids){
        return tagDao.getTagsByIds(ids);
    }

    public CompletableFuture<List<TagDto>> getByIdsAsync(List<Long> ids){
        return CompletableFuture.supplyAsync(()->tagDao.getTagsByIds(ids));
    }



}
