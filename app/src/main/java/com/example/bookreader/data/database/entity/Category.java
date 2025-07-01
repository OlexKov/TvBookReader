package com.example.bookreader.data.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


@NoArgsConstructor                // Потрібен Room для створення через рефлексію
@AllArgsConstructor              // Повний конструктор, якщо треба вручну створювати
@Builder                         // Додає патерн Builder
@Entity(
        tableName = "categories",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "parentId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("parentId")}
)
public class Category {

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public Long parentId = null; // NULL для основних категорій
    public Integer iconId = null;

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category book = (Category) o;
        return id == book.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
