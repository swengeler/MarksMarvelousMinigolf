/*
package bot2_0;

public class CheckDist {
	
	public CheckDist(){
	}
		
	public void labelDistance(Node node, float distance){
		
		if(node.getNorth().isWalkable()){
			if(node.getNorth().getDistance() > distance + 1){
				node.getNorth().setDistance(distance+1);
			}
			labelDistance(node.getNorth(), node.getNorth().getDistance());
		}
		
		
		
		if(node.getSouth().isWalkable()){
			if(node.getSouth().getDistance() > distance + 1){
				node.getSouth().setDistance(distance+1);
			}
			labelDistance(node.getSouth(), node.getSouth().getDistance());
		}
		
		
		if(node.getWest().isWalkable()){
			if(node.getWest().getDistance()> distance + 1){
				node.getWest().setDistance(distance+1);
			}
			labelDistance(node.getWest(), node.getWest().getDistance());
		}

		
		if(node.getEast().isWalkable()){
			if(node.getEast().getDistance()> distance + 1){
				node.getEast().setDistance(distance+1);
			}

			labelDistance(node.getEast(), node.getEast().getDistance());
		}
	}

}
*/
