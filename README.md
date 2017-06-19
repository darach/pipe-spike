# pipe-spike

Explores a potential higher level API than the jet DAG.

The Pipeline API explored is oriented around composition of and reuse
of simpler DAGs.

## Rules:

* A pipeline can be composed of pipelines
* A pipeline must be a valid jet DAG
* A template is a form of pipeline
  * Templates, like pipelines, must be valid jet DAGs
  * But, templates may wrap external APIs other than DAGs, such as jet streams, ...
  * Template structure is static
* An operator is a template whose structure is user defined or configured dynamically
* A port is a connection ( ~edge ) connecting vertices in pipelines
* A template may be used many times within a pipeline
* Only root pipelines ( the parent of all sub pipelines ) are ordinarily executable
  by Jet

## Branching

Causally ordered and non-causally ordered branching is explored using different
processor primitives on top of which to build higher level pipeline operators.

Issues with branching exposing buffering issues in the scheduler renders imposing
strict causal ordering difficult.

## General

Further structuring and boilerplate facilities needed to make this generally useful.
For pipelines that impose strict builtin or user defined ordering checks will need
to be performed against vertex and edge configurations to ensure the intent of the
operator is respected by the user.

Mechanisms to achieve this are defined ( eg: validation ) but patterns are as yet
to be identified and standardised.

## Summary

As and from Jet 0.4 a pipeline API is achievable with caveats. For example, iterating
over DAG vertices has the side-effect of validating a DAG. This won't be the case with
pipelines where validation is ideally lazy and may benefit from being deferred until
later when a final pipeline DAG is generated from pipeline configurations.

This implementation therefore defines well-defineness in terms of a piplines underlying
DAG being valid with respect to jet validation rules as exposed upon iteration.

A pipeline, template or operator is only valid, however, if it is both valid with respect
to jet and valid with respect to the rules intended by the author of the pipeline, template
or operator.

Some of these validations will be reasonably common, for example: whether or not a branching
construction implies ordering or can tolerate being out of order. Whether or not branching guaranteescausal processing or not.

Causal branching would allow punctuation to be inserted to drive causal combining in downstream
pipelines enabling scatter/gather, split/join and other processing styles to be driven by
operators.
