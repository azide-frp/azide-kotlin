# Azide (Kotlin)

test

A discrete Functional Reactive Programming framework.

Semantically, a family of meaningful time function definitions.

On the implementation side, a transactional push-pull information propagation system.

## Primitives

### `EventStream` for events of type `EventT`

A sequence of occurrences over time: at certain moments it “fires” a value of type `EventT`.

Semantically, a discrete time function like `(t: Time) -> EventT`.

On the implementation side, a vertex that propagates its ongoing event emission (if it happens in a given transaction).

Synopsis:

```kotlin
interface EventStream<out EventT> {
    // ...
}
```

### `Cell` for values of type `ValueT`

A value of type `ValueT` that is always defined and may change over time.

Semantically, a step time function (piecewise constant function) like `(t: Time) -> ValueT`.

On the implementation side, a vertex that stores its current "stable" value and propagates its ongoing value update (if
it happens in a given transaction).

Synopsis:

```kotlin 
interface Cell<out ValueT> {
    // ...
}
```

### Reactive collections

A collection that is always defined and may change over time. The specific element type (or key/value types) depends
on the collection kind.

Semantically, a cell for values of some collection type.

On the implementation side, a vertex that stores its current "stable" content and propagates its ongoing content changes
(if they happen in a given transaction). A _change_ is a difference (delta) that describes how the content of the
collection changes in a given transaction.

#### `ReactiveBag` for elements of type `ElementT`

An unordered reactive collection that makes no assumptions about its elements (even that they provide functional
`equals` / `hashCode`). Consequently, it's not possible to ask whether a specific element is contained within a
reactive bag.

Semantically, a wrapper for a cell like `Cell<Bag<ElementT>>`.

On the implementation side, a collection vertex that models a change as a tuple of:

- a map of added elements (keyed by tags)
- a map of replaced elements (keyed by tags)
- a set of removed tags

Synopsis:

```kotlin
interface ReactiveBag<out ElementT> {
    // ...
}
```

#### `ReactiveSet` for elements of type `ElementT`

An unordered reactive set. Assumes its elements provide functional `equals` / `hashCode`.

Semantically, a wrapper for a cell like `Cell<Set<ElementT>>`.

On the implementation side, a collection vertex that models a change as a pair of added and removed elements.

Synopsis:

```kotlin
interface ReactiveSet<out ElementT> {
    // ...
}
```

#### `ReactiveList` for elements of type `ElementT`

An ordered reactive collection that makes no assumptions about its elements (even that they provide functional
`equals` / `hashCode`).

Semantically, a wrapper for a cell like `Cell<List<ElementT>>`.

On the implementation side, a collection vertex that models a change as a list of _parts_, where each part contains
a range of indices (in the old list) and a list of new elements for that range.

Unlike other reactive collections, a single semantic update of a list has multiple possible change representations. It
shouldn't be observable which one is used, but the implementation may choose the one that minimizes the amount of change
data to be propagated.

Synopsis:

```kotlin
interface ReactiveList<out ElementT> {
    // ...
}
```

#### `ReactiveMap` of keys of type `KeyT` and values of type `ValueT`

An unordered reactive associative collection. Assumes its keys provide functional `equals` / `hashCode`.

Semantically, a wrapper for a cell like `Cell<Map<KeyT, ValueT>>`.

Not implemented yet.

Synopsis:

```kotlin
interface ReactiveMap<KeyT, out ValueT> {
    // ...
}
```

## Implementation details

### Propagation strategy

The reactive primitives form a directed **propagation graph** of vertices. When a transaction begins, one or more
source vertices emit notifications that propagate downstream through the graph.

Each vertex kind exposes one type of **ongoing notification**. Downstream vertices register as listeners on the
ongoing notifications of their inputs. Notifications are _pushed_ (propagated eagerly), but carry no payload — upon
being notified, a listener _pulls_ the relevant information from its inputs. Stable state may also be pulled deeply
(computed on demand if not cached).

The graph is technically allowed to contain cycles, but if a piece of information enters a cycle without being filtered
out, this constitutes a _causal loop_ semantically, which is not supported. The exact implementation behavior in this
case is to be specified.

#### Optimistic propagation with revocation and correction

The framework uses an **optimistic** propagation strategy. When a vertex receives a notification, it immediately
forwards a notification to its own listeners — optimistically assuming that the incoming change will result in a
meaningful outgoing change. If this assumption later turns out to be wrong, the vertex must correct itself. There are
two distinct cases:

