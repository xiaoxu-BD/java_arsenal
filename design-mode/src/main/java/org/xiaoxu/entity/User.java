package org.xiaoxu.entity;


public class User{
    private final Long id;   // 必填
    private final String name; // 必填
    private final Integer age; // 可选
    private final String email;  // 可选

    private User(Builder builder){
        this.id = builder.id;
        this.name = builder.name;
        this.age = builder.age;
        this.email = builder.email;
    }


    public static Builder builder(){
        return new Builder();
    }



    public static class Builder {
        private  Long id;
        private  String name;
        private  Integer age;
        private  String email;


        public Builder(){

        }

        public Builder id (Long id){
            this.id = id;
            return this;
        }

        public Builder name (String name){
            this.name = name;
            return this;
        }
        public Builder age (Integer age){
            this.age = age;
            return this;
        }
        public Builder email (String email){
            this.email = email;
            return this;
        }


        public User build(){
            return new User(this);
        }
    }
}