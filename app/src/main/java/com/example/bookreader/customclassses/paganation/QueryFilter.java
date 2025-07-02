package com.example.bookreader.customclassses.paganation;

import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class QueryFilter {
   @Setter(AccessLevel.NONE)
   @Getter (AccessLevel.NONE)
   private StringBuilder sb;
   @Setter(AccessLevel.NONE)
   @Getter (AccessLevel.NONE)
   private List<Object> args;


   private Long categoryId;
   private Long inParentCategoryId;
   private String searchName;
   private List<Long> tagIds;
   private String sortBy;
   private Boolean sortDesc;

   private void prebuildQuery(boolean buildPagination){
      sb = new StringBuilder();
      args = new ArrayList<>();
      if(buildPagination){
         sb.append("SELECT b.* FROM books b ");
      }
      else{
         sb.append("SELECT COUNT(*) FROM books b ");
      }

      boolean useTags = this.tagIds != null && !this.tagIds.isEmpty();
      if (useTags) {
         sb.append("JOIN book_tags bt ON b.id = bt.book_id ");
      }

      sb.append("WHERE 1=1 ");

      // categoryId
      if (categoryId != null) {
         if (categoryId < 0) {
            sb.append("AND categoryId IS NULL ");
         } else {
            sb.append("AND categoryId = ? ");
            args.add(categoryId);
         }
      }

      // inParentCategoryId
      if (inParentCategoryId != null) {
         if (inParentCategoryId < 0) {
            sb.append("OR categoryId IN (SELECT id FROM categories WHERE parentId IS NULL) ");
         } else {
            sb.append("OR categoryId IN (SELECT id FROM categories WHERE parentId = ?) ");
            args.add(inParentCategoryId);
         }
      }

      // name
      if (this.searchName != null && !this.searchName.isEmpty()) {
         sb.append("AND name LIKE ? ");
         args.add("%" + searchName + "%");
      }

      // теги
      if (useTags) {
         sb.append("AND bt.tag_id IN (");
         StringJoiner sj = new StringJoiner(", ");
         for (Long tagId : tagIds) {
            sj.add("?");
            args.add(tagId);
         }
         sb.append(sj.toString()).append(") ");
         sb.append("GROUP BY b.id HAVING COUNT(DISTINCT bt.tag_id) = ? ");
         args.add(tagIds.size());
      }

      // сортування (опціонально, додай до фільтра)
      if (this.sortBy != null) {
         sb.append("ORDER BY ").append(this.sortBy);
         if (this.sortDesc != null && this.sortDesc) {
            sb.append(" DESC ");
         } else {
            sb.append(" ASC ");
         }
      }
   }

   public SupportSQLiteQuery buildPagination(int size,int page){
      prebuildQuery(true);
      // пагінація
      sb.append("LIMIT ? OFFSET ? ");
      args.add(size);
      args.add((page - 1) * size);
      return new SimpleSQLiteQuery(sb.toString(), args.toArray());
   }

   public SupportSQLiteQuery buildCount(){
      prebuildQuery(false);
      return new SimpleSQLiteQuery(sb.toString(), args.toArray());
   }
}
