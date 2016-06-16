package bot2_0;

public class CheckDist {
	
	public CheckDist(){
	}
		
	public void labelDistance(Node node, int distance){
		
		if(node.north().isWalkable()){
			if(node.north().getDistance() > distance + 1){
				node.north().setDistance(distance+1);
			}
			labelDistance(Node node.north(), node.north().getDistance());
		}
		
		
		
		if(node.south().isWalkable()){
			if(node.south().getDistance() > distance + 1){
				node.south().setDistance(distance+1);
			}
			labelDistance(Node node.south(), node.south().getDistance());
		}
		
		
		if(node.west().isWalkable()){
			if(node.west().getDistance()> distance + 1){
				node.west().setDistance(distance+1);
			}
			labelDistance(Node node.west(), node.west().getDistance());
		}

		
		if(node.east().isWalkable()){
			if(node.east().getDistance()> distance + 1){
				node.east().setDistance(distance+1);
			}

			labelDistance(Node node.east(), node.east().getDistance());
		}
	}

}
