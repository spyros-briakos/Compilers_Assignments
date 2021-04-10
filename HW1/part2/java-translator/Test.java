public class Test {
    public static void main(String[] args) {
        construct_main("PAME");
    }

    static void construct_main(String body_main) {
        String result = "import java.lang.Math;\n\npublic class Main {\n\tpublic static void main(String[] args) {\t\t\n\t\t" + body_main + "\n\t}\n\n}";
        System.out.println(result);  
    }
}
