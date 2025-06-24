

`Connective`: A graph pattern with the following three pieces of information:
* the set of exposed variables: There are the variables that can be referenced in fields. Typically exposed variables are all the visible variables of a pattern.
* A list of source variables (order matters)
* A list of target variables (order matters)


`Traversal`: A traversal over a (possible empty) property path.


A GraphQL document is made up of a set of selections. The members are called selections and can typically have nested selection sets.
A selection is a member of such a set.

Selection Types:
* Field: A selection that is based on a connective. By default, a fields connective's source variables are joined with the parent connective's target variables.
   Additional annotations can be used for custom wireing.
  


### Building a Field:
The following information is needed to build a field:
* The parent field
* The connective
* Optionally: name / base name


The parent node needs to be (partly?) immutable so that we can check whether the information is the child is consistent with it.
A child node can be created without having it registered at the parent.