- **Revocation** — the vertex concludes it should not have notified at all (e.g. a `filter` operator whose predicate
  turned out to be false). It sends a revocation to its listeners, retracting the earlier notification entirely.
- **Correction** — the vertex concludes it _should_ have notified, but with a different value or change than what
  would currently be pulled (e.g. a `map2` operator that propagated optimistically on the first input, then received
  a notification from its second input in the same transaction — the combined result is now different). It sends a
  correction notification, prompting listeners to re-pull.

Both serve correctness. In practice, simple reactive graphs — which are the common case even in large real-world
applications — rarely trigger revocations or corrections.

### Transactions

A **transaction** is the implementation-level realization of a semantic _moment_ — a point in time at which one or
more things change simultaneously. Each transaction proceeds through three phases, in order:

1. **Propagation** — information flows through the vertex graph. Notifications are emitted, ongoing state is exposed,
   and vertices react to their inputs. During this phase, vertices may enqueue work for the subsequent phases.

2. **Commitment** — ongoing (transaction-temporary) state is applied to stable state. The commitment order is the
   **exact reverse** of the propagation order: a vertex is committed before the vertices that caused it to update.
   This means that during commitment, a vertex can still assume its inputs expose their ongoing state and their old
   stable state.

3. **External side effect execution** — side effects that were enqueued during propagation (e.g. user-registered
   callbacks) are executed after the reactive graph has fully settled.

### Edge APIs

The edge APIs are the public-facing interface between the reactive system and the outside world (I/O, user input,
external state, etc.). They are the entry and exit points of the propagation graph.

### Internal APIs

The internal APIs are framework-private interfaces used within the implementation itself. They are not exposed to
framework users. They are organized into two parts:

#### Listeners

- **Registration** — a listener registers itself on a specific ongoing notification of a specific input vertex
  (e.g. `sourceCell.registerListener(myListener)`), establishing a directed edge in the propagation graph.
- **Handling** — when a notification fires, each subscribed listener is triggered via a payload-less
  `handle(transactionContext)` call.

#### Pullable data

Once triggered, a listener pulls data from its input vertex (or vertices). There are two kinds of pullable data:

- **Ongoing** — transaction-temporary data exposed by a vertex while a transaction is in progress. Each vertex kind
  exposes a typed ongoing notification (e.g. `ongoingUpdate` on a cell vertex, `ongoingEmission` on an event stream
  vertex). When pulled, an ongoing notification is interpreted either as:
  - a **revocation** — indicating the vertex retracts its earlier notification entirely (currently represented as
    `null`, as an optimization), or
  - an **effective notification** — indicating the vertex has something to report, either as an original notification
    or as a correction to a previously sent one.

- **Stable** — the last committed (pre-transaction) state, available on cell and collection vertices
  (e.g. `getOldValue`, `getOldContentView`).

## Testing

Testing a Functional Reactive Programming system is challenging, so it must be approached methodically.

### Expectations

#### High-level expectations

From the framework's user perspective, it's expected that the system always behaves "as described in the semantics".
This means that, according to an intuitive mapping between the semantic domain and the real-world application, everything
is consistent and no _glitches_ occur. A glitch is an incident when the user is able to observe that the application
behaved **differently** than what the semantics describes.

We cannot rely on the edge APIs to test the high-level expectation regarding the parts of the system in isolation, as
they add their own behavior on top of the behavior we'd want to test.

The user has no awareness of the internal APIs, so they don't have any expectations about them.

#### Low-level contracts

The high-level expectations have some fundamental problems:

1. They are imprecise by their nature
2. They relate to the reactive system as a whole, not to its specific parts in isolation

Precisely capturing the high-level expectations is a tricky problem, since the implementation-level APIs and strategies are designed
solely to realize the semantics. _Fundamentally_, there are no inherent expectations for them. The low-level contracts
have to be synthesized in order to test the specific parts of the framework in isolation. If a large-scale issue is
recognized within the framework, the implementation strategies and the corresponding low-level contracts **can change
simultaneously**.

The FRP semantics is all about time and things happening simultaneously. The realistic implementation approach for
"things happening simultaneously" is executing internal operations in _some_ order, but it shouldn't matter which one.
This idea must be incorporated into the low-level contracts.

### Stimulation

