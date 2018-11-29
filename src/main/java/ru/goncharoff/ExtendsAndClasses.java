package ru.goncharoff;

import javafx.util.Pair;
import org.apache.hadoop.io.Text;

import java.util.ArrayList;
import java.util.List;

public class ExtendsAndClasses {

    private List<String> nameClassArr = new ArrayList<>();

    private List<String> nameExtendsClassArr = new ArrayList<>();

    public void addPair(final String nameClass, final String nameExtendsClass) {
        nameClassArr.add(nameClass);
        nameExtendsClassArr.add(nameExtendsClass);
    }

    public List<Pair<String,String>> getPairs() {
        final List<Pair<String,String>> resp = new ArrayList<>();

        for (int i = 0; i < nameClassArr.size(); i++) {
            resp.add(new Pair<>(nameClassArr.get(i), nameExtendsClassArr.get(i)));
        }

        return resp;
    }


    public static void main(String[] args) {
        Text text = new Text("awwww");
        System.out.println(text);
        text.append("bd".getBytes(), 0, 1);
        System.out.println(text);
    }






}
