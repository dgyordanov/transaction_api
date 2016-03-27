# Transaction Api

## Overview
A RESTful web service that stores transactions and returns information about those transactions.
The transactions to be stored have a type and an amount. The service should support returning all
transactions of a type. Also, transactions can be linked to each other (using a "parent_id") and we
need to know the total amount involved for all transactions linked(also transitively) to a particular transaction.

## How to run
Required JRE 8

This is a Spring boot applicaiton which could be run using gradle wrapper ot using the fat jar generated int the build directory.

Run with gradle wrapper:
```shell
./gradlew bootRun
```

## Data storage
Transactions are stored in the memory and everything is lost when the process is stopped.
Transactions are in a parent-child relation, so the most natural data structure seems to be a tree. The problem is that there is no order in such a tree. It means that finding a transaction will have O(n). A HashMap was chosen for a data structure where the transactions are stored. Every transaction keeps reference to all its children, so all the children can be found easily in a recursive manner.

Why a HashMap with key ID and value transaction is chosen as a data structure:

|         | Add transaction | Read by ID | Find all children |
| --------|:---------------:| ----------:| -----------------:|
| HashMap | O(1)            | O(1)       | O(1)              |
| Tree    | O(n)            |  O(n)      | O(n)              |

Regarding the request which lists all the transactions IDs which are from a particular type, a new HashMap is used as an index. It has the type as a key and list of IDs as a value. Without this HashMap, we need to go through the all transactions in order to find the result - O(n). With the HashMap, we read the list of ids by key O(1). The price is that we need to edit the HashMap on create/edit which brings complexity and some additional lag.

## Notes
* No security is implemented
* Transaction amount could be also negative but not 0
* HTTP PUT method is used for create and update and POST handler is not implemented

# REST endpoints
__PUT /transactionservice/transaction/$transaction_id__  
Body:  
{ "amount":double,"type":string,"parent_id":long }

*transaction_id* is a long specifying a new transaction  
*amount* is a double specifying the amount  
*type* is a string specifying a type of the transaction  
*parent_id* is an optional long that may specify the parent transaction of this transaction  

__GET /transactionservice/transaction/$transaction_id__  
Returns:  
{ "amount":double,"type":string,"parent_id":long }

__GET /transactionservice/types/$type__  
Returns:  
[ long, long, .... ]  
A json list of all transaction ids that share the same type $type.

__GET /transactionservice/sum/$transaction_id__  
Returns:  
{ "sum", double }  
A sum of all transactions that are transitively linked by their parent_id to $transaction_id.  
