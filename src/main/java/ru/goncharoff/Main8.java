package ru.goncharoff;


import akka.actor.*;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main8 {

    private static Map<String, List<String>> classToHisExtended = new HashMap<>();


    public static class ParentWithChild implements Serializable {
        private static final long serialVersionUID = 5095931000566324969L;

        public final String parent;

        public final String child;


        public ParentWithChild(String parent, String child) {
            this.parent = parent;
            this.child = child;
        }
    }

    public static class KernelPathActor extends UntypedActor {

        @Override
        public void onReceive(Object o) throws Exception {

            if (o instanceof Path) {
                Path path = (Path) o;

                List<File> files = Files.walk(path)
                        .filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .filter(file -> file.getName().endsWith(".java"))
                        .collect(Collectors.toList());

                for (File file : files) {
                    getContext()
                            .actorOf(new Props(FileActor.class))
                            .tell(file, getSelf());
                }

            } else if (o instanceof ParentWithChild) {
                ParentWithChild parentWithChild = (ParentWithChild) o;

                if (classToHisExtended.containsKey(parentWithChild.parent)) {
                    classToHisExtended.get(parentWithChild.parent).add(parentWithChild.child);
                } else {
                    classToHisExtended.putIfAbsent(parentWithChild.parent,
                            new ArrayList<>(Arrays.asList(parentWithChild.child)));
                }

                getSender().tell(PoisonPill.getInstance(), getSelf());

            }
        }
    }

    public static class FileActor extends UntypedActor {

        private static Pattern pattern = Pattern.compile("(.*?)(class|interface)(.*?)(extends|implements)(\\s\\w+)(.*?)");

        @Override
        public void onReceive(Object o) throws Exception {
            if (o instanceof File) {
                final File file = (File) o;

                Matcher matcher = pattern
                        .matcher(new String(Files
                                .readAllBytes(file
                                        .toPath())));

                while(matcher.find()) {

                    String child = matcher.group(3).trim();

                    String parent = matcher.group(5).trim();

                    getSender().tell(new ParentWithChild(parent, child), getSelf());

                }
            }

        }
    }


    public static void main(String[] args) throws InterruptedException {
        final ActorSystem system = ActorSystem.create("main");
        final ActorRef kernel = system.actorOf(new Props(KernelPathActor.class), "KernelPathActor");
        kernel.tell(Paths.get("/home/goncharoff/save-old-ubuntu/work/find_extends/data"));

        System.out.println("Waiting for the process to finish");
        Thread.sleep(10000L);
        System.out.println(classToHisExtended);
    }

}