import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
public class Junk {
  public static void main( String [] args) {
    calculateKmeans();
    
  }
  public static double[] minRowIndex (double[][] n) {
    double[] result = new double[n.length];
    double[] resultIndex = new double[n.length];
    for (int i = 0; i < n.length; i++) {
      double min = n[i][0];
      int minIndex = 0;
      for (int j = 0; j < n[0].length; j++) {
        if (n[i][j] < min) {
          min = n[i][j];
          minIndex = j;         
        }
      }    
      result[i] = min;
      resultIndex[i] = minIndex;
    }
    return resultIndex;
  }
  public static double calculateEuclidian(double[] a, double[] b) {
		double vectorSum = 0.0;
		for (int j = 0; j < a.length; j++) {
			double v = (b[j] - a[j]) * (b[j] - a[j]);
			vectorSum = vectorSum + v;						
		}
		double euclidianDistance = Math.sqrt(vectorSum);
		return euclidianDistance;
	}
  
  public static boolean hasConverged(double[] previousCentroid, double[] currentCentroid) {
		boolean checkConverge = false;
		double threshold = 0.1;
		double euclidianDistance = calculateEuclidian(previousCentroid , currentCentroid);
		if (euclidianDistance <= threshold) {
			checkConverge = true;
		}						
		return checkConverge;
	}
  
  public static double[] sumOfCols(double[][] arr) {
	  double sum[] = new double[2];	  
	  for (int i = 0; i < arr.length; i++){
	        for (int j = 0; j < arr[i].length; j++){
	        	sum[j] += arr[i][j];  
	        }
	    }
	return sum;
}
	  
  public static double[] meanOfVectors(double [] arr, int count) {
	  double mean[] = new double[2];	  
	  for (int i = 0; i < arr.length; i++){
	        
	        	mean[i] = arr[i]/count;  
	        }
	    
	return mean;
} 
  
  public static void calculateKmeans() {
	 double[][] wordMatrix = { {5,3}, {10,15}, {15,12}, {24,10}, {30,45}, {85,70}, {71,80}, {60,78}, {55,52}, {80,91} };
	 double[] centroid_0 = wordMatrix[0];
	 double[] centroid_1 = wordMatrix[1];
	 double[] newCentroid_0 = new double[2];
	 double[] newCentroid_1 = new double[2];
	 boolean checkConverge = hasConverged(newCentroid_1,centroid_1);
	 System.out.println(checkConverge);
	
	 while (checkConverge == false ) {
	 double[][] eclidianMatrix = new double[10][2];	  
	  for (int i = 0; i< 10; i++) {
		  double[] currentData = wordMatrix[i];
		  double euclidianDistance1 =  calculateEuclidian(centroid_0, currentData);
		  double euclidianDistance2 =  calculateEuclidian(centroid_1, currentData);
		  eclidianMatrix[i][0] = euclidianDistance1;
		  eclidianMatrix[i][1] = euclidianDistance2;		  
	  }
	  System.out.println(Arrays.deepToString(eclidianMatrix));
	  System.out.println("cluster assignments for each data point for this iteration...");
	  System.out.println(Arrays.toString(Junk.minRowIndex(eclidianMatrix)));
	  double[] clusterArray = Junk.minRowIndex(eclidianMatrix);
	  System.out.println("Calculating number of data points in each cluster...");
	  int count_0 = 0;
	  int count_1 = 0;	
	  
	  for (int j=0; j <clusterArray.length;j++) {
		  if (clusterArray[j]== 0.0) {
			  count_0++;
		  }else {
			  count_1++; 
		  }		  
	  }
	  System.out.println(count_0);
	  System.out.println(count_1);
	  
	  double[][] tmpCluster_0 = new double[10][2];
	  double[][] tmpCluster_1 = new double[10][2];
	  
	  System.out.println("Grouping Clusters...");
	  //double[] sumOfCoordinates = new double[2];
	  int x = 0;
	  for (int k = 0; k < clusterArray.length; k++) {		  
		  if (clusterArray[k] == 0.0) {
			  tmpCluster_0[x] = wordMatrix[k];	
			  x++;
		  }else {
			  tmpCluster_1[x] = wordMatrix[k]; 
			  x++;
		  }		 
	  }	  
	  System.out.println(Arrays.deepToString(tmpCluster_0));  
	  System.out.println(Arrays.deepToString(tmpCluster_1));
	  
	  System.out.println("Calculating sum of cooordinates...");	  
	  double[] sumOfCoordinates_0 = sumOfCols(tmpCluster_0);
	  double[] sumOfCoordinates_1 = sumOfCols(tmpCluster_1);
	  System.out.println(Arrays.toString(sumOfCoordinates_0));
	  System.out.println(Arrays.toString(sumOfCoordinates_1));	
	  
	  System.out.println("Calculating new centroid...");
	  
	  double[] meanOfCoordinates_0 = meanOfVectors(sumOfCoordinates_0, count_0);
	  double[] meanOfCoordinates_1 = meanOfVectors(sumOfCoordinates_1, count_1);
	  System.out.println(Arrays.toString(meanOfCoordinates_0));
	  System.out.println(Arrays.toString(meanOfCoordinates_1));
	  
	  newCentroid_0 = meanOfCoordinates_0;
	  newCentroid_1 = meanOfCoordinates_1;
	  checkConverge = hasConverged(newCentroid_1, centroid_1);
	  System.out.println(hasConverged(newCentroid_0, centroid_0));
	  System.out.println(hasConverged(newCentroid_1, centroid_1));
	  centroid_0 = newCentroid_0;
	  centroid_1 = newCentroid_1;	  
	  
	 }  
	 
	 
  }
  
  
}