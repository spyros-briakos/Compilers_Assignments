import java.io.InputStream;
import java.io.IOException;
import java.lang.Math;

class Calculator {
    private final InputStream in;
    private int lookahead;
    private int index_of_number;

    public Calculator(InputStream in) throws IOException {
        this.in = in;
        lookahead = in.read();
    }

    private void Consume(int symbol) throws IOException, ParseError {
        if (lookahead == symbol)
            lookahead = in.read();
        else
            throw new ParseError();
    }

    private boolean IsDigit(int c) {
        return '0' <= c && c <= '9';
    }

    private int EvalDigit(int c) {
        return c - '0';
    }

    public int Eval() throws IOException, ParseError {
        int value = Exp(0);
        
        if (lookahead != -1 && lookahead != '\n')
            throw new ParseError();
        
        return value;
    }

    private int Exp(int result) throws IOException, ParseError {
        if(lookahead == '+' || lookahead=='-' || lookahead == '*')
            throw new ParseError();
        result = Term(result);
        result = Exp2(result);
        return result;
    }

    private int Exp2(int result) throws IOException, ParseError {
        switch (lookahead) {
            case -1:
                return result;
            
            case '\n':
                return result;

            case ')':
               return result;

            case '+':
                Consume('+');
                result += Term(result);
                result = Exp2(result);
                return result;
                
            case '-':
                Consume('-');
                result -= Term(result);
                result = Exp2(result);
                return result;
            
            default: 
                throw new ParseError();
        }
    }

    private int Term(int result) throws IOException, ParseError {
        if(lookahead == '*')
            throw new ParseError();
        result = Factor(result);
        result = Term2(result); 
        return result;
    }

    private int Term2(int result) throws IOException, ParseError {
        if(IsDigit(lookahead) || lookahead == '(') {
            throw new ParseError();
        }

        if(lookahead == '*') {
            Consume('*');

            if(lookahead != '*') 
                throw new ParseError();

            Consume('*');
            
            int base = result;
            int exponent = Term(result);

            result = pow(base,exponent);
        }

        return result;
    }

    // Function for ** operator (from instructors)
    private static int pow(int base, int exponent) {
        if (exponent < 0)
            return 0;
        else if (exponent == 0)
            return 1;
        if (exponent == 1)
            return base;    
        
        if (exponent % 2 == 0) //even exp -> b ^ exp = (b^2)^(exp/2)
            return pow(base * base, exponent/2);
        else                   //odd exp -> b ^ exp = b * (b^2)^(exp/2)
            return base * pow(base * base, exponent/2);
    }

    private int Factor(int result) throws IOException, ParseError {
        if(lookahead == '(') {
            Consume('(');
            result = Exp(result);

            if (lookahead != ')') {
                throw new ParseError();
            }
            
            Consume(')');
            return result;
        }
        // Case: Num
        else {

            // Do not accept expressions like 1+ or 1-2-...
            if(!IsDigit(lookahead)) {
                throw new ParseError();
            }

            // Use an integer (private member), which shows in which digit of multi digit we are and pass it to function Num()
            index_of_number = 0;
            result = Num();  
            int length;
            
            if(result==0)   length = 1;
            else    length = (int)(Math.log10(result) + 1);
            
            // Don't accept numbers with leading zeroes... i.e. 002 or 03 or 0123
            // index_of_number will contain index of last digit, i.e. for 012 index_of_number will be 3
            // length of result (which will be the original number), i.e. 012 will convert to 12 so length will be 2
            // So as to accept number length must be equal to index_of_number
            
            if(index_of_number!=length) {
                throw new ParseError();  
            }
            return result;
        }
    }

    private int Num() throws IOException, ParseError {
        if(IsDigit(lookahead)) {

            int digit = EvalDigit(lookahead);
            Consume(lookahead);
            int num = Num();

            int multi_digit = ((int)Math.pow(10,index_of_number))*digit + num;
            
            // Next digit please...
            index_of_number++;
            // i.e. 132 => for 2, index_of_number = 0 (2*10**0=2)  \
            //          => for 3, index_of_number = 1 (3*10**1=30)  => 132
            //          => for 1, index_of_number = 2 (1*10**2=100)/

            return multi_digit;
        }  
        else if (lookahead == '('){
            throw new ParseError();
        }
        return 0;
    }
}