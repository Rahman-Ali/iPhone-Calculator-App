package com.example.androidcalculator;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Stack;
import java.util.regex.Pattern;
import java.text.DecimalFormat;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private EditText display;
    private StringBuilder inputExpression = new StringBuilder();
    private double memoryValue = 0;
    private boolean isRadMode = true; // true for radians, false for degrees
    private boolean isInSecondMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        display = findViewById(R.id.display);

        setupButtons();
    }

    private void setupButtons() {
        // Numbers
        setupButton(R.id.btn_0, "0");
        setupButton(R.id.btn_1, "1");
        setupButton(R.id.btn_2, "2");
        setupButton(R.id.btn_3, "3");
        setupButton(R.id.btn_4, "4");
        setupButton(R.id.btn_5, "5");
        setupButton(R.id.btn_6, "6");
        setupButton(R.id.btn_7, "7");
        setupButton(R.id.btn_8, "8");
        setupButton(R.id.btn_9, "9");
        setupButton(R.id.btn_decimal, ".");

        // Basic Operations
        setupButton(R.id.btn_add, "+");
        setupButton(R.id.btn_subtract, "-");
        setupButton(R.id.btn_multiply, "*");
        setupButton(R.id.btn_divide, "/");
        findViewById(R.id.btn_percent).setOnClickListener(v -> handlePercentage());
//        setupButton(R.id.btn_percent, "%");
        setupButton(R.id.btn_open_bracket, "(");
        setupButton(R.id.btn_close_bracket, ")");

        // Memory Functions
        try {
            findViewById(R.id.btn_mc).setOnClickListener(v -> {
                memoryValue = 0;
                Toast.makeText(MainActivity.this, "Memory cleared", Toast.LENGTH_SHORT).show();
            });

            findViewById(R.id.btn_m_plus).setOnClickListener(v -> {
                try {
                    double currentValue = evaluateExpression(inputExpression.toString());
                    memoryValue += currentValue;
                    Toast.makeText(MainActivity.this, "Added to memory: " + formatResult(memoryValue), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    showError("Invalid operation for memory addition");
                }
            });

            // Find btn_m_minus if it exists (first layout has it)
            Button mMinusButton = findViewById(R.id.btn_m_minus);
            if (mMinusButton != null) {
                mMinusButton.setOnClickListener(v -> {
                    try {
                        double currentValue = evaluateExpression(inputExpression.toString());
                        memoryValue -= currentValue;
                        Toast.makeText(MainActivity.this, "Subtracted from memory: " + formatResult(memoryValue), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        showError("Invalid operation for memory subtraction");
                    }
                });
            }

            findViewById(R.id.btn_mr).setOnClickListener(v -> {
                if (memoryValue == 0) {
                    Toast.makeText(this, "Memory is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                String memValue = formatResult(memoryValue);
                if (isOperatorLastChar() && inputExpression.length() > 0) {
                    inputExpression.append(memValue);
                } else if (inputExpression.toString().equals("0") || inputExpression.length() == 0) {
                    inputExpression = new StringBuilder(memValue);
                } else {
                    inputExpression.append(memValue);
                }
                updateDisplay();
                Toast.makeText(this, "Recalled: " + memValue, Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            // Handle case where some memory buttons might not exist
        }

        // Scientific Functions
        setupScientificButton(R.id.btn_sin, "sin");
        setupScientificButton(R.id.btn_cos, "cos");
        setupScientificButton(R.id.btn_tan, "tan");
        setupScientificButton(R.id.btn_ln, "ln");
        setupScientificButton(R.id.btn_log, "log");
        setupScientificButton(R.id.btn_sqrt, "√");
        setupScientificButton(R.id.btn_cube_root, "∛");
        setupScientificButton(R.id.btn_factorial, "!");
        setupScientificButton(R.id.btn_sinh, "sinh");
        setupScientificButton(R.id.btn_cosh, "cosh");
        setupScientificButton(R.id.btn_tanh, "tanh");
        setupScientificButton(R.id.btn_y_root, "yroot");
        setupScientificButton(R.id.btn_square, "^2");
        setupScientificButton(R.id.btn_cube, "^3");
        setupScientificButton(R.id.btn_power, "^");
        setupScientificButton(R.id.btn_reciprocal, "1/");

        // Constants and Special Functions
        try {
            findViewById(R.id.btn_pi).setOnClickListener(v -> {
                if (shouldReplaceEntireExpression()) {
                    inputExpression = new StringBuilder("3.14159265359");
                } else {
                    inputExpression.append("3.14159265359");
                }
                updateDisplay();
            });

            findViewById(R.id.btn_e).setOnClickListener(v -> {
                if (shouldReplaceEntireExpression()) {
                    inputExpression = new StringBuilder("2.718281828459");
                } else {
                    inputExpression.append("2.718281828459");
                }
                updateDisplay();
            });

            findViewById(R.id.btn_exp).setOnClickListener(v -> {
                try {
                    if (inputExpression.length() > 0 && !isOperatorLastChar()) {
                        double val = evaluateExpression(inputExpression.toString());
                        double result = Math.exp(val);
                        inputExpression = new StringBuilder(formatResult(result));
                        updateDisplay();
                    } else {
                        showError("Enter a value first");
                    }
                } catch (Exception e) {
                    showError("Invalid operation");
                }
            });

            findViewById(R.id.btn_ten_power).setOnClickListener(v -> {
                try {
                    if (inputExpression.length() > 0 && !isOperatorLastChar()) {
                        double val = evaluateExpression(inputExpression.toString());
                        double result = Math.pow(10, val);
                        inputExpression = new StringBuilder(formatResult(result));
                        updateDisplay();
                    } else {
                        showError("Enter a value first");
                    }
                } catch (Exception e) {
                    showError("Invalid operation");
                }
            });

            findViewById(R.id.btn_ee).setOnClickListener(v -> {
                String currentText = inputExpression.toString();

                if (currentText.length() > 0 &&
                        !isOperatorLastChar() &&
                        !currentText.matches(".*[0-9]E[0-9]*$")) { // No existing 'E' in the number

                    inputExpression.append("E");
                    updateDisplay();
                }
                else if (currentText.isEmpty()) {
                    showError("Enter a value first");
                }
                else {
                    showError("Invalid position for scientific notation");
                }
            });

            findViewById(R.id.btn_rand).setOnClickListener(v -> {
                double randomValue = new Random().nextDouble();
                if (shouldReplaceEntireExpression()) {
                    inputExpression = new StringBuilder(formatResult(randomValue));
                } else {
                    inputExpression.append(formatResult(randomValue));
                }
                updateDisplay();
            });

            findViewById(R.id.btn_rad).setOnClickListener(v -> {
                isRadMode = !isRadMode;
                Button radButton = findViewById(R.id.btn_rad);
                radButton.setText(isRadMode ? "Rad" : "Deg");
                Toast.makeText(MainActivity.this, isRadMode ? "Radian mode" : "Degree mode", Toast.LENGTH_SHORT).show();
            });

            findViewById(R.id.btn_second).setOnClickListener(v -> {
                isInSecondMode = !isInSecondMode;
                Toast.makeText(MainActivity.this, isInSecondMode ? "2nd functions enabled" : "Standard functions", Toast.LENGTH_SHORT).show();
                // Here you could change button labels to show inverse functions
            });


            // Initialize the button in onCreate/onCreateView
            Button buttonBracket = findViewById(R.id.buttonBracket); // or view.findViewById in Fragment

// Set click listener
            buttonBracket.setOnClickListener(v->{
                    handleBackspace();


            });

        } catch (Exception e) {
            // Handle case where some buttons might not exist depending on layout
        }

        // Clear and Equals
        findViewById(R.id.btn_ac).setOnClickListener(v -> {
            inputExpression = new StringBuilder("0");
            updateDisplay();
        });

        findViewById(R.id.btn_equals).setOnClickListener(v -> {
            try {
                if (inputExpression.length() > 0) {
                    String expression = inputExpression.toString();
                    double result = evaluateExpression(expression);
                    inputExpression = new StringBuilder(formatResult(result));
                    updateDisplay();
                }
            } catch (ArithmeticException e) {
                showError("Division by zero");
            } catch (Exception e) {
                showError("Invalid expression");
            }
        });
    }
    private void handleBackspace() {
        // Get current text from both the display and inputExpression
        String displayText = display.getText().toString();
        String expressionText = inputExpression.toString();

        // Determine which one has content (they should match, but we'll be safe)
        String currentText = !displayText.isEmpty() ? displayText : expressionText;

        if (!currentText.isEmpty()) {
            // Remove last character from both sources
            String newText = currentText.substring(0, currentText.length() - 1);

            // Handle empty case
            if (newText.isEmpty()) {
                newText = "0";
            }

            // Update both the display and inputExpression
            display.setText(newText);
            display.setSelection(newText.length());
            inputExpression = new StringBuilder(newText);
        }
    }

    // Add this new percentage function (place it near the other operation functions):
    private void handlePercentage() {
        try {
            if (inputExpression.length() > 0 && !isOperatorLastChar()) {
                // Evaluate the current expression and divide by 100
                double currentValue = evaluateExpression(inputExpression.toString());
                double percentageValue = currentValue / 100;
                inputExpression = new StringBuilder(formatResult(percentageValue));
                updateDisplay();
            } else {
                showError("Enter a value first");
            }
        } catch (Exception e) {
            showError("Invalid operation");
        }
    }


    private void setupButton(int id, String value) {
        try {
            findViewById(id).setOnClickListener(v -> {
                // Handle special case for initial "0"
                if (inputExpression.toString().equals("0") && !value.equals(".")) {
                    inputExpression = new StringBuilder(value);
                } else if (value.matches("[+\\-*/%]")) {
                    // Operator should be added only if the last character is not an operator
                    if (inputExpression.length() > 0 && !isOperatorLastChar()) {
                        inputExpression.append(value);
                    } else if (value.equals("-") && (inputExpression.length() == 0 ||
                            inputExpression.charAt(inputExpression.length() - 1) == '(')) {
                        // Allow minus sign at the beginning or after an open bracket
                        inputExpression.append(value);
                    }
                } else {
                    inputExpression.append(value);
                }

                // Now check for invalid "()"
                String str = inputExpression.toString();
                boolean invalid = false;
                for (int i = 0; i < str.length() - 1; ++i) {
                    if (str.charAt(i) == '(' && str.charAt(i + 1) == ')') {
                        invalid = true;
                        break;
                    }
                }

                if (invalid) {
                    showError("Enter valid input");
                    // Only delete last character if invalid
                    if (inputExpression.length() > 0) {
                        inputExpression.deleteCharAt(inputExpression.length() - 1);
                    }
                }

                updateDisplay();
            });
        } catch (Exception e) {
            // Button may not exist in both layouts
        }
    }

    private boolean isFunction(String func)
    {
        return (func=="sin"||func=="cos"||func=="tan"||func=="sinh"||func=="cosh"||func=="tanh"||func=="ln"||func=="log"||func=="√"||func=="∛" ||func=="^2"||func=="^3"||func=="^"||func=="1/"||func=="yroot");
    }
    private void setupScientificButton(int id, String function) {
        try {
            findViewById(id).setOnClickListener(v -> {

                switch (function) {
                    case "sin":
                    case "cos":
                    case "tan":
                    case "sinh":
                    case "cosh":
                    case "tanh":
                    case "ln":
                    case "log":
                    case "√":
                    case "∛":
                        String str = inputExpression.toString();
                        if (str.length() > 0) {
                            char ch = str.charAt(str.length() - 1);
                            if (!(ch == '(' || ch == '+' || ch == '-' || ch == '*' || ch == '/')) {
                                inputExpression = new StringBuilder("");
                            }
                        }
                        if (shouldReplaceEntireExpression()) {

                            inputExpression = new StringBuilder(function + "(");
                        } else {
                            inputExpression.append(function + "(");
                        }
                        break;
                    case "!":
                        if (!isOperatorLastChar() && inputExpression.length() > 0) {
                            // Check if we need to add parentheses
                            if (needsParentheses(inputExpression.toString())) {
                                inputExpression.append(")!");
                            } else {
                                inputExpression.append("!");
                            }
                        }
                        break;
                    case "^2":
                        if (!isOperatorLastChar() && inputExpression.length() > 0) {
                            // Check if we need to add parentheses
                            if (needsParentheses(inputExpression.toString())) {
                                inputExpression.append(")^2");
                            } else {
                                inputExpression.append("^2");
                            }
                        }
                        break;
                    case "^3":
                        if (!isOperatorLastChar() && inputExpression.length() > 0) {
                            // Check if we need to add parentheses
                            if (needsParentheses(inputExpression.toString())) {
                                inputExpression.append(")^3");
                            } else {
                                inputExpression.append("^3");
                            }
                        }
                        break;
                    case "^":
                        if (!isOperatorLastChar() && inputExpression.length() > 0) {
                            inputExpression.append("^");
                        }
                        break;
                    case "1/":
                        if (shouldReplaceEntireExpression()) {
                            inputExpression = new StringBuilder("1/(");
                        } else {
                            inputExpression.append("1/(");
                        }
                        break;
                    case "yroot":
                        if (!isOperatorLastChar() && inputExpression.length() > 0) {
                            inputExpression.append("^(1/");
                        }
                        break;
                }
                updateDisplay();
            });
        } catch (Exception e) {
            // Button may not exist in both layouts
        }
    }

    private boolean shouldReplaceEntireExpression() {
        return inputExpression.toString().equals("0") ||
                inputExpression.length() == 0 ||
                (inputExpression.length() == 1 && isOperatorLastChar());
    }

    private boolean isOperatorLastChar() {
        if (inputExpression.length() == 0) return false;
        char lastChar = inputExpression.charAt(inputExpression.length() - 1);
        return "+-*/^%(.".indexOf(lastChar) != -1;
    }

    private boolean needsParentheses(String expr) {
        // Check if the expression ends with a function call that needs closing
        return expr.matches(".*(?:sin|cos|tan|ln|log|√|∛|sinh|cosh|tanh)\\([^)]*$");
    }

    private void updateDisplay() {
        display.setText(inputExpression.toString());
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String formatResult(double result) {
        if (Double.isInfinite(result)) {
            return "Infinity";
        }

        if (Double.isNaN(result)) {
            return "Error";
        }

        // For very large or very small numbers, use scientific notation
        if (Math.abs(result) > 1e10 || (Math.abs(result) < 1e-10 && result != 0)) {
            return String.format("%e", result);
        }

        // Format with up to 10 decimal places, removing trailing zeros
        DecimalFormat df = new DecimalFormat("0.##########");
        df.setMinimumFractionDigits(0);
        return df.format(result);
    }

    private double evaluateExpression(String expression) {
        // Replace custom scientific functions and constants with math operations
        expression = preprocessExpression(expression);

        // Use a recursive descent parser to evaluate the expression
        return evaluate(expression);
    }

    private String preprocessExpression(String expression) {
        // Replace scientific functions with their calculated values
        StringBuilder processed = new StringBuilder(expression);

        // Process factorial operations
        int factIndex = processed.indexOf("!");
        while (factIndex != -1) {
            // Find the start of the number before !
            int start = factIndex - 1;
            while (start >= 0 && (Character.isDigit(processed.charAt(start)) ||
                    processed.charAt(start) == '.' || processed.charAt(start) == ')')) {
                start--;
            }
            start++;

            String numStr = processed.substring(start, factIndex);
            if (numStr.endsWith(")")) {
                // Find matching opening bracket
                int depth = 1;
                int openBracket = factIndex - 2;
                while (openBracket >= 0 && depth > 0) {
                    if (processed.charAt(openBracket) == ')') depth++;
                    else if (processed.charAt(openBracket) == '(') depth--;
                    openBracket--;
                }
                openBracket++;

                // Evaluate the content inside brackets
                String innerExpr = processed.substring(openBracket + 1, factIndex - 1);
                double innerResult = evaluate(preprocessExpression(innerExpr));
                if (innerResult < 0 || innerResult != Math.floor(innerResult)) {
                    throw new ArithmeticException("Factorial requires non-negative integer");
                }

                // Calculate factorial
                double factorial = calculateFactorial((int)innerResult);
                processed.replace(openBracket, factIndex + 1, String.valueOf(factorial));
            } else {
                // Simple number for factorial
                double num = Double.parseDouble(numStr);
                if (num < 0 || num != Math.floor(num)) {
                    throw new ArithmeticException("Factorial requires non-negative integer");
                }

                // Calculate factorial
                double factorial = calculateFactorial((int)num);
                processed.replace(start, factIndex + 1, String.valueOf(factorial));
            }

            factIndex = processed.indexOf("!");
        }

        // Process all trigonometric and other functions
        processed = new StringBuilder(processFunctions(processed.toString()));

        // Process power operations ^
        int powerIndex = processed.indexOf("^");
        while (powerIndex != -1) {
            // Find base (number before ^)
            int baseStart = powerIndex - 1;
            while (baseStart >= 0 && (Character.isDigit(processed.charAt(baseStart)) ||
                    processed.charAt(baseStart) == '.' || processed.charAt(baseStart) == ')')) {
                baseStart--;
            }
            baseStart++;

            // Find exponent (number after ^)
            int expEnd = powerIndex + 1;
            if (expEnd < processed.length() && processed.charAt(expEnd) == '(') {
                // If exponent is in brackets, find closing bracket
                int depth = 1;
                expEnd++;
                while (expEnd < processed.length() && depth > 0) {
                    if (processed.charAt(expEnd) == '(') depth++;
                    else if (processed.charAt(expEnd) == ')') depth--;
                    expEnd++;
                }
            } else {
                // Otherwise find the end of the number
                while (expEnd < processed.length() && (Character.isDigit(processed.charAt(expEnd)) ||
                        processed.charAt(expEnd) == '.')) {
                    expEnd++;
                }
            }

            String baseStr = processed.substring(baseStart, powerIndex);
            String expStr = processed.substring(powerIndex + 1, expEnd);

            double base;
            if (baseStr.endsWith(")")) {
                // Find matching opening bracket for base
                int depth = 1;
                int openBracket = powerIndex - 2;
                while (openBracket >= 0 && depth > 0) {
                    if (processed.charAt(openBracket) == ')') depth++;
                    else if (processed.charAt(openBracket) == '(') depth--;
                    openBracket--;
                }
                openBracket++;

                // Evaluate the content inside brackets
                String innerExpr = processed.substring(openBracket + 1, powerIndex - 1);
                base = evaluate(preprocessExpression(innerExpr));
                baseStart = openBracket;
            } else {
                base = Double.parseDouble(baseStr);
            }

            double exponent;
            if (expStr.startsWith("(") && expStr.endsWith(")")) {
                // Evaluate the expression inside brackets
                String innerExpr = expStr.substring(1, expStr.length() - 1);
                exponent = evaluate(preprocessExpression(innerExpr));
            } else {
                exponent = Double.parseDouble(expStr);
            }

            double result = Math.pow(base, exponent);
            processed.replace(baseStart, expEnd, String.valueOf(result));

            powerIndex = processed.indexOf("^");
        }

        return processed.toString();
    }

    private String processFunctions(String expr) {
        // Process all function calls
        String[] functions = {"sin", "cos", "tan", "sinh", "cosh", "tanh", "ln", "log", "√", "∛"};

        for (String func : functions) {
            int funcIndex = expr.indexOf(func + "(");
            while (funcIndex != -1) {
                // Find the matching closing bracket
                int openBracket = funcIndex + func.length();
                int depth = 1;
                int closeBracket = openBracket + 1;

                while (closeBracket < expr.length() && depth > 0) {
                    if (expr.charAt(closeBracket) == '(') {
                        depth++;
                    } else if (expr.charAt(closeBracket) == ')') {
                        depth--;
                    }
                    closeBracket++;
                }

                if (depth == 0) {
                    closeBracket--; // Point to the actual closing bracket

                    // Extract and evaluate the argument
                    String argument = expr.substring(openBracket + 1, closeBracket);
                    double argValue = evaluate(preprocessExpression(argument));

                    // Apply the function
                    double result = applyFunction(func, argValue);

                    // Replace the function call with its result
                    expr = expr.substring(0, funcIndex) + result + expr.substring(closeBracket + 1);
                } else {
                    throw new IllegalArgumentException("Mismatched brackets in function call: " + func);
                }

                funcIndex = expr.indexOf(func + "(");
            }
        }

        return expr;
    }

    private double applyFunction(String function, double arg) {
        switch (function) {
            case "sin":
                return isRadMode ? Math.sin(arg) : Math.sin(Math.toRadians(arg));
            case "cos":
                return isRadMode ? Math.cos(arg) : Math.cos(Math.toRadians(arg));
            case "tan":
                return isRadMode ? Math.tan(arg) : Math.tan(Math.toRadians(arg));
            case "sinh":
                return Math.sinh(arg);
            case "cosh":
                return Math.cosh(arg);
            case "tanh":
                return Math.tanh(arg);
            case "ln":
                if (arg <= 0) throw new ArithmeticException("Cannot take ln of non-positive number");
                return Math.log(arg);
            case "log": // log10
                if (arg <= 0) throw new ArithmeticException("Cannot take log of non-positive number");
                return Math.log10(arg);
            case "√": // square root
                if (arg < 0) throw new ArithmeticException("Cannot take square root of negative number");
                return Math.sqrt(arg);
            case "∛": // cube root
                return Math.cbrt(arg);
            default:
                throw new IllegalArgumentException("Unknown function: " + function);
        }
    }

    private double calculateFactorial(int n) {
        if (n > 170) {
            return Double.POSITIVE_INFINITY; // Factorial grows very quickly
        }
        double result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    private double evaluate(String expression) {
        // Remove all spaces
        expression = expression.replaceAll("\\s+", "");

        // Handle empty expression
        if (TextUtils.isEmpty(expression)) {
            return 0;
        }

        // Create a stack for operands and operators
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (c == ' ') {
                continue;
            } else if (c == '(') {
                operators.push(c);
            } else if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();

                // Get the full number including decimals and scientific notation
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) ||
                        expression.charAt(i) == '.' ||
                        expression.charAt(i) == 'E' ||
                        (expression.charAt(i) == '-' && i > 0 && expression.charAt(i-1) == 'E') ||
                        (expression.charAt(i) == '+' && i > 0 && expression.charAt(i-1) == 'E'))) {
                    sb.append(expression.charAt(i++));
                }
                i--; // Move back one position

                // Parse the number and push to stack
                values.push(Double.parseDouble(sb.toString()));
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }

                if (!operators.isEmpty() && operators.peek() == '(') {
                    operators.pop(); // Remove '('
                } else {
                    throw new IllegalArgumentException("Mismatched brackets");
                }
            } else if (isOperator(c)) {
                // Handle unary minus
                if (c == '-' && (i == 0 || expression.charAt(i - 1) == '(' || isOperator(expression.charAt(i - 1)))) {
                    values.push(0.0);
                }

                // While top of operator stack has higher or equal precedence, apply operators
                while (!operators.isEmpty() && precedence(c) <= precedence(operators.peek())) {
                    if (operators.peek() == '(') break;
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }

                // Push current operator
                operators.push(c);
            }
        }

        // Apply all remaining operators
        while (!operators.isEmpty()) {
            if (operators.peek() == '(') {
                throw new IllegalArgumentException("Mismatched brackets");
            }
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }

        // Result is the top of values stack
        if (values.isEmpty()) {
            return 0;
        }
        return values.pop();
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%';
    }

    private int precedence(char op) {
        switch (op) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
            case '%':
                return 2;
            default:
                return -1;
        }
    }

    private double applyOperator(char op, double b, double a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) throw new ArithmeticException("Division by zero");
                return a / b;
            case '%':
                if (b == 0) throw new ArithmeticException("Division by zero");
                return a % b;
            default:
                return 0;
        }
    }
}