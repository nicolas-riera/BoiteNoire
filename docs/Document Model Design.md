## *Document Model Design and Justification*

### Common fields across all events.
All events inherit from ``BaseEvent`` and share four indexed fields : ``id``, ``timestamp``, ``type``, and ``userId``. These fields enable efficient cross-cutting queries, such as searching for all actions performed by a specific user within a given timeframe regardless of the event type.

### Choice of Embedding and its justification.
For the ``PAYMENT_PROCESSED`` event, we chose **Embedding** for ``PaymentDetails`` and ``OrderItem`` :
- **Joint read frequency :** Whenever a payment event is queried, order items and payment details are systematically read together. Embedding retrieves all data in a single query.
- **Document size and growth :** An order contains a bounded, small list of items. The document size remains minimal and well within MongoDB's limits, with zero growth over time once created.

### Choice of Referencing and its justification.
For the ``USER_PROFILE_UPDATED`` event, we chose **Referencing** for the user identity (``updatedByUserId``) :
- **Growth over time and duplication :** Copying full user profile data into every event document would lead to massive data duplication and unnecessary database growth over time.
- **Single source of truth :** Storing only the reference ID keeps the event lightweight while keeping user profile data centralized in its dedicated collection.