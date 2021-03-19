EESchema Schematic File Version 4
EELAYER 30 0
EELAYER END
$Descr A4 11693 8268
encoding utf-8
Sheet 1 4
Title ""
Date ""
Rev ""
Comp ""
Comment1 ""
Comment2 ""
Comment3 ""
Comment4 ""
$EndDescr
$Comp
L COEN390_Hardware:Arduino_UNO U?
U 1 1 604941BD
P 750 4400
F 0 "U?" V 650 6350 50  0000 C CNN
F 1 "Arduino_UNO" V 650 5800 50  0000 C CNN
F 2 "" H 500 4400 50  0001 C CNN
F 3 "" H 500 4400 50  0001 C CNN
	1    750  4400
	-1   0    0    -1  
$EndComp
$Sheet
S 9100 950  900  500 
U 604A3244
F0 "Gas Sensor for Air Quality" 50
F1 "MQ-135.sch" 50
F2 "5V" I L 9100 1050 50 
F3 "Analog_Out" O R 10000 1350 50 
F4 "GND" I L 9100 1350 50 
F5 "AirSensorSwitch" I L 9100 1200 50 
$EndSheet
Wire Wire Line
	1850 2050 2100 2050
Wire Wire Line
	1850 2250 2300 2250
Wire Wire Line
	2300 2250 2300 1400
$Comp
L power:+5V #PWR?
U 1 1 604A44DC
P 2300 1400
F 0 "#PWR?" H 2300 1250 50  0001 C CNN
F 1 "+5V" H 2315 1573 50  0000 C CNN
F 2 "" H 2300 1400 50  0001 C CNN
F 3 "" H 2300 1400 50  0001 C CNN
	1    2300 1400
	1    0    0    -1  
$EndComp
Wire Wire Line
	1850 2450 2300 2450
$Comp
L power:GND #PWR?
U 1 1 604A5AA7
P 2500 2550
F 0 "#PWR?" H 2500 2300 50  0001 C CNN
F 1 "GND" H 2505 2377 50  0000 C CNN
F 2 "" H 2500 2550 50  0001 C CNN
F 3 "" H 2500 2550 50  0001 C CNN
	1    2500 2550
	1    0    0    -1  
$EndComp
$Comp
L power:+5V #PWR?
U 1 1 604A6ABC
P 8750 850
F 0 "#PWR?" H 8750 700 50  0001 C CNN
F 1 "+5V" H 8765 1023 50  0000 C CNN
F 2 "" H 8750 850 50  0001 C CNN
F 3 "" H 8750 850 50  0001 C CNN
	1    8750 850 
	1    0    0    -1  
$EndComp
$Comp
L power:+3V3 #PWR?
U 1 1 604A3AEB
P 2100 1400
F 0 "#PWR?" H 2100 1250 50  0001 C CNN
F 1 "+3V3" H 2115 1573 50  0000 C CNN
F 2 "" H 2100 1400 50  0001 C CNN
F 3 "" H 2100 1400 50  0001 C CNN
	1    2100 1400
	1    0    0    -1  
$EndComp
Wire Wire Line
	2100 2050 2100 1400
Wire Wire Line
	1850 2650 2300 2650
Wire Wire Line
	2300 2650 2300 2450
Wire Wire Line
	2300 2450 2500 2450
Wire Wire Line
	2500 2450 2500 2550
Connection ~ 2300 2450
$Comp
L power:GND #PWR?
U 1 1 604A8FF9
P 8750 1550
F 0 "#PWR?" H 8750 1300 50  0001 C CNN
F 1 "GND" H 8755 1377 50  0000 C CNN
F 2 "" H 8750 1550 50  0001 C CNN
F 3 "" H 8750 1550 50  0001 C CNN
	1    8750 1550
	1    0    0    -1  
$EndComp
Wire Wire Line
	9100 1050 8750 1050
