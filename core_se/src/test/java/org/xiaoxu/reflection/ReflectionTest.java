package org.xiaoxu.reflection;

import junit.framework.TestCase;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.xiaoxu.entity.Student;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

/**
 * @className: ReflectionTest
 * @author: xiaoxu
 * @date: 2025/5/24 7:55
 * @Version: 1.0
 * @description:
 */
@Slf4j
public class ReflectionTest extends TestCase {


    public void test(){

            Student student = new Student();
            Class<? extends Student> aClass = student.getClass();
            ClassLoader classLoader = aClass.getClassLoader();
            log.info("classLoader:{}",classLoader);

    }

    public void testClassLoader() throws ClassNotFoundException {
        ClassLoader c1 = String.class.getClassLoader();
        System.out.println("加载String类的类加载器：" + c1);

//        ClassLoader c2 = Class.forName("sun.util.resources.cldr.zh.TimeZoneNames_zh").getClassLoader();
//        System.out.println("加载sun.util.resources.cldr.zh.TimeZoneNames_zh类的类加载器：" + c2);

        ClassLoader c3 = ReflectionTest.class.getClassLoader();
        System.out.println("加载当前类的类加载器：" + c3);
    }

    public void testInputStreamAndReflection() throws IOException {
        //方式1：此时默认的相对路径是当前的module
        Properties properties = new Properties();
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        //getResourceAsStream(String str):获取类路径下的指定文件的输入流
        InputStream inputStream= loader.getResourceAsStream("info.properties");

        properties.load(inputStream);

        String value = properties.getProperty("student.name");
        log.info("{}",value);


        properties.forEach((k,v)->log.info("{} : {}",k,v));

    }

    public void testReflection() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Student student = Student.class.newInstance();
        log.info("{}",student);
        Constructor<?>[] constructors = Student.class.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            log.info("{}",constructor);
        }
    }

    public void testReflection2(){
        Class<Student> clazz = Student.class;
        //获取所有的属性
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            log.info("{}",declaredField);
        }

        log.info("===========================");
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            log.info("{}",method);
        }
    }
    public void testStringIds(){
        String ids = "1,2,3,4,5,6,7,8,9,10";
        Student student = new Student();
        student.setId(ids);
        List<String> strings = student.convertToList(ids);
        log.info("{}",strings);

    }

    public void testAnnotation(){

    }
}