A **stimulation** is the complete sequence of implementation-level actions applied to a test subject within a single
transaction — the notifications, revocations, corrections, and their ordering. It is not a semantic concept: multiple
different stimulations can correspond to the same semantic behavior. The low-level contracts should hold regardless of
which stimulation is used, as long as that stimulation is a valid realization of the intended semantic behavior.

### Test suite

#### Property (fuzz) tests

The main source of confidence in the system's correctness should come from property-based fuzz testing — a technique
that combines ideas from both property testing and fuzz testing.

Experience shows that there are so many possible combinations of scenarios and states in a reactive system
implementation, that an attempt to figure out all corner cases up-front **fails**:

- The number of unit tests grows beyond maintainable size
- ...and in spite of that, new glitches are easily found in a system passing the unit test suite

##### The property aspect (oracle)

FRP semantics give us a way to define the **property** — i.e. to implement an oracle that knows what the correct
behavior is. Given a semantic input behavior, we can automatically determine how the test subject should respond,
based on our understanding of the low-level contracts. The oracle doesn't care about the exact stimulation path or the
exact order of the test subject's observable activity — it only cares whether the activity, taken as a whole, exposes
the expected semantic behavior.

##### The fuzz aspect (stimulation generation)

The fuzz aspect is responsible for generating a random sequence of low-level stimulations that is equivalent to a given
semantic behavior. The process works in two steps:

1. **Draw a random semantic behavior** — e.g. a sequence of emissions, updates, or changes that describe what
   _semantically_ happens to the inputs of the test subject.
2. **Complicate it randomly** — introduce corrections, revocations, and randomized processing order, as long as the
   fundamental internal contracts are preserved (e.g. a revocation is never sent for something that wasn't notified
   first).

This way, the generated stimulation is semantically equivalent to the original behavior, but exercises different
internal code paths — maximizing the chance of discovering glitches.

##### Scope

Property-based fuzz testing can be used both at the single-vertex level and at the whole-system level (scenarios
involving multiple different entities). At the single-vertex level, we can verify the vertex's behavior via the
internal APIs, checking whether the low-level contracts are satisfied. At the whole-system level, we can verify the
system's behavior via the edge APIs.

#### Unit tests

The primary role of unit tests is **regression prevention**: once a specific glitch or edge case is identified, a
targeted unit test ensures it doesn't resurface. Beyond that, a well-designed unit test suite is a great help in
debugging — when a test fails, it points to a specific, named scenario, making it much easier to identify the cause
than a failed fuzz test would.

Each test suite can vary in structure depending on what it covers, but a typical unit test suite contains:

- **At least one passive sampling test** (for cells and reactive collections) — verifies that the test subject
  correctly exposes its stable state when sampled outside of a transaction.
- **Multiple reaction tests** — reaction tests form the vast majority of tests in most suites; each test verifies how the
  test subject responds to a specific input stimulation. The focus of each reaction test should be clear: it should
  be obvious what exact scenario (in terms of the input stimulation order and content) is being exercised, so that a
  failure can be quickly diagnosed.
- **One offline activation test** — verifies that the test subject behaves correctly when activated in the commitment
  phase

#### Test utils

Whether we write a unit test with a precise stimulation or a fuzz test with a generated one, common patterns emerge on
both sides of the equation:

- **Stimulation** — many tests follow similar stimulation structures (e.g. start something, poke some inputs, cancel,
  revoke, poke again). These patterns are repetitive and should be factored into reusable test orchestration utilities.
- **Verification** — as discussed above, correctness in FRP is observed indirectly, through the semantic interpretation
  of the test subject's observable activity. Even the simplest test needs logic to record, interpret, and verify the
  subject's behavior against the expected semantics.

This shared logic should live in dedicated test utilities, not be duplicated across individual tests. Well-maintained
test utils are essential to keeping the test suite manageable and consistent as the framework evolves.

### Handling regressions

When a part of the system (e.g. a vertex implementation) is broken in a specific aspect, the expected outcome is:

- The related single-vertex fuzz test fails
- Some of the whole-system fuzz tests fail (possibly many of them)
- A low number of unit tests fail (possibly a single one)

If a fuzz test discovers a new glitch (when the unit test suite succeeds), it's a good idea to add a unit test for it,
to make sure it doesn't regress in the future. We should determine what the exact nature of the glitch is, and the
unit test should be designed to produce the relevant input stimulation.

If a unit test discovers a regression (and the focused fuzz test succeeds), it might be a good idea to adjust the
fuzz generation method, to increase the chances of discovering similar or related glitches.
