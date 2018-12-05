package ru.goncharoff;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main6 {
    private static Pattern pattern = Pattern.compile("(.*?)(class|interface)(.*?)(extends|implements)(\\s\\w+)(.*?)");

    private static Map<String, List<String>> classToHisExtended = new HashMap<>();


    public static void main(String[] args) throws Exception {

        final int availableProcessors = Runtime.getRuntime().availableProcessors();

        final int sizeQueue = 20;

        final BlockingQueue<Runnable> callableBlockingQueue =
                new ArrayBlockingQueue<>(sizeQueue);

        final ThreadPoolExecutor poolExecutor =
                new ThreadPoolExecutor(
                        4,
                        4,
                        10,
                        TimeUnit.SECONDS,
                        callableBlockingQueue);

        poolExecutor.prestartAllCoreThreads();

        Files.walk(Paths.get("data"))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(".java"))
                .map(fileToMather())
                .forEach(matcher -> {
                    try {
                        callableBlockingQueue.put(() -> {
                            while (matcher.find()) {
                                saveInMap(matcher.group(3).trim(), matcher.group(5).trim());
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });


        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (callableBlockingQueue.isEmpty()) {
                    poolExecutor.shutdown();

                    classToHisExtended
                            .entrySet()
                            .stream()
                            .filter((entry) -> entry.getValue().size() >= 1)
                            .forEach(System.out::println);
                    timer.purge();
                    timer.cancel();
                    System.exit(0);
                }
            }
        }, 0, 3000);


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
