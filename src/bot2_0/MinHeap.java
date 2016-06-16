package bot2_0;

public class MinHeap {

	private Node[] heap;
	private int size;
	private int maxSize;
	private static final int FRONT = 1;
	private Node endNode;
	
	public MinHeap(int maxSize, Node endNode){
	    this.heap = new Node[maxSize+1];
	    heap[0] = null;
	    this.size = 0;
	    this.endNode = endNode;
	}
	
	private int getParent(int position){
	    return position/2;
	}
	
	private int getLeftChild(int position){
	    return 2*position;
	}
	
	private int getRightChild(int position){
	    return 2*position+1;
	}
	
	private void swap(int position1, int position2){
	    Node temp = heap[position1];
	    heap[position1] = heap[position2];
	    heap[position2] = temp;
	}
	
	private boolean isLeaf(int position){
	
	    if(position > size/2){
	        return true;
	    }
	    return false;
	}
	
	public void insert(Node data){
	    heap[++size] = data;
	    int currentItem = size;
	    while( heap[getParent(currentItem)].getFValue(endNode) > heap[currentItem].getFValue(endNode) ){
	        swap(getParent(currentItem),currentItem);
	        currentItem = getParent(currentItem);
	    }
	}
	
	public Node delete(){
	    Node itemPopped = heap[FRONT];
	    heap[FRONT] = heap[size--];
	    heapify(FRONT);
	    return itemPopped;
	}
	
	private void heapify(int position){
	    if(isLeaf(position)){
	        return;
	    }
	
	    if ( heap[position].getFValue(endNode) > heap[getLeftChild(position)].getFValue(endNode) || heap[position].getFValue(endNode) > heap[getRightChild(position)].getFValue(endNode)){
	
	        if(heap[getLeftChild(position)].getFValue(endNode) < heap[getRightChild(position)].getFValue(endNode)){
	            swap(position , getLeftChild(position));
	            heapify(getLeftChild(position));
	        }
	        else{
	            swap(position , getRightChild(position));
	            heapify(getRightChild(position));
	        }
	    }
	}
	
	@Override
	public String toString(){
	    StringBuilder output = new StringBuilder();
	    for(int i=1; i<= size/2; i++){
	        output.append("Parent :"+ heap[i]);
	        output.append("LeftChild : "+heap[getLeftChild(i)] +" RightChild :"+ heap[getRightChild(i)]).append("\n");
	    }
	    return output.toString();
	}
	
	public boolean isEmpty(){
		return size == 0;
	}
	
	public int indexOf(Node n){
		for(int i=0; i<=size; i++){
			if(heap[i] != null && heap[i].equals(n))
				return i;
		}
		return -1;
	}
	
	public boolean contains(Node n){
		for(int i=0; i<=size; i++){
			if(heap[i] != null && heap[i].equals(n))
				return true;
		}
		return false;
	}
	
	public void delete(Node n){
		int index = indexOf(n);
		if(index >= 0){
			heap[index] = heap[size-1];
		}
		heapify(index);
	}
}
