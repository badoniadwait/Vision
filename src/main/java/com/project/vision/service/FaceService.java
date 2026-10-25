package com.project.vision.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.FaceDetectorYN;
import org.opencv.objdetect.FaceRecognizerSF;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.project.vision.model.FaceVerificationResponse;

@Service

public class FaceService {

    @Value("${face.sface-model:models/face_recognition_sface_2021dec.onnx}")
    private String sfaceModelPath;
    @Value("${face.yunet-model:models/face_detection_yunet_2023mar.onnx}")
    private String yunetModelPath;

    private volatile FaceDetectorYN detector;
    private volatile FaceRecognizerSF recognizer;

    public FaceVerificationResponse compareFace(Mat ogImg, Mat og, Mat refImg, Mat ref) {

        FaceVerificationResponse res = new FaceVerificationResponse();
        res.setFaces(null);
        res.setVerified(true);

        recognizer = getRecognizer();

        Mat refCrop = new Mat();
        Mat ogCrop = new Mat();

        recognizer.alignCrop(refImg, ref, refCrop);
        recognizer.alignCrop(ogImg, og, ogCrop);

        Mat refFeature = new Mat();
        Mat ogFeature = new Mat();

        recognizer.feature(refCrop, refFeature);
        recognizer.feature(ogCrop, ogFeature);

        double similarityScore = recognizer.match(refFeature, ogFeature, FaceRecognizerSF.FR_COSINE);

        double similarityThreshold = 0.75;

        if (similarityScore > similarityThreshold) {
            res.setAreSame(true);
            res.setMessage("Both are same person");
        } else {
            res.setAreSame(false);
            res.setMessage("Both are not same person");
        }

        return res;
    }

    public FaceVerificationResponse detectFace(Mat image) throws IOException {

        FaceVerificationResponse res = new FaceVerificationResponse();
        res.setAreSame(false);
        res.setFaces(null);
        res.setVerified(false);

        detector = getDetector();

        if (image.empty()) {
            res.setMessage("Invalid Image");
            return res;
        }

        detector.setInputSize(
                new Size(image.cols(), image.rows())
        );

        Mat faces = new Mat();
        detector.setInputSize(new Size(image.cols(), image.rows()));
        detector.detect(image, faces);

        if (faces.rows() == 0) {
            res.setMessage("No face found");
            return res;
        }

        if (faces.rows() > 1) {
            res.setMessage("Multiple Face Found");

            return res;
        }

        res.setVerified(true);
        res.setMessage("Invalid Image");
        res.setFaces(faces);

        double score = faces.get(0, 14)[0];
        System.out.println("score is " + score);

        return res;
    }

    public Mat loadReferenceImage(String imageUrl) throws Exception {

        System.out.println(imageUrl);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(imageUrl))
                .GET()
                .build();

        HttpResponse<byte[]> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to fetch image");
        }

        MatOfByte imageBytes = new MatOfByte(response.body());

        try {
            return Imgcodecs.imdecode(
                    imageBytes,
                    Imgcodecs.IMREAD_COLOR
            );
        } finally {
            imageBytes.release();
        }

    }

    private FaceDetectorYN getDetector() {

        float faceDetectionThreshold = 0.7f;

        if (detector == null) {
            synchronized (this) {
                if (detector == null) {
                    File model = new File(yunetModelPath).getAbsoluteFile();
                    if (!model.isFile()) {
                        throw new FaceVerificationException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Face detection model not found: " + model);
                    }
                    try {
                        detector = FaceDetectorYN.create(
                                model.getAbsolutePath(),
                                "",
                                new Size(320, 320),
                                faceDetectionThreshold,
                                0.3f,
                                5000,
                                Dnn.DNN_BACKEND_OPENCV,
                                Dnn.DNN_TARGET_CPU);
                    } catch (Exception e) {

                        System.out.println("Failed to load YuNet face detection model" + e);
                        throw new FaceVerificationException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Face detection model could not be loaded.");
                    }
                }
            }
        }
        return detector;
    }

    private FaceRecognizerSF getRecognizer() {

        if (recognizer == null) {
            synchronized (this) {
                if (recognizer == null) {
                    File model = new File(sfaceModelPath).getAbsoluteFile();
                    if (!model.isFile()) {
                        throw new FaceVerificationException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Face recognition model not found: " + model);
                    }
                    try {
                        recognizer = FaceRecognizerSF.create(
                                model.getAbsolutePath(),
                                "",
                                Dnn.DNN_BACKEND_OPENCV,
                                Dnn.DNN_TARGET_CPU);
                    } catch (Exception e) {
                        System.out.println("Failed to load SFace face recognition model" + e);
                        throw new FaceVerificationException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Face recognition model could not be loaded.");
                    }
                }
            }
        }
        return recognizer;
    }
}
