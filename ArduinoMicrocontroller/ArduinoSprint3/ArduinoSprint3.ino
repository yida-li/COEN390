//initializations of the humidifier makr sure to add the adafruit library
#include "DHT.h"
#define DHTPIN 11
#define DHTTYPE DHT11
DHT dht(DHTPIN, DHTTYPE);

#define led 5

int sensorValue;
char command;
String string;
int data = 0;
bool fanFlip = true;
bool autoFlip = true;
bool atomizerFlip = true;

void setup(){ 
  
Serial.begin(9600);       // sets the serial port to 9600
pinMode(13, OUTPUT);     // sets the digital pin 13 as output
pinMode(12, OUTPUT);
pinMode(A0, INPUT);     // Grabs sensor readings

//humidifier
dht.begin();

digitalWrite(13, HIGH); 
 }
 
void loop()
{
  //Serial.println(string); //debugger
  data = analogRead(A0);        // Assigns data readings to integer data
  float humi = dht.readHumidity(); // records the humidity 
  delay(10);
  float temp = dht.readTemperature(); //records the temperature
  Serial.print("\n Air Quality in PPM is: ");
  Serial.print(data);          // send this value to app
  delay(1000);
  Serial.print("\n Humidity in %: ");
  Serial.print(humi);
  delay(1000);
  Serial.print("\n Temperature in Celcius  : ");
  Serial.print(temp);
  delay(1000);                //Every 2 seconds it will send to java and new reading
    
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
  if(string == "TO" && fanFlip == true)     //Java is sending the message TO to turn on the fan
  {
    fanOff();          // Note that relays operate on inverted logic, change this to fanon(); if not using relay
    delay(100);        // needed to protect the light/fan/relay
    fanFlip = false;
  }
  
  if(string =="TF" && fanFlip == false)
  {
    fanOn();                   // see comment above  
    //Serial.println(string); //debug
    delay(100);
    fanFlip = true;
  } 

//ATOMIZER CONTROLS
   if(string =="HO" && atomizerFlip == true)
  {
    atomOn();                   // see comment above  
    //Serial.println(string); //debug
    delay(100);
    atomizerFlip = false;
  }

  if(string =="HF" && atomizerFlip == false)
  {
    atomOff();                   // see comment above  
    //Serial.println(string); //debug
    delay(100);
    atomizerFlip = true;
  }

//AUTOFAN CONTROLS
  if(string =="AO" && autoFlip == true)
  {
    autoOn();                   // see comment above  
    //Serial.println(string); //debug
    autoFlip = false;
    delay(100);
    
  }
  
  if(string =="AF" && autoFlip == false)
  {
    autoOff();                   // see comment above  
    //Serial.println(string); //debug
    delay(100);
    autoFlip = true;
  }          
}


void fanOn() 
{
  digitalWrite(13, HIGH);   //Using pin 13 to power the relay circuit
  Serial.println("\n\n   You have turned OFF the fan"); // message that appears on java app
  delay(10);
}

void fanOff()  
{
  digitalWrite(13, LOW);
  Serial.println("\n\n   You have turned ON the fan");
  delay(10);
}


void atomOn()
{
  digitalWrite(12,HIGH);
  Serial.println("\n\n  You have turned ON the Humidifier");
  delay(10);
}

void atomOff()
{
  digitalWrite(12,LOW);
  Serial.println("\n\n  You have turned OFF the Humidifier");
  delay(10);
}

void autoOn() 
{
  digitalWrite(13, LOW);   
  Serial.println("\n\n   Fan turned ON Automatically"); 
  delay(10);
}

void autoOff()  
{
  digitalWrite(13, HIGH);
  Serial.println("\n\n   Fan turned OFF Automatically ");
  delay(10);
}
