
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
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
	private static Set<String> unique = new HashSet<String>();

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

	public static List<String> normalizeText(StringTokenizer stHtml) {
		List<String> cleanTokens = new ArrayList<>();
		minTokenSize = 6;
		//converting the words to lowercase and filtering out words with length less than 6
		//filtering out stopwords	

		while (stHtml.hasMoreTokens()) {  
			String cleanToken =  stHtml.nextToken().toLowerCase();		
			if(!(cleanToken.length() <= minTokenSize || stopWords.contains(cleanToken)) ) {
				cleanTokens.add(cleanToken.trim());
			}			
		}       
		return cleanTokens;
	}	

	public static List<String> mergeDocuments(String[] webPageList) throws IOException {
		List<String> mergeWords = new ArrayList<>();
		List<Integer> lengthOfDocs = new ArrayList<>();	
		for (String webPage : webPageList) {
			System.out.println(webPage);
			paragraphs = fetchText(webPage);
			stringHtml = cleanText(paragraphs);
			stHtml = tokenizeText(stringHtml);
			List<String> cleanTokens = normalizeText(stHtml);
			System.out.println((cleanTokens).toString());						
			mergeWords.addAll((cleanTokens));		
			lengthOfDocs.add(cleanTokens.size());					
		}		
		System.out.println(mergeWords.toString());
		System.out.println(lengthOfDocs.toString());
		return mergeWords;

	}

	public static int[][] vectorizeDocuments(List<String> mergeWords, String[] webPageList) throws IOException {		
		unique = new HashSet<String>(mergeWords);
		int nRow = 2;
		int nCol = unique.size();
		int[][] wordMatrix = new int[nRow][nCol];
		int j = 0;
		for (String word : unique) {
			List<String> doc = new ArrayList<>();
			int i=0;
			for (String webPage : webPageList) {
				paragraphs = fetchText(webPage);
				stringHtml = cleanText(paragraphs);
				stHtml = tokenizeText(stringHtml);
				doc = normalizeText(stHtml);
				System.out.println(word + ": " + Collections.frequency(doc, word));
				wordMatrix[i][j] = Collections.frequency(doc, word);
				//System.out.println(wordMatrix[i][j]);
				i++;
			}											
			j++;
		}
		return wordMatrix;				
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

	public static double[] findMinInCol(double[][] arr) {

		double[] minVector = new double[unique.size()];

		for (int i = 0; i < arr.length; i++) {
			double min = arr[0][i];
			for (int j = 0; j < arr[0].length; j++) {
				if (arr[j][i] < min) {
					min = arr[j][i];
				}
			}
			minVector[i] = min;
		}
		return minVector;
	}


	
	public static void main(String[] args) throws IOException {
		populateStopWords(stopWords);
		String[] webPageList = new String[2];
		webPageList[0] = "http://www.cs.cmu.edu/~ninamf/";
		webPageList[1] = "http://www.cs.cmu.edu/~awm/biography.html";
		List<String> mergeWords = mergeDocuments(webPageList);
		System.out.println(mergeWords.size());
		int[][] wordMatrix = vectorizeDocuments(mergeWords, webPageList);
		//calculateKmeans(wordMatrix);

	}


}


