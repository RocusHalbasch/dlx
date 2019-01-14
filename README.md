# dlx
This is an iterative implementation of Donald Knuths [dancing links](https://lanl.arxiv.org/pdf/cs/0011047).



I implemented Algorithm X to work as an iterative search which will return when a solution is found. The search can be continued 
where it left off to find the next solution. This can be called repeatedly to search the entire solution space if desired, but 
it also allows for a lot of other utility. The reason for this is that the implementation of `search(k)` in Knuth's paper was 
designed as a simple recursive algorithm to search the entire solution space, and every implementation of dancing links I found 
on the internet basically just implemented his `search(k)`. The problem is there are several things his `search(k)` clearly 
was never intended to do that can be accomplished using the same dancing links method. Following are several tasks that can be 
accomplished with my implementation that were not addressed by other simple transcriptions of `search(k)`.  

## Partial searches:

There are cases where you may only want to verify that at least one solution for a given problem exists, and while the other 
implementations will answer the question it would be a huge waste of resources in many cases as to searches through all the 
possible solutions. Since my implementation of `search()` returns once a solution is found we can do a partial search of the 
solutions. This allows us to skip most of that searching by just searching for the first solution by writing something like 
this:

```java
private boolean hasSolution() {
    LinkedList<Node> answer = search();
    return !answer.isEmpty();
}
```
While the above solution is much faster than doing a full search it does have a problem. When Knuth's `search(k)` does a full 
search it will search the whole tree and end by backtracking to the original state of all of the nodes with everything 
uncovered. The problem with the above solution is that since it has not done a full search it did not backtrack to the original 
state. Luckily as long as we have the stack representing the covered rows and order they where covered in we can quickly
backtrack to the original state with everything uncovered thus freeing us to use this data structure for more tasks. This 
process is much faster than creating a new instance and initializing the nodes again or completing a full search. To backtrack 
we simply call `undo()` with the stack representing the state of the grid. So that would change the method to something like 
this:

```java
private boolean hasSolution() {
    LinkedList<Node> answer = search();
    if (answer.isEmpty()) {
        return false;
    } else {
        undo(answer);
        return true;
    }
}
```
It is worth noting that if no solutions exist there is nothing to undo since that means that the whole tree was searched and 
`search()` already backtracked the nodes to the original state. 

Another useful bit of information someone might want is if one and only one solution exists. This is for example useful for 
Sudoku since a valid Sudoku has one and only one solution. We can write a simple method to check this without searching all 
possibilities when there are many more than one. To do this we will also use the `next()` method to continue the search from 
the last place we left off. The result is something like this:

```java
private boolean hasOnlyOneSolution() {
    LinkedList<Node> answer = search();
    if (answer.isEmpty()) {
        return false;
    } else {
        next(answer);
        if (answer.isEmpty()) {
            return true;
        } else {
            undo(answer);
            return false;
        }
    }
}
```
## Random Search:

Another thing that someone might want is a random solution to a problem. This could be done with the other implementations I 
found by searching for all solutions and randomly selecting one of them however once again that is horribly inefficient. For 
this purpose I have provided a `randomSearch()` method, which implements Algorithm X with random row selection. It is 
important to note that if you want multiple random solutions you should make sure to undo the previous search before searching 
for another. An example of this in action can be found in the `QueensDLX` class.

```java
private static void find2random(QueensDLX queens8) {
    // Find a random solution
    LinkedList<Node> answer1 = queens8.randomSearch();
    queens8.undo(new LinkedList<>(answer1));
    
    // Find another
    LinkedList<Node> answer2 = queens8.randomSearch();
    queens8.undo(new LinkedList<>(answer2));
    
    System.out.println("Solution #1:");
    queens8.printSolution(answer1);
    System.out.println();
    
    System.out.println("Solution #2:");
    queens8.printSolution(answer2);
}
```
It is worth noting here that since `undo()` will transform the list passed to it we are actually passing a copy of the list so
the original isn't modified. This allows us to print the solutions even after they are undone. 

## Partial Solutions:

Another useful thing the library can do is search for all solutions for a given partial solution. For example if you wanted to 
know all of the solutions to the 8 Queens problem that have a queen on R0F2 and R3F3. The other implementations I found could be
used to search for all 92 solutions and those could be paired down to only the ones with queens on R0F2 and R3F3, however that 
is a lot of work. But we can do better. We can manually remove rows with `removeRow()` and store them in a partial solution, 
then simply perform a full search of the remaining nodes. Example code can be found in the `QueensDLX` class.

```java
private static void partialSolution(QueensDLX queens8) {
    // Create a partial solution
    LinkedList<Node> partial = new LinkedList<>();
    Node r0f2 = queens8.R().D().D().D();
    queens8.removeRow(r0f2, partial);
    Node r3f3 = queens8.R().R().R().D().D().D();
    queens8.removeRow(r3f3, partial);

    // Print the partial solution
    System.out.println("Using the partial solution:");
    queens8.printSolution(partial);
    System.out.println();

    // Search for solutions
    queens8.searchAll();

    // Remove the partial solution
    queens8.undo(partial);
}
```
It is worth noting here that `searchAll()` will search the subset of the tree returning the nodes to the state of the partial 
solution. If we had just done a single call to `search()` we would want to first call `undo()` on the solution returned
before calling it on our partial solution. You will also notice that solutions found by `searchAll()` only contain the 
remaining six queens, also since the remaining search tree after these two queens have been chosen is much smaller than the 
whole tree this will be much faster than a full search.