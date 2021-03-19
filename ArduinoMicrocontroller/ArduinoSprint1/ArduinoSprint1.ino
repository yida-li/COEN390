
int sensorValue;
char command;
String string;
boolean fanon = false;
#define led 5
int data = 0;
int fanFlipOn = 0;
int fanFlipOff = 0;
int sensFlipOn = 0;
int sensFlipOff = 0;

void setup(){ 
Serial.begin(9600);       // sets the serial port to 9600
pinMode(13, OUTPUT);     // sets the digital pin 13 as output
pinMode(12, OUTPUT);
pinMode(A0, INPUT);     // Grabs sensor readings
 }
void loop()
{
     data = analogRead(A0);        // Assigns data readings to integer data
     Serial.print("\n Air Quality in PPM is: ");
     Serial.print(data);          // send this value to app
     delay(2000);                //Every 2 seconds it will send to java and new reading
  
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
  delay(1);
 }

//FAN CONTROLS 
  if(string == "TO")     //Java is sending the message TO to turn on the fan
  {
    fanOff();          // Note that relays operate on inverted logic, change this to fanon(); if not using relay
    fanon = false; 
    delay(100);        // needed to protect the light/fan/relay
  }
  
  if(string =="TF")
  {
    fanOn();                   // see comment above  
    fanon = true; 
    //Serial.println(string); //debug
    delay(100);
  } 

//SENSOR CONTROLS
   if(string =="SO")
  {
    SensOn();                   // see comment above  
    //Serial.println(string); //debug
    delay(100);
  }

  if(string =="SF")
  {
    SensOff();                   // see comment above  
    //Serial.println(string); //debug
    delay(100);
  }    
}

void fanOn() 
{
  digitalWrite(13, HIGH);   //Using pin 13 to power the relay circuit
  if(fanFlipOn == 0)           // using a count so that nothing happens if on button or off button is spammed
  {
    Serial.println("\n\n   You have turned OFF the fan"); // message that appears on java app
    fanFlipOn++;
    fanFlipOff = 0;
  }
delay(10);
}


void fanOff()  
{
  digitalWrite(13, LOW);
  if(fanFlipOff ==0)
  {
    Serial.println("\n\n   You have turned ON the fan");
    fanFlipOff++;
    fanFlipOn = 0;
  }
  delay(10);
}

void SensOn()
{
  digitalWrite(12,HIGH);
  if (sensFlipOn == 0)
  {
    Serial.println("\n\n  You have turned ON the Sensor");
    sensFlipOn++;
    sensFlipOff = 0;
  }
  delay(10);
}

void SensOff()
{
  digitalWrite(12,LOW);
  if (sensFlipOff == 0);
  {
    Serial.println("\n\n  You have turned OFF the Sensor");
    sensFlipOff++;
    sensFlipOn = 0;
  }
  delay(10);
}
