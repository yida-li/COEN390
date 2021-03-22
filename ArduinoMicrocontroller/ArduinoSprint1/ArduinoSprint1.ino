
int sensorValue;
char command;
String string;
#define led 5
int data = 0;
bool fanState;
bool sensState;

void setup(){ 
Serial.begin(9600);       // sets the serial port to 9600
pinMode(13, OUTPUT);
pinMode(12, OUTPUT);
pinMode(A0, INPUT);       // Grabs sensor readings
digitalWrite(13,HIGH);    //initial state for the fan is OFF for ben's set up
digitalWrite(12,HIGH);    // initial state fro the sensor is ON
 // careful with changing these as if chnaged the logic may not work off out of the gate
sensState = false; 
fanState = false;
 }
 
void loop()
{
    if(sensState == false)        //outputs to app only if the sensor is ON
    {
     data = analogRead(A0);       // Assigns data readings to integer data
     Serial.print("\n Air Quality in PPM is: ");
     Serial.print(data);          // send this value to app
     delay(2000);                 //Every 2 seconds it will send to java and new reading
  } 
  
  if (Serial.available() > 0)
  {string = "";
  }
  while(Serial.available() > 0)
    {
      command = ((byte)Serial.read());
      // Serial.print("We are now reading data");
      if(command == ':')
      {
     break;
     }
    else
     {
     string += command;
      }
    delay(1000);
   }

//FAN CONTROLS 
// "TO" from the app == ON
// "TF" fromt the app == OFF
// fanSate checks if the FAN is set to ON or OFF 
  if(string == "TO" && fanState == false)     
  {
    fanOn();          // Note that relays operate on inverted logic, change this to fanon(); if not using relay
    delay(100);       // needed to protect the light/fan/relay
    fanState = true; 
  }
  
  if(string =="TF" && fanState == true)
  {
    fanOff();                   
    //Serial.println(string); //debug
    delay(100);
    fanState = false;
  } 

//SENSOR CONTROLS
//"SO"  from the app == ON
//"SF" from the app == OFF
// sensSate checks if the SENSOR is set to ON or OFF 
   if(string =="SO" && sensState == true)
  {
    SensOn();
    //Serial.println(string); //debug
    delay(100);
    sensState = false;
  }

  if(string =="SF" && sensState == false)
  {
    SensOff(); 
    //Serial.println(string); //debug
    delay(100);
    sensState = true;
  }    
}

//FAN FUCNTIONS
// writes to the pin and prints message to the app
void fanOn() 
{
  digitalWrite(13, LOW);
  Serial.println("\n\n   You have turned ON the fan"); // message that appears on java app
  delay(10);
}
void fanOff()  
{
  digitalWrite(13, HIGH);
  Serial.println("\n\n   You have turned OFF the fan");
  delay(10);
}

//SENSOR FUNCTIONS 
//writes to the pin and prints message to the app
void SensOn()
{
  digitalWrite(12,HIGH);
  Serial.println("\n\n  You have turned ON the Sensor");
  delay(10);
}

void SensOff()
{
  digitalWrite(12,LOW);
  Serial.println("\n\n  You have turned OFF the Sensor");
  delay(10);
}
