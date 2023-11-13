package org.firstinspires.ftc.teamcode.autos;

import android.util.Size;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.*;

@Autonomous(name = "Vision", group = "Autonomous")
public class Vision extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {

        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());


        AprilTagProcessor processor = new AprilTagProcessor.Builder()
                .setDrawCubeProjection(true)
                .setDrawTagID(true)
                .build();


        VisionPortal visionPortal = new VisionPortal.Builder()
                .addProcessor(processor)
                .setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"))
                .setCameraResolution(new Size(640,480))
                .build();




        waitForStart();

        while (!isStopRequested() && opModeIsActive()) {

            if (processor.getDetections().size() > 0) {
                AprilTagDetection tag = processor.getDetections().get(0);

                telemetry.addData("X (LR)", tag.ftcPose.x);
                telemetry.addData("Y (FB)", tag.ftcPose.y);
                telemetry.addData("Z (UD)", tag.ftcPose.z);
                telemetry.addData("Roll", tag.ftcPose.roll);
                telemetry.addData("Pitch", tag.ftcPose.pitch);
                telemetry.addData("Yaw", tag.ftcPose.yaw);
                telemetry.addData("Tag ID", tag.id);

            }

            telemetry.update();
        }


    }

}