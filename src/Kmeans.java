
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
	private static Map<String, List<String>> dictionary = new HashMap<String, List<String>>();

	public static Elements fetchText(String connectionUrl) throws IOException {
		Document document = Jsoup.connect(connectionUrl).get();			  
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
		minTokenSize = 6;
		maxTokenSize = 15;

		//converting the words to lowercase and filtering out words with length less than 6
		//filtering out stopwords
		paragraphs = fetchText(webPage);
		stringHtml = cleanText(paragraphs);
		stHtml = tokenizeText(stringHtml);

		while (stHtml.hasMoreTokens()) {  
			String cleanToken =  stHtml.nextToken().toLowerCase();		
			if(!(cleanToken.length() <= minTokenSize || stopWords.contains(cleanToken)  || cleanToken.length() > maxTokenSize) ) {
				cleanTokens.add(cleanToken.trim());
			}			
		} 
		return cleanTokens;
	}	

	public static List<String> mergeDocuments(String[] webPageList) throws IOException {
		List<String> mergeWords = new ArrayList<>();
		List<Integer> lengthOfDocs = new ArrayList<>();	
		for (String webPage : webPageList) {
			List<String> cleanTokens = normalizeText(webPage);
			mergeWords.addAll((cleanTokens));		
			lengthOfDocs.add(cleanTokens.size());
			dictionary.put(webPage,cleanTokens);	
		}		
		System.out.println("Number of words in each Document : " + lengthOfDocs.toString());
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
		for (int j = 0; j < a.length; j++) {
			double v =  b[j] * a[j];
			vectorSum = vectorSum + v;						
		}
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

	public static boolean hasConverged(double[] previousCentroid, double[] currentCentroid, boolean cosine) {
		boolean checkConverge = false;
		double threshold = 0.1;
		double euclidianDistance = 0;

		if (cosine) {
			euclidianDistance = calculateCosineDist(previousCentroid , currentCentroid);
		} else {
			euclidianDistance = calculateEuclidian(previousCentroid , currentCentroid);
		}
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
			numbers[i] = (int)(Math.random() * 20 + 1);
			vecAbsSize = numbers[i] * numbers[i];
		}
		vecAbsSize = Math.sqrt(vecAbsSize);

		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = numbers[i] / vecAbsSize;
		}
		return numbers;
	}

	public static void calculateKmeans(double[][] wordMatrix) {
		double[] centroid_0 = wordMatrix[1];
		double[] centroid_1 = wordMatrix[4];
		double[] centroid_2 = wordMatrix[6];

		//		 double[] centroid_0 = generateRandom(unique.size());
		//	  	 double[] centroid_1 = generateRandom(unique.size());
		//	  	 double[] centroid_2 = generateRandom(unique.size());

		double[] newCentroid_0 = new double[unique.size()];
		double[] newCentroid_1 = new double[unique.size()];
		double[] newCentroid_2 = new double[unique.size()];

		boolean doCosine = true;

		boolean checkConverge_0 = hasConverged(newCentroid_0,centroid_0, doCosine);
		boolean checkConverge_1 = hasConverged(newCentroid_1,centroid_1, doCosine);
		boolean checkConverge_2 = hasConverged(newCentroid_2,centroid_2, doCosine);

		int nIteration = 0;

		while (checkConverge_0 == false || checkConverge_1 == false || checkConverge_2 == false ) {
			double[][] eclidianMatrix = new double[10][3];	  
			for (int i = 0; i< 10; i++) {
				double[] currentData = wordMatrix[i];
				double euclidianDistance1 = 0;
				double euclidianDistance2 = 0;
				double euclidianDistance3 = 0;
				if (!doCosine) {
					euclidianDistance1 =  calculateEuclidian(centroid_0, currentData);
					euclidianDistance2 =  calculateEuclidian(centroid_1, currentData);
					euclidianDistance3 =  calculateEuclidian(centroid_2, currentData);
				} else {
					euclidianDistance1 =  calculateCosineDist(centroid_0, currentData);
					euclidianDistance2 =  calculateCosineDist(centroid_1, currentData);
					euclidianDistance3 =  calculateCosineDist(centroid_2, currentData);
				}

				eclidianMatrix[i][0] = euclidianDistance1;
				eclidianMatrix[i][1] = euclidianDistance2;
				eclidianMatrix[i][2] = euclidianDistance3;
			}

			System.out.println("=================================================================");
			System.out.println("Distance of each document from every cluster...");
			System.out.println(Arrays.deepToString(eclidianMatrix));

			System.out.println("Cluster assignments for each Document in this iteration...");

			double[] clusterArray = minRowIndex(eclidianMatrix);
			System.out.println(Arrays.toString(clusterArray));

			int count_0 = 0;
			int count_1 = 0;	
			int count_2 = 0;

			List<double[]> tmpCluster_0 = new ArrayList<double[]>();
			List<double[]> tmpCluster_1 = new ArrayList<double[]>();
			List<double[]> tmpCluster_2 = new ArrayList<double[]>();

			System.out.println("Grouping Documents into respective Clusters...");
			for (int k = 0; k < clusterArray.length; k++) {		  
				if (clusterArray[k] == 0.0) {
					tmpCluster_0.add(wordMatrix[k]);
					count_0++;
				}else if (clusterArray[k] == 1.0) {
					tmpCluster_1.add(wordMatrix[k]); 
					count_1++; 
				}	else {
					tmpCluster_2.add(wordMatrix[k]);
					count_2++; 
				}
			}
			System.out.println("Calculating number of Documents in each cluster...");

			System.out.println("Number of documents in cluster 0 : " + count_0);
			System.out.println("Number of documents in cluster 1 : " +count_1);
			System.out.println("Number of documents in cluster 2 : " +count_2);

			System.out.println("Calculating sum of cooordinates...");	  

			double[] sumOfCoordinates_0 = sumOfCols(tmpCluster_0);
			double[] sumOfCoordinates_1 = sumOfCols(tmpCluster_1);
			double[] sumOfCoordinates_2 = sumOfCols(tmpCluster_2);

			System.out.println("Calculating new centroid...");

			double[] meanOfCoordinates_0 = meanOfVectors(sumOfCoordinates_0, count_0);
			double[] meanOfCoordinates_1 = meanOfVectors(sumOfCoordinates_1, count_1);
			double[] meanOfCoordinates_2 = meanOfVectors(sumOfCoordinates_2, count_2);

			newCentroid_0 = meanOfCoordinates_0;
			newCentroid_1 = meanOfCoordinates_1;
			newCentroid_2 = meanOfCoordinates_2;

			checkConverge_0 = hasConverged(newCentroid_0, centroid_0, doCosine);
			checkConverge_1 = hasConverged(newCentroid_1, centroid_1, doCosine);
			checkConverge_2 = hasConverged(newCentroid_2, centroid_2, doCosine);

			System.out.println("Convergence status cluster 0 : " + checkConverge_0);
			System.out.println("Convergence status cluster 1 : " + checkConverge_1);
			System.out.println("Convergence status cluster 2 : " + checkConverge_2);

			centroid_0 = newCentroid_0;
			centroid_1 = newCentroid_1;
			centroid_2 = newCentroid_2;
			nIteration++;

			System.out.println("Completed iteration number : "+ nIteration);
			System.out.println("===========================================");
		}  
	}

	public static void main(String[] args) throws IOException {
		populateStopWords(stopWords);
		String[] webPageList = new String[10];
		webPageList[0] = "https://www.csd.cs.cmu.edu/people/faculty/umut-acar";
		webPageList[1] = "https://www.archive.ece.cmu.edu/~ganger/";
		webPageList[2] = "http://www.cs.cmu.edu/~gibbons/";
		webPageList[3] = "http://www.cs.cmu.edu/~garth/";
		webPageList[4] = "http://www.cs.cmu.edu/~ebrun/";
		webPageList[5] = "http://www.rr.cs.cmu.edu/";
		webPageList[6] = "https://homes.cs.washington.edu/~nasmith/";
		webPageList[7] = "http://www.stat.cmu.edu/~jiashun/";
		webPageList[8] = "http://www.stat.cmu.edu/~cshalizi/";
		webPageList[9] = "http://www.stat.cmu.edu/~pfreeman/";

		List<String> mergeWords = mergeDocuments(webPageList);
		double[][] wordMatrix = vectorizeDocuments(mergeWords, webPageList);
		calculateKmeans(wordMatrix);
	}
}


