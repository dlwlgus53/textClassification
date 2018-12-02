package BugSeverityPrediction ;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File ;
import org.apache.commons.configuration.* ;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.trees.J48;
import weka.classifiers.functions.SMO ;
import weka.classifiers.bayes.NaiveBayes ;
import weka.core.FastVector;
import weka.core.Instances;
 
public class Classification {

	public static PropertiesConfiguration loadConfig(String fname)
	{
		PropertiesConfiguration config = null ;
		try {
			config = new PropertiesConfiguration(fname) ;
		}
		catch(ConfigurationException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}
		return config ;
	}
 

	public static Evaluation classify(Classifier model, Instances trainingSet, Instances testingSet) throws Exception 
	{
		Evaluation evaluation = new Evaluation(trainingSet);
		model.buildClassifier(trainingSet);
		evaluation.evaluateModel(model, testingSet);
 
		return evaluation;//평가하기
	}
 
	public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds)
	{
		Instances[][] split = new Instances[2][numberOfFolds];
 
		for (int i = 0; i < numberOfFolds; i++) {
			split[0][i] = data.trainCV(numberOfFolds, i);
			split[1][i] = data.testCV(numberOfFolds, i);
		}
 
		return split;//쪼개는 것! 10조각중에 1조각  그래서 어레이에 넣어주는것흑흙
	}

	public static void showPredictionResult(FastVector predictions) {
		double allCases = predictions.size() ;//전체경우의 수 필요없음
		
		double truePositive = 0 ;
		double trueNegative = 0 ;
		double falsePositive = 0 ;
		double falseNegative = 0 ;
 
		for (int i = 0; i < predictions.size(); i++) {
			NominalPrediction np = (NominalPrediction) predictions.elementAt(i);//np : prediction 한 수
			if (np.predicted() == np.actual()) {//정답과 prediction 맞은거
				if (np.predicted() == 1.0)///맞은건데 positive
					truePositive += 1.0 ;
				else
					trueNegative += 1.0 ;//맞은건데 negative

			}
			else {
				if (np.predicted() == 1.0) 
					falsePositive += 1.0 ;
				else
					falseNegative += 1.0 ;//fase negative
			}
		}
		//a : 위험하다 예측 A : 실제 위험한 것
		//b : normal로 예측 B : 실제로 normal
		//preceison : a/A
		//Recall : a+b/A+B
		System.out.println("-------------------------------------") ;
		System.out.println("Accuracy : " + ((double) (truePositive + trueNegative) / allCases * 100.0)) ;//전체중에 맞은거
		System.out.println("Precision: " + ((double) (truePositive) / (truePositive + falsePositive) * 100.0)) ;
		//맞다고 한 것중에 진짜 맞은건 몇개있는거(위험한 것이라고 한 것 중에 진짜 위험한건 몇개였는가)

		System.out.println("Recall   : " + ((double) (truePositive) / (truePositive + falseNegative) * 100.0)) ;//모든 정답중에 몇개를 맞추었는가

		
		System.out.println("-------------------------------------") ;
	}
 
	public static void main(String[] args) throws Exception 
	{
		PropertiesConfiguration config = loadConfig("config.properties") ;

		Instances data = new Instances(new BufferedReader(new FileReader(config.getString("arff.filename")))) ;
		
		data.setClassIndex(data.numAttributes() - 1);
			//몇번째 값을 라벨로 볼건가(멘마지막에 좋아한건지 아닌건지 넣을거다)
 
		Instances[][] split = crossValidationSplit(data, 10);
		Instances[] trainingSplits = split[0];
		Instances[] testingSplits = split[1];
			//weka가 파이를 나눠줌
		
		FastVector predictions = new FastVector();//array 인데 사이즈 늘어날 수 있는것

		Classifier classifier = new NaiveBayes() ;//겸손하게 생겼다.
		//Classifier classifier = new J48() ;	//decision tree
		//Classifier classifier = new SMO() ;	//supprot vector machine
		//셋 중 하나 사용하면 돼
		
		for (int i = 0; i < trainingSplits.length; i++) {
			Evaluation validation = classify(classifier, trainingSplits[i], testingSplits[i]);
 			predictions.appendElements(validation.predictions());
 		}
		showPredictionResult(predictions) ;
	}
}
