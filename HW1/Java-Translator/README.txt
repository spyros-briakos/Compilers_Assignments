##### Instructions #####

> Add .jar files inside main directory (Java-Translator/)
> make compile && make execute
> Insert your example
> Press \n (enter) and then once or twice Ctrl+D
> Copy generated Java code 
>> Paste it to an empty Main.java file
>> javac Main.java && java Main
OR 
>> Paste it to online Java compiler https://replit.com/languages/java
> Check for result ✅

##### Notes #####

~ Basic problem: distinguish function declaration from function call, idea by professor
                => resolved with MY_TOKEN = "){" 

~ Small problem: distinguish arguments of top-level call and inner-function call 
                => resolved with double code (2 seperate non terminals top_call & inner_call and expr & inner_expr)

~ If_else: resolved with implementation of function if_else(cond c, String s1, String s2) which returns ternary expression (c?s1:s2)

~ Condition: could be expr PREFIX|SUFFIX expr, where I utilize java functions startsWith() and endsWith()

~ The only precedence definitions that translator needed were precedence(if) < precedence(concat)

~ I did use as initial template instructors code and try to fix my java translator, which then I tested it with several cases from Piazza

~ Program starts with this non-terminal -> program ::= func_decs:e1 exprs:e2, so keep in mind that in input language function declarations precede all function calls.