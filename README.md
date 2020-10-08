## **Application "Payments"**


#### **Application structure**

_**Main**_ - the object that launches the ActorSystem. It is the entry point to the program.

_**PaymentsReader**_ - the class that starts stream to read lines from files in a directory. The directory and file mask are set in application.conf.

**_PaymentsChecker_** - the actor which receives the line with the payment and checks it for correctness. The payment mask is specified in application.conf.

_**PaymentParticipant**_ - the actor representing one payment participant (sender or recipient). Actor processes the payment: transfers or receives money, and also rolls back the transaction in case of insufficient balance.

**_LogIncorrectPayment_** - the actor which displays a message in the log stating that the payment line is invalid.

#### **Running the application**
**In the console:**
1) Open a console in the root of the "/Payments" project.
2) Enter the `sbt run` command.

**In Intellij IDEA:** 
1) Install the "Scala Plugin". 
2) Open the project in Intellij IDEA.
3) Open `sbt shell` in the bottom panel.
4) After the `sbt:Payments>` welcome message appears, enter the `run` command.

#### **Result**
The application processes files from the directory specified in application.conf. Then it outputs information about payments to log.

The log will display successful and unsuccessful transactions, as well as incorrect entries.