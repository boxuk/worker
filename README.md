
# Worker - Simultaneous Tasks!

This is a dirt simple library that tries to use futures to manage identical simultaneous tasks.  The
motivation came from a JSON API app that could have potentially lengthy (5-10 second) requests that
would produce identical results if all issued around the same time.  Worker allows them to be issued
as a single request to the backend service.

## Usage

You can install Worker via [Clojars](https://clojars.org/worker).

```clojure
(:use [worker.core :only [worker]])

(worker
  "a unique id"
  (my-long-running-task 1 2 3))
```

This will use the unique ID specified to check if this task is already running. If it isn't then it'll
create a future and store it, then blocking for the result.

When another request comes in, if this task is still running it'll then just block on the same future.
Both tasks will then be returned together and the future forgotten.

## Task IDs

The task ID used above was a string, but you can use vectors as well.

```clojure
(worker [:foo :bar :baz]
  (some-task))
```

Internally a map is used to store futures, accessed via _get-in_.

