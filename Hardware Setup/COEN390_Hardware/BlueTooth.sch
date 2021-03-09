EESchema Schematic File Version 4
EELAYER 30 0
EELAYER END
$Descr A4 11693 8268
encoding utf-8
Sheet 4 4
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
L COEN390_Hardware:HC-05(bluetooth) U?
U 1 1 604BDD1F
P 2200 1650
F 0 "U?" H 1681 2269 50  0000 C CNN
F 1 "HC-05(bluetooth)" H 1681 2178 50  0000 C CNN
F 2 "" H 2200 1500 50  0001 C CNN
F 3 "" H 2200 1500 50  0001 C CNN
	1    2200 1650
	1    0    0    -1  
$EndComp
Text HLabel 3650 1050 1    50   Input ~ 0
5V
Text HLabel 3650 1700 3    50   Input ~ 0
GND
Text HLabel 2900 1750 2    50   Output ~ 0
TX_Blue
Text HLabel 2900 1900 2    50   Input ~ 0
TX_Uno
Wire Wire Line
	2900 1900 2600 1900
Wire Wire Line
	2900 1750 2300 1750
Wire Wire Line
	2300 1600 3650 1600
Wire Wire Line
	3650 1600 3650 1700
Wire Wire Line
	3650 1050 3650 1450
Wire Wire Line
	3650 1450 2300 1450
Wire Wire Line
	2300 2050 2600 2050
Wire Wire Line
	2600 2050 2600 1900
Connection ~ 2600 1900
Wire Wire Line
	2600 1900 2300 1900
$EndSCHEMATC
