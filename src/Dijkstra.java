import java.util.stream.Stream;

//A Java program for Dijkstra's 
//single source shortest path  
//algorithm. The program is for 
//adjacency matrix representation 
//of the graph. 

public class Dijkstra { 

	private static final int NO_PARENT = -1; 

	// Function that implements Dijkstra's 
	// single source shortest path 
	// algorithm for a graph represented  
	// using adjacency matrix 
	// representation 
	public static String dijkstra(int[][] adjacencyMatrix,int startVertex,int endVertex) 
	{ 
		int nVertices = adjacencyMatrix[0].length; 

		// shortestDistances[i] will hold the 
		// shortest distance from src to i 
		int[] shortestDistances = new int[nVertices]; 

		// added[i] will true if vertex i is 
		// included / in shortest path tree 
		// or shortest distance from src to  
		// i is finalized 
		boolean[] added = new boolean[nVertices]; 

		// Initialize all distances as  
		// INFINITE and added[] as false 
		for (int vertexIndex = 0; vertexIndex < nVertices;  
				vertexIndex++) 
		{ 
			shortestDistances[vertexIndex] = Integer.MAX_VALUE; 
			added[vertexIndex] = false; 
		} 

		// Distance of source vertex from 
		// itself is always 0 
		shortestDistances[startVertex] = 0; 

		// Parent array to store shortest 
		// path tree 
		int[] parents = new int[nVertices]; 

		// The starting vertex does not  
		// have a parent 
		parents[startVertex] = NO_PARENT; 

		// Find shortest path for all  
		// vertices 
		for (int i = 1; i < nVertices; i++) 
		{ 

			// Pick the minimum distance vertex 
			// from the set of vertices not yet 
			// processed. nearestVertex is  
			// always equal to startNode in  
			// first iteration. 
			int nearestVertex = -1; 
			int shortestDistance = Integer.MAX_VALUE; 
			for (int vertexIndex = 0; 
					vertexIndex < nVertices;  
					vertexIndex++) 
			{ 
				if (!added[vertexIndex] && 
						shortestDistances[vertexIndex] <  
						shortestDistance)  
				{ 
					nearestVertex = vertexIndex; 
					shortestDistance = shortestDistances[vertexIndex]; 
				} 
			} 

			// Mark the picked vertex as 
			// processed 
			added[nearestVertex] = true; 

			// Update dist value of the 
			// adjacent vertices of the 
			// picked vertex. 
			for (int vertexIndex = 0; 
					vertexIndex < nVertices;  
					vertexIndex++)  
			{ 
				int edgeDistance = adjacencyMatrix[nearestVertex][vertexIndex]; 

				if (edgeDistance > 0
						&& ((shortestDistance + edgeDistance) <  
								shortestDistances[vertexIndex]))  
				{ 
					parents[vertexIndex] = nearestVertex; 
					shortestDistances[vertexIndex] = shortestDistance +  
							edgeDistance; 
				} 
			} 
		} 

		return printSolution(startVertex,endVertex, shortestDistances, parents); 
	} 

	// A utility function to print  
	// the constructed distances 
	// array and shortest paths 
	private static String printSolution(int startVertex,
			int endVertex,
			int[] distances, 
			int[] parents) 
	{ 
		int nVertices = distances.length; 
		//System.out.print("Vertex\t Distance\tPath"); 

		for (int vertexIndex = 0;  
				vertexIndex < nVertices;  
				vertexIndex++)  
		{ 
			if (vertexIndex == endVertex)  
			{ 
				//System.out.print("\n" + startVertex + " -> "); 
				//System.out.print(vertexIndex + " \t\t "); 
				//System.out.print(distances[vertexIndex] + "\t\t"); 
				return printPath(vertexIndex, parents); 
			} 
		}
		return ""; 
	} 

	// Function to print shortest path 
	// from source to currentVertex 
	// using parents array 
	private static String printPath(int currentVertex, 
			int[] parents) 
	{ 

		// Base case : Source node has 
		// been processed 
		if (currentVertex == NO_PARENT) 
		{ 
			return ""; 
		} 
		return printPath(parents[currentVertex], parents).concat(currentVertex + ";");
	}
    
    //should convert from the string notation to an array
    public static int[] printPathInt(String array) 
	{ 
        return Stream.of(array.split(";")).mapToInt(Integer::parseInt).toArray();
    }
}
