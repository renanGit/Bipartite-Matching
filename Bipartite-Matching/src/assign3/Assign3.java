
package assign3;

//friends0.txt friends1.txt friends2.txt friends3.txt friends4.txt friends5.txt friends6.txt friends7.txt friends8.txt

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author Renan Santana
 */

/*
* 
* 
*/
class Matcher{
    private Map<String, Map<String, Integer[]>> nameMap;// temp map
    private List<Matrix> matrixList;// list of matrices
    private SourceSink source;      // source connects to everyone at start
    private SourceSink sink;        // sink will be connected to everyone at end
    
    // Intialize Vars
    public Matcher(){
        nameMap = new LinkedHashMap<>();
        matrixList = new ArrayList<>();
        source = new SourceSink();
        sink = new SourceSink();
    }
    
    // a edge comes out of a matrix
    class Edge{
        private int masterIndex, edgeRank, prefRank;
        private Matrix mNext, mPrevious;
        
        Edge(int edgeRank){
            this.edgeRank = edgeRank;
        }
        public int getEdgeRank(){ return edgeRank; }
        
        public void setMasterIndex(int index){ masterIndex = index; }
        public int getMasterIndex(){ return masterIndex; }

        public void setPreferenceRank(int preferenceRank){ prefRank = preferenceRank; }
        public int getPreferenceRank(){ return prefRank; }
        
        public void setNext(Matrix next){ mNext = next; }
        public Matrix getNext(){ return mNext; }
        
        public void setPrevious(Matrix prev){ mPrevious = prev; }
        public Matrix getPrevious(){ return mPrevious; }
    }
    
    class Matrix{
        private final String masterName;    // Unique name
        private List<Edge> adj;             // list of adj's this connects to
        private List<Edge> connection;      // the acceptable connections that can be made
        private Set<Integer> avoidDuplicate;// dont add duplicate edges
        private int index;                  // index of matrix
        private SourceSink ss;              // is matrix connected to source / sink?

        Matrix(String m, int index){
            masterName = m;
            adj = new ArrayList<>();
            connection = new LinkedList<>();
            avoidDuplicate = new HashSet<>();
            this.index = index;
        }

        public String getMasterName(){ return masterName; }
        public Boolean addToSet(int avDup){ return avoidDuplicate.add(avDup); }
        public int getIndex(){ return index; }

        public void addAdj(Edge e){ adj.add(e); }
        public List<Edge> getAdj(){ return adj; }

        public void addConnection(Edge e){ connection.add(e); }
        public List<Edge> getConnections(){ return connection; }
        
        public void connectTo(SourceSink ss){ this.ss = ss; }
        public SourceSink getConnectsTo(){ return ss; }
    }

    class SourceSink{
        private List<Matrix> ss;
        SourceSink(){ ss = new ArrayList<>(); }
        
        public void addAdj(Matrix m){ ss.add(m); }
        public List<Matrix> getAdj(){ return ss; }
    }
    
    /*
    * The output of the male is not guaranteed to be the same as the input.
    * The output of the female is the same as the output.
    * Steps Taken On Male Side: 1. Get female 2. Female only has one connection 
    * so get that edge 3. Go to female adj get the pref index
    * 4. Now this edge has the masterIndex of the male
    * Important note : The edge that a female has is the edge the male adds to the
    * female connection method.
    */
    private void printMatches(int k){
        int n = matrixList.size()/2;
        
        System.out.println("Everybody is matched with top "+k+" preferences:");
        System.out.println("\nMen to Women");
        for (int index = n; index < n*2; index++) {
            Matrix mF = matrixList.get(index);
            Edge eF = mF.getConnections().get(0);
            Edge eM = mF.getAdj().get(eF.getPreferenceRank()-1);
            Matrix mM = matrixList.get(eM.getMasterIndex());
            
            System.out.println(mM.getMasterName()+": matched to "+
                    mF.getMasterName()+" (rank "+eF.getEdgeRank()+")");
            
        }

        System.out.println("\nWomen to Men");
        for (int index = n; index < n*2; index++) {
            Matrix mF = matrixList.get(index);
            Edge eF = mF.getConnections().get(0);
            Edge eM = mF.getAdj().get(eF.getPreferenceRank()-1);
            Matrix mM = matrixList.get(eM.getMasterIndex());
            
            System.out.println(mF.getMasterName()+": matched to "+
                    mM.getMasterName()+" (rank "+eM.getEdgeRank()+")");
        }
    }

    private Matrix getMale(Edge e){
        Matrix mF = matrixList.get(e.getMasterIndex());
        Edge eM = mF.getAdj().get(e.getPreferenceRank()-1);
        Matrix mM = matrixList.get(eM.getMasterIndex());
        return mM;
    }
    
