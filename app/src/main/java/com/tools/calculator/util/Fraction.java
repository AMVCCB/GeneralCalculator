package com.tools.calculator.util;

import com.tools.calculator.util.fractionutil.EvaluateConfig;
import com.tools.calculator.util.fractionutil.ExpressionTokenizer;
import com.tools.calculator.util.fractionutil.FormatExpression;

import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

public class Fraction {
    ExpressionTokenizer mTokenizer = new ExpressionTokenizer();
    ExprEvaluator mExprEvaluator = new ExprEvaluator();

    public String evaluateWithResultAsTex(String exprStr, EvaluateConfig config) {
        if (exprStr.isEmpty()) {
            return exprStr;
        }
        exprStr = FormatExpression.cleanExpression(exprStr, mTokenizer);
        IExpr result = evalStrSimple(exprStr, config);
        String res = result.toString();
        res = formatResult(res);
        return res;
    }

    public IExpr evalStrSimple(String exprInput, EvaluateConfig config) {
        IExpr result;
        result = evalStr(exprInput);
        return result;
    }

    public IExpr evalStr(String exprInput) {
        return mExprEvaluator.eval(exprInput);
    }

    private String formatResult(String res) {
        String cleanRes = res.replace("Sqrt", "√");
        cleanRes = cleanRes.replace("sqrt", "√");
        cleanRes = cleanRes.replace("*", "×");

        cleanRes = cleanRes.replace("Sin", "sin");
        cleanRes = cleanRes.replace("Cos", "cos");
        cleanRes = cleanRes.replace("Tan", "tan");

        cleanRes = cleanRes.replace("Csc", "csc");
        cleanRes = cleanRes.replace("Sec", "sec");
        cleanRes = cleanRes.replace("Cot", "cot");

        cleanRes = cleanRes.replace("Sinh", "sinh");
        cleanRes = cleanRes.replace("Cosh", "cosh");
        cleanRes = cleanRes.replace("Tanh", "tanh");

        cleanRes = cleanRes.replace("Csch", "csch");
        cleanRes = cleanRes.replace("Sech", "sech");
        cleanRes = cleanRes.replace("Coth", "coth");

        cleanRes = cleanRes.replace("arcsi", "sin⁻¹");
        cleanRes = cleanRes.replace("arcco", "cos⁻¹");
        cleanRes = cleanRes.replace("arcta", "tan⁻¹");

        cleanRes = cleanRes.replace("arcsh", "sinh⁻¹");
        cleanRes = cleanRes.replace("arcch", "cosh⁻¹");
        cleanRes = cleanRes.replace("arcth", "tanh⁻¹");

        cleanRes = cleanRes.replace("Log", "ln");
        cleanRes = cleanRes.replace("logten", "log");
        cleanRes = cleanRes.replace("cbrt", "∛");
        cleanRes = cleanRes.replace("Pi", "π");

        return cleanRes;
    }
}