package org.example;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        ApiDataRetriever retriever = new ApiDataRetriever();
        System.out.println(retriever.returnNews("PSD"));
    }
}