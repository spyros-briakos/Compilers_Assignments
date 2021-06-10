all: compile

compile:
	java -jar ./jtb132di.jar -te minijava.jj
	java -jar ./javacc5.jar minijava-jtb.jj
	
	javac ./src/*.java
	javac Main.java

clean:
	rm -f ./src/*.class *~
	rm -rf ./*.class ./*/*.class syntaxtree/ visitor/ MiniJava*.java Token*.java ParseException.java JavaCharStream.java minijava-jtb.jj out
