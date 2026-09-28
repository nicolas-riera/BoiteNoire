## *Why is choosing a document database the preferred option here compared to adding an SQL table?* 

### What a relational model of these events would yield, and its concrete limitations in this case.

If we had chosen an SQL table, we would have, since each event has different information :

- Option A : a ``events`` table with a lot of nullable columns (ex. ``ip_address``, ``amount``, ``stack_trace``, ``api_endpoint``...).
- Option B : a ``events`` table + a table per event type (``connection_events``, ``payment_events``, ``error_events``, ``api_events``) all connected with foreign keys.

While that structure works, it would be quite heavy, inefficient and hard to maintain. 

### What document modeling allows.

Meanwhile, with a NoSQL/document database, the structure is much more flexible : each document (a line in SQL) is independent, and the base doesn't enforce a list of data and their type when creating a collection (a table in SQL).

Moreover, a document database is inherently polymorphic, as a collection doesn't enforce any sort of column.

Concretely, we can have a ``events`` collection with common fields (like ``timestamp`` or ``event_type``) and then each document has its own fields.

Last but not least, since it's simply writing in a JSON file that doesn't have inter-table checks, it's significantly more performant, which is very important when having to write thousands of events per seconds.    

### Our conclusion and the deciding factor.   

The choice is very clear : **NoSQL** is the way to go for this project.  
Its flexibility and performance is unmatched by SQL.