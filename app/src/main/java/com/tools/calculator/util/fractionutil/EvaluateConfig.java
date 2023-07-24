package com.tools.calculator.util.fractionutil;

import android.content.Context;

public class EvaluateConfig implements Cloneable {
    //trig mode
    public static final int DEGREE = 0;
    public static final int RADIAN = 1;
    public static final int GRADIAN = 2;

    //calculate mode
    public static final int DECIMAL = 0;
    public static final int FRACTION = 1;
    private int trigMode = RADIAN;
    private int evalMode = DECIMAL;
    private int roundTo = 10;
    private int[] variableToKeep = new int[]{};

    private EvaluateConfig(EvaluateConfig other) {
        setEvalMode(other.getEvaluateMode());
        setRoundTo(other.getRoundTo());
        setTrigMode(other.getTrigMode());
    }

    private EvaluateConfig() {
    }

    public static EvaluateConfig loadFromSetting(Context c) {
        return CalculatorSetting.createEvaluateConfig(c);
    }

    public static EvaluateConfig newInstance() {
        return new EvaluateConfig();
    }


    public int getTrigMode() {
        return trigMode;
    }

    public EvaluateConfig setTrigMode(int trigMode) {
        this.trigMode = trigMode;
        return this;
    }

    public int getEvaluateMode() {
        return evalMode;
    }

    public EvaluateConfig setEvalMode(int evalMode) {
        this.evalMode = evalMode;
        return this;
    }

    public int getRoundTo() {
        return roundTo;
    }

    public EvaluateConfig setRoundTo(int roundTo) {
        this.roundTo = roundTo;
        return this;
    }
}