Wire Wire Line
	8750 1050 8750 850 
Text GLabel 2300 3200 2    50   Input ~ 0
AirQualityValue
Wire Wire Line
	2300 3200 1850 3200
Text GLabel 10400 1350 2    50   Output ~ 0
AirQualityValue
$Sheet
S 9100 2450 900  500 
U 604AB21D
F0 "Fan Configuration" 50
F1 "FanConfig.sch" 50
F2 "RelaySwitch" I L 9100 2550 50 
F3 "GND" I L 9100 2850 50 
$EndSheet
Text GLabel 1100 1200 1    50   Input ~ 0
120VAC\\9VDc
Wire Wire Line
	1100 1150 1100 1300
Text GLabel 2200 7150 2    50   Output ~ 0
TX_Uno
Text GLabel 2200 7350 2    50   Input ~ 0
TX_Blue
Wire Wire Line
	2200 7150 1850 7150
Wire Wire Line
	2200 7350 1850 7350
$Sheet
S 9100 3950 900  500 
U 604B18C9
F0 "Bluetooth Setup" 50
F1 "BlueTooth.sch" 50
F2 "5V" I L 9100 4050 50 
F3 "GND" I L 9100 4350 50 
F4 "TX_Uno" I L 9100 4200 50 
F5 "TX_Blue" O R 10000 4200 50 
$EndSheet
Wire Wire Line
	9100 1350 8750 1350
Wire Wire Line
	8750 1350 8750 1550
$Comp
L power:+5V #PWR?
U 1 1 604B3E33
P 8750 3750
F 0 "#PWR?" H 8750 3600 50  0001 C CNN
F 1 "+5V" H 8765 3923 50  0000 C CNN
F 2 "" H 8750 3750 50  0001 C CNN
F 3 "" H 8750 3750 50  0001 C CNN
	1    8750 3750
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR?
U 1 1 604B4308
P 8750 3100
F 0 "#PWR?" H 8750 2850 50  0001 C CNN
F 1 "GND" H 8755 2927 50  0000 C CNN
F 2 "" H 8750 3100 50  0001 C CNN
F 3 "" H 8750 3100 50  0001 C CNN
	1    8750 3100
	1    0    0    -1  
$EndComp
$Comp
L power:GND #PWR?
U 1 1 604B46D5
P 8750 4550
F 0 "#PWR?" H 8750 4300 50  0001 C CNN
F 1 "GND" H 8755 4377 50  0000 C CNN
F 2 "" H 8750 4550 50  0001 C CNN
F 3 "" H 8750 4550 50  0001 C CNN
	1    8750 4550
	1    0    0    -1  
$EndComp
Wire Wire Line
	8750 2550 9100 2550
Wire Wire Line
	9100 2850 8750 2850
Wire Wire Line
	8750 2850 8750 3100
Wire Wire Line
	8750 3750 8750 4050
Wire Wire Line
	8750 4050 9100 4050
Wire Wire Line
	9100 4350 8750 4350
Wire Wire Line
	8750 4350 8750 4550
Wire Wire Line
	9100 4200 8650 4200
Text GLabel 8650 4200 0    50   Input ~ 0
TX_Uno
Text GLabel 10250 4200 2    50   Output ~ 0
TX_Blue
Wire Wire Line
	10000 4200 10250 4200
Text GLabel 2300 4950 2    50   Output ~ 0
RelaySwitch
Wire Wire Line
	2300 4950 1850 4950
Text GLabel 8250 2550 2    50   Output ~ 0
RelaySwitch
Wire Wire Line
	10400 1350 10000 1350
Wire Wire Line
	9100 1200 8750 1200
Text GLabel 2300 5150 2    50   Output ~ 0
AirSensorSwitch
Wire Wire Line
	1850 5150 2300 5150
Text GLabel 8750 1200 0    50   Input ~ 0
AirSensorSwitch
$EndSCHEMATC
