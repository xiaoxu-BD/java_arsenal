package org.xiaoxu.utils;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @className: Result
 * @author: xiaoxu
 * @date: 2025/11/18 10:20
 * @Version: 1.0
 * @description:
 */
@Getter
@Setter
@ToString
public class Result<T>{



    private String msg;
    private Integer code;
    private Long timeStamp;
    private T data;



    public Result(){
        this.timeStamp = System.currentTimeMillis();
    }


    public static <T> Result<T> success(T data){
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setData(data);
        result.setMsg("Success");
        return  result;
    }

    public static <T> Result<T> success(){
        return success(null);
    }


    public static <T> Result<T> success(String message,T data){
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setData(data);
        result.setMsg(message);
        return  result;
    }


    public static <T> Result<T> error(){
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg("system ERROR");
        return  result;
    }

    public static <T> Result<T> error(Integer code,String message){
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(message);
        return  result;
    }



}
