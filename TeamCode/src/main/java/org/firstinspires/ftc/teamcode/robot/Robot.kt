@file:Suppress("unused", "NAME_SHADOWING")
package org.firstinspires.ftc.teamcode.robot

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.kinematics.MecanumKinematics
import com.acmerobotics.robomatic.util.PIDController
import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.hardware.rev.Rev2mDistanceSensor
import com.qualcomm.robotcore.hardware.*
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.utilities.AutoMode.*
import org.firstinspires.ftc.teamcode.utilities.DriveConstants
import org.firstinspires.ftc.teamcode.utilities.DriveConstants.AutoDriveTolerance
import org.firstinspires.ftc.teamcode.utilities.DriveConstants.AutoTurnTolerance
import org.firstinspires.ftc.teamcode.utilities.DriveConstants.drive_kD
import org.firstinspires.ftc.teamcode.utilities.DriveConstants.drive_kI
import org.firstinspires.ftc.teamcode.utilities.DriveConstants.drive_kP
import org.firstinspires.ftc.teamcode.utilities.DriveConstants.heading_kD
import org.firstinspires.ftc.teamcode.utilities.DriveConstants.heading_kI
import org.firstinspires.ftc.teamcode.utilities.DriveConstants.heading_kP
import org.firstinspires.ftc.teamcode.utilities.DriveConstants.strafeMultiplier
import org.firstinspires.ftc.teamcode.utilities.DriveConstants.turn_kD
import org.firstinspires.ftc.teamcode.utilities.DriveConstants.turn_kI
import org.firstinspires.ftc.teamcode.utilities.DriveConstants.turn_kP
import org.firstinspires.ftc.teamcode.utilities.QOL.Companion.radToDeg
import org.firstinspires.ftc.teamcode.utilities.QOL.Companion.ticksToInches
import org.firstinspires.ftc.teamcode.utilities.RumbleStrength
import org.firstinspires.ftc.teamcode.utilities.Side
import kotlin.math.abs


class Robot(hwMap: HardwareMap?) {
    var FL: DcMotorEx
    var FR: DcMotorEx
    var BL: DcMotorEx
    var BR: DcMotorEx

    var driveMotors: Array<DcMotorEx>


    var LIFT: DcMotorEx

    var WRIST: Servo

    var LG: Servo
    var RG: Servo


    var IMU: BNO055IMU

    val trackWidth = 12.0
    val wheelBase = 9.5
    val lateralMultiplier = 1.1

    private var distanceTarget: Double = 0.0
    private var distanceError: Double = 0.0

    private var angleTarget: Double = 0.0
    private var errorAngle: Double = 0.0

    private var headingError: Double = 0.0
    private var headingTarget: Double = 0.0

    private var correction: Double = 0.0

    private var headingCorrection: Double = 0.0

//    var lastAngle: Double = botHeading

//    var globalAngle: Double = botHeading

    private var hasBeenRun = true

    var autoMode = UNKNOWN

    private var hardwareMap: HardwareMap? = null

    var headingPIDController = PIDController(heading_kP, heading_kI, heading_kD)
    var turnPIDController = PIDController(turn_kP, turn_kI, turn_kD)
    var drivePIDController = PIDController(drive_kP, drive_kI, drive_kD)

    init {
        hardwareMap = hwMap


        FL = hardwareMap!!.get(DcMotorEx::class.java, "FL")
        FR = hardwareMap!!.get(DcMotorEx::class.java, "FR")
        BL = hardwareMap!!.get(DcMotorEx::class.java, "BL")
        BR = hardwareMap!!.get(DcMotorEx::class.java, "BR")

        driveMotors = arrayOf(FL, FR, BL, BR)


        LIFT = hardwareMap!!.get(DcMotorEx::class.java, "LIFT")

        WRIST = hardwareMap!!.get(Servo::class.java, "WRIST")

        LG = hardwareMap!!.get(Servo::class.java, "LG")

        RG = hardwareMap!!.get(Servo::class.java, "RG")


        headingPIDController.setOutputBounds(-0.1, 0.1)
        turnPIDController.setOutputBounds(-1.0, 1.0)
        drivePIDController.setOutputBounds(-1.0, 1.0)


        FL.direction = DcMotorSimple.Direction.REVERSE
        BL.direction = DcMotorSimple.Direction.REVERSE



        FR.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        FL.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        BR.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        BL.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        FR.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        FL.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        BR.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        BL.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        IMU = hardwareMap!!.get(BNO055IMU::class.java, "imu")
        val parameters = BNO055IMU.Parameters()
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC
        IMU.initialize(parameters)
    }

    val currentPosition: Int
        get() {
            return (FL.currentPosition + FR.currentPosition + BL.currentPosition + BR.currentPosition) / 4
        }

    val isBusy: Boolean
        get() {
            for (motor in driveMotors) {
                if (motor.isBusy) {
                    return true
                }
            }
            return false
        }

//    val botHeading: Double
//        get() {
//            val currentAngle: Double = radToDeg(IMU.angularOrientation.firstAngle)
//
//            var deltaAngle = currentAngle - lastAngle
//
//            if (deltaAngle < -180) {
//                deltaAngle += 360
//            } else if (deltaAngle > 180) {
//                deltaAngle -= 360
//            }
//
//            globalAngle += deltaAngle
//
//            lastAngle = currentAngle
//
//            return globalAngle
//        }

