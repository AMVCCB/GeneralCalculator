package com.tools.calculator.util.fractionutil;

import java.util.ArrayList;

public class ExpressionTokenizer {
    private final ArrayList<Localize> mReplacements;

    public ExpressionTokenizer() {
        mReplacements = new ArrayList<>();
    }

    private void generateReplacements() {
        mReplacements.clear();
        mReplacements.add(new Localize("/", "÷"));
        mReplacements.add(new Localize("*", "×"));
        mReplacements.add(new Localize("-", "-"));
        mReplacements.add(new Localize("-", "\u2010"));
        mReplacements.add(new Localize("-", "\u2012"));
        mReplacements.add(new Localize("-", "\u2212"));
        mReplacements.add(new Localize("-", "\u2796"));

        mReplacements.add(new Localize("cbrt", "³√"));
        mReplacements.add(new Localize("infinity", "\u221e"));
        mReplacements.add(new Localize("sqrt", "√"));
        mReplacements.add(new Localize("<=", "≤"));
        mReplacements.add(new Localize(">=", "≥"));
        mReplacements.add(new Localize("!=", "≠"));

        mReplacements.add(new Localize("(pi)", "π"));
        mReplacements.add(new Localize("(degree)", "°"));

        //re translate
        mReplacements.add(new Localize("pi", "π"));
        mReplacements.add(new Localize("degree", "°"));
    }

    public String getNormalExpression(String expr) {
        generateReplacements();
        for (Localize replacement : mReplacements) {
            expr = expr.replace(replacement.local, replacement.english);
        }
        return expr;
    }

    public String getLocalizedExpression(String expr) {
        generateReplacements();
        for (Localize replacement : mReplacements) {
            expr = expr.replace(replacement.english, replacement.local);
        }
        return expr;
    }


    private class Localize {
        String english;
        String local;

        Localize(String english, String local) {
            this.english = english;
            this.local = local;
        }
    }
}