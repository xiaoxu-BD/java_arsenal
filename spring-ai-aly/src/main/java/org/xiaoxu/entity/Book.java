package org.xiaoxu.entity;


public class Book {
    private String name;

    private String desc;

    private String publisher;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPublisher() {
        return publisher;
    }

    @Override
    public String toString() {
        return "Book{" +
                "name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", publisher='" + publisher + '\'' +
                '}';
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
