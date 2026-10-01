## *Document Optimization Report*

### Target query selection.
We selected Analysis #2 (``Application Error Distribution by Type and Day``) from ``AnalyticsRunner.java`` as our performance benchmark, which filters on ``ApplicationErrorEvent`` over a temporal range.

### Performance baseline before index creation.
Executing an ``explain("executionStats")`` on the unindexed collection yielded the following baseline metrics:
- **Execution stage :** ``COLLSCAN`` (Full collection scan)
- **Documents examined :** 100,000 documents (``totalDocsExamined``)
- **Documents returned :** 20,109 documents (``nReturned``)
- **Execution time :** ~65 ms

### Index design and field order justification.
We added a compound index in ``BaseEvent.java`` defined as ``@CompoundIndex(name = "class_timestamp_idx", def = "{'_class': 1, 'timestamp': 1}")``.
Following the **ESR Rule** (Equality, Sort, Range):
- **Equality (``_class``) :** Placed first to immediately isolate ``ApplicationErrorEvent`` instances and discard ~80% of unneeded event types (logins, API calls, payments).
- **Range (``timestamp``) :** Placed second to efficiently slice the required date range directly within the index B-Tree.

### Performance metrics after index creation.
Re-evaluating the aggregation pipeline with the compound index produced the following improved metrics:
- **Execution stage :** ``IXSCAN`` / ``ixseek`` (Targeted index scan)
- **Keys examined :** 20,109 keys (``totalKeysExamined``)
- **Documents examined :** 20,109 documents (``totalDocsExamined``)
- **Documents returned :** 20,109 documents (``nReturned``)

### Summary of impact.
Before optimization, the aggregation required a full collection scan across all 100,000 documents.
Adding the compound index ``{ _class: 1, timestamp: 1 }`` enabled MongoDB to execute a targeted index scan instead.
This reduced total examined documents to exactly 20,109, bypassing 80% of unneeded memory reads.