    var zeroPowerBehavior: DcMotor.ZeroPowerBehavior
        get() {
            return FL.zeroPowerBehavior
        }
        set(value) {
            FL.zeroPowerBehavior = value
            FR.zeroPowerBehavior = value
            BL.zeroPowerBehavior = value
            BR.zeroPowerBehavior = value
        }

    var mode: DcMotor.RunMode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        set(value) {
            FL.mode = value
            FR.mode = value
            BL.mode = value
            BR.mode = value
            field = value
        }





    private fun tDrive(drive: Double, turn: Double){
        FL.power = drive + turn
        FR.power = drive - turn
        BL.power = drive + turn
        BR.power = drive - turn

        autoMode = MANUAL
//        update()
    }

    fun RCDrive(y: Double, x: Double, rx: Double) {
        val x = x * strafeMultiplier

        //val denominator = max(abs(y) + abs(x) + abs(rx), 1.0).toDouble()

        // Combine the joystick requests for each axis-motion to determine each wheel's power.
        // Set up a variable for each drive wheel to save the power level for telemetry.
        val leftFrontPower: Double = y + x + rx
        val rightFrontPower: Double = y - x - rx
        val leftBackPower: Double = y - x + rx
        val rightBackPower: Double = y + x - rx

        FL.power = leftFrontPower
        BL.power = leftBackPower
        FR.power = rightFrontPower
        BR.power = rightBackPower

        autoMode = MANUAL
//        update()
    }

//    fun FCDrive(y: Double, x: Double, turn: Double) {
//        /*val x = x * strafeMultiplier
//        val rotX = x * cos(-botHeading) - y * sin(-botHeading)
//        val rotY = y * sin(-botHeading) + x * cos(-botHeading)*/
//
//        val input = Vector2d(y, -x).rotated(-botHeading)
//
//        RCDrive(input.y, input.x, turn)
//    }

    fun gamepadDrive(controller: Gamepad, multiplier: Double) {
        RCDrive(
            -controller.left_stick_y.toDouble() * multiplier,
            controller.left_stick_x.toDouble() * multiplier,
            controller.right_stick_x.toDouble() * multiplier
        )
    }

//    fun update() {
//        when (autoMode) {
//            UNKNOWN -> {
//                stop()
//            }
//            TURN -> {
//                while (abs(errorAngle) > AutoTurnTolerance) {
//                    errorAngle = angleTarget - botHeading
//
//                    correction = turnPIDController.update(errorAngle)
//
//                    tDrive(0.0, correction)
//                }
//                stop()
//            }
//            STRAIGHT -> {
//                while (abs(distanceError) > AutoDriveTolerance) {
//                    headingError = headingTarget - botHeading
//
//                    headingCorrection = headingPIDController.update(headingError)
//
//                    distanceError = distanceTarget - ticksToInches(currentPosition)
//
//                    correction = drivePIDController.update(distanceError)
//
//                    tDrive(correction, headingCorrection)
//                }
//                stop()
//            }
//            MANUAL -> {
//
//            }
//        }
//    }

    private fun resetHeading(){
        var globalAngle = 0.0
    }

    fun stop() {
        RCDrive(0.0, 0.0, 0.0)
        autoMode = UNKNOWN
    }

    private fun prepareMotors(){
        for (motor in driveMotors){
            motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            motor.mode = DcMotor.RunMode.RUN_USING_ENCODER
        }
    }
    fun turnRight(angle: Int = -90){
        turnPIDController.reset()

        autoMode = TURN

        resetHeading()

        angleTarget = -angle.toDouble()

        prepareMotors()

//        update()
    }

//    fun straight(inches: Double){
//        hasBeenRun = false
//        prepareMotors()
//
//        distanceTarget = inches
//
//        distanceError = distanceTarget
//
//        autoMode = STRAIGHT
//
//        headingTarget = botHeading
//
//        update()
//    }

    fun turnLeft(angle: Int = 90){
        turnPIDController.reset()

        autoMode = TURN

        resetHeading()

        angleTarget = angle.toDouble()

        prepareMotors()

//        update()
    }

    fun rumble(controller: Gamepad, side: Side, power: RumbleStrength, durationMS: Int = 100) {
        val pwr = power.strength
        when (side) {
            Side.LEFT -> {
                controller.rumble(pwr, 0.0, durationMS)
            }
            Side.RIGHT -> {
                controller.rumble(0.0, pwr, durationMS)
            }
            Side.BOTH -> {
                controller.rumble(pwr / 2, pwr / 2, durationMS)
            }
        }
    }

    fun getWheelPositions(): List<Double> {
        val wheelPositions: MutableList<Double> = ArrayList()
        for (motor in driveMotors) {
            wheelPositions.add(ticksToInches(motor.currentPosition))
        }
        return wheelPositions
    }

    fun getWheelVelocities(): List<Double>? {
        val wheelVelocities: MutableList<Double> = ArrayList()
        for (motor in driveMotors) {
            wheelVelocities.add(ticksToInches(motor.velocity))
        }
        return wheelVelocities
    }

    fun setDrivePower(drivePower: Pose2d) {
        val powers = MecanumKinematics.robotToWheelVelocities(
                drivePower,
                1.0,
                1.0,
                lateralMultiplier
        )
        setMotorPowers(powers[0], powers[1], powers[2], powers[3])
    }
    private fun setMotorPowers(v: Double, v1: Double, v2: Double, v3: Double) {
        FL.power = v
        BL.power = v1
        BR.power = v2
        FR.power = v3
    }

    fun getExternalHeadingVelocity(): Double {
        return IMU.angularVelocity.zRotationRate.toDouble()
    }



}
