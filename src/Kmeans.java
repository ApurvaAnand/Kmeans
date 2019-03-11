
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Kmeans {

	private static Elements paragraphs;
	private static StringTokenizer stHtml;
	private static String stringHtml;
	private static List<String> stopWords = new ArrayList<>();
	private static int minTokenSize;
	private static int maxTokenSize;
	private static Set<String> unique = new HashSet<String>();
	//private static List<String> cleanTokens = new ArrayList<>(); 
	private static Map<String, List<String>> dictionary = new HashMap<String, List<String>>();

	public static Elements fetchText(String connectionUrl) throws IOException {
		Document document = Jsoup.connect(connectionUrl).get();			  
		//System.out.println(document.title());
		paragraphs = document.getAllElements();
		return paragraphs;
	}

	public static String cleanText(Elements paragraphs) throws IOException {	
		StringBuilder sbHtml = new StringBuilder();
		for (Element paragraph : paragraphs) {
			sbHtml.append(paragraph.ownText().replaceAll("[0-9=%$-.*›,:;—@#^?)]", " ")); 
		}
		stringHtml = sbHtml.toString().trim().replaceAll(" +", " ");
		return stringHtml;
	}

	public static StringTokenizer tokenizeText(String stringHtml) throws IOException {
		StringTokenizer stHtml = new StringTokenizer(stringHtml," ");
		return stHtml;	
	}

	public static void populateStopWords(List<String> stopWords) throws FileNotFoundException {
		Scanner s = new Scanner(new File("StopWords.txt"));
		while (s.hasNext()) {
			stopWords.add(s.next());
		}			
	}

	public static List<String> normalizeText(String webPage) throws IOException {
		List<String> cleanTokens = new ArrayList<>();
		minTokenSize = 2;
		maxTokenSize = 15;
		
		//converting the words to lowercase and filtering out words with length less than 6
		//filtering out stopwords
		paragraphs = fetchText(webPage);
		stringHtml = cleanText(paragraphs);
		stHtml = tokenizeText(stringHtml);
		System.out.println(stHtml);

		while (stHtml.hasMoreTokens()) {  
			String cleanToken =  stHtml.nextToken().toLowerCase();		
			if(!(cleanToken.length() <= minTokenSize || stopWords.contains(cleanToken)  || cleanToken.length() > maxTokenSize) ) {
				cleanTokens.add(cleanToken.trim());
			}			
		} 
		System.out.println(cleanTokens) ;   
		return cleanTokens;
	}	

	public static List<String> mergeDocuments(String[] webPageList) throws IOException {
		List<String> mergeWords = new ArrayList<>();
		List<Integer> lengthOfDocs = new ArrayList<>();	
		for (String webPage : webPageList) {
			System.out.println(webPage);
			List<String> cleanTokens = normalizeText(webPage);
			mergeWords.addAll((cleanTokens));		
			lengthOfDocs.add(cleanTokens.size());
			dictionary.put(webPage,cleanTokens);	
		}		
		System.out.println(mergeWords.toString());
		System.out.println(lengthOfDocs.toString());
		return mergeWords;

	}

	

	public static double[][] vectorizeDocuments(List<String> mergeWords, String[] webPageList) throws IOException {		
		unique = new HashSet<String>(mergeWords);
		
		int nRow = 10;
		int nCol = unique.size();
		double[] vecAbsSize = new double[nRow];
		double[][] wordMatrix = new double[nRow][nCol];
		int j = 0;
		for (String word : unique) {
			int i=0;
			for (String webPage : dictionary.keySet()) {
				List<String> values = dictionary.get(webPage);
				//System.out.println(word + ": " + Collections.frequency(values, word));
				
				wordMatrix[i][j] = Collections.frequency(values, word);
				vecAbsSize[i] += wordMatrix[i][j] * wordMatrix[i][j];
				i++;
			}											
			j++;
		}
		
		for (int i =0; i < nRow; i++) {
			vecAbsSize[i] = Math.sqrt(vecAbsSize[i]);
		}
		
		for (int i =0; i < nRow; i++) {
			for (int k =0; k < unique.size(); k++) {
				wordMatrix[i][k] = wordMatrix[i][k] / vecAbsSize[i];				
			}
			
		}
		
		return wordMatrix;				
	}
	
	public static double calculateCosineDist(double[] a, double[] b) {
		double vectorSum = 0.0;
//		double bAbsSize = 0.0;
//		double aAbsSize = 0.0;
		
		for (int j = 0; j < a.length; j++) {
			double v =  b[j] * a[j];
			vectorSum = vectorSum + v;						
		}
		//double euclidianDistance = Math.sqrt(vectorSum);
		return 1-vectorSum;
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
		double threshold = 0.0000001;
		//double euclidianDistance = calculateEuclidian(previousCentroid , currentCentroid);
		double euclidianDistance = calculateCosineDist(previousCentroid , currentCentroid);
		System.out.println(euclidianDistance);
		if (euclidianDistance <= threshold) {
			checkConverge = true;
		}						
		return checkConverge;
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
	
	public static double[] sumOfCols(double[][] arr) {
		  double sum[] = new double[unique.size()];	  
		  for (int i = 0; i < arr.length; i++){
		        for (int j = 0; j < arr[i].length; j++){
		        	sum[j] += arr[i][j];  
		        }
		    }
		return sum;
	}
	
	public static double[] sumOfCols( List<double[]> arr) {
		  double sum[] = new double[unique.size()];	
		  for (double[] element : arr) {
			  for (int j = 0; j < element.length; j++){
		        	sum[j] += element[j];  
		        }			  
		  }
		  
		return sum;
	}
		  
	  public static double[] meanOfVectors(double [] arr, int count) {
		  double mean[] = new double[unique.size()];	
		  double vecAbsSize = 0;
		  if (count == 0) {
			  return generateRandom(unique.size());
		  }
		  for (int i = 0; i < arr.length; i++){		        
		        	mean[i] = arr[i]/count;
		        	vecAbsSize += mean[i] * mean[i];
		        }
		  vecAbsSize = Math.sqrt(vecAbsSize);
		  
		  for (int i = 0; i < mean.length; i++) {
			   mean[i] = mean[i] / vecAbsSize;
			   
		   }
		return mean;
	} 
	  
	  
	  public static double[] generateRandom(int size) {
		  double[] numbers = new double[size];  
		  double vecAbsSize = 0;
		  for(int i = 0; i < numbers.length; i++) {
			  numbers[i] = (int)(Math.random()*20 + 1);
			  vecAbsSize = numbers[i] * numbers[i];
		  }
		  vecAbsSize = Math.sqrt(vecAbsSize);
		  
		  for (int i = 0; i < numbers.length; i++) {
			  numbers[i] = numbers[i] / vecAbsSize;
		   }
		  
		  return numbers;

	  }
	  public static void calculateKmeans(double[][] wordMatrix) {
			 
//			 double[] centroid_0 = wordMatrix[0];
//			 double[] centroid_1 = wordMatrix[5];
//			 double[] centroid_2 = wordMatrix[8];
//			 
		  	 double[] centroid_0 = generateRandom(unique.size());
		  	 double[] centroid_1 = generateRandom(unique.size());
		  	 double[] centroid_2 = generateRandom(unique.size());
//		  
		  
			 
			 double[] newCentroid_0 = new double[unique.size()];
			 double[] newCentroid_1 = new double[unique.size()];
			 double[] newCentroid_2 = new double[unique.size()];
			 
			 
			 boolean checkConverge_0 = hasConverged(newCentroid_0,centroid_0);
			 boolean checkConverge_1 = hasConverged(newCentroid_1,centroid_1);
			 boolean checkConverge_2 = hasConverged(newCentroid_2,centroid_2);
			 
			 int nIteration = 0;
			 System.out.println(checkConverge_0);
			 System.out.println(checkConverge_1);
			 System.out.println(checkConverge_2);
			 
			 while (checkConverge_0 == false || checkConverge_1 == false || checkConverge_2 == false ) {
				 double[][] eclidianMatrix = new double[10][3];	  
				 for (int i = 0; i< 10; i++) {
					 double[] currentData = wordMatrix[i];
//					 double euclidianDistance1 =  calculateEuclidian(centroid_0, currentData);
//					 double euclidianDistance2 =  calculateEuclidian(centroid_1, currentData);
//					 double euclidianDistance3 =  calculateEuclidian(centroid_2, currentData);
					 
					 double euclidianDistance1 =  calculateCosineDist(centroid_0, currentData);
					 double euclidianDistance2 =  calculateCosineDist(centroid_1, currentData);
					 double euclidianDistance3 =  calculateCosineDist(centroid_2, currentData);
					 
					 
					 
					 eclidianMatrix[i][0] = euclidianDistance1;
					 eclidianMatrix[i][1] = euclidianDistance2;
					 eclidianMatrix[i][2] = euclidianDistance3;
				 }
				 System.out.println(Arrays.deepToString(eclidianMatrix));
				 System.out.println("cluster assignments for each data point for this iteration...");
				 
				 double[] clusterArray = minRowIndex(eclidianMatrix);
				 System.out.println(Arrays.toString(clusterArray));
				 System.out.println("Calculating number of data points in each cluster...");

				 int count_0 = 0;
				 int count_1 = 0;	
				 int count_2 = 0;

//				 for (int j=0; j <clusterArray.length;j++) {
//					 if (clusterArray[j]== 0.0) {
//						 count_0++;
//					 }else if (clusterArray[j]== 1.0){
//						 count_1++; 
//					 }	else {
//						 count_2++; 
//					 }
//				 }
//				 System.out.println(count_0);
//				 System.out.println(count_1);

//				 double[][] tmpCluster_0 = new double[10][unique.size()];
//				 double[][] tmpCluster_1 = new double[10][unique.size()];
//				 double[][] tmpCluster_2 = new double[10][unique.size()];
				 
				 List<double[]> tmpCluster_0 = new ArrayList<double[]>();
				 List<double[]> tmpCluster_1 = new ArrayList<double[]>();
				 List<double[]> tmpCluster_2 = new ArrayList<double[]>();

				 System.out.println("Grouping Clusters...");
				 //double[] sumOfCoordinates = new double[2];
				 int x = 0;
				 for (int k = 0; k < clusterArray.length; k++) {		  
					 if (clusterArray[k] == 0.0) {
						 tmpCluster_0.add(wordMatrix[k]);
						 count_0++;
						 x++;
					 }else if (clusterArray[k] == 1.0) {
						 tmpCluster_1.add(wordMatrix[k]); 
						 count_1++; 
						 x++;
					 }	else {
						 tmpCluster_2.add(wordMatrix[k]);
						 count_2++; 
						 x++;

					 }
				 }
				 
				 System.out.println(count_0);
				 System.out.println(count_1);
				 System.out.println(count_2);
				 
//				 System.out.println(Arrays.deepToString(tmpCluster_0));  
//				 System.out.println(Arrays.deepToString(tmpCluster_1));
//				 System.out.println(Arrays.deepToString(tmpCluster_2));
				
//				 System.out.println(Arrays.deepToString(tmpCluster_0.toArray()));  
//				 System.out.println(Arrays.deepToString(tmpCluster_1.toArray()));
//				 System.out.println(Arrays.deepToString(tmpCluster_2.toArray()));

				 System.out.println("Calculating sum of cooordinates...");	  
//				 double[] sumOfCoordinates_0 = sumOfCols(tmpCluster_0);
//				 double[] sumOfCoordinates_1 = sumOfCols(tmpCluster_1);
//				 double[] sumOfCoordinates_2 = sumOfCols(tmpCluster_2);

				 double[] sumOfCoordinates_0 = sumOfCols(tmpCluster_0);
				 double[] sumOfCoordinates_1 = sumOfCols(tmpCluster_1);
				 double[] sumOfCoordinates_2 = sumOfCols(tmpCluster_2);
				 
//				 System.out.println(Arrays.toString(sumOfCoordinates_0));
//				 System.out.println(Arrays.toString(sumOfCoordinates_1));
//				 System.out.println(Arrays.toString(sumOfCoordinates_2));

				 System.out.println("Calculating new centroid...");

				 double[] meanOfCoordinates_0 = meanOfVectors(sumOfCoordinates_0, count_0);
				 double[] meanOfCoordinates_1 = meanOfVectors(sumOfCoordinates_1, count_1);
				 double[] meanOfCoordinates_2 = meanOfVectors(sumOfCoordinates_2, count_2);

//				 System.out.println(Arrays.toString(meanOfCoordinates_0));
//				 System.out.println(Arrays.toString(meanOfCoordinates_1));
//				 System.out.println(Arrays.toString(meanOfCoordinates_2));

				 newCentroid_0 = meanOfCoordinates_0;
				 newCentroid_1 = meanOfCoordinates_1;
				 newCentroid_2 = meanOfCoordinates_2;

				 checkConverge_0 = hasConverged(newCentroid_0, centroid_0);
				 checkConverge_1 = hasConverged(newCentroid_1, centroid_1);
				 checkConverge_2 = hasConverged(newCentroid_2, centroid_2);

				 System.out.println(checkConverge_0);
				 System.out.println(checkConverge_1);
				 System.out.println(checkConverge_2);

				 centroid_0 = newCentroid_0;
				 centroid_1 = newCentroid_1;
				 centroid_2 = newCentroid_2;
				 nIteration++;
				 System.out.println(nIteration);

			 }  
			 
			 
		  }


	
	public static void main(String[] args) throws IOException {
		populateStopWords(stopWords);
		String[] webPageList = new String[10];
		webPageList[0] = "https://www.csd.cs.cmu.edu/people/faculty/umut-acar";
		webPageList[1] = "https://www.archive.ece.cmu.edu/~ganger/";
		webPageList[2] = "http://www.cs.cmu.edu/~gibbons/";
		webPageList[3] = "http://www.cs.cmu.edu/~garth/";
		//webPageList[4] = "https://web.stanford.edu/group/bergmann/cgi-bin/bergmannlab/research";
		webPageList[4] = "https://web.stanford.edu/~fukamit/research.htm";
		webPageList[5] = "http://www.dixonlaboratory.com/research-1#research";
		//webPageList[6] = "http://fernaldlab.stanford.edu/research/reproduction/";
		webPageList[6] = "https://gozani-lab-website.github.io/website/research.html";
		webPageList[7] = "http://www.stat.cmu.edu/~jiashun/";
		webPageList[8] = "http://www.stat.cmu.edu/~cshalizi/";
		webPageList[9] = "http://www.stat.cmu.edu/~pfreeman/";
		
//		webPageList[4] = "http://www.cs.cmu.edu/~ebrun/";
//		webPageList[5] = "http://www.rr.cs.cmu.edu/";
//		webPageList[6] = "https://homes.cs.washington.edu/~nasmith/";
//		
		
		List<String> mergeWords = mergeDocuments(webPageList);
		System.out.println(mergeWords.size());
		double[][] wordMatrix = vectorizeDocuments(mergeWords, webPageList);
		calculateKmeans(wordMatrix);

	}


}


