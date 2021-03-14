
int sensorValue;
char command;
String string;
boolean fanon = false;
#define led 5
int data = 0;
int count1 = 0;
int count2 = 0;

void setup(){ 
Serial.begin(9600);       // sets the serial port to 9600
pinMode(13, OUTPUT);     // sets the digital pin 13 as output
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
}

void fanOn() 
{
digitalWrite(13, HIGH);   //Using pin 13 to power the relay circuit
if(count1 == 0)           // using a count so that nothing happens if on button or off button is spammed
{
Serial.println("\n\n   You have turned OFF the fan"); // message that appears on java app
count1++;
count2 = 0;
}
delay(10);
}


void fanOff()  
{
digitalWrite(13, LOW);
if(count2 ==0)
{
Serial.println("\n\n   You have turned ON the fan");
count2++;
count1 = 0;
}
delay(10);
}
