
int sensorValue;
char command;
String string;
#define led 5
int data = 0;
bool fanFlip = true;
bool sensFlip = false;
bool autoFlip = false;

void setup(){ 
Serial.begin(9600);       // sets the serial port to 9600
pinMode(13, OUTPUT);     // sets the digital pin 13 as output
pinMode(12, OUTPUT);
pinMode(A0, INPUT);     // Grabs sensor readings

digitalWrite(13, HIGH);
digitalWrite(12, HIGH);
 }
 
void loop()
{
  //if i remove this serial print my buttons do not work 
  //Serial.println(string); //debugger
  if (sensFlip == false)
  {
     data = analogRead(A0);        // Assigns data readings to integer data
     Serial.print("\n Air Quality in PPM is: ");
     Serial.print(data);          // send this value to app
  }
  delay(500);                //Every 2 seconds it will send to java and new reading
    
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

//SENSOR CONTROLS
   if(string =="SO" && sensFlip == true)
  {
    SensOn();                   // see comment above  
    //Serial.println(string); //debug
    delay(100);
    sensFlip = false;
  }

  if(string =="SF" && sensFlip == false)
  {
    sensFlip = true;
    SensOff();                   // see comment above  
    //Serial.println(string); //debug
    delay(100);
  }
  
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