    /*
    * We pop the first edge in the stack. This edge is always ending at the
    * female side. The rest is pop and flipped. At the end we need to remove
    * the connection from the source to the starting matrix
    */
    private void reverseFlow(Stack<Edge> s){
        int n = matrixList.size()/2;
        Edge e;
        Matrix mM, mF;

        e = s.pop();
        // Male side
        mM = e.getPrevious();
        // male matrix remove the occurance of this edge
        e.getPrevious().getConnections().remove(e);
        // male matrix connects to the source
        e.getPrevious().connectTo(source);

        // Female side
        mF = e.getNext();
        // flip the connection
        e.setNext(mM);
        e.setPrevious(mF);
        // add the connection on female side
        mF.addConnection(e);
        // female doesnt connect to the sink any more
        mF.connectTo(null);
        sink.addAdj(mF);
        
        while(!s.isEmpty()){
            e = s.pop();
            if(e.getNext().getIndex() < n){
                mF = e.getPrevious();
                mM = e.getNext();
                
                mF.getConnections().remove(e);
                
                e.setNext(mF);
                e.setPrevious(mM);
                
                mM.addConnection(e);
            }
            else{
                mF = e.getNext();
                mM = e.getPrevious();
                
                mM.getConnections().remove(e);
                
                e.setNext(mM);
                e.setPrevious(mF);
                
                mF.addConnection(e);
            }
        }
        source.getAdj().remove(e.getNext());
    }
    
    /*
    * This method attempts to add a flow from source to sink
    * The matrix it picks must attempt to find a path to sink.
    * If it cant make a path one or more problems occured: 1. The edges were
    * visited and needs to backtrack 2. The matrix has no other connection
    */
    private void augment(){
        int n = matrixList.size()/2;
        int index = 0;
        boolean backTrack = false;
        boolean badTrace = false;
        boolean noNext = false;
        Set<Edge> visited = new HashSet<>();
        
        Stack<Edge> reverse = new Stack();
        Matrix mS; // The starting matrix
        Edge eF = null, eM; // eF = <M> to <F> / eM = <F> to <M>
        
        while(index < source.getAdj().size()){
            // The connection we want to make
            Matrix mM = source.getAdj().get(index);
            mS = mM;
            
            // no connections for the matrix to make : skip
            if(mM.getConnections().isEmpty()){ index++; continue; }
            
            Iterator<Edge> temp = mM.getConnections().iterator();
            
            // try to find a outgoing edge / if not continue
            while(true){
                try{ eF = temp.next(); }
                catch(Exception ex){ noNext = true; break; }
                // mark as visited
                if(visited.add(eF)){ break; }
            }
            if(noNext){ index++; noNext = false; continue; }
            
            Matrix mF = matrixList.get(eF.getMasterIndex());
            
            // set the directions
            eF.setNext(mF);
            eF.setPrevious(mM);
            // push edge <M> to <F>
            reverse.push(eF);
            
            do{
                
                // edge cant go to sink
                if(mF.getConnectsTo() == null){
                    // bounce back to the male side
                    eM = mF.getConnections().get(0);
                    mM = eM.getNext();
                    
                    // push edge <F> to <M>
                    reverse.push(eM);
                    
                    // Three cases: 1. edge to male was visited
                    // 2. male has no edges 3. all male edges are visited
                    if(!visited.add(eM)){ backTrack = true; }
                    else if(mM.getConnections().isEmpty()){ backTrack = true; }
                    else{
                        temp = mM.getConnections().iterator();

                        while(true){
                            try{ eF = temp.next(); }
                            catch(Exception ex){ backTrack = true; break; }
                            // mark as visited
                            if(visited.add(eF)){ break; }
                        }
                    }
                    
                    // back track needed cases: 
                    // mM has no connections, mM all edges are visited, mF edge
                    // was visited
                    while(backTrack){
                        backTrack = false;

                        reverse.pop();          // pop <F> to <M>
                        eF = reverse.pop();     // pop <M> to <F>
                        mM = eF.getPrevious();

                        temp = mM.getConnections().iterator();
                        
                        // We made two steps back now we need to find a new path
                        while(true){
                            try{ eF = temp.next(); }
                            catch(Exception ex){ backTrack = true; break; }
                            // mark as visited
                            if(visited.add(eF)){ break; }
                        }
                        // If we are at the beginning and we cant find another
                        // edge to go on we must kill this search
                        if(mM == mS && backTrack){ break; }
                    }
                    // After the break set some conditions
                    if(mM == mS && backTrack){ 
                        backTrack = false; 
                        badTrace = true; 
                        break; 
                    }
                    
                    mF = matrixList.get(eF.getMasterIndex());

                    eF.setNext(mF);
                    eF.setPrevious(mM);
                    // push edge <M> to <F>
                    reverse.push(eF);
                }
                
            // This while will continue until we connect to sink or 
            // we need to kill this search b/c of a bad trace
            }while(!(mF.getConnectsTo() == sink) && !badTrace);
            
            // reverse flow. we made a complete connection
            if(!badTrace){ reverseFlow(reverse); }
            else{
                badTrace = false;
                reverse.clear();
            }
        }// end while
    }

