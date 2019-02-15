/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.GenericHID;

import edu.wpi.first.wpilibj.PowerDistributionPanel;
//import edu.wpi.first.wpilibj.Talon;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

/*************************************************************
 * KEY:
 * 
 * //? = unshure eliment
 * 
 */
/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends IterativeRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  XboxController controller1 = new XboxController(0);
  XboxController controller2 = new XboxController(1);

  
  int startTime = 0; 
  int endTime = 500; //50 = about 1 second
  int halfTime = 0; 
  int stallEndTime = 500; 
  int outTime = 0;

  double kMaxRPM = 5840;
  double kSensorUnitsPerRotation = 3600; //4096?
  double kGearRatio = 9.47; //?
  double maxSpeed = (kMaxRPM / 600) * (kSensorUnitsPerRotation / kGearRatio); //?

  boolean stall = false;
  boolean out = false;
  boolean slaveMode1 = true;
  boolean slaveMode2 = true;
  boolean fullPower = controller1.getXButton();
  boolean intakeBumper = controller2.getBumper(GenericHID.Hand.kLeft);

  TalonSRX talonLeftMaster = new TalonSRX(10);
  VictorSPX victorLeftSlave1 = new VictorSPX(12);
  VictorSPX victorLeftSlave2 = new VictorSPX(14);

  TalonSRX talonRightMaster = new TalonSRX(11);
  VictorSPX victorRightSlave1 = new VictorSPX(13);
  VictorSPX victorRightSlave2 = new VictorSPX(15);

  TalonSRX pivotArm = new TalonSRX(20);
  TalonSRX pivotExtend = new TalonSRX(21);

  VictorSPX frontArmLift = new VictorSPX(40);
  VictorSPX rearArmLift = new VictorSPX(43);

  CANSparkMax liftLWheel = new CANSparkMax(42, MotorType.kBrushless);
  CANSparkMax liftRWheel = new CANSparkMax(41, MotorType.kBrushless);

  TalonSRX wrist = new TalonSRX(30);
  VictorSPX intake = new VictorSPX(31);

  PowerDistributionPanel pdp = new PowerDistributionPanel(1);

  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    talonLeftMaster.setInverted(true);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // autoSelected = SmartDashboard.getString("Auto Selector",
    // defaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  public void timeDelay(long t) {
    try {
        Thread.sleep(t);
    } catch (InterruptedException e) {}
}

@Override
  public void teleopInit() {

  }



/**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    double forwardpower = controller1.getX(GenericHID.Hand.kRight);
    double turnpower = controller1.getY(GenericHID.Hand.kLeft);

    double forwardVelocity = (forwardpower * maxSpeed);
    double turnVelocity = (turnpower * maxSpeed);

    double LstickDead = 0.15;
    double RstickDead = 0.15;

    if (forwardpower < LstickDead && forwardpower > -LstickDead){
      forwardpower = 0;
    }
    if (turnpower < RstickDead && turnpower > -RstickDead){
      turnpower = 0;
    }

    double totalLSpeed = forwardVelocity - turnVelocity;
    double totalRSpeed = forwardVelocity + turnVelocity;

    talonLeftMaster.set(ControlMode.Velocity, totalLSpeed);
    talonRightMaster.set(ControlMode.Velocity, totalRSpeed);
 /**
    double currentLeftMaster = pdp.getCurrent(0);
    double currentRightMaster = pdp.getCurrent(13);
    double currentLeftSlave1 = pdp.getCurrent(1);
    double currentRightSlave1 = pdp.getCurrent(14);
    double currentLeftSlave2 = pdp.getCurrent(12);
    double currentRightSlave2 = pdp.getCurrent(15); 
    double currentPivotArm = pdp.getCurrent(3);
    double currentPivotExtend = pdp.getCurrent(4);
    double currentFrontLift = pdp.getCurrent(2);
    double currentRearLift = pdp.getCurrent(5);
    double currentRLiftWheel = pdp.getCurrent(11);
    double currentLLiftWheel = pdp.getCurrent(10);
    double currentWrist = pdp.getCurrent(6);
    double currentIntake = pdp.getCurrent(5);
    //double currentVRM = pdp.getCurrent(8);

    double test = pdp.getTemperature()

    double totalCurrent = currentLeftMaster + currentRightMaster + currentLeftSlave1 + currentRightSlave1 + currentLeftSlave2 + currentRightSlave2 + currentPivotArm + currentPivotExtend + currentFrontLift + currentRearLift + currentRLiftWheel + currentLLiftWheel + currentWrist + currentIntake; // + currentVRM
    */

    double totalCurrent = pdp.getTotalCurrent();

    double temp = pdp.getTemperature();


    if (slaveMode1 == true) {
      victorLeftSlave1.setNeutralMode(NeutralMode.Brake);
      victorRightSlave1.setNeutralMode(NeutralMode.Brake);
      victorLeftSlave1.follow(talonLeftMaster);
      victorRightSlave1.follow(talonRightMaster);
    } else {
      victorLeftSlave1.setNeutralMode(NeutralMode.Coast);
      victorRightSlave1.setNeutralMode(NeutralMode.Coast);
    }
    if (slaveMode2 == true) {
      victorLeftSlave2.setNeutralMode(NeutralMode.Brake);
      victorRightSlave2.setNeutralMode(NeutralMode.Brake);
      victorLeftSlave2.follow(talonLeftMaster);
      victorRightSlave2.follow(talonRightMaster);
    } else {
      victorLeftSlave2.setNeutralMode(NeutralMode.Coast);
      victorRightSlave2.setNeutralMode(NeutralMode.Coast);

    }

    if (fullPower){
      slaveMode1 = true;
      slaveMode2 = true;
      halfTime = 0;
      stall = false;
      out = false;
      startTime = 0;
    } else {

      if (totalCurrent >= 110){
        startTime++;
       } else {
        startTime = 0;
      }

      if (startTime >= endTime){
        stall = true;
      }

      if (stall == true){
        halfTime++;
        slaveMode1 = false;
       } else {
        slaveMode1 = true;
        halfTime = 0;
      }

      if (halfTime >= stallEndTime) {
        out = true;
      } 

      if (out == true){
        outTime++;
        slaveMode2 = false;
       } else {
        outTime = 0;
        slaveMode2 = true;
      }

      if (outTime >= stallEndTime){
        slaveMode1 = true;
        slaveMode2 = true;
        halfTime = 0;
        stall = false;
        out = false;
        startTime = 0;
      }
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    if (intakeBumper){
      intake.set(ControlMode.PercentOutput, 1.0);
    } else {
      intake.set(ControlMode.PercentOutput, 0.0);
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //system debug
    System.out.println("-------------------------------");
    if (totalCurrent <= 110){
    System.out.println("current " + totalCurrent);
    } else if (totalCurrent > 110){
      System.out.println("current " + "!!!" + totalCurrent + "!!!");
    }
    System.out.println("stall " + stall);
    System.out.println("start " + startTime);
    System.out.println("half " + halfTime);
    System.out.println("out " + outTime);
    System.out.println("left power " + (forwardVelocity - turnVelocity) + " / " + maxSpeed);
    System.out.println("right power " + (forwardVelocity + turnVelocity) + " / " + maxSpeed);
    System.out.println("PDP temp " + temp);

  }
  
  @Override
  public void testPeriodic() {
  }
}


