Bipartite-Matching
==================

>Note: There exists a error/bug in the code that I've forgot to address (the bug is, when augmenting unmark the edge to allow fair game in the next match).
>Note 2: K represents the rank ex. Chandler ranks Rachel: 2 / Rachel ranks Chandler: 3

The Objective:

Find the least offensive matchings for a group of N men and N women. You must find a
matching that minimizes the value K, where K is the ranking of the "worst" pair. In other words, if one matching
has everyone paired with one of their top four choices, and another matching pairs everyone with their top choice,
except there is one person left with choice #5, the first matching is preferable.


Input Format:

```sh
Chandler:Monica,Rachel,Phoebe
Joey:Rachel,Phoebe,Monica
Ross:Rachel,Phoebe,Monica

Monica:Chandler,Joey,Ross
Phoebe:Joey,Ross,Chandler
Rachel:Ross,Joey,Chandler
```

From the first line, you should be able to deduce N, and the names of all the women. It should then be easy to
check the remainder of the file to ensure that the underlying data represents a bipartite graph.
In this case, everybody can be matched with either their first or second preference, and it is impossible to match
everyone with their first preference, so the minimum value of K is 2. Your output should indicate the value
of K that you obtain, and the matching, and the amount of time. For instance,

```sh
Everybody matched with top 2 preferences:
Chandler: matched to Monica (rank 1)
Joey: matched to Phoebe (rank 2)
Ross: matched to Rachel (rank 1)

Monica: matched to Chandler (rank 1)
Phoebe: matched to Joey (rank 1)
Rachel: matched to Ross (rank 1)
```
