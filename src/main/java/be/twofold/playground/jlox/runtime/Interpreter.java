package be.twofold.playground.jlox.runtime;

import be.twofold.playground.jlox.*;
import be.twofold.playground.jlox.ast.*;
import be.twofold.playground.jlox.model.*;
import be.twofold.playground.jlox.parser.*;

import java.util.*;

public class Interpreter implements Expr.Visitor<LoxValue> {
    final Environment globals = new Environment();

    public Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public LoxValue call(Interpreter interpreter, List<LoxValue> arguments) {
                return LoxValue.number(System.currentTimeMillis() / 1000.0);
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    public void interpret(Expr expr) {
        try {
            LoxValue result = evaluate(expr);
            System.out.println(result);
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private LoxValue evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public LoxValue visitBinaryExpr(Expr.Binary expr) {
        LoxValue left = evaluate(expr.left);
        LoxValue right = evaluate(expr.right);

        switch (expr.operator.getType()) {
            case PLUS:
                if (left.isNumber() && right.isNumber()) {
                    return LoxValue.number(left.asNumber() + right.asNumber());
                }
                if (left.isString() && right.isString()) {
                    return LoxValue.string(left.asString() + right.asString());
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return LoxValue.number(left.asNumber() - right.asNumber());
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return LoxValue.number(left.asNumber() / right.asNumber());
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return LoxValue.number(left.asNumber() * right.asNumber());
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return LoxValue.bool(left.asNumber() > right.asNumber());
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return LoxValue.bool(left.asNumber() >= right.asNumber());
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return LoxValue.bool(left.asNumber() < right.asNumber());
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return LoxValue.bool(left.asNumber() <= right.asNumber());
            case BANG_EQUAL:
                return LoxValue.bool(!isEqual(left, right));
            case EQUAL_EQUAL:
                return LoxValue.bool(isEqual(left, right));
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public LoxValue visitCallExpr(Expr.Call expr) {
        LoxValue callee = evaluate(expr.callee);

        List<LoxValue> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    @Override
    public LoxValue visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public LoxValue visitLiteralExpr(Expr.Literal expr) {
        if (expr.value instanceof String) {
            return LoxValue.string((String) expr.value);
        } else if (expr.value instanceof Double) {
            return LoxValue.number((Double) expr.value);
        } else if (expr.value instanceof Boolean) {
            return LoxValue.bool((Boolean) expr.value);
        } else if (expr.value == null) {
            return LoxValue.nil();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public LoxValue visitLogicalExpr(Expr.Logical expr) {
        LoxValue left = evaluate(expr.left);

        if (expr.operator.getType() == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public LoxValue visitUnaryExpr(Expr.Unary expr) {
        LoxValue right = evaluate(expr.right);

        switch (expr.operator.getType()) {
            case BANG:
                return LoxValue.bool(!isTruthy(right));
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return LoxValue.number(-right.asNumber());
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public LoxValue visitVariableExpr(Expr.Variable expr) {
        return globals.get(expr.name);
    }

    private void checkNumberOperand(Token operator, LoxValue operand) {
        if (operand.isNumber()) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, LoxValue left, LoxValue right) {
        if (left.isNumber() && right.isNumber()) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(LoxValue object) {
        if (object.isNil()) return false;
        if (object.isBool()) return object.asBool();
        return true;
    }

    private boolean isEqual(LoxValue a, LoxValue b) {
        if (a.isNil() && b.isNil()) return true;
        if (a.isNil()) return false;
        return a.equals(b);
    }

}
