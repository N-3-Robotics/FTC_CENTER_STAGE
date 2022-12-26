package org.firstinspires.ftc.teamcode.utilities

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.utilities.DriveConstants.AutoTurnTolerance
import org.firstinspires.ftc.teamcode.utilities.DriveConstants.tileLength
import org.firstinspires.ftc.teamcode.utilities.QOL.Companion.calcPower
import org.firstinspires.ftc.teamcode.utilities.QOL.Companion.calcTurnPower
import org.firstinspires.ftc.teamcode.utilities.QOL.Companion.inchesToTicks
import kotlin.math.abs


open class Auto: LinearOpMode() {
    fun forward(tiles: Int){
        val target = ROBOT.currentPosition + inchesToTicks(tiles * tileLength)
        while (abs(ROBOT.currentPosition - target) > 50) {

            val power = calcPower(target, ROBOT.currentPosition)

            ROBOT.RCDrive(power, 0.0, 0.0)
            telemetry.addData("Position", ROBOT.currentPosition)
            telemetry.addData("Target", target)
            telemetry.addData("Distance Remaining", abs(ROBOT.currentPosition - target))
            telemetry.addData("Power", power)
            telemetry.update()
        }
        ROBOT.stop()
    }
    fun backward(tiles: Int){
        val target = ROBOT.currentPosition + inchesToTicks(-tiles * tileLength)
        while (abs(ROBOT.currentPosition - target) > 50) {

            val power = calcPower(target, ROBOT.currentPosition)

            ROBOT.RCDrive(power, 0.0, 0.0)
            telemetry.addData("Position", ROBOT.currentPosition)
            telemetry.addData("Target", target)
            telemetry.addData("Distance Remaining", abs(ROBOT.currentPosition - target))
            telemetry.addData("Power", power)
            telemetry.update()
        }
        ROBOT.stop()
    }
    fun left(tiles: Int){
        val target = ROBOT.currentPosition + inchesToTicks(-tiles * tileLength)
        while (abs(ROBOT.currentPosition - target) > 50) {

            val power = calcPower(target, ROBOT.currentPosition)

            ROBOT.RCDrive(0.0, power, 0.0)
            telemetry.addData("Position", ROBOT.currentPosition)
            telemetry.addData("Target", target)
            telemetry.addData("Distance Remaining", abs(ROBOT.currentPosition - target))
            telemetry.addData("Power", power)
            telemetry.update()
        }
        ROBOT.stop()
    }

    fun right(tiles: Int){
        val target = ROBOT.currentPosition + inchesToTicks(tiles * tileLength)
        while (abs(ROBOT.currentPosition - target) > 50) {
            //start at 0.75 power, and decrease power curve-ly as the robot gets closer to the target

            val power = calcPower(target, ROBOT.currentPosition)

            ROBOT.RCDrive(0.0, power, 0.0)
            telemetry.addData("Position", ROBOT.currentPosition)
            telemetry.addData("Target", target)
            telemetry.addData("Distance Remaining", abs(ROBOT.currentPosition - target))
            telemetry.addData("Power", power)
            telemetry.update()
        }
        ROBOT.stop()
    }


    // #TODO: Implement Proportional turn control, using the IMU.
    fun turnRight(angle: Int = -90){
        val target = ROBOT.IMU.angularOrientation.firstAngle + angle
        // while the IMU is not withing the AutoTurnTolerance of the target angle, calculate the power and turn right
        while (abs(ROBOT.IMU.angularOrientation.firstAngle - target) > AutoTurnTolerance) {
            val power = calcTurnPower(target, ROBOT.IMU.angularOrientation.firstAngle)
            ROBOT.RCDrive(0.0, 0.0, power)
            telemetry.addData("Angle", ROBOT.IMU.angularOrientation.firstAngle)
            telemetry.addData("Target", angle)
            telemetry.addData("Angle Remaining", abs(ROBOT.IMU.angularOrientation.firstAngle - target))
            telemetry.addData("Power", power)
            telemetry.update()
        }
    }
    fun turnLeft(angle: Int = 90){
        val target = ROBOT.IMU.angularOrientation.firstAngle + angle
        // while the IMU is not withing the AutoTurnTolerance of the target angle, calculate the power and turn right
        while (abs(ROBOT.IMU.angularOrientation.firstAngle - target) > AutoTurnTolerance) {
            val power = calcTurnPower(target, ROBOT.IMU.angularOrientation.firstAngle)
            ROBOT.RCDrive(0.0, 0.0, power)
            telemetry.addData("Angle", ROBOT.IMU.angularOrientation.firstAngle)
            telemetry.addData("Target", angle)
            telemetry.addData("Angle Remaining", abs(ROBOT.IMU.angularOrientation.firstAngle - target))
            telemetry.addData("Power", power)
            telemetry.update()
        }
    }


    val ROBOT = RobotConfig(hardwareMap)
        
    override fun runOpMode() {
        TODO("Not yet implemented")
    }
}
