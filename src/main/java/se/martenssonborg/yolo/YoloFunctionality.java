package se.martenssonborg.yolo;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.datavec.api.records.metadata.RecordMetaDataImageURI;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.objdetect.ObjectDetectionRecordReader;
import org.datavec.image.recordreader.objdetect.impl.SvhnLabelProvider;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.fetchers.DataSetType;
import org.deeplearning4j.datasets.fetchers.SvhnDataFetcher;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.WorkspaceMode;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.objdetect.Yolo2OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.layers.objdetect.DetectedObject;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.zoo.model.YOLO2;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Random;

import static org.bytedeco.opencv.global.opencv_core.CV_8U;
import static org.bytedeco.opencv.global.opencv_core.RGB;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * Example transfer learning from a Tiny YOLO model pretrained on ImageNet and
 * Pascal VOC to perform object detection with bounding boxes on The Street View
 * House Numbers (SVHN) Dataset.
 * <p>
 * References: <br>
 * - YOLO: Real-Time Object Detection: https://pjreddie.com/darknet/yolo/ <br>
 * - The Street View House Numbers (SVHN) Dataset:
 * http://ufldl.stanford.edu/housenumbers/ <br>
 * <p>
 * Please note, cuDNN should be used to obtain reasonable performance:
 * https://deeplearning4j.konduit.ai/config/backends/config-cudnn#using-deeplearning-4-j-with-cudnn
 *
 * @author saudet
 */
public class YoloFunctionality {
	private static final Logger log = LoggerFactory.getLogger(YoloFunctionality.class);

	// Enable different colour bounding box for different classes
	public static final Scalar RED = RGB(255.0, 0, 0);
	public static final Scalar GREEN = RGB(0, 255.0, 0);
	public static final Scalar BLUE = RGB(0, 0, 255.0);
	public static final Scalar YELLOW = RGB(255.0, 255.0, 0);
	public static final Scalar CYAN = RGB(0, 255.0, 255.0);
	public static final Scalar MAGENTA = RGB(255.0, 0.0, 255.0);
	public static final Scalar ORANGE = RGB(255.0, 128.0, 0);
	public static final Scalar PINK = RGB(255.0, 192.0, 203.0);
	public static final Scalar LIGHTBLUE = RGB(153.0, 204.0, 255.0);
	public static final Scalar VIOLET = RGB(238.0, 130.0, 238.0);