    /*
    * Create matchs has one purpose : its to add possible matches to the
    * list iff theres a agreement between the two. The agreement is
    * at the current "k" value <M> to <F> -> <F> to <M> must be on or within
    * the bound of k.
    */
    private int createMatches(){
        int n = matrixList.size()/2;
        int twoN = matrixList.size();

        // for loop k
        for(int k = 1; k <= n; k++){

            // loop the masterNames
            for(int index = 0; index < twoN; index++){
                Matrix m = matrixList.get(index);
                Edge e = m.getAdj().get(k-1);

                int prefRank = e.getPreferenceRank();

                if(prefRank <= k){
                    // flip the connection
                    if(index >= n){
                        m = matrixList.get(e.getMasterIndex());
                        e = m.getAdj().get(e.getPreferenceRank()-1);
                    }
                    List<Edge> conEdge = m.getConnections();
                    // add the possible connection
                    if(m.addToSet(e.getMasterIndex()))
                        conEdge.add(0, e);
                }
            }
            
            // terminating case if the source is empty that means all males
            // are connected to the source. Other wise there exist a connection
            // can augment (ie increase flow)
            if(sink.getAdj().size() < n)
                augment();
            if(source.getAdj().isEmpty())
                return k;
        }// end for (k)
        return n;
    }

    private void readFile(String file) throws IOException{

        BufferedReader br;

        try{
            br = new BufferedReader(new FileReader(file));
        }catch( FileNotFoundException e ){ 
            System.out.println("Missing file: " + file); 
            return;
        }

        String line;
        int currentIndex = 0;
        while( (line = br.readLine()) != null ){
            if(line.equals("")){ continue; }

            String[] edge = line.split(":|,");

            Matrix m = new Matrix(edge[0], currentIndex);
            Map<String, Integer[]> names = new LinkedHashMap<>();

            for(int rank = 1; rank < edge.length; rank++){

                Integer[] indexRank = {currentIndex, rank};
                names.put(edge[rank], indexRank);

                Edge e = new Edge(rank);
                m.addAdj(e);

                // Will only enter here if we are now scanning the female to male
                // in which case we can fill in useful data in the edge class
                if(nameMap.containsKey(edge[rank])){
                    // 1. get the masterName <MaleName>
                    Map<String, Integer[]> nameList = nameMap.get(edge[rank]);
                    // 1. -> 2. get the edgeName <FemaleName>
                    Integer[] name = nameList.get(edge[0]);

                    // 1. -> 2. -> 3. get the masterName <MaleIndex>
                    Matrix matrixTemp = matrixList.get(name[0]);
                    // 1. -> 2. -> 3. -> 4. get the edgeName <FemaleIndex>
                    Edge edgeTemp =  matrixTemp.getAdj().get(name[1]-1);

                    // set the prefernce of <M> to <F> -> <F> to <M>
                    edgeTemp.setPreferenceRank(rank);
                    // set the index refernce of male index in matrix list
                    edgeTemp.setMasterIndex(currentIndex);

                    // set the prefernce of <F> to <M> -> <M> to <F> 
                    e.setPreferenceRank(name[1]);
                    // set the index refernce of male index in matrix list
                    e.setMasterIndex(name[0]);
                }
            }
            currentIndex++;
            matrixList.add(m);
            nameMap.put(edge[0], names);
        }
        // free the map / we are done with it
        nameMap.clear();
    }
    
    /*
    * Sort of a API approach so the user can just find the matches
    */
    public void matcher(String file) throws IOException{
        
        readFile(file);
        
        for (int index = 0; index < matrixList.size(); index++) {
            if(index < matrixList.size()/2)
                source.addAdj(matrixList.get(index));
            else
                matrixList.get(index).connectTo(sink);
        }
        
        double t1 = System.nanoTime();
        int k = createMatches();
        double t2 = System.nanoTime();
        
        printMatches(k);
        System.out.println("\nTime elapsed for file " 
                          + file + " is " + ((t2-t1)/1000000000.0) + " seconds");
        
        matrixList.clear();
    }
}

public class Assign3 {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException{
        for(String str : args){
            Matcher match = new Matcher();
            System.out.println("----Gathering Matches---- File: "+str);
            match.matcher(str);
            System.out.println("----*****************----\n");
        }
    }
    
}