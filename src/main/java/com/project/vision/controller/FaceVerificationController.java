package com.project.vision.controller;

import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.vision.model.FaceVerificationRequest;
import com.project.vision.model.FaceVerificationResponse;
import com.project.vision.service.FaceService;
import com.project.vision.service.FaceVerificationException;
// import com.project.vision.service.FaceVerificationService;

@RestController
public class FaceVerificationController {

    // private final FaceVerificationService faceVerificationService;
    @Autowired
    private FaceService faceService;

    // public FaceVerificationController(FaceVerificationService faceVerificationService) {
    //     this.faceVerificationService = faceVerificationService;
    // }
    @PostMapping("/api/verify-face")
    public ResponseEntity<FaceVerificationResponse> verifyFace(@ModelAttribute FaceVerificationRequest req) throws IOException, Exception {

        MultipartFile image = req.getImage();

        byte[] bytes = image.getBytes();

        Mat img = Imgcodecs.imdecode(
                new MatOfByte(bytes),
                Imgcodecs.IMREAD_COLOR
        );

        // FaceVerificationResponse response = faceVerificationService.verify(image);
        FaceVerificationResponse refImageRes = faceService.detectFace(img);
        if (!refImageRes.isVerified()) {
            System.out.println(refImageRes.getMessage());
            return ResponseEntity.badRequest().body(refImageRes);
        }
        System.out.println("Before loading reference image");
        System.out.println("imgUrl from request: " + req.getImgUrl());

        Mat refImage = faceService.loadReferenceImage(req.getImgUrl());

        Imgcodecs.imwrite("scanned_image.jpg", img);
        System.out.println("scanned_image.jpg created");
        Imgcodecs.imwrite("supabase_image.jpg", refImage);
        System.out.println("supabase_image.jpg created");

        FaceVerificationResponse ogImageRes = faceService.detectFace(refImage);
        if (!ogImageRes.isVerified()) {
            System.out.println(ogImageRes.getMessage());
            return ResponseEntity.badRequest().body(ogImageRes);
        }

        Mat refFace = refImageRes.getFaces(), ogFace = ogImageRes.getFaces();

        FaceVerificationResponse comparison = faceService.compareFace(refImage, ogFace, img, refFace);
        // System.out.println(response.getSimilarity());
        return ResponseEntity.ok(comparison);
    }

    @ExceptionHandler(FaceVerificationException.class)
    public ResponseEntity<FaceVerificationResponse> handleFaceVerificationException(FaceVerificationException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new FaceVerificationResponse(false, ex.getMessage(), null, false));
    }
}