	public static void testYoloWithSvhnData(){

		// parameters matching the pretrained YOLO2 model
		int widthCNN = 416;
		int heightCNN = 416;
		int nChannelsCNN = 3;
		int gridWidth = 13;
		int gridHeight = 13;

		// number classes (digits) for the SVHN datasets
		int nClasses = 10;

		// parameters for the Yolo2OutputLayer
		int nBoxes = 5;
		double lambdaNoObj = 0.5;
		double lambdaCoord = 1.0;
		double[][] priorBoxes = { { 2, 5 }, { 2.5, 6 }, { 3, 7 }, { 3.5, 8 }, { 4, 9 } };
		double detectionThreshold = 0.5;

		// parameters for the training phase
		int batchSize = 10;
		int nEpochs = 20;
		double learningRate = 1e-4;
		int seed = 123;
		Random rng = new Random(seed);

		// Data
		try {
			SvhnDataFetcher fetcher = new SvhnDataFetcher();
			File trainDir = fetcher.getDataSetPath(DataSetType.TRAIN);
			File testDir = fetcher.getDataSetPath(DataSetType.TEST);
	
			FileSplit trainData = new FileSplit(trainDir, NativeImageLoader.ALLOWED_FORMATS, rng);
			FileSplit testData = new FileSplit(testDir, NativeImageLoader.ALLOWED_FORMATS, rng);
	
			ObjectDetectionRecordReader recordReaderTrain = new ObjectDetectionRecordReader(heightCNN, widthCNN, nChannelsCNN, gridHeight, gridWidth, new SvhnLabelProvider(trainDir));
			recordReaderTrain.initialize(trainData);
	
			ObjectDetectionRecordReader recordReaderTest = new ObjectDetectionRecordReader(heightCNN, widthCNN, nChannelsCNN, gridHeight, gridWidth, new SvhnLabelProvider(testDir));
			recordReaderTest.initialize(testData);
	
			// ObjectDetectionRecordReader performs regression, so we need to specify it here
			RecordReaderDataSetIterator train = new RecordReaderDataSetIterator(recordReaderTrain, batchSize, 1, 1, true);
			train.setPreProcessor(new ImagePreProcessingScaler(0, 1));
	
			RecordReaderDataSetIterator test = new RecordReaderDataSetIterator(recordReaderTest, 1, 1, 1, true);
			test.setPreProcessor(new ImagePreProcessingScaler(0, 1));
			
			// Create model
			ComputationGraph model = null;
			String filePath = "...";
			
			// Build -> Train -> Save -> Validate
			buildModel(model, priorBoxes, seed, learningRate, nBoxes, nClasses, lambdaNoObj, lambdaCoord, heightCNN, widthCNN, nChannelsCNN);
			trainModel(model, train, nEpochs);
			saveModel(model, filePath);
			validateModel(model, train, test, detectionThreshold, gridWidth, gridHeight);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void buildModel(ComputationGraph model, double[][] priorBoxes, int seed, double learningRate, int nBoxes, int nClasses, double lambdaNoObj, double lambdaCoord, long heightCNN, long widthCNN, long nChannelsCNN) {
		log.info("Build model...");
		try {
			ComputationGraph pretrained = (ComputationGraph) YOLO2.builder().build().initPretrained();
			INDArray priors = Nd4j.create(priorBoxes);

			FineTuneConfiguration fineTuneConf = new FineTuneConfiguration.Builder()
					.seed(seed)
					.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
					.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
					.gradientNormalizationThreshold(1.0)
					.updater(new Adam.Builder().learningRate(learningRate).build())
					// .updater(new
					// Nesterovs.Builder().learningRate(learningRate).momentum(lrMomentum).build())
					.l2(0.00001)
					.activation(Activation.IDENTITY)
					.trainingWorkspaceMode(WorkspaceMode.ENABLED)
					.inferenceWorkspaceMode(WorkspaceMode.ENABLED)
					.build();

			model = new TransferLearning.GraphBuilder(pretrained)
					.fineTuneConfiguration(fineTuneConf)
					.removeVertexKeepConnections("conv2d_9")
					.removeVertexKeepConnections("outputs")
					.addLayer("convolution2d_9",
							new ConvolutionLayer.Builder(1, 1)
									.nIn(1024)
									.nOut(nBoxes * (5 + nClasses))
									.stride(1, 1)
									.convolutionMode(ConvolutionMode.Same)
									.weightInit(WeightInit.XAVIER)
									.activation(Activation.IDENTITY)
									.build(),
							"leaky_re_lu_8")
					.addLayer("outputs",
							new Yolo2OutputLayer.Builder()
									.lambdaNoObj(lambdaNoObj)
									.lambdaCoord(lambdaCoord)
									.boundingBoxPriors(priors)
									.build(),
							"convolution2d_9")
					.setOutputs("outputs")
					.build();

			System.out.println(model.summary(InputType.convolutional(heightCNN, widthCNN, nChannelsCNN)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void trainModel(ComputationGraph model, RecordReaderDataSetIterator train, int nEpochs) {
		log.info("Train model...");
		model.setListeners(new ScoreIterationListener(1));
		model.fit(train, nEpochs);
	}

	public static void saveModel(ComputationGraph model, String filePath) {
		log.info("Save model...");
		try {
			ModelSerializer.writeModel(model, filePath, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void loadModel(ComputationGraph model, String filePath) {
		log.info("Load model...");
		try {
			ComputationGraph.load(new File(filePath), true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void validateModel(ComputationGraph model, RecordReaderDataSetIterator train, RecordReaderDataSetIterator test, double detectionThreshold, double gridWidth, double gridHeight) {
		// visualize results on the test set
		NativeImageLoader imageLoader = new NativeImageLoader();
		CanvasFrame frame = new CanvasFrame("HouseNumberDetection");
		OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
		org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer yout = (org.deeplearning4j.nn.layers.objdetect.Yolo2OutputLayer) model.getOutputLayer(0);
		List<String> labels = train.getLabels();
		test.setCollectMetaData(true);
		Scalar[] colormap = { RED, BLUE, GREEN, CYAN, YELLOW, MAGENTA, ORANGE, PINK, LIGHTBLUE, VIOLET };

		while (test.hasNext() && frame.isVisible()) {
			org.nd4j.linalg.dataset.DataSet ds = test.next();
			RecordMetaDataImageURI metadata = (RecordMetaDataImageURI) ds.getExampleMetaData().get(0);
			INDArray features = ds.getFeatures();
			INDArray results = model.outputSingle(features);
			List<DetectedObject> objs = yout.getPredictedObjects(results, detectionThreshold);
			File file = new File(metadata.getURI());
			log.info(file.getName() + ": " + objs);

			Mat mat = imageLoader.asMat(features);
			Mat convertedMat = new Mat();
			mat.convertTo(convertedMat, CV_8U, 255, 0);
			int w = metadata.getOrigW() * 2;
			int h = metadata.getOrigH() * 2;
			Mat image = new Mat();
			resize(convertedMat, image, new Size(w, h));
			for (DetectedObject obj : objs) {
				double[] xy1 = obj.getTopLeftXY();
				double[] xy2 = obj.getBottomRightXY();
				String label = labels.get(obj.getPredictedClass());
				int x1 = (int) Math.round(w * xy1[0] / gridWidth);
				int y1 = (int) Math.round(h * xy1[1] / gridHeight);
				int x2 = (int) Math.round(w * xy2[0] / gridWidth);
				int y2 = (int) Math.round(h * xy2[1] / gridHeight);
				rectangle(image, new Point(x1, y1), new Point(x2, y2), colormap[obj.getPredictedClass()]);
				putText(image, label, new Point(x1 + 2, y2 - 2), FONT_HERSHEY_DUPLEX, 1, colormap[obj.getPredictedClass()]);

			}
			frame.setTitle(new File(metadata.getURI()).getName() + " - HouseNumberDetection");
			frame.setCanvasSize(w, h);
			frame.showImage(converter.convert(image));
			try {
				frame.waitKey();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		frame.dispose();
	}
}