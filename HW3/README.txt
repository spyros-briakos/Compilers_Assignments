Μπριάκος Σπυρίδων 1115201700101 

Αφού τοποθετηθούν τα javacc5.jar και jtb132di.jar αρχεία στον κεντρικό folder HW3/, από τον ίδιο folder:

1) Compile: make 

2) Execute: java Main filepath.java
            clang -o out filepath.ll 
            ./out

3) Clean:   make clean

Η εργασία αποτελείται από: 
- minijava-examples/, όπου υπάρχουν test cases που χρησιμοποιήθηκαν για να ελέγξω αν όλα λειτουργούν με σωστό τρόπο.
- llvm-examples/, ομοίως με παραπάνω
- src/, όπου υπάρχουν όλα τα .java αρχεία που δημιουργήθηκαν για την εν λόγω εργασία (προστέθηκε LLVM_Visitor.java στο κώδικα της 2ης εργασίας)
- javacc5.jar
- jtb132di.jar
- Main.java (σχολιάστηκε ο Visitor που έλεγχε για σημασιολογικά λάθη και στη θέση του μπήκε ο LLVM_Visitor)
- makefile
- minijava.jj
- README.txt
- script.sh

Μπορείτε να τρέξετε από τον κεντρικό folder ούτως ώστε να τρέξουν όλα αυτόματα (θα βγει το output του clang στο terminal) με τον παρακάτω τρόπο: 
./script.sh filepath
Για παράδειγμα: ./script.sh llvm_examples/classes/Classes.java
Σημείωση: Το .ll file παράγεται στο ακριβώς ίδιο path με το filepath, με το ίδιο όνομα. Για το παραπάνω παράδειγμα θα δημιουργηθεί llvm_examples/classes/Classes.ll