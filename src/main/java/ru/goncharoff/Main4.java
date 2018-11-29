package ru.goncharoff;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main4 {

    private static Pattern pattern = Pattern.compile("(.*?)(class|interface)(.*?)(extends|implements)(\\s\\w+)(.*?)");

    private static Map<String, List<String>> classToHisExtended = new HashMap<>();


    public static void main(String[] args) throws IOException, InterruptedException {

        List<Thread> threads =  Files.walk(Paths.get("data"))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(".java"))
                .collect(Collectors.toList())
                .stream()
                .map(fileToMather())
                .map(matcher -> new Thread(() -> {

                    while(matcher.find()) {
                    
                        saveInMap(matcher.group(3).trim(), matcher.group(5).trim());
                    }
                }))
                .collect(Collectors.toList());

        threads.forEach(Thread::start);

        for (Thread thread: threads) {
            thread.join();
        }


        classToHisExtended
                .entrySet()
                .stream()
                .filter((entry) -> entry.getValue().size() >= 1)
                .forEach(System.out::println);



    }

    private synchronized static void saveInMap(String nameClass, String nameExtendsClass) {
        if (classToHisExtended.containsKey(nameExtendsClass)) {
            classToHisExtended.get(nameExtendsClass).add(nameClass);
        } else {
            classToHisExtended.putIfAbsent(nameExtendsClass,
                    new ArrayList<>(Arrays.asList(nameClass)));
        }
    }

    private static Function<File, Matcher> fileToMather() {
        return new Function<File, Matcher>() {
            @Override
            public Matcher apply(File file) {
                try {
                    return pattern.matcher(new String(Files.readAllBytes(file.toPath())));
                }  catch (IOException exception){
                    return pattern.matcher("");
                }
            }

            @Override
            public <V> Function<V, Matcher> compose(Function<? super V, ? extends File> function) {
                return null;
            }

            @Override
            public <V> Function<File, V> andThen(Function<? super Matcher, ? extends V> function) {
                return null;
            }
        };
    }
}
